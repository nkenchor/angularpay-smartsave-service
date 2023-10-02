package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.adapters.outbound.RedisAdapter;
import io.angularpay.smartsave.domain.RequestStatus;
import io.angularpay.smartsave.domain.Role;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.CommandException;
import io.angularpay.smartsave.exceptions.ErrorObject;
import io.angularpay.smartsave.helpers.CommandHelper;
import io.angularpay.smartsave.models.*;
import io.angularpay.smartsave.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.smartsave.exceptions.ErrorCode.REQUEST_CANCELLED_ERROR;
import static io.angularpay.smartsave.exceptions.ErrorCode.REQUEST_COMPLETED_ERROR;
import static io.angularpay.smartsave.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.smartsave.models.UserNotificationType.*;

@Service
public class UpdateRequestStatusCommand extends AbstractCommand<UpdateRequestStatusCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public UpdateRequestStatusCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("UpdateRequestStatusCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateRequestStatusCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateRequestStatusCommandRequest request) {
        SmartSaveRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        if (found.getStatus() == RequestStatus.COMPLETED) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_COMPLETED_ERROR)
                    .message(REQUEST_COMPLETED_ERROR.getDefaultMessage())
                    .build();
        }
        if (found.getStatus() == RequestStatus.CANCELLED) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_CANCELLED_ERROR)
                    .message(REQUEST_CANCELLED_ERROR.getDefaultMessage())
                    .build();
        }
        Supplier<GenericCommandResponse> supplier = () -> updateRequestStatus(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateRequestStatus(UpdateRequestStatusCommandRequest request) throws OptimisticLockingFailureException {
        SmartSaveRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        SmartSaveRequest response = this.commandHelper.updateProperty(found, request::getStatus, found::setStatus);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .smartSaveRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateRequestStatusCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_KYC_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }

    @Override
    public String convertToUpdatesMessage(SmartSaveRequest smartSaveRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(smartSaveRequest);
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public UserNotificationType getUserNotificationType(GenericCommandResponse commandResponse) {
        RequestStatus status = commandResponse.getSmartSaveRequest().getStatus();
        switch (status) {
            case ACTIVE:
                return INVESTMENT_ACTIVATED;
            case INACTIVE:
                return INVESTMENT_DEACTIVATED;
            case CANCELLED:
                return INVESTMENT_CANCELLED;
            case COMPLETED:
            default:
                return INVESTMENT_COMPLETED;
        }
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return Collections.singletonList(commandResponse.getSmartSaveRequest().getInvestee().getUserReference());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, SmartSaveRequest> parameters) throws JsonProcessingException {
        UserNotificationType type = this.getUserNotificationType(parameters.getCommandResponse());
        String status;
        switch (type) {
            case INVESTMENT_ACTIVATED:
                status = "active";
                break;
            case INVESTMENT_DEACTIVATED:
                status = "inactive";
                break;
            case INVESTMENT_CANCELLED:
                status = "cancelled";
                break;
            case INVESTMENT_COMPLETED:
            default:
                status = "completed";
                break;
        }
        String summary = "your Smart Save post was marked as: " + status;

        UserNotificationRequestPayload userNotificationInvestmentPayload = UserNotificationRequestPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .build();
        String payload = mapper.writeValueAsString(userNotificationInvestmentPayload);

        String attributes = mapper.writeValueAsString(parameters.getRequest());

        UserNotification userNotification = UserNotification.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(parameters.getRequest().getServiceCode())
                .userReference(parameters.getUserReference())
                .type(parameters.getType())
                .summary(summary)
                .payload(payload)
                .attributes(attributes)
                .build();

        return mapper.writeValueAsString(userNotification);
    }
}

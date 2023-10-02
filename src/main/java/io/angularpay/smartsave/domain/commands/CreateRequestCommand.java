package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.adapters.outbound.RedisAdapter;
import io.angularpay.smartsave.domain.Investee;
import io.angularpay.smartsave.domain.Role;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.ErrorObject;
import io.angularpay.smartsave.helpers.CommandHelper;
import io.angularpay.smartsave.models.CreateRequestCommandRequest;
import io.angularpay.smartsave.models.GenericCommandResponse;
import io.angularpay.smartsave.models.GenericReferenceResponse;
import io.angularpay.smartsave.models.ResourceReferenceResponse;
import io.angularpay.smartsave.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.smartsave.helpers.ObjectFactory.pmtRequestWithDefaults;

@Slf4j
@Service
public class CreateRequestCommand extends AbstractCommand<CreateRequestCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public CreateRequestCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("CreateRequestCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(CreateRequestCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(CreateRequestCommandRequest request) {
        SmartSaveRequest smartSaveRequestWithDefaults = pmtRequestWithDefaults();
        SmartSaveRequest withOtherDetails = smartSaveRequestWithDefaults.toBuilder()
                .maturesOn(request.getCreateRequest().getMaturesOn())
                .spentAmount(request.getCreateRequest().getSpentAmount())
                .applicableGoal(request.getCreateRequest().getApplicableGoal())
                .smartSaveAmount(request.getCreateRequest().getSmartSaveAmount())
                .investee(Investee.builder()
                        .userReference(request.getAuthenticatedUser().getUserReference())
                        .build())
                .build();
        SmartSaveRequest response = this.mongoAdapter.createRequest(withOtherDetails);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .smartSaveRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(CreateRequestCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
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
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getRequestReference());
    }
}

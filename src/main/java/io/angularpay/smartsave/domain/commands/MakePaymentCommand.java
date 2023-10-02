package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.adapters.outbound.RedisAdapter;
import io.angularpay.smartsave.domain.InvestmentStatus;
import io.angularpay.smartsave.domain.InvestmentTransactionStatus;
import io.angularpay.smartsave.domain.Role;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.CommandException;
import io.angularpay.smartsave.exceptions.ErrorObject;
import io.angularpay.smartsave.helpers.CommandHelper;
import io.angularpay.smartsave.models.GenericCommandResponse;
import io.angularpay.smartsave.models.MakePaymentCommandRequest;
import io.angularpay.smartsave.models.ResourceReferenceResponse;
import io.angularpay.smartsave.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.smartsave.exceptions.ErrorCode.REQUEST_COMPLETED_ERROR;
import static io.angularpay.smartsave.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.smartsave.helpers.CommandHelper.validRequestStatusOrThrow;

@Service
public class MakePaymentCommand extends AbstractCommand<MakePaymentCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public MakePaymentCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("MakePaymentCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(MakePaymentCommandRequest request) {
        return commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(MakePaymentCommandRequest request) {
        SmartSaveRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> makePayment(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse makePayment(MakePaymentCommandRequest request) {
        SmartSaveRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String transactionReference = UUID.randomUUID().toString();

        if ((Objects.nonNull(found.getInvestmentStatus()) && found.getInvestmentStatus().getStatus() == InvestmentTransactionStatus.SUCCESSFUL)) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_COMPLETED_ERROR)
                    .message(REQUEST_COMPLETED_ERROR.getDefaultMessage())
                    .build();
        }

        if (Objects.isNull(found.getInvestmentStatus())) {
            found.setInvestmentStatus(InvestmentStatus.builder().status(InvestmentTransactionStatus.PENDING).build());
        }

        found.getInvestee().setBankAccountReference(request.getPaymentRequest().getBankAccountReference());

        // TODO: integrate with transaction service
        //  all of these details should come from transaction service
        found.getInvestmentStatus().setTransactionReference(transactionReference);
        found.getInvestmentStatus().setTransactionDatetime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        found.getInvestmentStatus().setStatus(InvestmentTransactionStatus.SUCCESSFUL);

        SmartSaveRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .itemReference(transactionReference)
                .smartSaveRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(MakePaymentCommandRequest request) {
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
        return new ResourceReferenceResponse(genericCommandResponse.getItemReference());
    }
}

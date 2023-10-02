package io.angularpay.smartsave.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.configurations.AngularPayConfiguration;
import io.angularpay.smartsave.domain.RequestStatus;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.CommandException;
import io.angularpay.smartsave.exceptions.ErrorCode;
import io.angularpay.smartsave.models.GenericCommandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.angularpay.smartsave.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    private final MongoAdapter mongoAdapter;
    private final ObjectMapper mapper;
    private final AngularPayConfiguration configuration;

    public GenericCommandResponse executeAcid(Supplier<GenericCommandResponse> supplier) {
        int maxRetry = this.configuration.getMaxUpdateRetry();
        OptimisticLockingFailureException optimisticLockingFailureException;
        int counter = 0;
        //noinspection ConstantConditions
        do {
            try {
                return supplier.get();
            } catch (OptimisticLockingFailureException exception) {
                if (counter++ >= maxRetry) throw exception;
                optimisticLockingFailureException = exception;
            }
        }
        while (Objects.nonNull(optimisticLockingFailureException));
        throw optimisticLockingFailureException;
    }

    public String getRequestOwner(String requestReference) {
        SmartSaveRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        return found.getInvestee().getUserReference();
    }

    private static CommandException commandException(HttpStatus status, ErrorCode errorCode) {
        return CommandException.builder()
                .status(status)
                .errorCode(errorCode)
                .message(errorCode.getDefaultMessage())
                .build();
    }

    public <T> SmartSaveRequest updateProperty(SmartSaveRequest smartSaveRequest, Supplier<T> getter, Consumer<T> setter) {
        setter.accept(getter.get());
        return this.mongoAdapter.updateRequest(smartSaveRequest);
    }

    public <T> String toJsonString(T t) throws JsonProcessingException {
        return this.mapper.writeValueAsString(t);
    }

    public static SmartSaveRequest getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String requestReference) {
        return mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

    public static void validRequestStatusOrThrow(SmartSaveRequest found) {
        if (found.getStatus() == RequestStatus.COMPLETED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_COMPLETED_ERROR);
        }
        if (found.getStatus() == RequestStatus.CANCELLED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_CANCELLED_ERROR);
        }
    }
}

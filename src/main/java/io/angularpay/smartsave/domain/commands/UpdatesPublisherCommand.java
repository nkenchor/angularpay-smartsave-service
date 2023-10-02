package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.smartsave.adapters.outbound.RedisAdapter;
import io.angularpay.smartsave.domain.SmartSaveRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T extends SmartSaveRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(SmartSaveRequest smartSaveRequest) throws JsonProcessingException;

    default void publishUpdates(T t) {
        SmartSaveRequest smartSaveRequest = t.getSmartSaveRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(smartSaveRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToUpdatesMessage(smartSaveRequest);
                redisAdapter.publishUpdates(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}

package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.smartsave.adapters.outbound.RedisAdapter;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.models.UserNotificationBuilderParameters;
import io.angularpay.smartsave.models.UserNotificationType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public interface UserNotificationsPublisherCommand<T extends SmartSaveRequestSupplier> {

    RedisAdapter getRedisAdapter();
    UserNotificationType getUserNotificationType(T commandResponse);
    List<String> getAudience(T commandResponse);
    String convertToUserNotificationsMessage(UserNotificationBuilderParameters<T, SmartSaveRequest> parameters) throws JsonProcessingException;

    default void publishUserNotification(T commandResponse) {
        SmartSaveRequest request = commandResponse.getSmartSaveRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        UserNotificationType type = this.getUserNotificationType(commandResponse);
        List<String> audience = this.getAudience(commandResponse);

        if (Objects.nonNull(request) && Objects.nonNull(redisAdapter)
        && Objects.nonNull(type) && !CollectionUtils.isEmpty(audience)) {
            audience.stream().parallel().forEach(userReference-> {
                try {
                    UserNotificationBuilderParameters<T, SmartSaveRequest> parameters = UserNotificationBuilderParameters.<T, SmartSaveRequest>builder()
                            .userReference(userReference)
                            .request(request)
                            .commandResponse(commandResponse)
                            .type(type)
                            .build();
                    String message = this.convertToUserNotificationsMessage(parameters);
                    redisAdapter.publishUserNotification(message);
                } catch (JsonProcessingException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}

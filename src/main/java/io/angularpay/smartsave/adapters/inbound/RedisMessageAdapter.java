package io.angularpay.smartsave.adapters.inbound;

import io.angularpay.smartsave.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.smartsave.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.smartsave.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.smartsave.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}

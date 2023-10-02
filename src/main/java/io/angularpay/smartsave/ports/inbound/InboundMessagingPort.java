package io.angularpay.smartsave.ports.inbound;

import io.angularpay.smartsave.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}

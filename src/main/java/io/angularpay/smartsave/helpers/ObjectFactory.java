package io.angularpay.smartsave.helpers;

import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.domain.RequestStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.angularpay.smartsave.common.Constants.SERVICE_CODE;
import static io.angularpay.smartsave.util.SequenceGenerator.generateRequestTag;

public class ObjectFactory {

    public static SmartSaveRequest pmtRequestWithDefaults() {
        return SmartSaveRequest.builder()
                .reference(UUID.randomUUID().toString())
                .serviceCode(SERVICE_CODE)
                .status(RequestStatus.ACTIVE)
                .requestTag(generateRequestTag())
                .build();
    }
}
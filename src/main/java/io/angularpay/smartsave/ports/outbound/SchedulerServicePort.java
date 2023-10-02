package io.angularpay.smartsave.ports.outbound;

import io.angularpay.smartsave.models.SchedulerServiceRequest;
import io.angularpay.smartsave.models.SchedulerServiceResponse;

import java.util.Map;
import java.util.Optional;

public interface SchedulerServicePort {
    Optional<SchedulerServiceResponse> createScheduledRequest(SchedulerServiceRequest request, Map<String, String> headers);
}

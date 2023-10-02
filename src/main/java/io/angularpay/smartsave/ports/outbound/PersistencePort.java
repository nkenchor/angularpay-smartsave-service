package io.angularpay.smartsave.ports.outbound;

import io.angularpay.smartsave.domain.RequestStatus;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    SmartSaveRequest createRequest(SmartSaveRequest request);
    SmartSaveRequest updateRequest(SmartSaveRequest request);
    Optional<SmartSaveRequest> findRequestByReference(String reference);
    Page<SmartSaveRequest> listRequests(Pageable pageable);
    Page<SmartSaveRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses);
    Page<SmartSaveRequest>  findByInvesteeUserReference(Pageable pageable, String userReference);
    long getCountByRequestStatus(RequestStatus status);
    long getTotalCount();
}

package io.angularpay.smartsave.adapters.outbound;

import io.angularpay.smartsave.domain.RequestStatus;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SmartSaveRepository extends MongoRepository<SmartSaveRequest, String> {

    Optional<SmartSaveRequest> findByReference(String reference);
    Page<SmartSaveRequest> findAll(Pageable pageable);
    Page<SmartSaveRequest> findByStatusIn(Pageable pageable, List<RequestStatus> statuses);
    Page<SmartSaveRequest> findAByInvesteeUserReference(Pageable pageable, String userReference);
    long countByStatus(RequestStatus status);
}

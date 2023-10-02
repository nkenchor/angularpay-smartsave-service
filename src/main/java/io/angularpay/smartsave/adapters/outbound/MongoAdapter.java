package io.angularpay.smartsave.adapters.outbound;

import io.angularpay.smartsave.domain.RequestStatus;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final SmartSaveRepository smartSaveRepository;

    @Override
    public SmartSaveRequest createRequest(SmartSaveRequest request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return smartSaveRepository.save(request);
    }

    @Override
    public SmartSaveRequest updateRequest(SmartSaveRequest request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return smartSaveRepository.save(request);
    }

    @Override
    public Optional<SmartSaveRequest> findRequestByReference(String reference) {
        return smartSaveRepository.findByReference(reference);
    }

    @Override
    public Page<SmartSaveRequest> listRequests(Pageable pageable) {
        return smartSaveRepository.findAll(pageable);
    }

    @Override
    public Page<SmartSaveRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses) {
        return smartSaveRepository.findByStatusIn(pageable, statuses);
    }

    @Override
    public Page<SmartSaveRequest> findByInvesteeUserReference(Pageable pageable, String userReference) {
        return smartSaveRepository.findAByInvesteeUserReference(pageable, userReference);
    }

    @Override
    public long getCountByRequestStatus(RequestStatus status) {
        return smartSaveRepository.countByStatus(status);
    }

    @Override
    public long getTotalCount() {
        return smartSaveRepository.count();
    }
}

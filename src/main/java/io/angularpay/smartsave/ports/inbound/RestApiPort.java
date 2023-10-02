package io.angularpay.smartsave.ports.inbound;

import io.angularpay.smartsave.domain.*;
import io.angularpay.smartsave.models.*;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    GenericReferenceResponse createScheduledRequest(String schedule, CreateRequest request, Map<String, String> headers);
    GenericReferenceResponse create(CreateRequest request, Map<String, String> headers);
    GenericReferenceResponse makePayment(String requestReference, PaymentRequest paymentRequest, Map<String, String> headers);
    void updateRequestStatus(String requestReference, RequestStatusModel status, Map<String, String> headers);
    SmartSaveRequest getRequestByReference(String requestReference, Map<String, String> headers);
    List<SmartSaveRequest> getNewsfeedModel(int page, Map<String, String> headers);
    List<UserRequestModel> getUserRequests(int page, Map<String, String> headers);
    List<SmartSaveRequest> getNewsfeedByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<SmartSaveRequest> getRequestListByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<SmartSaveRequest> getRequestList(int page, Map<String, String> headers);
    List<Statistics> getStatistics(Map<String, String> headers);
}

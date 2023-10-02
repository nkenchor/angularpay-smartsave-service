package io.angularpay.smartsave.adapters.inbound;

import io.angularpay.smartsave.configurations.AngularPayConfiguration;
import io.angularpay.smartsave.domain.*;
import io.angularpay.smartsave.domain.commands.*;
import io.angularpay.smartsave.models.*;
import io.angularpay.smartsave.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.smartsave.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/smart-save/requests")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    private final AngularPayConfiguration configuration;

    private final CreateRequestCommand createRequestCommand;
    private final MakePaymentCommand makePaymentCommand;
    private final UpdateRequestStatusCommand updateRequestStatusCommand;
    private final GetRequestByReferenceCommand getRequestByReferenceCommand;
    private final GetNewsfeedCommand getNewsfeedCommand;
    private final GetUserRequestsCommand getUserRequestsCommand;
    private final GetNewsfeedByStatusCommand getNewsfeedByStatusCommand;
    private final GetRequestListByStatusCommand getRequestListByStatusCommand;
    private final GetRequestListCommand getRequestListCommand;
    private final ScheduledRequestCommand scheduledRequestCommand;
    private final GetStatisticsCommand getStatisticsCommand;

    @PostMapping("/schedule/{schedule}")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse createScheduledRequest(
            @PathVariable String schedule,
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ScheduledRequestCommandRequest scheduledRequestCommandRequest = ScheduledRequestCommandRequest.builder()
                .runAt(schedule)
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return scheduledRequestCommand.execute(scheduledRequestCommandRequest);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse create(
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateRequestCommandRequest createRequestCommandRequest = CreateRequestCommandRequest.builder()
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return createRequestCommand.execute(createRequestCommandRequest);
    }

    @PostMapping("{requestReference}/payment")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse makePayment(
            @PathVariable String requestReference,
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        MakePaymentCommandRequest makePaymentCommandRequest = MakePaymentCommandRequest.builder()
                .requestReference(requestReference)
                .paymentRequest(paymentRequest)
                .authenticatedUser(authenticatedUser)
                .build();
        return makePaymentCommand.execute(makePaymentCommandRequest);
    }

    @PutMapping("/{requestReference}/status")
    @Override
    public void updateRequestStatus(
            @PathVariable String requestReference,
            @RequestBody RequestStatusModel status,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateRequestStatusCommandRequest updateRequestStatusCommandRequest = UpdateRequestStatusCommandRequest.builder()
                .requestReference(requestReference)
                .status(status.getStatus())
                .authenticatedUser(authenticatedUser)
                .build();
        updateRequestStatusCommand.execute(updateRequestStatusCommandRequest);
    }

    @GetMapping("/{requestReference}")
    @Override
    public SmartSaveRequest getRequestByReference(
            @PathVariable String requestReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestByReferenceCommandRequest getRequestByReferenceCommandRequest = GetRequestByReferenceCommandRequest.builder()
                .requestReference(requestReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return getRequestByReferenceCommand.execute(getRequestByReferenceCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}")
    @Override
    public List<SmartSaveRequest> getNewsfeedModel(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getNewsfeedCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/list/user-request/page/{page}")
    @Override
    public List<UserRequestModel> getUserRequests(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetUserRequestsCommandRequest getUserRequestsCommandRequest = GetUserRequestsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getUserRequestsCommand.execute(getUserRequestsCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<SmartSaveRequest> getNewsfeedByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getNewsfeedByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<SmartSaveRequest> getRequestListByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getRequestListByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @ResponseBody
    @Override
    public List<SmartSaveRequest> getRequestList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getRequestListCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/statistics")
    @ResponseBody
    @Override
    public List<Statistics> getStatistics(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetStatisticsCommandRequest getStatisticsCommandRequest = GetStatisticsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        return getStatisticsCommand.execute(getStatisticsCommandRequest);
    }
}

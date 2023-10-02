package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.domain.Role;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.ErrorObject;
import io.angularpay.smartsave.models.GenericGetRequestListCommandRequest;
import io.angularpay.smartsave.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GetNewsfeedCommand extends AbstractCommand<GenericGetRequestListCommandRequest, List<SmartSaveRequest>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetNewsfeedCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("GetNewsfeedCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GenericGetRequestListCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected List<SmartSaveRequest> handle(GenericGetRequestListCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.listRequests(pageable).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GenericGetRequestListCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}

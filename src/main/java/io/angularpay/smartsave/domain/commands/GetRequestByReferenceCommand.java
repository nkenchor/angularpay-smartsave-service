package io.angularpay.smartsave.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.smartsave.adapters.outbound.MongoAdapter;
import io.angularpay.smartsave.domain.Role;
import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.exceptions.ErrorObject;
import io.angularpay.smartsave.models.GetRequestByReferenceCommandRequest;
import io.angularpay.smartsave.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.smartsave.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class GetRequestByReferenceCommand extends AbstractCommand<GetRequestByReferenceCommandRequest, SmartSaveRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetRequestByReferenceCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetRequestByReferenceCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetRequestByReferenceCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected SmartSaveRequest handle(GetRequestByReferenceCommandRequest request) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
    }

    @Override
    protected List<ErrorObject> validate(GetRequestByReferenceCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}

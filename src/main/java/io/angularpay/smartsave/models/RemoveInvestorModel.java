package io.angularpay.smartsave.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RemoveInvestorModel extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotEmpty
    private String investmentReference;

    RemoveInvestorModel(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}

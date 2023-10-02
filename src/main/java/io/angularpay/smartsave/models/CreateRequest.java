
package io.angularpay.smartsave.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.smartsave.domain.Amount;
import io.angularpay.smartsave.domain.ApplicableGoal;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreateRequest {

    @NotNull
    @Valid
    @JsonProperty("spent_amount")
    private Amount spentAmount;

    @NotNull
    @Valid
    @JsonProperty("smart_save_amount")
    private Amount smartSaveAmount;

    @NotNull
    @Valid
    @JsonProperty("applicable_goal")
    private ApplicableGoal applicableGoal;

    @NotEmpty
    @JsonProperty("matures_on")
    private String maturesOn;
}

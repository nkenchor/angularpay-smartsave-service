
package io.angularpay.smartsave.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableGoal {

    private Amount from;
    @JsonProperty("save_percent")
    private Long savePercent;
    private Amount to;
}

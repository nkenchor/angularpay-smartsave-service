
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
public class Investee {

    @JsonProperty("bank_account_reference")
    private String bankAccountReference;
    @JsonProperty("user_reference")
    private String userReference;
}


package io.angularpay.smartsave.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("smartsave_requests")
public class SmartSaveRequest {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("service_code")
    private String serviceCode;
    @JsonProperty("smart_save_amount")
    private Amount smartSaveAmount;
    @JsonProperty("spent_amount")
    private Amount spentAmount;
    @JsonProperty("applicable_goal")
    private ApplicableGoal applicableGoal;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("matures_on")
    private String maturesOn;
    private Investee investee;
    @JsonProperty("investment_status")
    private InvestmentStatus investmentStatus;
    @JsonProperty("last_modified")
    private String lastModified;
    private String reference;
    @JsonProperty("request_tag")
    private String requestTag;
    private RequestStatus status;
}


package io.angularpay.smartsave.models;

import io.angularpay.smartsave.domain.SmartSaveRequest;
import io.angularpay.smartsave.domain.commands.SmartSaveRequestSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class GenericCommandResponse extends GenericReferenceResponse implements SmartSaveRequestSupplier {

    private final String requestReference;
    private final String itemReference;
    private final SmartSaveRequest smartSaveRequest;
}

package io.angularpay.smartsave.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}

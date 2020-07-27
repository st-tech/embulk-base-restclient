package org.embulk.base.restclient;

import org.embulk.base.restclient.record.ValueLocator;

public interface ServiceRequestMapperBuildable<T extends RestClientOutputTaskBase> {
    ServiceRequestMapper<? extends ValueLocator> buildServiceRequestMapper(T task);
}

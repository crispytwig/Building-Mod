package com.crispytwig.bbb.platform;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        T service = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
        BuildingButBetter.LOGGER.debug("Loaded {} for service {}", service, clazz);
        return service;
    }
}

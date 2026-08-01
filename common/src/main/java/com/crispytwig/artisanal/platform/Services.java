package com.crispytwig.artisanal.platform;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.services.IConfigHelper;
import com.crispytwig.artisanal.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IConfigHelper CONFIG = load(IConfigHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        T service = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
        Artisanal.LOGGER.debug("Loaded {} for service {}", service, clazz);
        return service;
    }
}

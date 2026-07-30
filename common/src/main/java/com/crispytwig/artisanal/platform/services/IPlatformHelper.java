package com.crispytwig.artisanal.platform.services;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();
}

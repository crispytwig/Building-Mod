package com.crispytwig.bbb.platform.services;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();
}

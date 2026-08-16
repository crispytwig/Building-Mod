package com.crispytwig.artisanal.client;

import com.crispytwig.artisanal.platform.Services;
import com.crispytwig.artisanal.platform.services.IClientHelper;

public final class ClientServices {
    public static final IClientHelper CLIENT = Services.load(IClientHelper.class);

    private ClientServices() {
    }
}

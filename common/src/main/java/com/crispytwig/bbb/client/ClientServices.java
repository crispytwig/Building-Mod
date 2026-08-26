package com.crispytwig.bbb.client;

import com.crispytwig.bbb.platform.Services;
import com.crispytwig.bbb.platform.services.IClientHelper;

public final class ClientServices {
    public static final IClientHelper CLIENT = Services.load(IClientHelper.class);

    private ClientServices() {
    }
}

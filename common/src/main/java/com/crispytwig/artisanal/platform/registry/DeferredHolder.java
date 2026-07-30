package com.crispytwig.artisanal.platform.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceLocation id;
    private final Supplier<T> supplier;

    public DeferredHolder(ResourceLocation id, Supplier<T> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return supplier.get();
    }

    public ResourceLocation getId() {
        return id;
    }
}

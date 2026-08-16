package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.entity.SeatEntity;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Artisanal.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = register("seat",
            EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .noSave()
                    .noSummon());

    private ModEntityTypes() {
    }

    public static void init() {
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(Artisanal.location(name).toString()));
    }
}

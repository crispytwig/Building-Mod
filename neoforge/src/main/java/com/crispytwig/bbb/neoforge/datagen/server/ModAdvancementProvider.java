package com.crispytwig.bbb.neoforge.datagen.server;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementProvider.AdvancementGenerator {
    public static AdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        return new AdvancementProvider(output, lookup, helper, List.of(new ModAdvancementProvider()));
    }

    @Override
    public void generate(HolderLookup.@NotNull Provider provider, @NotNull Consumer<AdvancementHolder> consumer, @NotNull ExistingFileHelper helper) {
    }
}

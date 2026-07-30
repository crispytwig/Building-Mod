package com.crispytwig.bbb.neoforge.datagen.server;

import com.crispytwig.bbb.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)), lookup);
    }

    public static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            getKnownBlocks().forEach(this::dropSelf);
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(entry -> (Block) entry.get())
                    .toList();
        }
    }
}

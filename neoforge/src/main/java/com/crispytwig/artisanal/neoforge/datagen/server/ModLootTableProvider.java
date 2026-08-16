package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)), lookup);
    }

    public static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            getKnownBlocks().forEach(block -> {
                if (block instanceof SlabBlock) {
                    add(block, createSlabItemTable(block));
                } else {
                    dropSelf(block);
                }
            });
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(entry -> (Block) entry.get())
                    .toList();
        }
    }

    public static class ModEntityLoot extends EntityLootSubProvider {
        protected ModEntityLoot(HolderLookup.Provider provider) {
            super(FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        public void generate() {
        }

        @Override
        protected Stream<EntityType<?>> getKnownEntityTypes() {
            return ModEntityTypes.ENTITY_TYPES.getEntries().stream().map(entry -> (EntityType<?>) entry.get());
        }
    }
}

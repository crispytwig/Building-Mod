package com.crispytwig.bbb.neoforge.datagen;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.neoforge.datagen.client.ModBlockStateProvider;
import com.crispytwig.bbb.neoforge.datagen.client.ModItemModelProvider;
import com.crispytwig.bbb.neoforge.datagen.client.ModLanguageProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModAdvancementProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModBlockTagsProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModCompatRecipeProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModItemTagsProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModLootTableProvider;
import com.crispytwig.bbb.neoforge.datagen.server.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BuildingButBetter.MOD_ID)
public final class ModDatagen {
    private ModDatagen() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));

        ModBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookup, helper));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookup, blockTags.contentsGetter(), helper));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new ModCompatRecipeProvider(output));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(output, lookup));
        generator.addProvider(event.includeServer(), ModAdvancementProvider.create(output, lookup, helper));
    }
}

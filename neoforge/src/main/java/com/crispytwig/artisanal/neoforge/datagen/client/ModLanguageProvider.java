package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, Artisanal.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Artisanal.MOD_ID, "Artisanal");
        add(ModBlocks.OAK_TRIM.get(), "Oak Trim");
        add(ModBlocks.OAK_TRIM_STAIRS.get(), "Oak Trim Stairs");
        add(ModBlocks.OAK_TRIM_SLAB.get(), "Oak Trim Slab");
        add(ModItems.ARCHITECTS_SCEPTER.get(), "Architect's Scepter");
        add(ModItems.WRIGHT_SPAWN_EGG.get(), "Wright Spawn Egg");
        add(ModEntityTypes.WRIGHT.get(), "Wright");
    }
}

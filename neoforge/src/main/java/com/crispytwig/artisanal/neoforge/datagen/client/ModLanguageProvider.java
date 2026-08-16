package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, Artisanal.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Artisanal.MOD_ID, "Artisanal");

        add("artisanal.configuration.title", "Artisanal Config");
        add("artisanal.configuration.cherry_wood_sounds", "Cherry Wood Sounds");
        add("artisanal.configuration.cherry_wood_sounds.tooltip", "Makes every woodset use the Cherry woodset's sounds");

        add(ModBlocks.OAK_BOARDS.get(), "Oak Boards");
        add(ModBlocks.OAK_BOARD_STAIRS.get(), "Oak Board Stairs");
        add(ModBlocks.OAK_BOARD_SLAB.get(), "Oak Board Slab");
        add(ModBlocks.POLISHED_OAK.get(), "Polished Oak");

        add(ModBlocks.OAK_TRIM.get(), "Oak Trim");
        add(ModBlocks.OAK_TRIM_STAIRS.get(), "Oak Trim Stairs");
        add(ModBlocks.OAK_TRIM_SLAB.get(), "Oak Trim Slab");
    }
}

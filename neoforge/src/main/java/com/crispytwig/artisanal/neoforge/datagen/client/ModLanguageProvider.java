package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, Artisanal.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Artisanal.MOD_ID, "Artisanal");
        add("itemGroup." + Artisanal.MOD_ID + ".colored", "Artisanal Colored Blocks");

        add("artisanal.configuration.title", "Artisanal Config");
        add("artisanal.configuration.cherry_wood_sounds", "Wood Sounds");
        add("artisanal.configuration.cherry_wood_sounds.tooltip", "Makes every woodset use the Cherry woodset's sounds");
        add("artisanal.configuration.prismarine_deepslate_sounds", "Prismarine Sounds");
        add("artisanal.configuration.prismarine_deepslate_sounds.tooltip", "Makes every Prismarine block use Deepslate sounds");

        add(ModItems.FACADE.get(), "Facade");
        add(ModItems.FACADE.get().getDescriptionId() + ".material", "%s Facade");

        ModBlocks.BLOCKS.getEntries().forEach(this::addTitleCased);
    }

    private void addTitleCased(DeferredHolder<Block, ? extends Block> holder) {
        add(holder.get(), Arrays.stream(holder.getId().getPath().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" ")));
    }
}

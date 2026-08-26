package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModItems;
import com.crispytwig.artisanal.registry.ModTags;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;


public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, Artisanal.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Artisanal.MOD_ID, "Artisanal");
        add("itemGroup." + Artisanal.MOD_ID + ".colored", "Artisanal Colored Blocks");
        add("itemGroup." + Artisanal.MOD_ID + ".layers", "Artisanal Layers");

        add("artisanal.configuration.title", "Artisanal Config");
        add("artisanal.configuration.cherry_wood_sounds", "Wood Sounds");
        add("artisanal.configuration.cherry_wood_sounds.tooltip", "Makes every woodset use the Cherry woodset's sounds");
        add("artisanal.configuration.prismarine_deepslate_sounds", "Prismarine Sounds");
        add("artisanal.configuration.prismarine_deepslate_sounds.tooltip", "Makes every Prismarine block use Deepslate sounds");

        add(ModItems.FACADE.get(), "Facade");
        add(ModItems.FACADE.get().getDescriptionId() + ".material", "%s Facade");

        addTag(ModTags.LAYERS.location(), "Layers");
        addTag(ModTags.WOODEN_LAYERS.location(), "Wooden Layers");
        addTag(ModTags.PRISMARINE.location(), "Prismarine");
        addTag(ModTags.FACADE_MATERIALS.location(), "Facade Materials");

        ModBlocks.BLOCKS.getEntries().forEach(this::addTitleCased);
    }

    private void addTag(ResourceLocation id, String name) {
        add("tag.block." + id.getNamespace() + "." + id.getPath(), name);
        add("tag.item." + id.getNamespace() + "." + id.getPath(), name);
    }

    private void addTitleCased(DeferredHolder<Block, ? extends Block> holder) {
        add(holder.get(), Artisanal.titleCase(holder.getId().getPath()));
    }
}

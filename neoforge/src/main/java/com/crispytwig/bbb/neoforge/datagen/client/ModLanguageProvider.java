package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.common.registry.ModBlocks;
import com.crispytwig.bbb.common.registry.ModItems;
import com.crispytwig.bbb.common.registry.ModTags;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;


public class ModLanguageProvider extends LanguageProvider {
    private static final String POLISHED = "polished_";

    public ModLanguageProvider(PackOutput output) {
        super(output, BuildingButBetter.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + BuildingButBetter.MOD_ID, "Building But Better");
        add("itemGroup." + BuildingButBetter.MOD_ID + ".colored", "Building But Better Colored Blocks");
        add("itemGroup." + BuildingButBetter.MOD_ID + ".layers", "Building But Better Layers");

        add("bbb.configuration.title", "Building But Better Config");
        add("bbb.configuration.cherry_wood_sounds", "Wood Sounds");
        add("bbb.configuration.cherry_wood_sounds.tooltip", "Makes every woodset use the Cherry woodset's sounds");
        add("bbb.configuration.prismarine_deepslate_sounds", "Prismarine Sounds");
        add("bbb.configuration.prismarine_deepslate_sounds.tooltip", "Makes every Prismarine block use Deepslate sounds");

        add("container.bbb.sofa_crevice", "Sofa Crevice");

        add(ModItems.FACADE.get(), "Facade");
        add(ModItems.PAINT_BRUSH.get(), "Paint Brush");
        add("subtitles.bbb.block_painted", "Block painted");
        add("subtitles.bbb.curtain_open", "Curtain opens");
        add("subtitles.bbb.curtain_close", "Curtain closes");
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
        String path = holder.getId().getPath();
        add(holder.get(), BuildingButBetter.titleCase(polishedColorName(path)));
    }

    private static String polishedColorName(String path) {
        if (!path.startsWith(POLISHED)) {
            return path;
        }
        String rest = path.substring(POLISHED.length());
        for (DyeColor color : DyeColor.values()) {
            if (rest.equals(color.getName())) {
                return path + "_wood";
            }
            if (rest.equals(color.getName() + "_slab")) {
                return POLISHED + color.getName() + "_wood_slab";
            }
        }
        return path;
    }
}

package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("facade", mcLoc("item/generated")).texture("layer0", mcLoc("item/flower_banner_pattern"));
        ModBlocks.lanterns().forEach(holder -> {
            String name = holder.getId().getPath();
            generated(name, modLoc("item/" + name));
        });
        withExistingParent("hammer", mcLoc("item/handheld")).texture("layer0", modLoc("item/hammer"));
        paintBrush();
    }

    private void paintBrush() {
        ItemModelBuilder base = generated("paint_brush", modLoc("item/paint_brush"));
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_paint_brush";

            generated("item/paint_brushes/" + name, modLoc("item/" + name));
            base.override()
                    .predicate(BuildingButBetter.location("color"), (color.getId() + 1) / 17.0F)
                    .model(new ModelFile.UncheckedModelFile(BuildingButBetter.location("item/paint_brushes/" + name)))
                    .end();
        }
    }

    private ItemModelBuilder generated(String name, ResourceLocation texture) {
        return withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
    }
}

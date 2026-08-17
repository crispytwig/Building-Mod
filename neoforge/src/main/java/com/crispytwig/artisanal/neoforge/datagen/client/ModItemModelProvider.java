package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Artisanal.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("facade", mcLoc("item/generated")).texture("layer0", mcLoc("item/flower_banner_pattern"));
    }
}

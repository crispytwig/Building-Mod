package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
    }
}

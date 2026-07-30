package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
    }
}

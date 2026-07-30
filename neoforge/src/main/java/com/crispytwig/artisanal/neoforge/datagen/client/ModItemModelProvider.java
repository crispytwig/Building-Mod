package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Artisanal.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.ARCHITECTS_SCEPTER);
    }

    private void handheldItem(Supplier<? extends Item> item) {
        ResourceLocation name = BuiltInRegistries.ITEM.getKey(item.get());
        withExistingParent(name.getPath(), mcLoc("item/handheld"))
                .texture("layer0", Artisanal.location("item/" + name.getPath()));
    }
}

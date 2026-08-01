package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.ScepterOccupant;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Artisanal.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        scepterItem();
        spawnEggItem(ModItems.WRIGHT_SPAWN_EGG);
        withExistingParent("panel", mcLoc("item/generated")).texture("layer0", mcLoc("item/flower_banner_pattern"));
    }

    private void scepterItem() {
        ResourceLocation name = BuiltInRegistries.ITEM.getKey(((Supplier<? extends Item>) ModItems.ARCHITECTS_SCEPTER).get());
        ItemModelBuilder base = handheldItem(ModItems.ARCHITECTS_SCEPTER);
        for (ScepterOccupant occupant : ScepterOccupant.values()) {
            ItemModelBuilder captured = handheldLayer(name.getPath() + "_" + occupant.getSerializedName());
            base.override()
                    .predicate(Artisanal.location("captured"), occupant.getPredicateValue())
                    .model(captured)
                    .end();
        }
    }

    private void spawnEggItem(Supplier<? extends Item> item) {
        ResourceLocation name = BuiltInRegistries.ITEM.getKey(item.get());
        withExistingParent(name.getPath(), mcLoc("item/template_spawn_egg"));
    }

    private ItemModelBuilder handheldItem(Supplier<? extends Item> item) {
        return handheldLayer(BuiltInRegistries.ITEM.getKey(item.get()).getPath());
    }

    private ItemModelBuilder handheldLayer(String path) {
        return withExistingParent(path, mcLoc("item/handheld"))
                .texture("layer0", Artisanal.location("item/" + path));
    }
}

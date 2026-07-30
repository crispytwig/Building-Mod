package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, BuildingButBetter.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
    }
}

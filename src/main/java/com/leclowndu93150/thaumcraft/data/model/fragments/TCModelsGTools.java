package com.leclowndu93150.thaumcraft.data.model.fragments;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public final class TCModelsGTools {
    private TCModelsGTools() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItems.THAUMOMETER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SCRIBING_TOOLS.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }
}

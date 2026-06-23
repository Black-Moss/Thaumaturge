package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsHContainers;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.resources.Identifier;

public final class TCItemsHContainersModels {
    private TCItemsHContainersModels() {}

    public static void register(ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItemsHContainers.PHIAL.get(), ModelTemplates.FLAT_ITEM);
        registerPrimordialPearl(itemModels);
    }

    private static void registerPrimordialPearl(ItemModelGenerators itemModels) {
        Identifier pearlModel = itemModels.createFlatItemModel(TCItemsHContainers.PRIMORDIAL_PEARL.get(), ModelTemplates.FLAT_ITEM);
        Identifier noduleModel = itemModels.createFlatItemModel(TCItemsHContainers.PRIMORDIAL_PEARL.get(), "_nodule", ModelTemplates.FLAT_ITEM);
        Identifier moteModel = itemModels.createFlatItemModel(TCItemsHContainers.PRIMORDIAL_PEARL.get(), "_mote", ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked pearl = ItemModelUtils.plainModel(pearlModel);
        ItemModel.Unbaked nodule = ItemModelUtils.plainModel(noduleModel);
        ItemModel.Unbaked mote = ItemModelUtils.plainModel(moteModel);
        float noduleThreshold = (float) (PrimordialPearlItem.PEARL_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        float moteThreshold = (float) (PrimordialPearlItem.NODULE_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        itemModels.itemModelOutput.accept(
                TCItemsHContainers.PRIMORDIAL_PEARL.get(),
                ItemModelUtils.rangeSelect(
                        new Damage(true),
                        pearl,
                        ItemModelUtils.override(nodule, noduleThreshold),
                        ItemModelUtils.override(mote, moteThreshold)
                )
        );
    }
}

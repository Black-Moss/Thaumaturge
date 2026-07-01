package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.client.color.AspectColorTint;
import com.leclowndu93150.thaumcraft.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class TCItemsHContainersModels {
    private TCItemsHContainersModels() {}

    public static void register(ItemModelGenerators itemModels) {
        registerPhial(itemModels);
        registerPrimordialPearl(itemModels);
    }

    private static void registerPhial(ItemModelGenerators itemModels) {
        Identifier phialModel = itemModels.createFlatItemModel(TCItems.PHIAL.get(), ModelTemplates.FLAT_ITEM);
        Identifier filledModel = itemModels.generateLayeredItem(ModelLocationUtils.getModelLocation(TCItems.PHIAL.get(),"_filled"), TextureMapping.getItemTexture(TCItems.PHIAL.get()),TextureMapping.getItemTexture(TCItems.PHIAL.get(),"_overlay"));
        ItemModel.Unbaked phial = ItemModelUtils.plainModel(phialModel);
        ItemModel.Unbaked filled = ItemModelUtils.tintedModel(filledModel, new Constant(0xFFFFFF), new AspectColorTint(0xFFFFFF));
        itemModels.itemModelOutput.accept(
                TCItems.PHIAL.get(),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(TCDataComponents.ASPECTS.get()),
                        filled,
                        phial
                )
        );
    }

    private static void registerPrimordialPearl(ItemModelGenerators itemModels) {
        Identifier pearlModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), ModelTemplates.FLAT_ITEM);
        Identifier noduleModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), "_nodule", ModelTemplates.FLAT_ITEM);
        Identifier moteModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), "_mote", ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked pearl = ItemModelUtils.plainModel(pearlModel);
        ItemModel.Unbaked nodule = ItemModelUtils.plainModel(noduleModel);
        ItemModel.Unbaked mote = ItemModelUtils.plainModel(moteModel);
        float noduleThreshold = (float) (PrimordialPearlItem.PEARL_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        float moteThreshold = (float) (PrimordialPearlItem.NODULE_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        itemModels.itemModelOutput.accept(
                TCItems.PRIMORDIAL_PEARL.get(),
                ItemModelUtils.rangeSelect(
                        new Damage(true),
                        pearl,
                        ItemModelUtils.override(nodule, noduleThreshold),
                        ItemModelUtils.override(mote, moteThreshold)
                )
        );
    }
}

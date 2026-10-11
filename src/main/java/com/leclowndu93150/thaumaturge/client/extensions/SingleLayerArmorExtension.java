package com.leclowndu93150.thaumaturge.client.extensions;

import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

final class SingleLayerArmorExtension implements IClientItemExtensions {
    private final Function<ModelPart, ? extends Model> factory;
    private final ModelLayerLocation layer;
    private Model model;

    SingleLayerArmorExtension(Function<ModelPart, ? extends Model> factory, ModelLayerLocation layer) {
        this.factory = factory;
        this.layer = layer;
    }

    @Override
    public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
        if (model == null) {
            model = factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
        return model;
    }
}

package com.leclowndu93150.thaumcraft.compat.curio.client;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;
import java.util.function.Function;

public class GoggleCurioRenderer implements ICurioRenderer {

    private final Function<LayerTextureKey, Identifier> layerTextureLookup;

    public GoggleCurioRenderer() {
        this.layerTextureLookup = Util.memoize(key -> key.layer.getTextureLocation(key.layerType));
    }

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
        IClientItemExtensions extensions = IClientItemExtensions.of(stack);
        M model = (M) extensions.getGenericArmorModel(stack, EquipmentClientInfo.LayerType.HUMANOID, renderLayerParent.getModel());
        List<EquipmentClientInfo.Layer> layers = context.getEquipmentAssets().get(ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath(TCIds.MODID,"goggles"))).getLayers(EquipmentClientInfo.LayerType.HUMANOID);
        if (!layers.isEmpty()) {
            int dyeColor = extensions.getDefaultDyeColor(stack);
            boolean renderFoil = stack.hasFoil();
            int nextOrder = 0;

            int idx = 0;
            for (EquipmentClientInfo.Layer layer : layers) {
                int color = extensions.getArmorLayerTintColor(stack, layer, idx, dyeColor);
                if (color != 0) {
                    Identifier layerTexture = this.layerTextureLookup.apply(new LayerTextureKey(EquipmentClientInfo.LayerType.HUMANOID, layer));
                    layerTexture = net.neoforged.neoforge.client.ClientHooks.getArmorTexture(stack, EquipmentClientInfo.LayerType.HUMANOID, layer, layerTexture);
                    submitNodeCollector.order(nextOrder++)
                            .submitModel(
                                    model,
                                    renderState,
                                    poseStack,
                                    RenderTypes.armorCutoutNoCull(layerTexture),
                                    packedLight,
                                    OverlayTexture.NO_OVERLAY,
                                    color,
                                    null,
                                    renderState.outlineColor,
                                    null
                            );
                    if (renderFoil) {
                        submitNodeCollector.order(nextOrder++)
                                .submitModel(
                                        model,
                                        renderState,
                                        poseStack,
                                        RenderTypes.armorEntityGlint(),
                                        packedLight,
                                        OverlayTexture.NO_OVERLAY,
                                        color,
                                        null,
                                        renderState.outlineColor,
                                        null
                                );
                    }

                    renderFoil = false;
                }
                idx++;
            }
        }
    }

    private record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }
}

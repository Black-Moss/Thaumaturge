package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockModelsEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCBlockEntityRenderers {
    private static final float RECHARGE_PEDESTAL_ITEM_SCALE = 1.5F;

    private TCBlockEntityRenderers() {}

    @SubscribeEvent
    public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.INFUSION_MATRIX.get(), InfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.RECHARGE_PEDESTAL.get(),
                context -> new PedestalRenderer<>(context, RECHARGE_PEDESTAL_ITEM_SCALE));
        event.registerBlockEntityRenderer(TCBlockEntities.JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_VOID.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_BRAIN.get(), JarBrainRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.DIOPTRA.get(), DioptraRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.GOLEM_BUILDER.get(), GolemBuilderRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.HUNGRY_CHEST.get(), HungryChestRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BANNER.get(), BannerRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BELLOWS.get(), BellowsRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterStandalone event){
        for (int i = 0; i < BellowsRenderer.parts.length; i++) {
            String part = BellowsRenderer.parts[i];
            Identifier modelId = TCIds.rl("block/bellows/" + part);
            BellowsRenderer.MODEL_KEYS[i] = new StandaloneModelKey<>(modelId::toString);
            event.register(BellowsRenderer.MODEL_KEYS[i], SimpleUnbakedStandaloneModel.blockStateModel(modelId));
        }
    }
}

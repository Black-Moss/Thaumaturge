package com.leclowndu93150.thaumcraft.client.model;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public class TCModelsHandlers {

    public static final Identifier JAR_MODEL_ID =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "jar");

    @SubscribeEvent
    public static void onRegisterItemModels(RegisterSpecialModelRendererEvent event){
        event.register(JAR_MODEL_ID, JarItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

}

package com.leclowndu93150.thaumcraft.client.model;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.model.obj.TCObjUnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public class TCModelsHandlers {

    public static final Identifier JAR_MODEL_ID =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "jar");

    public static final Identifier OBJ_LOADER_ID =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "obj");

    @SubscribeEvent
    public static void onRegisterItemModels(RegisterSpecialModelRendererEvent event){
        event.register(JAR_MODEL_ID, JarItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterLoaders(ModelEvent.RegisterLoaders event) {
        event.register(OBJ_LOADER_ID, TCObjUnbakedModel.Loader.INSTANCE);
    }

}

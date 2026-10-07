package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ConnectedModelRegistration {
    private ConnectedModelRegistration() {}

    @SubscribeEvent
    public static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(ConnectedSheetModel.TYPE, ConnectedSheetModel.CODEC);
        event.registerModel(ConnectedTilesModel.TYPE, ConnectedTilesModel.CODEC);
        event.registerModel(ConnectedStairsModel.TYPE, ConnectedStairsModel.CODEC);
    }
}

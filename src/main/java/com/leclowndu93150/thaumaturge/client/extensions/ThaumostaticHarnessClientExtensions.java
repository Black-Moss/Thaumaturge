package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.ThaumostaticHarnessModel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ThaumostaticHarnessClientExtensions {
    private ThaumostaticHarnessClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new SingleLayerArmorExtension(ThaumostaticHarnessModel::new, TTModelLayers.THAUMOSTATIC_HARNESS), TTItems.THAUMOSTATIC_HARNESS.get());
    }
}

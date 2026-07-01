package com.leclowndu93150.thaumcraft.client.taint;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCFluidTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCClientFluidExtensions {
    private TCClientFluidExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new FluxGooClientExtensions(), TCFluidTypes.FLUX_GOO.get());
    }
}

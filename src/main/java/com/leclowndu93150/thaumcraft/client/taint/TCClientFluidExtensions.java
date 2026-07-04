package com.leclowndu93150.thaumcraft.client.taint;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCFluidTypes;
import com.leclowndu93150.thaumcraft.registry.TCFluids;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.client.renderer.block.FluidModel;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCClientFluidExtensions {
    private TCClientFluidExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new FluxGooClientExtensions(), TCFluidTypes.FLUX_GOO.get());
    }

    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        Material gooTexture = new Material(TCIds.rl("block/flux_goo"));
        event.register(new FluidModel.Unbaked(gooTexture, gooTexture, null, null),
                TCFluids.FLUX_GOO_SOURCE.get(), TCFluids.FLUX_GOO_FLOWING.get());
    }
}

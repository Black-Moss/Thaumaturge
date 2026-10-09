package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class InlayBlockColors {
    private static final int MAX_CHARGE = 15;
    private static final int MAX_CHANNEL = 255;
    private static final int OPAQUE_ALPHA = 255;
    private static final float UNCHARGED_BRIGHTNESS = 0.3F;
    private static final float CHARGED_BRIGHTNESS_SPAN = 0.5F;
    private static final float CHARGED_BRIGHTNESS_FLOOR = 0.5F;

    private InlayBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        BlockTintSource source = InlayBlockColors::tint;
        event.register(List.of(source), TTBlocks.INLAY.get());
    }

    private static int tint(BlockState state) {
        int charge = state.getValue(BlockInlay.CHARGE);
        float brightness = charge == 0 ? UNCHARGED_BRIGHTNESS : (float) charge / MAX_CHARGE * CHARGED_BRIGHTNESS_SPAN + CHARGED_BRIGHTNESS_FLOOR;
        int channel = Mth.clamp((int) (brightness * MAX_CHANNEL), 0, MAX_CHANNEL);
        return ARGB.color(OPAQUE_ALPHA, channel, channel, channel);
    }
}

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
    private static final float RAMP_SPAN = 0.5F;
    private static final float RAMP_BASE = 0.5F;
    private static final int FIRST_CHARGE = 1;

    private InlayBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        BlockTintSource source = InlayBlockColors::tint;
        event.register(List.of(source), TTBlocks.INLAY.get());
    }

    private static int tint(BlockState state) {
        int grey = Mth.clamp((int) (brightness(state.getValue(BlockInlay.CHARGE)) * MAX_CHANNEL), 0, MAX_CHANNEL);
        return ARGB.color(OPAQUE_ALPHA, grey, grey, grey);
    }

    private static float brightness(int charge) {
        if (charge < FIRST_CHARGE) {
            return UNCHARGED_BRIGHTNESS;
        }
        return (float) charge / MAX_CHARGE * RAMP_SPAN + RAMP_BASE;
    }
}

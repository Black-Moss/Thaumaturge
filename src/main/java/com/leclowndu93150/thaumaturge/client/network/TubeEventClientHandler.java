package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeCreakPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeVentPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TubeEventClientHandler {
    private static final float HISS_VOLUME = 0.25F;
    private static final float HISS_PITCH_BASE = 1.6F;
    private static final float HISS_PITCH_SPREAD = 0.4F;
    private static final float CREAK_VOLUME = 0.6F;
    private static final float CREAK_PITCH_BASE = 1.15F;
    private static final float CREAK_PITCH_SPREAD = 0.15F;

    private TubeEventClientHandler() {}

    public static void handleVent(ClientboundTubeVentPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> vent(payload));
    }

    public static void handleCreak(ClientboundTubeCreakPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> creak(payload));
    }

    private static void vent(ClientboundTubeVentPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockPos pos = payload.pos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlockEntityTube tube)) {
            return;
        }
        boolean alreadyVenting = tube.ventingTicks() > 0;
        tube.triggerVent(payload.color());
        if (!alreadyVenting) {
            RandomSource random = level.getRandom();
            playSound(level, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, HISS_VOLUME, HISS_PITCH_BASE + random.nextFloat() * HISS_PITCH_SPREAD);
        }
    }

    private static void creak(ClientboundTubeCreakPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RandomSource random = level.getRandom();
        playSound(level, payload.pos(), TTSounds.CREAK.get(), SoundSource.AMBIENT, CREAK_VOLUME, CREAK_PITCH_BASE + random.nextFloat() * CREAK_PITCH_SPREAD);
    }

    private static void playSound(ClientLevel level, BlockPos pos, SoundEvent event, SoundSource source, float volume, float pitch) {
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, event, source, volume, pitch, false);
    }
}

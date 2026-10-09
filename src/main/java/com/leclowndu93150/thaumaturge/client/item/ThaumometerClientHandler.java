package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedEntity;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ThaumometerClientHandler {
    private static final int HIGHLIGHT_INTERVAL_TICKS = 5;
    private static final int LOCK_WINDOW_TICKS = 2;
    private static final int PULSE_INTERVAL_TICKS = 2;
    private static final float SOUND_VOLUME = 0.2F;
    private static final float SOUND_PITCH_BASE = 0.45F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;
    private static final float RUNE_CHANNEL_MIN = 0.3F;
    private static final float RUNE_CHANNEL_SPREAD = 0.7F;
    private static final float RUNE_GRAVITY = 0.03F;
    private static final float ENTITY_RUNE_TICKS_PER_HEIGHT = 15.0F;
    private static final int BLOCK_RUNE_TICKS = 15;
    private static final double BLOCK_RUNE_Y_OFFSET = 0.25;
    private static final double RUNE_CENTRE_OFFSET = 0.5;
    private static final float EYE_HEIGHT_DIVISOR = 2.0F;

    private static @Nullable Identity lockedTarget;

    private ThaumometerClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.isPaused()) {
            return;
        }
        tickScan(minecraft, player, level);
        tickHighlight(player, level);
    }

    private static boolean holdsThaumometer(LocalPlayer player) {
        Item thaumometer = TTItems.THAUMOMETER.get();
        return player.getMainHandItem().is(thaumometer) || player.getOffhandItem().is(thaumometer);
    }

    private static void tickHighlight(LocalPlayer player, ClientLevel level) {
        if (player.tickCount % HIGHLIGHT_INTERVAL_TICKS != 0 || !holdsThaumometer(player)) {
            return;
        }
        ScanTarget target = ThaumometerItem.resolveTarget(level, player);
        if (!ScanningManager.isStillScannable(player, target)) {
            return;
        }
        if (target instanceof ScannedBlock(BlockPos pos)) {
            ClientEffects.scanSparkles(level, pos);
        } else if (target instanceof ScannedEntity(Entity entity)) {
            ClientEffects.scanSparkles(entity);
        }
    }

    private static void tickScan(Minecraft minecraft, LocalPlayer player, ClientLevel level) {
        if (!player.isUsingItem() || !player.getUseItem().is(TTItems.THAUMOMETER.get())) {
            lockedTarget = null;
            return;
        }
        int elapsed = player.getTicksUsingItem();
        ScanTarget target = ThaumometerItem.resolveTarget(level, player);
        if (!ScanningManager.isStillScannable(player, target) || !keepsLock(elapsed, target)) {
            cancelScan(minecraft, player);
            return;
        }
        if (elapsed % PULSE_INTERVAL_TICKS == 0) {
            pulse(level, player, target);
        }
        if (elapsed >= ThaumometerItem.SCAN_COMPLETE_ELAPSED_TICKS) {
            cancelScan(minecraft, player);
        }
    }

    private static boolean keepsLock(int elapsed, ScanTarget target) {
        Identity current = Identity.of(target);
        if (elapsed < LOCK_WINDOW_TICKS) {
            lockedTarget = current;
            return true;
        }
        return current.equals(lockedTarget);
    }

    private static void cancelScan(Minecraft minecraft, LocalPlayer player) {
        lockedTarget = null;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (gameMode != null) {
            gameMode.releaseUsingItem(player);
        }
    }

    private static void pulse(ClientLevel level, LocalPlayer player, ScanTarget target) {
        RandomSource random = level.getRandom();
        float pitch = SOUND_PITCH_BASE + random.nextFloat() * SOUND_PITCH_SPREAD;
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), TTSounds.CAMERA_TICKS.get(), SoundSource.PLAYERS, SOUND_VOLUME, pitch, false);
        float red = RUNE_CHANNEL_MIN + random.nextFloat() * RUNE_CHANNEL_SPREAD;
        float blue = RUNE_CHANNEL_MIN + random.nextFloat() * RUNE_CHANNEL_SPREAD;
        if (target instanceof ScannedEntity(Entity entity)) {
            double x = entity.getX() - RUNE_CENTRE_OFFSET;
            double y = entity.getY() + entity.getEyeHeight() / EYE_HEIGHT_DIVISOR;
            double z = entity.getZ() - RUNE_CENTRE_OFFSET;
            int lifetime = (int) (entity.getBoundingBox().getYsize() * ENTITY_RUNE_TICKS_PER_HEIGHT);
            ClientEffects.runeGlyph(level, x, y, z, red, 0.0F, blue, lifetime, RUNE_GRAVITY);
        } else if (target instanceof ScannedBlock(BlockPos pos)) {
            ClientEffects.runeGlyph(level, pos.getX(), pos.getY() + BLOCK_RUNE_Y_OFFSET, pos.getZ(), red, 0.0F, blue, BLOCK_RUNE_TICKS, RUNE_GRAVITY);
        }
    }

    private record Identity(int entityId, @Nullable BlockPos blockPos) {
        private static final int NO_ENTITY = -1;

        static Identity of(ScanTarget target) {
            if (target instanceof ScannedEntity(Entity entity)) {
                return new Identity(entity.getId(), null);
            }
            if (target instanceof ScannedBlock(BlockPos pos)) {
                return new Identity(NO_ENTITY, pos.immutable());
            }
            return new Identity(NO_ENTITY, null);
        }
    }
}

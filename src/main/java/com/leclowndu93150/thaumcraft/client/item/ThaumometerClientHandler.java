package com.leclowndu93150.thaumcraft.client.item;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.client.fx.FXClient;
import com.leclowndu93150.thaumcraft.content.item.ThaumometerItem;
import com.leclowndu93150.thaumcraft.content.research.scan.PointedEntityHelper;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumometerClientHandler {
    private static final int HIGHLIGHT_INTERVAL_TICKS = 5;
    private static final double HIGHLIGHT_ENTITY_RANGE = 16.0;
    private static final float HIGHLIGHT_ENTITY_PADDING = 5.0F;
    private static final double WILD_RAY_RANGE = 16.0;
    private static final int WILD_RAY_ANGLE_SPREAD = 25;
    private static final int SCAN_RUNE_BURSTS = 10;
    private static final float RUNE_ENTITY_HEIGHT_SCALE = 15.0F;
    private static final int RUNE_BLOCK_DURATION = 15;
    private static final float RUNE_GRAVITY = 0.03F;
    private static final float SCAN_VOLUME = 0.5F;
    private static final float SCAN_PITCH = 1.0F;

    private ThaumometerClientHandler() {}

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || !event.getItemStack().is(TCItems.THAUMOMETER.get())) {
            return;
        }
        Player player = event.getEntity();
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                TCSounds.SCAN.get(), SoundSource.PLAYERS, SCAN_VOLUME, SCAN_PITCH, false);
        drawScanFx(player.level(), player);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        boolean held = player.getMainHandItem().is(TCItems.THAUMOMETER.get())
                || player.getOffhandItem().is(TCItems.THAUMOMETER.get());
        if (!held) {
            ThaumometerTargetOverlay.setTarget(null);
            return;
        }
        if (player.tickCount % HIGHLIGHT_INTERVAL_TICKS != 0) {
            return;
        }
        Entity target = PointedEntityHelper.getPointedEntity(mc.level, player,
                ThaumometerItem.SCAN_ENTITY_MIN_RANGE, HIGHLIGHT_ENTITY_RANGE, HIGHLIGHT_ENTITY_PADDING, true);
        if (target != null && ScanningManager.isThingStillScannable(player, target)) {
            FXClient.scanHighlight(target);
        }
        ThaumometerTargetOverlay.setTarget(target);
        BlockHitResult wild = wildBlockRay(mc.level, player);
        if (wild.getType() == HitResult.Type.BLOCK
                && ScanningManager.isThingStillScannable(player, wild.getBlockPos())) {
            FXClient.scanHighlight(mc.level, wild.getBlockPos());
        }
    }

    private static void drawScanFx(Level level, Player player) {
        RandomSource rand = level.getRandom();
        Entity target = PointedEntityHelper.getPointedEntity(level, player,
                ThaumometerItem.SCAN_ENTITY_MIN_RANGE, ThaumometerItem.SCAN_ENTITY_RANGE, 0.0F, true);
        if (target != null) {
            for (int a = 0; a < SCAN_RUNE_BURSTS; a++) {
                FXClient.blockRunes(level,
                        target.getX() - 0.5,
                        target.getY() + target.getEyeHeight() / 2.0F,
                        target.getZ() - 0.5,
                        0.3F + rand.nextFloat() * 0.7F, 0.0F, 0.3F + rand.nextFloat() * 0.7F,
                        (int) (target.getBbHeight() * RUNE_ENTITY_HEIGHT_SCALE), RUNE_GRAVITY);
            }
            return;
        }
        BlockHitResult hit = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange())),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            for (int a = 0; a < SCAN_RUNE_BURSTS; a++) {
                FXClient.blockRunes(level,
                        pos.getX(), pos.getY() + 0.25, pos.getZ(),
                        0.3F + rand.nextFloat() * 0.7F, 0.0F, 0.3F + rand.nextFloat() * 0.7F,
                        RUNE_BLOCK_DURATION, RUNE_GRAVITY);
            }
        }
    }

    private static BlockHitResult wildBlockRay(Level level, Player player) {
        RandomSource rand = level.getRandom();
        float pitch = player.getXRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        float yaw = player.getYRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        Vec3 from = player.getEyePosition();
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        Vec3 to = from.add(direction.scale(WILD_RAY_RANGE));
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
    }
}

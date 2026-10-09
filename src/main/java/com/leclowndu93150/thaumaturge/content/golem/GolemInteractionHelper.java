package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class GolemInteractionHelper {
    private static final int CLICK_RANK_XP = 1;
    private static final double FACE_OFFSET = 0.5D;

    private GolemInteractionHelper() {}

    public static void golemClick(Level level, IGolemAPI golem, BlockPos pos, Direction face, ItemStack stack, boolean leftClick, boolean sneaking) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity entity = golem.asEntity();
        FakePlayer player = TTFakePlayer.GOLEM.at(serverLevel, entity);
        try {
            player.setShiftKeyDown(sneaking);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            if (leftClick) {
                player.gameMode.destroyBlock(pos);
            } else {
                rightClick(serverLevel, entity, player, pos, face, stack);
            }
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.error("Golem click failed at {}", pos, e);
        }
        player.setShiftKeyDown(false);
        returnInventory(serverLevel, golem, entity, player);
        golem.addRankXp(CLICK_RANK_XP);
        golem.swingArm();
    }

    private static void rightClick(ServerLevel level, LivingEntity entity, FakePlayer player, BlockPos pos, Direction face, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem && new AABB(pos.relative(face)).intersects(entity.getBoundingBox())) {
            entity.setPos(entity.position().add(face.getStepX(), face.getStepY(), face.getStepZ()));
            player.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
        }
        Vec3 hit = Vec3.atCenterOf(pos).relative(face, FACE_OFFSET);
        player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, pos, false));
    }

    private static void returnInventory(ServerLevel level, IGolemAPI golem, LivingEntity entity, FakePlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack remaining = inventory.getItem(slot);
            if (remaining.isEmpty()) {
                continue;
            }
            ItemStack overflow = golem.hands().hold(remaining.copy());
            if (!overflow.isEmpty()) {
                entity.spawnAtLocation(level, overflow);
            }
        }
        inventory.clearContent();
    }
}

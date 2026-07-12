package com.leclowndu93150.thaumcraft.content.item;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.TCResearchEntries;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import com.leclowndu93150.thaumcraft.content.research.scan.PointedEntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class ThaumometerItem extends Item {
    public static final double SCAN_ENTITY_MIN_RANGE = 1.0;
    public static final double SCAN_ENTITY_RANGE = 9.0;
    public static final int USE_DURATION_TICKS = 25;
    public static final int SCAN_COMPLETE_ELAPSED_TICKS = 20;

    private static final int SCAN_RELEASE_TOLERANCE_TICKS = 18;
    private static final int AURA_CHECK_INTERVAL_TICKS = 20;
    private static final int FLUX_WARN_BASE_DIVISOR = 3;

    public ThaumometerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!ScanningManager.isThingStillScannable(player, resolveTarget(level, player))) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME.withoutItem();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player)) {
            return;
        }
        if (USE_DURATION_TICKS - remaining >= SCAN_COMPLETE_ELAPSED_TICKS) {
            entity.releaseUsingItem();
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return false;
        }
        if (USE_DURATION_TICKS - remaining < SCAN_RELEASE_TOLERANCE_TICKS) {
            return false;
        }
        Object target = resolveTarget(level, player);
        if (!ScanningManager.isThingStillScannable(player, target)) {
            return false;
        }
        ScanningManager.scanTheThing(player, target);
        return true;
    }

    public static @Nullable Object resolveTarget(Level level, Player player) {
        Entity target = PointedEntityHelper.getPointedEntity(
                level, player, SCAN_ENTITY_MIN_RANGE, SCAN_ENTITY_RANGE, 0.0F, true);
        if (target != null) {
            return target;
        }
        BlockHitResult hit = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange())),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        if (held && owner.tickCount % AURA_CHECK_INTERVAL_TICKS == 0 && owner instanceof ServerPlayer player) {
            warnAboutFlux(level, player);
        }
    }

    private static void warnAboutFlux(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        float flux = AuraHelper.getFlux(level, pos);
        boolean dangerous = flux > AuraHelper.getVis(level, pos)
                || flux > AuraHelper.getAuraBase(level, pos) / (float) FLUX_WARN_BASE_DIVISOR;
        if (dangerous && !KnowledgeAccess.of(player).isResearchKnown(TCResearchEntries.FLUX)) {
            ResearchManager.complete(player, TCResearchEntries.FLUX);
            player.sendOverlayMessage(Component.translatable("research.thaumcraft.flux.warn")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}

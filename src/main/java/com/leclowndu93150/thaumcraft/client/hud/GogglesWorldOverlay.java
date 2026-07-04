package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class GogglesWorldOverlay {
    private static final float TAG_SCALE_CAP = 0.3F;
    private static final float TAG_SCALE_GROWTH = 0.031F;
    private static final float TAG_SCALE_DECAY_DIVISOR = 10.0F;
    private static final float SPACE_ABOVE_LIFT = 0.4F;
    private static final float TAG_ALPHA = 0.75F;

    private static float tagScale;
    private static @Nullable BlockPos lastTarget;

    private GogglesWorldOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!GogglesAccess.wearsGoggles(mc.player)) {
            resetAnimation();
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            resetAnimation();
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof IAspectContainer container)) {
            resetAnimation();
            return;
        }
        AspectList aspects = container.getAspects();
        if (aspects.isEmpty()) {
            resetAnimation();
            return;
        }
        if (!pos.equals(lastTarget)) {
            tagScale = 0.0F;
            lastTarget = pos.immutable();
        }
        if (tagScale < TAG_SCALE_CAP) {
            tagScale = tagScale + (TAG_SCALE_GROWTH - tagScale / TAG_SCALE_DECAY_DIVISOR);
        }

        boolean spaceAbove = mc.level.getBlockState(pos.above()).isAir();
        Direction dir = spaceAbove ? Direction.UP : mc.player.getDirection().getOpposite();
        double y = pos.getY() + (spaceAbove ? SPACE_ABOVE_LIFT : 0.0F);
        drawTags(event.getPoseStack(), mc, pos.getX(), y, pos.getZ(), aspects, dir);
    }

    private static void resetAnimation() {
        tagScale = 0.0F;
        lastTarget = null;
    }

    private static void drawTags(PoseStack poseStack, Minecraft mc,
                                 double x, double y, double z, AspectList aspects, Direction dir) {
        AspectTagWorldRenderer.renderTagCloud(poseStack, mc, x, y, z, aspects, dir, tagScale, TAG_ALPHA);
    }
}

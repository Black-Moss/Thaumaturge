package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.hud.tag.TagAnimation;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ThaumometerAspectOverlay {
    private static final float SCALE_CAP = 0.5F;
    private static final float TAG_ALPHA = 0.85F;
    private static final double CLOUD_LIFT = 0.4;
    private static final double ENTITY_CLOUD_SHIFT = 0.5;
    private static final double HALF_BLOCK = 0.5;
    private static final double BLOCK_TOP = 1.0;

    private static final TagAnimation ANIMATION = new TagAnimation(SCALE_CAP);

    private ThaumometerAspectOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        LocalPlayer player = mc.player;
        Optional<TagCloud> cloud = Optional.of(player).filter(p -> isHoldingThaumometer(mc, p)).flatMap(p -> resolve(p, mc.level));
        if (cloud.isPresent()) {
            TagCloud tags = cloud.get();
            AspectTagWorldRenderer.renderTagCloud(event.getPoseStack(), mc, tags.x(), tags.y(), tags.z(), tags.aspects(), tags.face(), tags.scale(), TAG_ALPHA,
                    aspect -> AspectPools.isDiscovered(player, aspect));
        } else {
            ANIMATION.reset();
        }
    }

    private static boolean isHoldingThaumometer(Minecraft mc, LocalPlayer player) {
        ItemStack offHand = player.getOffhandItem();
        boolean mainHeld = player.getMainHandItem().is(TTItems.THAUMOMETER);
        boolean ownOverlay = mainHeld && offHand.isEmpty() && mc.options.getCameraType().isFirstPerson();
        return (mainHeld || offHand.is(TTItems.THAUMOMETER)) && !ownOverlay;
    }

    private static Optional<TagCloud> resolve(LocalPlayer player, Level level) {
        HitResult result = ScanRaycastHelper.performRaycast(player, ClipContext.Fluid.SOURCE_ONLY);
        if (result instanceof EntityHitResult entityHit) {
            return resolveEntity(player, entityHit.getEntity());
        }
        if (result instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            return resolveBlock(player, level, blockHit);
        }
        return Optional.empty();
    }

    private static Optional<TagCloud> resolveEntity(LocalPlayer player, Entity entity) {
        return gated(player, ScanKeys.entity(entity.getType()), EntityAspects.of(entity), aspects -> new TagCloud(entity.getX() - ENTITY_CLOUD_SHIFT, entity.getY() + entity.getBbHeight() + CLOUD_LIFT,
                entity.getZ() - ENTITY_CLOUD_SHIFT, aspects, Direction.UP, ANIMATION.advance(entity)));
    }

    private static Optional<TagCloud> resolveBlock(LocalPlayer player, Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        Optional<BlockEntityNode> node = Optional.ofNullable(level.getBlockEntity(pos)).filter(BlockEntityNode.class::isInstance).map(BlockEntityNode.class::cast);
        if (node.isPresent()) {
            return gated(player, ScanNode.researchKey(level, pos), node.get().getAspects(),
                    aspects -> new TagCloud(pos.getX(), pos.getY() + CLOUD_LIFT, pos.getZ(), aspects, Direction.UP, ANIMATION.advance(pos)));
        }
        ItemStack stack = ScanningManager.stackOf(player, ScanTarget.block(pos));
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return gated(player, ScanKeys.item(stack.getItem()), AspectIndexAccess.index().of(stack), aspects -> {
            boolean viewedFromAbove = player.getEyeY() > pos.getY() + BLOCK_TOP && level.getBlockState(pos.above()).isAir();
            Direction face = viewedFromAbove ? Direction.UP : hit.getDirection();
            return new TagCloud(pos.getX() + HALF_BLOCK * face.getStepX(), pos.getY() + HALF_BLOCK * face.getStepY(), pos.getZ() + HALF_BLOCK * face.getStepZ(), aspects, face, ANIMATION.advance(pos));
        });
    }

    private static Optional<TagCloud> gated(LocalPlayer player, Identifier research, AspectList aspects, Function<AspectList, TagCloud> factory) {
        if (aspects.isEmpty() || !KnowledgeAccess.of(player).isResearchKnown(research)) {
            return Optional.empty();
        }
        return Optional.of(factory.apply(aspects));
    }

    private record TagCloud(double x, double y, double z, AspectList aspects, Direction face, float scale) {
    }
}

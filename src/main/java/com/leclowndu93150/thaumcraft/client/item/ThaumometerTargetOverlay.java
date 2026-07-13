package com.leclowndu93150.thaumcraft.client.item;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumcraft.content.aspect.EntityAspects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumometerTargetOverlay {
    private static final float TAG_SCALE_CAP = 0.5F;
    private static final float TAG_SCALE_GROWTH = 0.031F;
    private static final float TAG_SCALE_DECAY_DIVISOR = 10.0F;
    private static final float TAG_ALPHA = 220.0F / 255.0F;

    private static @Nullable Entity target;
    private static float tagScale;

    private ThaumometerTargetOverlay() {}

    public static void setTarget(@Nullable Entity newTarget) {
        if (newTarget != target) {
            tagScale = 0.0F;
        }
        target = newTarget;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui || target == null || target.isRemoved()) {
            return;
        }
        AspectList aspects = EntityAspects.of(target);
        if (aspects.isEmpty()) {
            return;
        }
        if (tagScale < TAG_SCALE_CAP) {
            tagScale = tagScale + (TAG_SCALE_GROWTH - tagScale / TAG_SCALE_DECAY_DIVISOR);
        }
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double x = Mth.lerp(partialTicks, target.xOld, target.getX());
        double y = Mth.lerp(partialTicks, target.yOld, target.getY());
        double z = Mth.lerp(partialTicks, target.zOld, target.getZ());
        AspectTagWorldRenderer.renderTagCloud(event.getPoseStack(), mc,
                x, y + target.getBbHeight(), z, aspects, null, tagScale, TAG_ALPHA);
    }
}

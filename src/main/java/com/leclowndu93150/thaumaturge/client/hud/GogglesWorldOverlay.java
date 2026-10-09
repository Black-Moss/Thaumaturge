package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.hud.tag.GogglesTagResolver;
import com.leclowndu93150.thaumaturge.client.hud.tag.GogglesTagTarget;
import com.leclowndu93150.thaumaturge.client.hud.tag.TagAnimation;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GogglesWorldOverlay {
    private static final float SCALE_CAP = 0.3F;
    private static final float TAG_ALPHA = 0.75F;
    private static final Predicate<Holder<IAspect>> ALL_DISCOVERED = aspect -> true;
    private static final TagAnimation ANIMATION = new TagAnimation(SCALE_CAP);

    private GogglesWorldOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        GogglesTagTarget tags = findTarget(mc, mc.level, mc.player);
        if (tags == null) {
            ANIMATION.reset();
            return;
        }
        float scale = ANIMATION.advance(tags.block());
        Vec3 at = tags.origin();
        AspectTagWorldRenderer.renderTagCloud(event.getPoseStack(), mc, at.x, at.y, at.z, tags.aspects(), tags.face(), scale, TAG_ALPHA, ALL_DISCOVERED, tags.showAmounts());
    }

    private static GogglesTagTarget findTarget(Minecraft mc, Level level, LocalPlayer player) {
        HitResult hit = mc.hitResult;
        if (hit == null) {
            return null;
        }
        return GogglesTagResolver.resolve(level, player, hit).orElse(null);
    }
}

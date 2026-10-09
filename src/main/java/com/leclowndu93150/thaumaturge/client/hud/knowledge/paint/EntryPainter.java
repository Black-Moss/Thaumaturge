package com.leclowndu93150.thaumaturge.client.hud.knowledge.paint;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.ActiveGain;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainEntry;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.motion.EntryPose;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.motion.EntryPoseCalculator;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.motion.GlintCalculator;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.motion.ScreenPoint;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class EntryPainter {
    private EntryPainter() {}

    public static ScreenPoint draw(GuiGraphicsExtractor graphics, ClientLevel level, ActiveGain gain, float partialTick) {
        GainEntry entry = gain.entry();
        float remaining = gain.remainingAt(partialTick);
        EntryPose pose = EntryPoseCalculator.pose(entry.life(), remaining);
        ScreenPoint point = EntryPoseCalculator.locate(entry.rolls(), pose, graphics.guiWidth(), graphics.guiHeight());
        float tiltRadians = entry.rolls().tiltDegrees() * Mth.DEG_TO_RAD;
        GlintCalculator.plan(entry.life(), remaining, entry.rolls())
                .ifPresent(plan -> StripBlitter.draw(graphics, ParticleTextures.STAR_GLINT, point.x(), point.y(), plan.size(), plan.rotationDegrees() * Mth.DEG_TO_RAD, plan.frame(), plan.color()));
        if (pose.size() > 0.0F) {
            float scale = pose.size() / EntryPoseCalculator.ICON_SIZE;
            graphics.pose().pushMatrix();
            graphics.pose().translate(point.x(), point.y());
            graphics.pose().rotate(tiltRadians);
            graphics.pose().scale(scale, scale);
            entry.subject().drawIcon(graphics, level);
            graphics.pose().popMatrix();
        }
        return point;
    }
}

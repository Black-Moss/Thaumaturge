package com.leclowndu93150.thaumaturge.client.hud.knowledge.paint;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.spark.SparkPool;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class SparkPainter {
    private static final float FADE_SPAN = 1.0F / 6.0F;
    private static final float MIN_SIZE = 4.8F;
    private static final float MAX_SIZE = 9.6F;
    private static final int MAX_CHANNEL = 255;

    private SparkPainter() {}

    public static void draw(GuiGraphicsExtractor graphics, SparkPool pool, float partialTick) {
        for (int i = 0; i < pool.size(); i++) {
            if (pool.delay(i) > 0) {
                continue;
            }
            float progress = (pool.age(i) + partialTick) / pool.life(i);
            int alphaByte = Mth.clamp((int) (fade(progress) * MAX_CHANNEL), 0, MAX_CHANNEL);
            float size = Mth.lerp(Mth.clamp(progress, 0.0F, 1.0F), MIN_SIZE, MAX_SIZE);
            boolean star = pool.star(i);
            Identifier strip = star ? ParticleTextures.STAR_GLINT : ParticleTextures.ORB_GLOW;
            int frames = star ? ParticleTextures.STAR_GLINT_FRAMES : ParticleTextures.ORB_GLOW_FRAMES;
            float x = (float) Mth.lerp(partialTick, pool.lastX(i), pool.x(i));
            float y = (float) Mth.lerp(partialTick, pool.lastY(i), pool.y(i));
            StripBlitter.draw(graphics, strip, x, y, size, 0.0F, pool.age(i) % frames, ARGB.color(alphaByte, MAX_CHANNEL, pool.green(i), pool.blue(i)));
        }
    }

    private static float fade(float progress) {
        if (progress < FADE_SPAN) {
            return progress / FADE_SPAN;
        }
        if (progress > 1.0F - FADE_SPAN) {
            return (1.0F - progress) / FADE_SPAN;
        }
        return 1.0F;
    }
}

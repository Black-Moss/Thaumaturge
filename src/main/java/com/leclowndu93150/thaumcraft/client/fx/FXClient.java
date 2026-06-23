package com.leclowndu93150.thaumcraft.client.fx;

import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.content.fx.data.NitorCoreData;
import com.leclowndu93150.thaumcraft.content.fx.helper.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FXClient {
    private FXClient() {}

    public static void fxGeneric(Level level, FXGenericData data, double x, double y, double z) {
        spawn(data, x, y, z, 0.0, 0.0, 0.0);
    }

    public static void essentiaDrop(Level level, double x, double y, double z, float r, float g, float b, float alpha) {
        RandomSource rand = level.getRandom();
        FXGenericData data = FXGenericData.builder()
                .motion(rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005)
                .maxAge(20 + rand.nextInt(10))
                .color(r, g, b)
                .alpha(alpha)
                .particle(25)
                .scale(0.4F + rand.nextFloat() * 0.2F, 0.2F)
                .layer(1)
                .gravity(0.01F)
                .build();
        spawn(data, x, y, z, 0.0, 0.0, 0.0);
    }

    public static void nitorCore(Level level, double x, double y, double z, double vx, double vy, double vz, int color) {
        float cr = ((color >> 16) & 0xFF) / 255.0F;
        float cg = ((color >> 8) & 0xFF) / 255.0F;
        float cb = (color & 0xFF) / 255.0F;
        NitorCoreData data = new NitorCoreData(cr, cg, cb);
        spawn(data, x, y, z, vx, vy, vz);
    }

    public static void nitorFlames(Level level, double x, double y, double z, double vx, double vy, double vz, int color, int delay) {
        RandomSource rand = level.getRandom();
        float cr = ((color >> 16) & 0xFF) / 255.0F;
        float cg = ((color >> 8) & 0xFF) / 255.0F;
        float cb = (color & 0xFF) / 255.0F;
        FXGenericData data = FXGenericData.builder()
                .motion(vx, vy, vz)
                .maxAge(10 + rand.nextInt(5))
                .color(cr, cg, cb)
                .alpha(0.66F)
                .loop(true)
                .grid(Sprites.WISP_LOOP.grid())
                .particles(Sprites.WISP_LOOP.start(), Sprites.WISP_LOOP.num(), Sprites.WISP_LOOP.inc())
                .scale(3.0F + rand.nextFloat(), 0.05F)
                .random(0.0025F, 0.0F, 0.0025F)
                .delay(delay)
                .build();
        spawn(data, x, y, z, vx, vy, vz);
    }

    private static void spawn(ParticleOptions data, double x, double y, double z, double vx, double vy, double vz) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.particleEngine.createParticle(data, x, y, z, vx, vy, vz);
    }
}

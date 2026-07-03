package com.leclowndu93150.thaumcraft.client.fx;

import com.leclowndu93150.thaumcraft.content.fx.data.BlockRunesData;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.content.fx.data.NitorCoreData;
import com.leclowndu93150.thaumcraft.content.fx.helper.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

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

    public static void blockRunes(Level level, double x, double y, double z,
                                  float r, float g, float b, int duration, float gravity) {
        BlockRunesData data = new BlockRunesData(r, g, b, duration, gravity, false);
        spawn(data, x + 0.5, y + 0.5, z + 0.5, 0.0, 0.0, 0.0);
    }

    public static void scanHighlight(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        scanHighlight(level, shape.bounds().move(pos));
    }

    public static void scanHighlight(Entity entity) {
        scanHighlight(entity.level(), entity.getBoundingBox());
    }

    public static void scanHighlight(Level level, AABB box) {
        RandomSource rand = level.getRandom();
        int num = Mth.ceil(box.getSize() * 2.0);
        double ax = (box.minX + box.maxX) / 2.0;
        double ay = (box.minY + box.maxY) / 2.0;
        double az = (box.minZ + box.maxZ) / 2.0;
        for (Direction face : Direction.values()) {
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; a++) {
                double x = Mth.clamp(mx + rand.nextGaussian() * (box.maxX - box.minX), box.minX - ax, box.maxX - ax);
                double y = Mth.clamp(my + rand.nextGaussian() * (box.maxY - box.minY), box.minY - ay, box.maxY - ay);
                double z = Mth.clamp(mz + rand.nextGaussian() * (box.maxZ - box.minZ), box.minZ - az, box.maxZ - az);
                float r = Mth.nextInt(rand, 16, 32) / 255.0F;
                float g = Mth.nextInt(rand, 132, 165) / 255.0F;
                float b = Mth.nextInt(rand, 223, 239) / 255.0F;
                drawSimpleSparkle(level, rand, ax + x, ay + y, az + z, 0.0, 0.0, 0.0,
                        0.4F + (float) rand.nextGaussian() * 0.1F, r, g, b,
                        rand.nextInt(10), 1.0F, 0.0F, 4);
            }
        }
    }

    public static void drawSimpleSparkle(Level level, RandomSource rand, double x, double y, double z,
                                         double vx, double vy, double vz, float scale,
                                         float r, float g, float b, int delay, float decay, float gravity, int baseAge) {
        boolean small = rand.nextFloat() < 0.2F;
        int age = baseAge * 4 + rand.nextInt(baseAge);
        float[] alphas = new float[6 + rand.nextInt(age / 3)];
        for (int a = 1; a < alphas.length - 1; a++) {
            alphas[a] = rand.nextFloat();
        }
        FXGenericData data = FXGenericData.builder()
                .motion(vx, vy, vz)
                .maxAge(age)
                .color(r, g, b)
                .alpha(alphas)
                .particles(small ? 320 : 512, 16, 1)
                .loop(true)
                .gravity(gravity)
                .scale(scale, scale * 2.0F)
                .layer(0)
                .slowDown(decay)
                .random(5.0E-4F, 0.001F, 5.0E-4F)
                .wind(5.0E-4)
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

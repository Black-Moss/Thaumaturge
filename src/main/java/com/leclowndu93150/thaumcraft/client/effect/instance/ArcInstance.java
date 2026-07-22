package com.leclowndu93150.thaumcraft.client.effect.instance;

import com.leclowndu93150.thaumcraft.client.effect.manager.IFXInstance;

import com.leclowndu93150.thaumcraft.client.effect.ClientEffects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ArcInstance implements IFXInstance {
    static final int MAX_AGE = 3;
    private static final int MAX_SEGMENTS = 50;
    private static final double GRAVITY = 0.115;
    private static final double NOISE = 0.25;

    public final double startX;
    public final double startY;
    public final double startZ;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float length;
    public final List<Vec3> points = new ArrayList<>();
    int age = 0;
    boolean expired = false;

    public ArcInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float gravityHint) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        double dtx = tx - sx;
        double dty = ty - sy;
        double dtz = tz - sz;
        Vec3 ve = new Vec3(dtx, dty, dtz);
        this.length = (float)ve.length();
        ClientLevel level = Minecraft.getInstance().level;
        RandomSource rand = level != null ? level.getRandom() : RandomSource.create();
        Vec3 velocity = calculateVelocity(new Vec3(0, 0, 0), ve, gravityHint, GRAVITY);
        double velLenSq = velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z;
        Vec3 vc = new Vec3(0, 0, 0);
        this.points.add(vc);
        for (int c = 0; distanceSq(ve, vc) > velLenSq && c < MAX_SEGMENTS; c++) {
            Vec3 vt = vc.add(velocity.x, velocity.y, velocity.z);
            vc = vt;
            Vec3 noised = vt.add(
                    (rand.nextDouble() - rand.nextDouble()) * NOISE,
                    (rand.nextDouble() - rand.nextDouble()) * NOISE,
                    (rand.nextDouble() - rand.nextDouble()) * NOISE);
            this.points.add(noised);
            if (level != null) {
                spawnChildSparkle(level, rand, sx + noised.x, sy + noised.y, sz + noised.z, rand);
            }
            velocity = velocity.subtract(0, GRAVITY / 1.9, 0);
        }
        this.points.add(ve);
    }

    private void spawnChildSparkle(ClientLevel level, RandomSource rand, double wx, double wy, double wz, RandomSource r) {
        ClientEffects.drawSimpleSparkle(level, rand, wx, wy, wz, 0.0, 0.0, 0.0, 0.5F,
                Mth.clamp(this.colorR * 3.0F, 0.0F, 1.0F),
                Mth.clamp(this.colorG * 3.0F, 0.0F, 1.0F),
                Mth.clamp(this.colorB * 3.0F, 0.0F, 1.0F),
                2 + rand.nextInt(3), 0.995F, 0.1F, 8);
    }

    public void tick() {
        if (this.age++ >= MAX_AGE) this.expired = true;
    }

    public boolean isExpired() { return this.expired; }

    public float alpha(float partialTick) {
        return 1.0F - (this.age + partialTick) / (float)MAX_AGE;
    }

    private static double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static Vec3 calculateVelocity(Vec3 from, Vec3 to, double heightGain, double gravity) {
        double endGain = to.y - from.y;
        double dxz = to.x - from.x;
        double dzz = to.z - from.z;
        double horizDist = Math.sqrt(dxz * dxz + dzz * dzz);
        double maxGain = Math.max(heightGain, endGain + heightGain);
        double a = -horizDist * horizDist / (4.0 * maxGain);
        double c = -endGain;
        double slope = -horizDist / (2.0 * a) - Math.sqrt(horizDist * horizDist - 4.0 * a * c) / (2.0 * a);
        double vy = Math.sqrt(maxGain * gravity);
        double vh = vy / slope;
        if (horizDist < 1.0E-9) return new Vec3(0, vy, 0);
        double dirx = dxz / horizDist;
        double dirz = dzz / horizDist;
        return new Vec3(vh * dirx, vy, vh * dirz);
    }
}

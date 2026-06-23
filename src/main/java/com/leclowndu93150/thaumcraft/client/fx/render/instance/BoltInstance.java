package com.leclowndu93150.thaumcraft.client.fx.render.instance;

import com.leclowndu93150.thaumcraft.client.fx.render.manager.IFXInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BoltInstance implements IFXInstance {
    static final int MAX_AGE = 3;

    public final double startX;
    public final double startY;
    public final double startZ;
    public final double targetX;
    public final double targetY;
    public final double targetZ;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float width;
    public final float length;
    public final float dr;
    public final long seed;
    int age = 0;
    boolean expired = false;

    public BoltInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.targetX = tx - sx;
        this.targetY = ty - sy;
        this.targetZ = tz - sz;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.width = width;
        double dist = Math.sqrt(this.targetX * this.targetX + this.targetY * this.targetY + this.targetZ * this.targetZ);
        this.length = (float)(dist * Math.PI);
        ClientLevel level = Minecraft.getInstance().level;
        RandomSource rand = level != null ? level.getRandom() : RandomSource.create();
        this.dr = (float)(rand.nextInt(50) * Math.PI);
        this.seed = rand.nextInt(1000);
    }

    public void tick() {
        if (this.age++ >= MAX_AGE) this.expired = true;
    }

    public boolean isExpired() { return this.expired; }

    public List<PathStep> computePath(float partialTick) {
        Random rr = new Random(this.seed);
        int steps = (int)this.length;
        if (steps < 3) steps = 3;
        List<PathStep> result = new ArrayList<>(steps);
        result.add(new PathStep(0, 0, 0, this.width));
        float ampl = (this.age + partialTick) / 10.0F;
        for (int a = 1; a < steps - 1; a++) {
            float dist = a * (this.length / steps) + this.dr;
            double dx = this.targetX / steps * a + Mth.sin(dist / 4.0F) * ampl;
            double dy = this.targetY / steps * a + Mth.sin(dist / 3.0F) * ampl;
            double dz = this.targetZ / steps * a + Mth.sin(dist / 2.0F) * ampl;
            dx += (rr.nextFloat() - rr.nextFloat()) * 0.1F;
            dy += (rr.nextFloat() - rr.nextFloat()) * 0.1F;
            dz += (rr.nextFloat() - rr.nextFloat()) * 0.1F;
            float w = (rr.nextInt(4) == 0 ? 1.0F - this.age * 0.25F : 1.0F) * this.width;
            result.add(new PathStep(dx, dy, dz, w));
        }
        result.add(new PathStep(this.targetX, this.targetY, this.targetZ, this.width));
        return result;
    }

    public float alpha(float partialTick) {
        return Mth.clamp(1.0F - (this.age + partialTick) / (float)MAX_AGE, 0.1F, 1.0F);
    }

    public record PathStep(double x, double y, double z, float width) {}

    public Vec3 origin() {
        return new Vec3(this.startX, this.startY, this.startZ);
    }
}

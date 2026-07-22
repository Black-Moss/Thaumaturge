package com.leclowndu93150.thaumcraft.client.effect.instance;

import com.leclowndu93150.thaumcraft.client.effect.manager.IFXInstance;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class VoidStreamInstance implements IFXInstance {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int seed;
    private float particleScale;
    private double posX;
    private double posY;
    private double posZ;
    private double motionX;
    private double motionY;
    private double motionZ;
    private final float gravity = 0.2F;
    private int age = 0;
    private int maxAge;
    private int length = 40;
    private int growing = -1;
    private boolean expired = false;
    private final List<EssentiaStreamInstance.Quat> vecs = new ArrayList<>();

    public VoidStreamInstance(double sx, double sy, double sz, double tx, double ty, double tz, int seed, float scale) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.posX = sx;
        this.posY = sy;
        this.posZ = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.seed = seed;
        RandomSource rand = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getRandom() : RandomSource.create();
        this.particleScale = (float)(scale * (1.0 + rand.nextGaussian() * 0.15F));
        double dx = tx - sx;
        double dy = ty - sy;
        double dz = tz - sz;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 21.0F);
        if (base < 1) base = 1;
        this.maxAge = base * 2;
        this.motionX = Mth.sin(seed / 4.0F) * 0.025F;
        this.motionY = Mth.sin(seed / 3.0F) * 0.025F;
        this.motionZ = Mth.sin(seed / 2.0F) * 0.025F;
        this.vecs.add(new EssentiaStreamInstance.Quat(0.001, 0.0, 0.0, 0.0));
        this.vecs.add(new EssentiaStreamInstance.Quat(0.001, 0.0, 0.0, 0.0));
    }

    public boolean isExpired() {
        return this.expired;
    }

    public void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        RandomSource rand = level.getRandom();
        if (this.age++ < this.maxAge && this.length >= 1) {
            this.motionY += 0.01 * this.gravity;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.motionX *= 0.985;
            this.motionY *= 0.985;
            this.motionZ *= 0.985;
            this.motionX = Mth.clamp((float)this.motionX, -0.04F, 0.04F);
            this.motionY = Mth.clamp((float)this.motionY, -0.04F, 0.04F);
            this.motionZ = Mth.clamp((float)this.motionZ, -0.04F, 0.04F);
            double dx = this.targetX - this.posX;
            double dy = this.targetY - this.posY;
            double dz = this.targetZ - this.posZ;
            double d13 = 0.01;
            double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
            if (dist > 1.0E-6) {
                dx /= dist;
                dy /= dist;
                dz /= dist;
                this.motionX += dx * (d13 / Math.min(1.0, dist)) + rand.nextGaussian() * 0.015F;
                this.motionY += dy * (d13 / Math.min(1.0, dist)) + rand.nextGaussian() * 0.015F;
                this.motionZ += dz * (d13 / Math.min(1.0, dist)) + rand.nextGaussian() * 0.015F;
            }
            float scale = this.particleScale * (0.75F + Mth.sin((this.seed + this.age) / 2.0F) * 0.25F);
            if (dist < 0.5) {
                float f = Mth.sin((float)(dist * (Math.PI / 2.0)));
                scale *= f;
                this.particleScale *= f;
            }
            if (this.particleScale > 0.001F) {
                this.vecs.add(new EssentiaStreamInstance.Quat(scale, this.posX - this.startX, this.posY - this.startY, this.posZ - this.startZ));
            } else {
                if (this.growing < 0) this.growing = this.age;
                this.length--;
            }
            if (this.vecs.size() > this.length) {
                this.vecs.remove(0);
            }
        } else {
            this.expired = true;
        }
    }

    public EssentiaStreamInstance.Snapshot snapshotWithRadiusMul(float partialTick, float radiusMul) {
        int n = this.vecs.size();
        if (n < 3) return null;
        double[][] points = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        for (int i = 0; i < n; i++) {
            int c = n - 1 - i;
            EssentiaStreamInstance.Quat v = this.vecs.get(i);
            float variance = 1.0F + Mth.sin((c + this.age) / 3.0F) * 0.2F;
            float xx = Mth.sin((c + this.age) / 6.0F) * 0.01F;
            float yy = Mth.sin((c + this.age) / 7.0F) * 0.01F;
            float zz = Mth.sin((c + this.age) / 8.0F) * 0.01F;
            points[i][0] = v.x + xx;
            points[i][1] = v.y + yy;
            points[i][2] = v.z + zz;
            radii[i] = v.s * variance * radiusMul;
            if (c > n - 10) {
                radii[i] *= Mth.cos((float)((c - (n - 12)) / 10.0F * (Math.PI / 2.0)));
            }
            if (i == 0)      radii[i] = 0.0;
            else if (i == 1) radii[i] = 0.0;
            else if (i == 2) radii[i] = (this.particleScale * 0.5F + radii[i]) / 2.0;
            else if (i == 3) radii[i] = (this.particleScale + radii[i]) / 2.0;
            else if (i == 4) radii[i] = (this.particleScale + radii[i] * 2.0) / 3.0;
            colours[i][0] = 1.0F;
            colours[i][1] = 1.0F;
            colours[i][2] = 1.0F;
            colours[i][3] = 1.0F;
        }
        float startSlice = this.growing < 0 ? 0.0F : 0.075F * (this.age - this.growing + partialTick);
        return new EssentiaStreamInstance.Snapshot(points, colours, radii, this.startX, this.startY, this.startZ, 0.075F, startSlice);
    }
}

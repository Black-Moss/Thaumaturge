package com.leclowndu93150.thaumcraft.client.fx.render.instance;

import com.leclowndu93150.thaumcraft.client.fx.render.manager.IFXInstance;

import com.leclowndu93150.thaumcraft.client.fx.FXClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class EssentiaStreamInstance implements IFXInstance {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float colorR;
    private final float colorG;
    private final float colorB;
    private final int count;
    private final double dropSpawnY;
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
    private int length;
    private int growing = -1;
    private boolean expired = false;
    private final List<Quat> vecs = new ArrayList<>();

    public EssentiaStreamInstance(
            double sx, double sy, double sz,
            double tx, double ty, double tz,
            int color, int count, float scale, int extend, double my) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.posX = sx;
        this.posY = sy;
        this.posZ = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        RandomSource rand = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getRandom() : RandomSource.create();
        this.particleScale = (float)(scale * (1.0 + rand.nextGaussian() * 0.15F));
        this.length = Math.max(20, extend);
        this.count = count;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        double dx = tx - sx;
        double dy = ty - sy;
        double dz = tz - sz;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 21.0F);
        if (base < 1) base = 1;
        this.maxAge = base;
        this.motionX = Mth.sin(count / 4.0F) * 0.015F;
        this.motionY = my + Mth.sin(count / 3.0F) * 0.015F;
        this.motionZ = Mth.sin(count / 2.0F) * 0.015F;
        this.vecs.add(new Quat(0.001, 0.0, 0.0, 0.0));
        this.vecs.add(new Quat(0.001, 0.0, 0.0, 0.0));
        this.dropSpawnY = sy;
    }

    public void extend(int amount) {
        int add = Math.max(amount, 5);
        this.length += add;
        this.maxAge += add;
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
            this.motionX = Mth.clamp((float)this.motionX, -0.05F, 0.05F);
            this.motionY = Mth.clamp((float)this.motionY, -0.05F, 0.05F);
            this.motionZ = Mth.clamp((float)this.motionZ, -0.05F, 0.05F);
            double dx = this.targetX - this.posX;
            double dy = this.targetY - this.posY;
            double dz = this.targetZ - this.posZ;
            double d13 = 0.01;
            double d11 = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
            if (d11 > 1.0E-6) {
                dx /= d11;
                dy /= d11;
                dz /= d11;
                this.motionX += dx * (d13 / Math.min(1.0, d11));
                this.motionY += dy * (d13 / Math.min(1.0, d11));
                this.motionZ += dz * (d13 / Math.min(1.0, d11));
            }
            float scale = this.particleScale * (0.75F + Mth.sin((this.count + this.age) / 2.0F) * 0.25F);
            if (d11 < 1.0) {
                float f = Mth.sin((float)(d11 * (Math.PI / 2.0)));
                scale *= f;
                this.particleScale *= f;
            }

            if (this.particleScale > 0.001F) {
                this.vecs.add(new Quat(scale, this.posX - this.startX, this.posY - this.startY, this.posZ - this.startZ));
            } else {
                if (this.growing < 0) this.growing = this.age;
                this.length--;
                FXClient.essentiaDrop(level,
                        this.targetX + rand.nextGaussian() * 0.075,
                        this.targetY + rand.nextGaussian() * 0.075,
                        this.targetZ + rand.nextGaussian() * 0.075,
                        this.colorR, this.colorG, this.colorB, 0.5F);
            }

            if (this.vecs.size() > this.length) {
                this.vecs.remove(0);
            }

            if (this.vecs.size() > 2 && rand.nextBoolean()) {
                int q = rand.nextInt(3);
                if (rand.nextBoolean()) q = this.vecs.size() - 2;
                Quat sample = this.vecs.get(q);
                FXClient.essentiaDrop(level,
                        sample.x + this.startX,
                        sample.y + this.startY,
                        sample.z + this.startZ,
                        this.colorR, this.colorG, this.colorB, 0.5F);
            }
        } else {
            this.expired = true;
        }
    }

    public Snapshot snapshot(float partialTick) {
        int n = this.vecs.size();
        if (n < 3) return null;
        double[][] points = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        int c = n;
        for (Quat v : this.vecs) {
            c--;
            float variance = 1.0F + Mth.sin((c + this.age) / 3.0F) * 0.2F;
            float xx = Mth.sin((c + this.age) / 6.0F) * 0.03F;
            float yy = Mth.sin((c + this.age) / 7.0F) * 0.03F;
            float zz = Mth.sin((c + this.age) / 8.0F) * 0.03F;
            points[c][0] = v.x + xx;
            points[c][1] = v.y + yy;
            points[c][2] = v.z + zz;
            radii[c] = v.s * variance;
            if (c > n - 10) {
                radii[c] *= Mth.cos((float)((c - (n - 12)) / 10.0F * (Math.PI / 2.0)));
            }
            if (c == 0)      radii[c] = 0.0;
            else if (c == 1) radii[c] = 0.0;
            else if (c == 2) radii[c] = (this.particleScale * 0.5F + radii[c]) / 2.0;
            else if (c == 3) radii[c] = (this.particleScale + radii[c]) / 2.0;
            else if (c == 4) radii[c] = (this.particleScale + radii[c] * 2.0) / 3.0;
            float v2 = 1.0F - Mth.sin((c + this.age) / 2.0F) * 0.1F;
            colours[c][0] = Math.min(1.0F, this.colorR * v2);
            colours[c][1] = Math.min(1.0F, this.colorG * v2);
            colours[c][2] = Math.min(1.0F, this.colorB * v2);
            colours[c][3] = 1.0F;
        }
        float startSlice = this.growing < 0 ? 0.0F : 0.075F * (this.age - this.growing + partialTick);
        return new Snapshot(points, colours, radii, this.startX, this.startY, this.startZ, 0.075F, startSlice);
    }

    public record Snapshot(double[][] points, float[][] colours, double[] radii, double originX, double originY, double originZ, float texSlice, float start) {}

    public static final class Quat {
        public final double s;
        public final double x;
        public final double y;
        public final double z;
        public Quat(double s, double x, double y, double z) {
            this.s = s;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

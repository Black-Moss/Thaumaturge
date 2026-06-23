package com.leclowndu93150.thaumcraft.client.render.stream;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BoreStreamInstance {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final float colorR;
    private final float colorG;
    private final float colorB;
    private final int count;
    private final int targetEntityId;
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
    private final List<EssentiaStreamInstance.Quat> vecs = new ArrayList<>();

    public BoreStreamInstance(double sx, double sy, double sz, int targetEntityId,
                              int color, int count, float scale, int extend, double my) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.posX = sx;
        this.posY = sy;
        this.posZ = sz;
        this.targetEntityId = targetEntityId;
        RandomSource rand = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getRandom() : RandomSource.create();
        this.particleScale = (float)(scale * (1.0 + rand.nextGaussian() * 0.15F));
        this.length = Math.max(5, extend);
        this.count = count;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.maxAge = this.length * 10;
        this.motionX = Mth.sin(count / 4.0F) * 0.15F;
        this.motionY = my + Mth.sin(count / 3.0F) * 0.15F;
        this.motionZ = Mth.sin(count / 2.0F) * 0.15F;
        this.vecs.add(new EssentiaStreamInstance.Quat(0.001, 0.0, 0.0, 0.0));
        this.vecs.add(new EssentiaStreamInstance.Quat(0.001, 0.0, 0.0, 0.0));
    }

    public boolean isExpired() {
        return this.expired;
    }

    public void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity target = level.getEntity(this.targetEntityId);
        if (target == null) {
            this.expired = true;
            return;
        }
        if (this.age++ < this.maxAge && this.length >= 1) {
            this.motionY += 0.01 * this.gravity;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.motionX *= 0.985;
            this.motionY *= 0.985;
            this.motionZ *= 0.985;
            double dx = target.getX() - this.posX;
            double dy = (target.getY() + target.getEyeHeight()) - this.posY;
            double dz = target.getZ() - this.posZ;
            double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
            double clamp = dist / 10.0;
            this.motionX = Mth.clamp((float)this.motionX, -clamp, clamp);
            this.motionY = Mth.clamp((float)this.motionY, -clamp, clamp);
            this.motionZ = Mth.clamp((float)this.motionZ, -clamp, clamp);
            if (dist > 1.0E-6) {
                dx /= dist;
                dy /= dist;
                dz /= dist;
                this.motionX += dx * (clamp / Math.min(1.0, dist));
                this.motionY += dy * (clamp / Math.min(1.0, dist));
                this.motionZ += dz * (clamp / Math.min(1.0, dist));
            }
            float scale = this.particleScale * (0.75F + Mth.sin((this.count + this.age) / 2.0F) * 0.25F);
            if (dist < 1.0) {
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

    public EssentiaStreamInstance.Snapshot snapshot(float partialTick) {
        int n = this.vecs.size();
        if (n < 3) return null;
        double[][] points = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        for (int i = 0; i < n; i++) {
            int c = n - 1 - i;
            EssentiaStreamInstance.Quat v = this.vecs.get(i);
            float variance = 1.0F + Mth.sin((c + this.age) / 3.0F) * 0.2F;
            float xx = Mth.sin((c + this.age) / 6.0F) * 0.03F;
            float yy = Mth.sin((c + this.age) / 7.0F) * 0.03F;
            float zz = Mth.sin((c + this.age) / 8.0F) * 0.03F;
            points[i][0] = v.x + xx;
            points[i][1] = v.y + yy;
            points[i][2] = v.z + zz;
            radii[i] = v.s * variance;
            if (c > n - 10) {
                radii[i] *= Mth.cos((float)((c - (n - 12)) / 10.0F * (Math.PI / 2.0)));
            }
            if (i == 0)      radii[i] = 0.0;
            else if (i == 1) radii[i] = 0.0;
            else if (i == 2) radii[i] = (this.particleScale * 0.5F + radii[i]) / 2.0;
            else if (i == 3) radii[i] = (this.particleScale + radii[i]) / 2.0;
            else if (i == 4) radii[i] = (this.particleScale + radii[i] * 2.0) / 3.0;
            float v2 = 1.0F - Mth.sin((c + this.age) / 2.0F) * 0.1F;
            colours[i][0] = this.colorR * v2;
            colours[i][1] = this.colorG * v2;
            colours[i][2] = this.colorB * v2;
            colours[i][3] = 1.0F;
        }
        float startSlice = this.growing < 0 ? 0.0F : 0.075F * (this.age - this.growing + partialTick);
        return new EssentiaStreamInstance.Snapshot(points, colours, radii, this.startX, this.startY, this.startZ, 0.075F, startSlice);
    }
}

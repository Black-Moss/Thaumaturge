package com.leclowndu93150.thaumaturge.client.effect;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BoxSurfaceSampler {
    private static final double POINTS_PER_AREA = 4.0;

    private final AABB box;
    private final double areaX;
    private final double areaY;
    private final double areaZ;

    public BoxSurfaceSampler(AABB box) {
        this.box = box;
        this.areaX = box.getYsize() * box.getZsize();
        this.areaY = box.getXsize() * box.getZsize();
        this.areaZ = box.getXsize() * box.getYsize();
    }

    public int pointCount() {
        return Mth.ceil(2.0 * (areaX + areaY + areaZ) * POINTS_PER_AREA);
    }

    public Vec3 sample(RandomSource random) {
        double pick = random.nextDouble() * (areaX + areaY + areaZ);
        boolean high = random.nextBoolean();
        double u = random.nextDouble();
        double v = random.nextDouble();
        if (pick < areaX) {
            return new Vec3(face(box.minX, box.maxX, high), Mth.lerp(u, box.minY, box.maxY), Mth.lerp(v, box.minZ, box.maxZ));
        }
        if (pick < areaX + areaY) {
            return new Vec3(Mth.lerp(u, box.minX, box.maxX), face(box.minY, box.maxY, high), Mth.lerp(v, box.minZ, box.maxZ));
        }
        return new Vec3(Mth.lerp(u, box.minX, box.maxX), Mth.lerp(v, box.minY, box.maxY), face(box.minZ, box.maxZ, high));
    }

    private static double face(double low, double high, boolean upper) {
        return upper ? high : low;
    }
}

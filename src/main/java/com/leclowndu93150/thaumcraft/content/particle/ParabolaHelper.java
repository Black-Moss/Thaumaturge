package com.leclowndu93150.thaumcraft.content.particle;

import org.joml.Vector3d;

public final class ParabolaHelper {
    private ParabolaHelper() {}

    public static Vector3d calculateVelocity(Vector3d from, Vector3d to, double heightGain, double gravity) {
        double endGain = to.y - from.y;
        double horizDist = Math.sqrt(distanceSquared2d(from, to));
        double maxGain = heightGain > endGain + heightGain ? heightGain : endGain + heightGain;
        double a = -horizDist * horizDist / (4.0 * maxGain);
        double c = -endGain;
        double slope = -horizDist / (2.0 * a) - Math.sqrt(horizDist * horizDist - 4.0 * a * c) / (2.0 * a);
        double vy = Math.sqrt(maxGain * gravity);
        double vh = vy / slope;
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double mag = Math.sqrt(dx * dx + dz * dz);
        if (mag < 1.0E-9) {
            return new Vector3d(0.0, vy, 0.0);
        }
        double dirx = dx / mag;
        double dirz = dz / mag;
        return new Vector3d(vh * dirx, vy, vh * dirz);
    }

    public static double distanceSquared2d(Vector3d from, Vector3d to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return dx * dx + dz * dz;
    }

    public static double distanceSquared3d(Vector3d from, Vector3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        return dx * dx + dy * dy + dz * dz;
    }
}

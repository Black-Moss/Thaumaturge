package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class EssentiaDripEmitter {
    private static final int MIN_TRAIL_FOR_DRIPS = 3;
    private static final float DRIP_CHANCE = 0.5F;
    private static final float HEAD_DRIP_CHANCE = 0.2F;
    private static final int OLDEST_WINDOW = 4;
    private static final int HEAD_BACKSET = 2;
    private static final float DRIP_ALPHA = 0.5F;
    private static final double DESTINATION_SCATTER = 0.04;

    private final Vec3 origin;
    private final float red;
    private final float green;
    private final float blue;

    public EssentiaDripEmitter(Vec3 origin, float red, float green, float blue) {
        this.origin = origin;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void dropAtDestination(ClientLevel level, Vec3 destination) {
        RandomSource random = level.getRandom();
        double x = destination.x + random.nextGaussian() * DESTINATION_SCATTER;
        double y = destination.y + random.nextGaussian() * DESTINATION_SCATTER;
        double z = destination.z + random.nextGaussian() * DESTINATION_SCATTER;
        ClientEffects.essentiaDrop(level, x, y, z, this.red, this.green, this.blue, DRIP_ALPHA);
    }

    public void dripFromTrail(ClientLevel level, List<TrailPoint> trail) {
        int size = trail.size();
        RandomSource random = level.getRandom();
        if (size < MIN_TRAIL_FOR_DRIPS || random.nextFloat() >= DRIP_CHANCE) {
            return;
        }
        int index = random.nextFloat() < HEAD_DRIP_CHANCE ? size - HEAD_BACKSET : random.nextInt(Math.min(OLDEST_WINDOW, size));
        TrailPoint point = trail.get(index);
        ClientEffects.essentiaDrop(level, this.origin.x + point.x(), this.origin.y + point.y(), this.origin.z + point.z(), this.red, this.green, this.blue, DRIP_ALPHA);
    }
}

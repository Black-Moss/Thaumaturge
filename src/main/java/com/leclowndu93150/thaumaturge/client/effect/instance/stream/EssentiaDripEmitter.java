package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class EssentiaDripEmitter {
    private static final float DROP_ALPHA = 0.5F;
    private static final double DESTINATION_SPREAD = 0.075;
    private static final int DRIP_POOL = 3;
    private static final int MIN_TRAIL_FOR_DRIP = 2;
    private static final int SECOND_NEWEST_BACK = 2;

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
        double x = destination.x + random.nextGaussian() * DESTINATION_SPREAD;
        double y = destination.y + random.nextGaussian() * DESTINATION_SPREAD;
        double z = destination.z + random.nextGaussian() * DESTINATION_SPREAD;
        ClientEffects.essentiaDrop(level, x, y, z, this.red, this.green, this.blue, DROP_ALPHA);
    }

    public void dripFromTrail(ClientLevel level, List<TrailPoint> trail) {
        int size = trail.size();
        if (size <= MIN_TRAIL_FOR_DRIP) {
            return;
        }
        RandomSource random = level.getRandom();
        if (!random.nextBoolean()) {
            return;
        }
        int pick = random.nextInt(DRIP_POOL);
        if (random.nextBoolean()) {
            pick = size - SECOND_NEWEST_BACK;
        }
        TrailPoint point = trail.get(pick);
        ClientEffects.essentiaDrop(level, point.x() + this.origin.x, point.y() + this.origin.y, point.z() + this.origin.z, this.red, this.green, this.blue, DROP_ALPHA);
    }
}

package com.leclowndu93150.thaumaturge.client.effect.instance.beam;

import com.leclowndu93150.thaumaturge.client.effect.instance.BeamPayloadIds;
import net.minecraft.world.phys.Vec3;

public record BeamSourceKey(int beamType, boolean withSource, int anchorEntityId, Vec3 launch) {
    private static final double SAME_LAUNCH_DISTANCE_SQR = 0.01;

    public boolean matches(BeamSourceKey other) {
        if (this.beamType != other.beamType || this.withSource != other.withSource) {
            return false;
        }
        if (this.anchorEntityId != BeamPayloadIds.NO_ENTITY || other.anchorEntityId != BeamPayloadIds.NO_ENTITY) {
            return this.anchorEntityId == other.anchorEntityId;
        }
        return this.launch.distanceToSqr(other.launch) < SAME_LAUNCH_DISTANCE_SQR;
    }
}

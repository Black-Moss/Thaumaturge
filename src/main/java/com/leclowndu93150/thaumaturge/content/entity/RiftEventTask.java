package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.server.level.ServerLevel;

final class RiftEventTask implements RiftTask {
    private static final int EVENT_INTERVAL = 600;
    private static final int ROLL_RANGE = 1000;

    @Override
    public int interval() {
        return EVENT_INTERVAL;
    }

    @Override
    public void run(ServerLevel level, EntityFluxRift rift) {
        float stability = rift.stabilityValue();
        if (stability >= 0.0F) {
            return;
        }
        if (rift.getRandom().nextInt(ROLL_RANGE) >= Math.abs(stability) + rift.currentSize()) {
            return;
        }
        RiftEvents.roll(level, rift);
    }
}

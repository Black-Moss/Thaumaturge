package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import net.minecraft.world.phys.Vec3;

public record WobbleLaunch(double amplitude, double verticalBoost) implements LaunchRule {
    @Override
    public void apply(HeadMotion head, int waveSeed) {
        head.launch(Vec3.ZERO, waveSeed, this.amplitude, this.verticalBoost);
    }
}

package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import net.minecraft.world.phys.Vec3;

public record DirectedLaunch(Vec3 direction, double speed, double wobble) implements LaunchRule {
    @Override
    public void apply(HeadMotion head, int waveSeed) {
        Vec3 heading = this.direction.lengthSqr() > 0.0 ? this.direction.normalize().scale(this.speed) : Vec3.ZERO;
        head.launch(heading, waveSeed, this.wobble, 0.0);
    }
}

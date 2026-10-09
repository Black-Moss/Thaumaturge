package com.leclowndu93150.thaumaturge.content.spell.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

record FxStyle(FxFamily family, FxParams params, double motionScale) {
    void spawn(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        Vec3 velocity = motion.scale(motionScale);
        level.addParticle(family.build(params, color, random), at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
    }
}

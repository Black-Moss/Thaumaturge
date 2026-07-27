package com.leclowndu93150.thaumaturge.mixin.world.entity.monster;

import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slime.class)
public interface SlimeAccessor {
    @Accessor("wasOnGround")
    boolean thaumaturge$getWasOnGround();

    @Accessor("wasOnGround")
    void thaumaturge$setWasOnGround(boolean wasOnGround);
}

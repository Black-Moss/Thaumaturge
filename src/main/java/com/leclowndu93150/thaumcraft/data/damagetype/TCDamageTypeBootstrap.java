package com.leclowndu93150.thaumcraft.data.damagetype;

import com.leclowndu93150.thaumcraft.api.damagesource.TCDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public final class TCDamageTypeBootstrap {
    private TCDamageTypeBootstrap() {}

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(TCDamageTypes.TAINT,
                new DamageType("thaumcraft.taint", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
        context.register(TCDamageTypes.TENTACLE,
                new DamageType("thaumcraft.tentacle", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
        context.register(TCDamageTypes.SWARM,
                new DamageType("thaumcraft.swarm", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
        context.register(TCDamageTypes.DISSOLVE,
                new DamageType("thaumcraft.dissolve", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
    }
}

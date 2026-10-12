package com.leclowndu93150.thaumaturge.content.world.plant;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;

public final class MagicPlantTraits {
    private static final float VISHROOM_SPORE_CHANCE = 0.3F;
    private static final float VISHROOM_CAP_HEIGHT = 0.3F;
    private static final float VISHROOM_SPORE_SPREAD = 0.35F;
    private static final float VISHROOM_SPORE_DRIFT = 0.0F;

    private static final float CINDERPEARL_PUFF_CHANCE = 0.4F;
    private static final float CINDERPEARL_BULB_HEIGHT = 0.65F;
    private static final float CINDERPEARL_PUFF_SPREAD = 0.06F;
    private static final float CINDERPEARL_PUFF_DRIFT = 0.0F;

    private static final float SHIMMERLEAF_GLINT_CHANCE = 0.3F;
    private static final float SHIMMERLEAF_LEAF_HEIGHT = 0.45F;
    private static final float SHIMMERLEAF_GLINT_SPREAD = 0.12F;
    private static final float SHIMMERLEAF_GLINT_DRIFT = 0.012F;

    public static final PlantAura VISHROOM_SPORES = new PlantAura(List.of(ParticleTypes.WITCH), VISHROOM_SPORE_CHANCE, VISHROOM_CAP_HEIGHT, VISHROOM_SPORE_SPREAD, VISHROOM_SPORE_DRIFT);

    public static final PlantAura CINDERPEARL_EMBERS = new PlantAura(List.of(ParticleTypes.SMOKE, ParticleTypes.FLAME), CINDERPEARL_PUFF_CHANCE, CINDERPEARL_BULB_HEIGHT, CINDERPEARL_PUFF_SPREAD,
            CINDERPEARL_PUFF_DRIFT);

    public static final PlantAura SHIMMERLEAF_GLINTS = new PlantAura(List.of(ParticleTypes.END_ROD), SHIMMERLEAF_GLINT_CHANCE, SHIMMERLEAF_LEAF_HEIGHT, SHIMMERLEAF_GLINT_SPREAD,
            SHIMMERLEAF_GLINT_DRIFT);

    public static final Optional<PlantContactEffect> VISHROOM_NAUSEA = Optional.of(new PlantContactEffect(MobEffects.NAUSEA, 200, 5));

    private MagicPlantTraits() {}
}

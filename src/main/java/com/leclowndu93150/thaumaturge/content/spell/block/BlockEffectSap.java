package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEffectSap extends Block {
    private static final int EFFECT_TICKS = 40;
    private static final int WITHER_AMPLIFIER = 0;
    private static final int SLOWNESS_AMPLIFIER = 1;
    private static final int HUNGER_AMPLIFIER = 1;
    private static final double SPARK_BASE_HEIGHT = 0.1515;
    private static final float SPARK_MAX_LIFT = 0.33F;
    private static final double SPARK_LIFT_HEIGHT_SHARE = 0.5;
    private static final float SPARK_BASE_SCALE = 3.0F;
    private static final float SPARK_LIFT_SCALE = 6.0F;
    private static final float SPARK_ALPHA = 1.0F;
    private static final float SPARK_RED_BASE = 0.3F;
    private static final float SPARK_RED_DROP = 0.1F;
    private static final float SPARK_GREEN = 0.0F;
    private static final float SPARK_BLUE_BASE = 0.5F;
    private static final float SPARK_BLUE_RANGE = 0.2F;
    private static final int HUM_ODDS = 50;
    private static final float HUM_VOLUME = 0.25F;
    private static final float HUM_PITCH_SPREAD = 0.2F;

    public static final MapCodec<BlockEffectSap> CODEC = simpleCodec(BlockEffectSap::new);

    public BlockEffectSap(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEffectSap> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean intersects) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || living.is(ThaumaturgeEntityTypeTags.ELDRITCH) || living.hasEffect(MobEffects.WITHER)) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_TICKS, WITHER_AMPLIFIER, true, true));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_TICKS, SLOWNESS_AMPLIFIER, true, true));
        living.addEffect(new MobEffectInstance(MobEffects.HUNGER, EFFECT_TICKS, HUNGER_AMPLIFIER, true, true));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float lift = random.nextFloat() * SPARK_MAX_LIFT;
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + SPARK_BASE_HEIGHT + lift * SPARK_LIFT_HEIGHT_SHARE;
        double z = pos.getZ() + random.nextDouble();
        int color = ARGB.colorFromFloat(SPARK_ALPHA, SPARK_RED_BASE - random.nextFloat() * SPARK_RED_DROP, SPARK_GREEN, SPARK_BLUE_BASE + random.nextFloat() * SPARK_BLUE_RANGE);
        level.addParticle(new SparkParticleOptions(color, SPARK_ALPHA, SPARK_BASE_SCALE + SPARK_LIFT_SCALE * lift), x, y, z, 0.0, 0.0, 0.0);
        if (random.nextInt(HUM_ODDS) == 0) {
            float pitch = 1.0F + (random.nextFloat() - random.nextFloat()) * HUM_PITCH_SPREAD;
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TTSounds.JACOBS.get(), SoundSource.AMBIENT, HUM_VOLUME, pitch, false);
        }
    }
}

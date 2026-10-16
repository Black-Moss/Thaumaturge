package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEffectSap extends Block {
    public static final MapCodec<BlockEffectSap> CODEC = simpleCodec(BlockEffectSap::new);

    private static final int AFFLICTION_TICKS = 40;
    private static final int WITHER_AMPLIFIER = 0;
    private static final int SLOWNESS_AMPLIFIER = 1;
    private static final int HUNGER_AMPLIFIER = 1;

    private static final float SPARK_MAX_HEIGHT_ROLL = 0.33F;
    private static final double SPARK_FLOOR_OFFSET = 0.1515;
    private static final double SPARK_RISE_PER_ROLL = 0.5;
    private static final float SPARK_MIN_SCALE = 3.0F;
    private static final float SPARK_SCALE_PER_ROLL = 6.0F;
    private static final float SPARK_ALPHA = 1.0F;
    private static final float SPARK_RED_LOW = 0.2F;
    private static final float SPARK_RED_SPAN = 0.1F;
    private static final float SPARK_GREEN = 0.0F;
    private static final float SPARK_BLUE_LOW = 0.5F;
    private static final float SPARK_BLUE_SPAN = 0.2F;

    private static final int CRACKLE_CHANCE_DENOMINATOR = 50;
    private static final float CRACKLE_VOLUME = 0.25F;
    private static final float CRACKLE_PITCH_BASE = 1.0F;
    private static final float CRACKLE_PITCH_SPREAD = 0.2F;

    public BlockEffectSap(BlockBehaviour.Properties properties) {
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
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || !susceptible(living)) {
            return;
        }
        living.addEffect(affliction(MobEffects.WITHER, WITHER_AMPLIFIER));
        living.addEffect(affliction(MobEffects.SLOWNESS, SLOWNESS_AMPLIFIER));
        living.addEffect(affliction(MobEffects.HUNGER, HUNGER_AMPLIFIER));
    }

    private static boolean susceptible(LivingEntity living) {
        return !living.is(ThaumaturgeEntityTypeTags.ELDRITCH) && !living.hasEffect(MobEffects.WITHER);
    }

    private static MobEffectInstance affliction(Holder<MobEffect> effect, int amplifier) {
        return new MobEffectInstance(effect, AFFLICTION_TICKS, amplifier, true, true);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float roll = random.nextFloat() * SPARK_MAX_HEIGHT_ROLL;
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + SPARK_FLOOR_OFFSET + roll * SPARK_RISE_PER_ROLL;
        double z = pos.getZ() + random.nextDouble();
        int colour = ARGB.colorFromFloat(SPARK_ALPHA, SPARK_RED_LOW + SPARK_RED_SPAN * (1.0F - random.nextFloat()), SPARK_GREEN, SPARK_BLUE_LOW + random.nextFloat() * SPARK_BLUE_SPAN);
        float scale = SPARK_MIN_SCALE + roll * SPARK_SCALE_PER_ROLL;
        level.addParticle(new SparkParticleOptions(colour, SPARK_ALPHA, scale), x, y, z, 0.0, 0.0, 0.0);
        if (random.nextInt(CRACKLE_CHANCE_DENOMINATOR) == 0) {
            float pitch = CRACKLE_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * CRACKLE_PITCH_SPREAD;
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TTSounds.JACOBS.get(), SoundSource.AMBIENT, CRACKLE_VOLUME, pitch, false);
        }
    }
}

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

    private static final float SPARK_MAX_RISE = 0.25F;
    private static final double SPARK_FLOOR_OFFSET = 0.05;
    private static final float SPARK_MIN_SCALE = 2.0F;
    private static final float SPARK_SCALE_PER_RISE = 14.0F;
    private static final float SPARK_ALPHA = 0.7F;
    private static final float SPARK_RED_LOW = 0.22F;
    private static final float SPARK_RED_SPAN = 0.12F;
    private static final float SPARK_GREEN_LOW = 0.04F;
    private static final float SPARK_GREEN_SPAN = 0.06F;
    private static final float SPARK_BLUE_LOW = 0.30F;
    private static final float SPARK_BLUE_SPAN = 0.15F;
    private static final float OPAQUE = 1.0F;

    private static final int CRACKLE_CHANCE_DENOMINATOR = 70;
    private static final float CRACKLE_VOLUME = 0.15F;
    private static final float CRACKLE_PITCH_LOW = 0.7F;
    private static final float CRACKLE_PITCH_SPAN = 0.3F;
    private static final double BLOCK_CENTRE = 0.5;

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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float rise = random.nextFloat() * SPARK_MAX_RISE;
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + SPARK_FLOOR_OFFSET + rise;
        double z = pos.getZ() + random.nextDouble();
        int colour = ARGB.colorFromFloat(OPAQUE, SPARK_RED_LOW + random.nextFloat() * SPARK_RED_SPAN, SPARK_GREEN_LOW + random.nextFloat() * SPARK_GREEN_SPAN,
                SPARK_BLUE_LOW + random.nextFloat() * SPARK_BLUE_SPAN);
        float scale = SPARK_MIN_SCALE + rise * SPARK_SCALE_PER_RISE;
        level.addParticle(new SparkParticleOptions(colour, SPARK_ALPHA, scale), x, y, z, 0.0, 0.0, 0.0);
        if (random.nextInt(CRACKLE_CHANCE_DENOMINATOR) == 0) {
            float pitch = CRACKLE_PITCH_LOW + random.nextFloat() * CRACKLE_PITCH_SPAN;
            level.playLocalSound(pos.getX() + BLOCK_CENTRE, pos.getY() + BLOCK_CENTRE, pos.getZ() + BLOCK_CENTRE, TTSounds.JACOBS.get(), SoundSource.AMBIENT, CRACKLE_VOLUME, pitch, false);
        }
    }
}

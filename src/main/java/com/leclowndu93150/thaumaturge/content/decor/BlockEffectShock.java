package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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

public final class BlockEffectShock extends Block {
    public static final MapCodec<BlockEffectShock> CODEC = simpleCodec(BlockEffectShock::new);
    private static final float SHOCK_DAMAGE = 1.0F;
    private static final int SLOWNESS_TICKS = 20;
    private static final int SLOWNESS_AMPLIFIER = 0;
    private static final int REMOVAL_ODDS = 100;
    private static final float SPARK_OPAQUE = 1.0F;
    private static final float SPARK_BASE_HEIGHT = 0.1515F;
    private static final float SPARK_HEIGHT_BOUND = 0.33F;
    private static final float SPARK_HEIGHT_SHARE = 0.5F;
    private static final float SPARK_BASE_SCALE = 3.0F;
    private static final float SPARK_SCALE_PER_HEIGHT = 6.0F;
    private static final float SPARK_ALPHA = 0.8F;
    private static final float SPARK_RED_BASE = 0.65F;
    private static final float SPARK_RED_SPREAD = 0.1F;
    private static final float SPARK_GREEN = 1.0F;
    private static final float SPARK_BLUE = 1.0F;
    private static final int HUM_ODDS = 50;
    private static final float HUM_VOLUME = 0.25F;
    private static final float HUM_PITCH_BASE = 1.0F;
    private static final float HUM_PITCH_SPREAD = 0.2F;
    private static final double BLOCK_CENTER = 0.5;

    public BlockEffectShock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEffectShock> codec() {
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
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            living.hurtServer(serverLevel, serverLevel.damageSources().magic(), SHOCK_DAMAGE);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER, true, true));
        }
        if (serverLevel.getRandom().nextInt(REMOVAL_ODDS) == 0) {
            dissipate(serverLevel, pos);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float height = random.nextFloat() * SPARK_HEIGHT_BOUND;
        float scale = SPARK_BASE_SCALE + height * SPARK_SCALE_PER_HEIGHT;
        float red = SPARK_RED_BASE + random.nextFloat() * SPARK_RED_SPREAD;
        int color = ARGB.colorFromFloat(SPARK_OPAQUE, red, SPARK_GREEN, SPARK_BLUE);
        level.addParticle(new SparkParticleOptions(color, SPARK_ALPHA, scale), pos.getX() + random.nextFloat(), pos.getY() + SPARK_BASE_HEIGHT + height * SPARK_HEIGHT_SHARE, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        if (random.nextInt(HUM_ODDS) == 0) {
            SoundEvent hum = TTSounds.JACOBS.get();
            float pitch = HUM_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * HUM_PITCH_SPREAD;
            level.playLocalSound(pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, hum, SoundSource.AMBIENT, HUM_VOLUME, pitch, false);
        }
    }

    private static void dissipate(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        dissipate(level, pos);
    }

}

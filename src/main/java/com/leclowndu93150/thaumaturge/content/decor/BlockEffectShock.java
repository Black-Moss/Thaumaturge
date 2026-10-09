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
    private static final double SPARK_BASE_HEIGHT = 0.1515;
    private static final float SPARK_MAX_LIFT = 0.33F;
    private static final double SPARK_LIFT_HEIGHT_SHARE = 0.5;
    private static final float SPARK_BASE_SCALE = 3.0F;
    private static final float SPARK_LIFT_SCALE = 6.0F;
    private static final float SPARK_ALPHA = 0.8F;
    private static final float SPARK_RED_MIN = 0.65F;
    private static final float SPARK_RED_RANGE = 0.1F;
    private static final float SPARK_OPAQUE = 1.0F;
    private static final int HUM_ODDS = 50;
    private static final float HUM_VOLUME = 0.25F;
    private static final float HUM_PITCH_SPREAD = 0.2F;

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

    private static void dissipate(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        dissipate(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float lift = random.nextFloat() * SPARK_MAX_LIFT;
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + SPARK_BASE_HEIGHT + lift * SPARK_LIFT_HEIGHT_SHARE;
        double z = pos.getZ() + random.nextDouble();
        int color = ARGB.colorFromFloat(SPARK_OPAQUE, SPARK_RED_MIN + random.nextFloat() * SPARK_RED_RANGE, SPARK_OPAQUE, SPARK_OPAQUE);
        level.addParticle(new SparkParticleOptions(color, SPARK_ALPHA, SPARK_BASE_SCALE + SPARK_LIFT_SCALE * lift), x, y, z, 0.0, 0.0, 0.0);
        if (random.nextInt(HUM_ODDS) == 0) {
            float up = random.nextFloat();
            float down = random.nextFloat();
            float pitch = 1.0F + HUM_PITCH_SPREAD * (up - down);
            SoundEvent hum = TTSounds.JACOBS.get();
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), hum, SoundSource.AMBIENT, HUM_VOLUME, pitch, false);
        }
    }
}

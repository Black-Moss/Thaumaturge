package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintCreatureEvents {
    private static final int FLUX_TAINT_TICKS = 600;
    private static final int SPLOSION_ATTEMPTS = 10;
    private static final float SPLOSION_SPREAD = 6.0F;
    private static final float SPLOSION_PRESSURE = 0.01F;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getExplosion().getDirectSourceEntity() instanceof EntityTaintCreeper creeper)) {
            return;
        }
        for (Entity entity : event.getAffectedEntities()) {
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true, false));
            }
        }
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            taintSplosion(level, creeper.blockPosition(), level.getRandom());
        }
    }

    private static void taintSplosion(ServerLevel level, BlockPos center, RandomSource random) {
        for (int attempt = 0; attempt < SPLOSION_ATTEMPTS; attempt++) {
            int x = center.getX() + (int) ((random.nextFloat() - random.nextFloat()) * SPLOSION_SPREAD);
            int z = center.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * SPLOSION_SPREAD);
            if (!random.nextBoolean()) {
                continue;
            }
            BlockPos column = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (!level.hasChunkAt(column) || !TaintBiomeManager.taintColumn(level, column)) {
                continue;
            }
            if (level.getBlockState(column).canBeReplaced() && level.getBlockState(column).getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, column)) {
                level.setBlock(column, BlockTaintFibre.stateForWorld(level, column), Block.UPDATE_ALL);
            }
            TaintEcology.addPressure(level, column, SPLOSION_PRESSURE);
        }
    }
}

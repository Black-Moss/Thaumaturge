package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockGrassAmbient extends GrassBlock {
    private static final int CAVE_MOTE_ONE_IN = 5;
    private static final int DAYLIGHT_CUTOFF = 5;
    private static final int CHANCE_DENOMINATOR = 7;
    private static final int CHANCE_AT_DARK = 6;
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_START_HEIGHT = 5;
    private static final int SEARCH_DEPTH = 10;
    private static final int SEARCH_FLOOR_Y = 50;
    private static final float HUE_BASE = 0.28F;
    private static final float HUE_SPAN = 0.22F;
    private static final float SATURATION_BASE = 0.25F;
    private static final float SATURATION_SPAN = 0.30F;
    private static final float VALUE = 1.0F;
    private static final int AGE_BASE = 320;
    private static final int AGE_VARIATION = 100;
    private static final int OPAQUE_ALPHA = 255;
    private static final float MOTE_GRAVITY = -0.008F;

    public BlockGrassAmbient(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBiome(pos).is(TTBiomes.MAGICAL_FOREST_CAVES)) {
            if (random.nextInt(CAVE_MOTE_ONE_IN) == 0 && level.getBlockState(above).isAir()) {
                emitMote(level, random, above, true);
            }
            return;
        }
        int daylight = level.isDarkOutside() ? 0 : Math.max(0, level.getBrightness(LightLayer.SKY, above) - level.getSkyDarken());
        if (daylight >= DAYLIGHT_CUTOFF || random.nextInt(CHANCE_DENOMINATOR) >= CHANCE_AT_DARK - daylight) {
            return;
        }
        BlockPos grass = findNearbyGrass(level, random, pos);
        if (grass != null) {
            emitMote(level, random, grass.above(), false);
        }
    }

    private static BlockPos findNearbyGrass(Level level, RandomSource random, BlockPos pos) {
        int x = pos.getX() + random.nextInt(2 * SEARCH_RADIUS + 1) - SEARCH_RADIUS;
        int z = pos.getZ() + random.nextInt(2 * SEARCH_RADIUS + 1) - SEARCH_RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int step = 0; step < SEARCH_DEPTH; step++) {
            int y = pos.getY() + SEARCH_START_HEIGHT - step;
            if (y <= SEARCH_FLOOR_Y) {
                return null;
            }
            cursor.set(x, y, z);
            if (level.getBlockState(cursor).is(Blocks.GRASS_BLOCK)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static void emitMote(Level level, RandomSource random, BlockPos cell, boolean emissive) {
        int color = Mth.hsvToArgb(HUE_BASE + random.nextFloat() * HUE_SPAN, SATURATION_BASE + random.nextFloat() * SATURATION_SPAN, VALUE, OPAQUE_ALPHA);
        int age = AGE_BASE + random.nextInt(AGE_VARIATION);
        WispyMoteParticleOptions options = new WispyMoteParticleOptions(color, age, MOTE_GRAVITY, WispyMoteParticleOptions.NO_ENTITY, emissive);
        level.addParticle(options, cell.getX() + random.nextDouble(), cell.getY(), cell.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
    }
}

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
    private static final int SEARCH_RADIUS = 8;
    private static final int PROBE_START_ABOVE = 5;
    private static final int PROBE_STEPS = 10;
    private static final int SURFACE_PROBE_FLOOR = 50;
    private static final float VALUE = 1.0F;
    private static final int OPAQUE_ALPHA = 255;
    private static final float GREEN_HUE = 0.28F;
    private static final float AQUA_HUE = 0.50F;
    private static final float MIN_TINT_SATURATION = 0.25F;
    private static final float MAX_TINT_SATURATION = 0.55F;
    private static final int MOTE_LIFE_TICKS = 320;
    private static final int MOTE_LIFE_VARIATION = 100;
    private static final float MOTE_BUOYANCY = -0.008F;

    public BlockGrassAmbient(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level.getBiome(pos).is(TTBiomes.MAGICAL_FOREST_CAVES)) {
            releaseCaveMote(level, pos, random);
            return;
        }
        int daylight = daylightAbove(level, pos);
        if (daylight >= DAYLIGHT_CUTOFF || random.nextInt(CHANCE_DENOMINATOR) <= daylight) {
            return;
        }
        releaseSurfaceMote(level, pos, random);
    }

    private static int daylightAbove(Level level, BlockPos pos) {
        if (level.isDarkOutside()) {
            return 0;
        }
        return Math.max(0, level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken());
    }

    private static void releaseCaveMote(Level level, BlockPos origin, RandomSource random) {
        if (random.nextInt(CAVE_MOTE_ONE_IN) != 0) {
            return;
        }
        BlockPos above = origin.above();
        if (level.isEmptyBlock(above)) {
            level.addParticle(paleMote(random, true), above.getX() + random.nextDouble(), above.getY(), above.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }

    private static void releaseSurfaceMote(Level level, BlockPos origin, RandomSource random) {
        int x = origin.getX() + random.nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
        int z = origin.getZ() + random.nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x, origin.getY() + PROBE_START_ABOVE, z);
        for (int step = 0; step < PROBE_STEPS && probe.getY() > SURFACE_PROBE_FLOOR; step++) {
            if (level.getBlockState(probe).is(Blocks.GRASS_BLOCK)) {
                level.addParticle(paleMote(random, false), probe.getX() + random.nextDouble(), probe.getY() + 1, probe.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                return;
            }
            probe.move(0, -1, 0);
        }
    }

    private static WispyMoteParticleOptions paleMote(RandomSource random, boolean emissive) {
        float hue = Mth.lerp(random.nextFloat(), GREEN_HUE, AQUA_HUE);
        float saturation = Mth.lerp(random.nextFloat(), MIN_TINT_SATURATION, MAX_TINT_SATURATION);
        int color = Mth.hsvToArgb(hue, saturation, VALUE, OPAQUE_ALPHA);
        int life = MOTE_LIFE_TICKS + random.nextInt(MOTE_LIFE_VARIATION);
        return new WispyMoteParticleOptions(color, life, MOTE_BUOYANCY, WispyMoteParticleOptions.NO_ENTITY, emissive);
    }
}

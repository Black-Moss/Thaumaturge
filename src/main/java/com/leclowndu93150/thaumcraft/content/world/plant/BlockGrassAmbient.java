package com.leclowndu93150.thaumcraft.content.world.plant;

import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.content.fx.helper.Sprites;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockGrassAmbient extends GrassBlock {
    private static final int MOTE_SEARCH_RANGE = 8;
    private static final int MOTE_MIN_Y = 50;
    private static final int MOTE_AGE = 400;
    private static final float MOTE_GRAVITY = -0.01F;

    public BlockGrassAmbient(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
        float angle = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, pos)
                * (float) (Math.PI / 180.0);
        float target = angle < (float) Math.PI ? 0.0F : (float) (Math.PI * 2);
        angle += (target - angle) * 0.2F;
        skyLight = Math.round(skyLight * Mth.cos(angle));
        skyLight = Mth.clamp(skyLight, 0, 15);
        if (4 + skyLight * 2 < 1 + random.nextInt(13)) {
            int dx = Mth.nextInt(random, -MOTE_SEARCH_RANGE, MOTE_SEARCH_RANGE);
            int dz = Mth.nextInt(random, -MOTE_SEARCH_RANGE, MOTE_SEARCH_RANGE);
            BlockPos target2 = pos.offset(dx, 5, dz);
            for (int q = 0; q < 10 && target2.getY() > MOTE_MIN_Y
                    && !level.getBlockState(target2).is(Blocks.GRASS_BLOCK); q++) {
                target2 = target2.below();
            }
            if (level.getBlockState(target2).is(Blocks.GRASS_BLOCK)) {
                spawnMote(level, target2.above(), random);
            }
        }
    }

    private static void spawnMote(Level level, BlockPos pos, RandomSource random) {
        FXGenericData data = FXGenericData.builder()
                .maxAge((int) (MOTE_AGE + MOTE_AGE / 2 * random.nextFloat()))
                .color(0.4F + random.nextFloat() * 0.6F,
                        0.6F + random.nextFloat() * 0.4F,
                        0.6F + random.nextFloat() * 0.4F)
                .alpha(0.0F, 0.6F, 0.6F, 0.0F)
                .grid(Sprites.WISPY_LOOP.grid())
                .particles(Sprites.WISPY_LOOP.start(), Sprites.WISPY_LOOP.num(), Sprites.WISPY_LOOP.inc())
                .scale(1.0F, 0.5F)
                .loop(true)
                .wind(0.001)
                .gravity(MOTE_GRAVITY)
                .random(0.0025F, 0.0F, 0.0025F)
                .build();
        level.addParticle(data,
                pos.getX() + random.nextFloat(), pos.getY(), pos.getZ() + random.nextFloat(),
                0.0, 0.0, 0.0);
    }
}

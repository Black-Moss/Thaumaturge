package com.leclowndu93150.thaumcraft.content.eldritch.block;

import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.content.fx.helper.Sprites;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class BlockEldritchSparkFX {
    private BlockEldritchSparkFX() {}

    public static void spawnShockSpark(Level level, BlockPos pos, RandomSource random) {
        int sparkStart = Sprites.SPARK_LOOP.start() + random.nextInt(3) * 16;
        FXGenericData data = FXGenericData.builder()
                .maxAge(5 + random.nextInt(5))
                .alpha(0.8F)
                .color(0.65F + random.nextFloat() * 0.1F, 1.0F, 1.0F)
                .grid(Sprites.SPARK_LOOP.grid())
                .particles(sparkStart, Sprites.SPARK_LOOP.num(), Sprites.SPARK_LOOP.inc())
                .scale(0.5F)
                .flipped(random.nextBoolean())
                .build();
        level.addParticle(data,
                pos.getX() + random.nextFloat(),
                pos.getY() + random.nextFloat(),
                pos.getZ() + random.nextFloat(),
                0.0, 0.0, 0.0);
    }
}

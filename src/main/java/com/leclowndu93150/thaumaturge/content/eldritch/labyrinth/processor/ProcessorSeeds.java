package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.LabyrinthNoise;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.LabyrinthPlaceSettings;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.jspecify.annotations.Nullable;

final class ProcessorSeeds {
    static final int DEFAULT_PITCH = 4;
    private static final int MAX_PITCH = 32;
    static final Codec<Integer> PITCH_CODEC = Codec.intRange(1, MAX_PITCH);

    private ProcessorSeeds() {}

    static long seed(StructurePlaceSettings settings) {
        return settings instanceof LabyrinthPlaceSettings labyrinth ? labyrinth.seed() : 0L;
    }

    static float decorationScale(StructurePlaceSettings settings) {
        return settings instanceof LabyrinthPlaceSettings labyrinth ? labyrinth.tuning().decorationScale() : 1.0F;
    }

    static <T> @Nullable T pick(WeightedList<T> list, long seed, int x, int y, int z, int salt) {
        List<Weighted<T>> entries = list.unwrap();
        int total = 0;
        for (Weighted<T> entry : entries) {
            total += entry.weight();
        }
        if (total <= 0) {
            return null;
        }
        int roll = (int) Long.remainderUnsigned(LabyrinthNoise.hash(seed, x, y, z, salt), total);
        for (Weighted<T> entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.value();
            }
        }
        return null;
    }
}

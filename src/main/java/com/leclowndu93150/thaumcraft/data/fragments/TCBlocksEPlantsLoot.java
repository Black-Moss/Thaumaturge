package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksEPlants;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;

public final class TCBlocksEPlantsLoot {
    private TCBlocksEPlantsLoot() {}

    public static void register(Consumer<Block> dropSelf) {
        dropSelf.accept(TCBlocksEPlants.PLANT_SHIMMERLEAF.get());
        dropSelf.accept(TCBlocksEPlants.PLANT_CINDERPEARL.get());
        dropSelf.accept(TCBlocksEPlants.PLANT_VISHROOM.get());
    }
}

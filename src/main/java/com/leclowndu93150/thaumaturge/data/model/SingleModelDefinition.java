package com.leclowndu93150.thaumaturge.data.model;

import java.util.Map;
import java.util.Optional;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;

public record SingleModelDefinition(Block block, BlockStateModel.Unbaked model) implements BlockModelDefinitionGenerator {
    @Override
    public BlockStateModelDispatcher create() {
        return new BlockStateModelDispatcher(Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(Map.of("", model))), Optional.empty());
    }
}

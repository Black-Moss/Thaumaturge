package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class TCBlocksDecorModels {
    private TCBlocksDecorModels() {}

    public static void register(BlockModelGenerators blockModels) {
        slab(blockModels, TCBlocks.SLAB_GREATWOOD.get(), TCBlocks.PLANK_GREATWOOD.get(),
                texture("plank_greatwood"), texture("plank_greatwood"), texture("plank_greatwood"));
        slab(blockModels, TCBlocks.SLAB_SILVERWOOD.get(), TCBlocks.PLANK_SILVERWOOD.get(),
                texture("plank_silverwood"), texture("plank_silverwood"), texture("plank_silverwood"));
        slab(blockModels, TCBlocks.SLAB_ARCANE_STONE.get(), TCBlocks.STONE_ARCANE.get(),
                texture("arcane_stone_1"), texture("arcane_stone_2"), texture("arcane_stone_3"));
        slab(blockModels, TCBlocks.SLAB_ARCANE_BRICK.get(), TCBlocks.STONE_ARCANE_BRICK.get(),
                texture("arcane_brick_stone"), texture("arcane_brick_stone"), texture("arcane_brick_stone"));
        slab(blockModels, TCBlocks.SLAB_ANCIENT.get(), TCBlocks.STONE_ANCIENT.get(),
                texture("ancient_stone_1"), texture("ancient_stone_2"), texture("ancient_stone_3"));
        slab(blockModels, TCBlocks.SLAB_ELDRITCH.get(), TCBlocks.STONE_ELDRITCH_TILE.get(),
                texture("eldritch_stone_1"), texture("eldritch_stone_2"), texture("eldritch_stone_3"));
        stairs(blockModels, TCBlocks.STAIRS_GREATWOOD.get(), texture("plank_greatwood"));
        stairs(blockModels, TCBlocks.STAIRS_SILVERWOOD.get(), texture("plank_silverwood"));
        existingModelWithItem(blockModels, TCBlocks.TABLE_WOOD.get(), "table_wood");
        existingModelWithItem(blockModels, TCBlocks.TABLE_STONE.get(), "table_stone");
        paving(blockModels, TCBlocks.PAVING_STONE_TRAVEL.get(), "paving_stone_travel");
        paving(blockModels, TCBlocks.PAVING_STONE_BARRIER.get(), "paving_stone_barrier");
    }

    private static Material texture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + name));
    }

    private static void slab(BlockModelGenerators blockModels, Block slab, Block fullBlock,
                             Material bottom, Material top, Material side) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top)
                .put(TextureSlot.SIDE, side);
        MultiVariant bottomModel = BlockModelGenerators.plainVariant(
                ModelTemplates.SLAB_BOTTOM.create(slab, mapping, blockModels.modelOutput));
        MultiVariant topModel = BlockModelGenerators.plainVariant(
                ModelTemplates.SLAB_TOP.create(slab, mapping, blockModels.modelOutput));
        MultiVariant doubleModel = BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(fullBlock));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(slab)
                .with(PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE)
                        .select(SlabType.BOTTOM, bottomModel)
                        .select(SlabType.TOP, topModel)
                        .select(SlabType.DOUBLE, doubleModel)));
        blockModels.registerSimpleItemModel(slab.asItem(), ModelLocationUtils.getModelLocation(slab));
    }

    static void stairsFor(BlockModelGenerators blockModels, Block block, Material all) {
        stairs(blockModels, block, all);
    }

    private static void stairs(BlockModelGenerators blockModels, Block block, Material all) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, all)
                .put(TextureSlot.TOP, all)
                .put(TextureSlot.SIDE, all);
        MultiVariant straight = BlockModelGenerators.plainVariant(
                ModelTemplates.STAIRS_STRAIGHT.create(block, mapping, blockModels.modelOutput));
        MultiVariant inner = BlockModelGenerators.plainVariant(
                ModelTemplates.STAIRS_INNER.create(block, mapping, blockModels.modelOutput));
        MultiVariant outer = BlockModelGenerators.plainVariant(
                ModelTemplates.STAIRS_OUTER.create(block, mapping, blockModels.modelOutput));
        blockModels.blockStateOutput.accept(createStairsDispatch(block, straight, inner, outer));
        blockModels.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(block));
    }

    private static MultiVariantGenerator createStairsDispatch(Block block, MultiVariant straight,
                                                              MultiVariant inner, MultiVariant outer) {
        return MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING,
                                BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, straight)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer)
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner)
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK)));
    }

    private static void existingModelWithItem(BlockModelGenerators blockModels, Block block, String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private static void paving(BlockModelGenerators blockModels, Block block, String name) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.DIRT, texture("arcane_brick_stone"))
                .put(TextureSlot.TOP, texture(name))
                .put(TextureSlot.PARTICLE, texture(name));
        Identifier model = ModelTemplates.FARMLAND.create(block, mapping, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }
}

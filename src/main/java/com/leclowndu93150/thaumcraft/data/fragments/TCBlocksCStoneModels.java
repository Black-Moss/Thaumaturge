package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class TCBlocksCStoneModels {
    private TCBlocksCStoneModels() {}

    public static void register(BlockModelGenerators blockModels) {
        simpleCube(blockModels, TCBlocks.STONE_ARCANE.get(), "stone_arcane");
        simpleCube(blockModels, TCBlocks.STONE_ARCANE_BRICK.get(), "stone_arcane_brick");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT.get(), "stone_ancient");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_TILE.get(), "stone_ancient_tile");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_ROCK.get(), "stone_ancient_rock");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_GLYPHED.get(), "stone_ancient_glyphed");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_DOORWAY.get(), "stone_ancient_doorway");
        simpleCube(blockModels, TCBlocks.STONE_ELDRITCH_TILE.get(), "stone_eldritch_tile");
        simpleCube(blockModels, TCBlocks.STONE_POROUS.get(), "stone_porous");

        stairs(blockModels, TCBlocks.STAIRS_ARCANE.get(), "arcane_stairs", "arcane_inner_stairs", "arcane_outer_stairs");
        stairs(blockModels, TCBlocks.STAIRS_ARCANE_BRICK.get(), "arcane_brick_stairs", "arcane_brick_inner_stairs", "arcane_brick_outer_stairs");
        stairs(blockModels, TCBlocks.STAIRS_ANCIENT.get(), "ancient_stairs", "ancient_inner_stairs", "ancient_outer_stairs");

    }

    private static void simpleCube(BlockModelGenerators blockModels, Block block, String modelName) {
        MultiVariant variant = variantOf(modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }

    private static void stairs(BlockModelGenerators blockModels, Block block, String straightName, String innerName, String outerName) {
        MultiVariant straight = variantOf(straightName);
        MultiVariant inner = variantOf(innerName);
        MultiVariant outer = variantOf(outerName);
        PropertyDispatch<MultiVariant> dispatch = PropertyDispatch.initial(
                        BlockStateProperties.HORIZONTAL_FACING,
                        BlockStateProperties.HALF,
                        BlockStateProperties.STAIRS_SHAPE)
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
                .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }
}

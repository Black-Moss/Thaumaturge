package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.core.Direction;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;

public final class TCBlocksEldritchModels {
    private TCBlocksEldritchModels() {}

    public static void register(BlockModelGenerators blockModels) {
        cube(blockModels, TCBlocks.OBSIDIAN_TILE.get(), "obsidian_tile", true);
        cube(blockModels, TCBlocks.ELDRITCH_STONE.get(), "eldritch_stone", true);
        cube(blockModels, TCBlocks.ELDRITCH_STONE_INERT.get(), "eldritch_stone", true);
        cube(blockModels, TCBlocks.ELDRITCH_ROCK.get(), "eldritch_rock", true);
        cube(blockModels, TCBlocks.ELDRITCH_CRUST.get(), "eldritch_crust", true);
        cube(blockModels, TCBlocks.ELDRITCH_CRUST_GLOWING.get(), "eldritch_crust_glowing", true);
        cube(blockModels, TCBlocks.ELDRITCH_DOOR.get(), "eldritch_door", true);
        cube(blockModels, TCBlocks.ELDRITCH_STONE_CRYSTAL.get(), "eldritch_stone_crystal", true);
        cube(blockModels, TCBlocks.ELDRITCH_LOCK.get(), "eldritch_deco", true);
        crabSpawner(blockModels);
        column(blockModels, TCBlocks.ELDRITCH_PEDESTAL.get(), "eldritch_pedestal_side", "eldritch_stone");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_ALTAR.get(), "eldritch_altar");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_OBELISK.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_PILLAR.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_CAPSTONE.get(), "eldritch_deco");
        trap(blockModels);
        invisible(blockModels, TCBlocks.ELDRITCH_NOTHING.get());
        invisible(blockModels, TCBlocks.ELDRITCH_PORTAL.get());
        stairs(blockModels);
    }

    private static Material texture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + name));
    }

    private static void cube(BlockModelGenerators blockModels, Block block, String textureName, boolean item) {
        Identifier model = ModelTemplates.CUBE_ALL.create(block,
                new TextureMapping().put(TextureSlot.ALL, texture(textureName)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
        if (item) {
            blockModels.registerSimpleItemModel(block.asItem(), model);
        }
    }

    private static void column(BlockModelGenerators blockModels, Block block, String side, String end) {
        Identifier model = ModelTemplates.CUBE_COLUMN.create(block,
                new TextureMapping().put(TextureSlot.SIDE, texture(side)).put(TextureSlot.END, texture(end)),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private static void trap(BlockModelGenerators blockModels) {
        Block block = TCBlocks.ELDRITCH_TRAP.get();
        WeightedList.Builder<Variant> variants = WeightedList.builder();
        for (int i = 0; i < 4; i++) {
            Identifier model = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_" + i,
                    new TextureMapping().put(TextureSlot.ALL, texture("eldritch_trap_" + i)),
                    blockModels.modelOutput);
            variants.add(new Variant(model), 1);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                new MultiVariant(variants.build())));
        blockModels.registerSimpleItemModel(block.asItem(),
                ModelLocationUtils.getModelLocation(block, "_0"));
    }

    private static void crabSpawner(BlockModelGenerators blockModels) {
        Block block = TCBlocks.ELDRITCH_CRAB_SPAWNER.get();
        Identifier model = ModelLocationUtils.getModelLocation(block);
        MultiVariant base = BlockModelGenerators.plainVariant(model);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockEldritchCrabSpawner.FACING)
                        .select(Direction.UP, base)
                        .select(Direction.DOWN, base.with(BlockModelGenerators.X_ROT_180))
                        .select(Direction.NORTH, base.with(BlockModelGenerators.X_ROT_90))
                        .select(Direction.EAST, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_270))));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private static void invisibleWithCubeItem(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier itemModel = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inventory",
                new TextureMapping().put(TextureSlot.ALL, texture(textureName)), blockModels.modelOutput);
        blockModels.registerSimpleItemModel(block.asItem(), itemModel);
        Identifier model = ModelTemplates.PARTICLE_ONLY.create(block,
                TextureMapping.particle(texture(textureName)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
    }

    private static void invisible(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.PARTICLE_ONLY.create(block,
                TextureMapping.particle(texture("eldritch_stone")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                BlockModelGenerators.plainVariant(model)));
    }

    private static void stairs(BlockModelGenerators blockModels) {
        TCBlocksDecorModels.stairsFor(blockModels, TCBlocks.STAIRS_ELDRITCH.get(), texture("eldritch_stone"));
    }
}

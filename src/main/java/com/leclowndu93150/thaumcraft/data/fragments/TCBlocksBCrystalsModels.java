package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksBCrystals;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;

public final class TCBlocksBCrystalsModels {
    private TCBlocksBCrystalsModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_AER.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_IGNIS.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_AQUA.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_TERRA.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_ORDO.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_PERDITIO.get());
        crystal(blockModels, TCBlocksBCrystals.CRYSTAL_VITIUM.get());
    }

    private static void crystal(BlockModelGenerators blockModels, Block block) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/crystal");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }
}

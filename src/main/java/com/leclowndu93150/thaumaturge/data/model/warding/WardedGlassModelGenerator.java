package com.leclowndu93150.thaumaturge.data.model.warding;

import com.leclowndu93150.thaumaturge.client.model.connected.ConnectedCornersModel;
import com.leclowndu93150.thaumaturge.client.warding.WardConnectedTexture;
import com.leclowndu93150.thaumaturge.data.model.SingleModelDefinition;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class WardedGlassModelGenerator {
    private static final String ITEM_MODEL_SUFFIX = "_item";

    private WardedGlassModelGenerator() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block glass = TTBlocks.WARDED_GLASS.get();
        blockModels.blockStateOutput.accept(new SingleModelDefinition(glass, new ConnectedCornersModel(WardConnectedTexture.SPRITES, TextureMapping.getBlockTexture(glass).sprite())));
        Identifier itemModel = ModelTemplates.CUBE_ALL.createWithSuffix(glass, ITEM_MODEL_SUFFIX, TextureMapping.cube(glass), blockModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.WARDED_GLASS.get(), ItemModelUtils.plainModel(itemModel));
    }
}

package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.world.tree.TrunkQuadrant;
import com.mojang.serialization.JsonOps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class WideTrunkModels {
    private static final Identifier BLOCK_PARENT = Identifier.withDefaultNamespace("block/block");
    private static final String CORNER_SUFFIX = "_quadrant_";
    private static final String SIDE_REFERENCE = "#side";
    private static final String END_REFERENCE = "#end";
    private static final int FULL = 16;
    private static final int HALF = 8;

    private WideTrunkModels() {}

    public static void register(BlockModelGenerators blockModels, Block log, Block wood, Material wideSide, Material wideEnd) {
        TextureMapping narrow = TextureMapping.logColumn(log);
        Identifier vertical = ModelTemplates.CUBE_COLUMN.create(log, narrow, blockModels.modelOutput);
        MultiVariant horizontal = BlockModelGenerators.plainVariant(ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(log, narrow, blockModels.modelOutput));
        Map<TrunkQuadrant, MultiVariant> upright = uprightVariants(blockModels, log, vertical, narrow.get(TextureSlot.PARTICLE), wideSide, wideEnd);
        PropertyDispatch.C2<MultiVariant, Direction.Axis, TrunkQuadrant> dispatch = PropertyDispatch.initial(BlockStateProperties.AXIS, TrunkQuadrant.PROPERTY);
        for (TrunkQuadrant quadrant : TrunkQuadrant.values()) {
            dispatch.select(Direction.Axis.Y, quadrant, upright.get(quadrant));
            dispatch.select(Direction.Axis.Z, quadrant, horizontal.with(BlockModelGenerators.X_ROT_90));
            dispatch.select(Direction.Axis.X, quadrant, horizontal.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(log).with(dispatch));
        blockModels.registerSimpleItemModel(log.asItem(), vertical);
        blockModels.woodProvider(log).wood(wood);
    }

    private static Map<TrunkQuadrant, MultiVariant> uprightVariants(BlockModelGenerators blockModels, Block log, Identifier vertical, Material particle, Material wideSide, Material wideEnd) {
        Map<TrunkQuadrant, MultiVariant> variants = new EnumMap<>(TrunkQuadrant.class);
        for (TrunkQuadrant quadrant : TrunkQuadrant.values()) {
            variants.put(quadrant, BlockModelGenerators.plainVariant(quadrant.isCorner() ? cornerModel(blockModels, log, quadrant, particle, wideSide, wideEnd) : vertical));
        }
        return variants;
    }

    private static Identifier cornerModel(BlockModelGenerators blockModels, Block log, TrunkQuadrant quadrant, Material particle, Material wideSide, Material wideEnd) {
        Identifier model = ModelLocationUtils.getModelLocation(log, CORNER_SUFFIX + quadrant.getSerializedName());
        blockModels.modelOutput.accept(model, cornerInstance(quadrant, particle, wideSide, wideEnd));
        return model;
    }

    private static ModelInstance cornerInstance(TrunkQuadrant quadrant, Material particle, Material wideSide, Material wideEnd) {
        return () -> {
            JsonObject root = new JsonObject();
            root.addProperty("parent", BLOCK_PARENT.toString());
            JsonObject textures = new JsonObject();
            textures.add(TextureSlot.PARTICLE.getId(), encode(particle));
            textures.add(TextureSlot.SIDE.getId(), encode(wideSide));
            textures.add(TextureSlot.END.getId(), encode(wideEnd));
            root.add("textures", textures);
            JsonArray elements = new JsonArray();
            elements.add(cornerElement(quadrant));
            root.add("elements", elements);
            return root;
        };
    }

    private static JsonElement encode(Material material) {
        return Material.CODEC.encodeStart(JsonOps.INSTANCE, material).getOrThrow();
    }

    private static JsonObject cornerElement(TrunkQuadrant quadrant) {
        int east = quadrant.eastOffset();
        int south = quadrant.southOffset();
        JsonObject faces = new JsonObject();
        addFace(faces, Direction.NORTH, SIDE_REFERENCE, HALF * (1 - east), 0, HALF, FULL);
        addFace(faces, Direction.SOUTH, SIDE_REFERENCE, HALF * east, 0, HALF, FULL);
        addFace(faces, Direction.WEST, SIDE_REFERENCE, HALF * south, 0, HALF, FULL);
        addFace(faces, Direction.EAST, SIDE_REFERENCE, HALF * (1 - south), 0, HALF, FULL);
        addFace(faces, Direction.UP, END_REFERENCE, HALF * east, HALF * south, HALF, HALF);
        addFace(faces, Direction.DOWN, END_REFERENCE, HALF * east, HALF * (1 - south), HALF, HALF);
        JsonObject element = new JsonObject();
        element.add("from", numbers(0, 0, 0));
        element.add("to", numbers(FULL, FULL, FULL));
        element.add("faces", faces);
        return element;
    }

    private static void addFace(JsonObject faces, Direction direction, String texture, int left, int top, int width, int height) {
        JsonObject face = new JsonObject();
        face.add("uv", numbers(left, top, left + width, top + height));
        face.addProperty("texture", texture);
        face.addProperty("cullface", direction.getSerializedName());
        faces.add(direction.getSerializedName(), face);
    }

    private static JsonArray numbers(int... values) {
        JsonArray array = new JsonArray();
        for (int value : values) {
            array.add(value);
        }
        return array;
    }
}

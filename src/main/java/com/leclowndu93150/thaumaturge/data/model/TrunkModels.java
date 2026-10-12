package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.world.tree.TrunkCorner;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.data.models.BlockModelGenerators;
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

public final class TrunkModels {
    private static final int FULL = 16;
    private static final int HALF = 8;
    private static final String SIDE = "#side";
    private static final String END = "#end";

    private TrunkModels() {}

    public static void register(BlockModelGenerators blockModels, Block log, Block wood, Material wideSide, Material wideEnd) {
        TextureMapping mapping = TextureMapping.logColumn(log);
        Identifier vertical = ModelTemplates.CUBE_COLUMN.create(log, mapping, blockModels.modelOutput);
        Identifier horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(log, mapping, blockModels.modelOutput);
        Map<TrunkCorner, Identifier> corners = new EnumMap<>(TrunkCorner.class);
        for (TrunkCorner corner : TrunkCorner.values()) {
            if (corner == TrunkCorner.NONE) {
                continue;
            }
            Identifier model = ModelLocationUtils.getModelLocation(log, "_trunk_" + corner.getSerializedName());
            blockModels.modelOutput.accept(model, cornerModel(corner, wideSide.sprite(), wideEnd.sprite(), mapping.get(TextureSlot.SIDE).sprite()));
            corners.put(corner, model);
        }
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(log).with(PropertyDispatch.initial(BlockStateProperties.AXIS, TrunkCorner.PROPERTY).generate((axis, corner) -> switch (axis) {
                    case Y -> BlockModelGenerators.plainVariant(corner == TrunkCorner.NONE ? vertical : corners.get(corner));
                    case Z -> BlockModelGenerators.plainVariant(horizontal).with(BlockModelGenerators.X_ROT_90);
                    case X -> BlockModelGenerators.plainVariant(horizontal).with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90);
                })));
        blockModels.registerSimpleItemModel(log, vertical);
        blockModels.woodProvider(log).wood(wood);
    }

    private static ModelInstance cornerModel(TrunkCorner corner, Identifier wideSide, Identifier wideEnd, Identifier particle) {
        return () -> {
            JsonObject root = new JsonObject();
            root.addProperty("parent", "minecraft:block/block");
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", particle.toString());
            textures.addProperty("side", wideSide.toString());
            textures.addProperty("end", wideEnd.toString());
            root.add("textures", textures);
            JsonObject element = new JsonObject();
            element.add("from", triple(0, 0, 0));
            element.add("to", triple(FULL, FULL, FULL));
            int x = corner.offsetX();
            int z = corner.offsetZ();
            JsonObject faces = new JsonObject();
            faces.add("north", face(SIDE, (1 - x) * HALF, 0, HALF, FULL, Direction.NORTH));
            faces.add("south", face(SIDE, x * HALF, 0, HALF, FULL, Direction.SOUTH));
            faces.add("west", face(SIDE, z * HALF, 0, HALF, FULL, Direction.WEST));
            faces.add("east", face(SIDE, (1 - z) * HALF, 0, HALF, FULL, Direction.EAST));
            faces.add("up", face(END, x * HALF, z * HALF, HALF, HALF, Direction.UP));
            faces.add("down", face(END, x * HALF, (1 - z) * HALF, HALF, HALF, Direction.DOWN));
            element.add("faces", faces);
            JsonArray elements = new JsonArray();
            elements.add(element);
            root.add("elements", elements);
            return root;
        };
    }

    private static JsonObject face(String texture, int u, int v, int width, int height, Direction cull) {
        JsonObject face = new JsonObject();
        JsonArray uv = new JsonArray();
        uv.add(u);
        uv.add(v);
        uv.add(u + width);
        uv.add(v + height);
        face.add("uv", uv);
        face.addProperty("texture", texture);
        face.addProperty("cullface", cull.getSerializedName());
        return face;
    }

    private static JsonArray triple(int a, int b, int c) {
        JsonArray array = new JsonArray();
        array.add(a);
        array.add(b);
        array.add(c);
        return array;
    }
}

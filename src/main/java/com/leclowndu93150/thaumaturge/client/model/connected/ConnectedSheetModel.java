package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public record ConnectedSheetModel(Optional<Identifier> model, Map<Direction, ConnectedTexture> faces, Optional<TagKey<Block>> group) implements CustomUnbakedBlockStateModel {
    public static final Identifier TYPE = TTIds.rl("connected");
    public static final MapCodec<ConnectedSheetModel> CODEC = RecordCodecBuilder
            .<ConnectedSheetModel>mapCodec(instance -> instance.group(Identifier.CODEC.optionalFieldOf("model").forGetter(ConnectedSheetModel::model),
                    Codec.unboundedMap(Direction.CODEC, ConnectedTexture.CODEC).fieldOf("faces").forGetter(ConnectedSheetModel::faces),
                    TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("connects_with").forGetter(ConnectedSheetModel::group)).apply(instance, ConnectedSheetModel::new))
            .validate(ConnectedSheetModel::validate);

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final float BLOCK_PIXELS = 16.0F;

    public ConnectedSheetModel {
        Map<Direction, ConnectedTexture> ordered = new EnumMap<>(Direction.class);
        ordered.putAll(faces);
        faces = Collections.unmodifiableMap(ordered);
    }

    public static ConnectedSheetModel cube(ConnectedTexture texture, Optional<TagKey<Block>> group) {
        Map<Direction, ConnectedTexture> faces = new EnumMap<>(Direction.class);
        for (Direction face : DIRECTIONS) {
            faces.put(face, texture);
        }
        return new ConnectedSheetModel(Optional.empty(), faces, group);
    }

    private static DataResult<ConnectedSheetModel> validate(ConnectedSheetModel model) {
        if (model.model.isEmpty() && model.faces.size() < DIRECTIONS.length) {
            return DataResult.error(() -> "Connected model without a base model needs a texture for every face");
        }
        return DataResult.success(model);
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        float[] insets = new float[DIRECTIONS.length];
        if (model.isEmpty()) {
            Material.Baked particle = ConnectedQuadBaker.material(baker, faces.get(Direction.UP).texture());
            return new ConnectedBlockStateModel(SheetFaceQuads.bake(baker, faces, insets), QuadCollection.EMPTY, particle, true, group.orElse(null));
        }
        BlockStateModelPart part = SimpleModelWrapper.bake(baker, model.get(), BlockModelRotation.IDENTITY);
        QuadCollection.Builder fixed = new QuadCollection.Builder();
        for (Direction face : DIRECTIONS) {
            insets[face.ordinal()] = BLOCK_PIXELS;
            for (BakedQuad quad : part.getQuads(face)) {
                keepOrMeasure(quad, insets, fixed, face);
            }
        }
        for (BakedQuad quad : part.getQuads(null)) {
            keepOrMeasure(quad, insets, fixed, null);
        }
        for (Direction face : DIRECTIONS) {
            if (insets[face.ordinal()] >= BLOCK_PIXELS) {
                insets[face.ordinal()] = 0.0F;
            }
        }
        return new ConnectedBlockStateModel(SheetFaceQuads.bake(baker, faces, insets), fixed.build(), part.particleMaterial(), part.useAmbientOcclusion(), group.orElse(null));
    }

    private void keepOrMeasure(BakedQuad quad, float[] insets, QuadCollection.Builder fixed, @Nullable Direction cull) {
        Direction facing = quad.direction();
        if (faces.containsKey(facing)) {
            insets[facing.ordinal()] = Math.min(insets[facing.ordinal()], inset(quad.position0(), facing));
        } else if (cull != null) {
            fixed.addCulledFace(cull, quad);
        } else {
            fixed.addUnculledFace(quad);
        }
    }

    private static float inset(Vector3fc position, Direction facing) {
        float depth = switch (facing) {
            case DOWN -> position.y();
            case UP -> 1.0F - position.y();
            case NORTH -> position.z();
            case SOUTH -> 1.0F - position.z();
            case WEST -> position.x();
            case EAST -> 1.0F - position.x();
        };
        return Math.max(0.0F, depth * BLOCK_PIXELS);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        model.ifPresent(resolver::markDependency);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }
}

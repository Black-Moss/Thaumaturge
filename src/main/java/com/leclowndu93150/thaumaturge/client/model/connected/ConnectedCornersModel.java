package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record ConnectedCornersModel(Identifier sprites, Identifier particle) implements CustomUnbakedBlockStateModel {
    public static final Identifier TYPE = TTIds.rl("connected_corners");
    public static final MapCodec<ConnectedCornersModel> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Identifier.CODEC.fieldOf("sprites").forGetter(ConnectedCornersModel::sprites), Identifier.CODEC.fieldOf("particle").forGetter(ConnectedCornersModel::particle))
                    .apply(instance, ConnectedCornersModel::new));

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        return new ConnectedBlockStateModel(CornerFaceQuads.bake(baker, sprites), QuadCollection.EMPTY, ConnectedQuadBaker.material(baker, particle), true, null);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }
}

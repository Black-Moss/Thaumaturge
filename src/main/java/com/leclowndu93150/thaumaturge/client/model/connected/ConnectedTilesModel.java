package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record ConnectedTilesModel(Identifier texture) implements CustomUnbakedBlockStateModel {
    public static final Identifier TYPE = TTIds.rl("connected_tiles");
    public static final MapCodec<ConnectedTilesModel> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf("texture").forGetter(ConnectedTilesModel::texture)).apply(instance, ConnectedTilesModel::new));

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        List<Material.Baked> tiles = new ArrayList<>(TileFaceQuads.TILE_COUNT);
        for (int tile = 1; tile <= TileFaceQuads.TILE_COUNT; tile++) {
            tiles.add(ConnectedQuadBaker.material(baker, texture.withSuffix("_" + tile)));
        }
        return new ConnectedBlockStateModel(TileFaceQuads.bake(baker, tiles), QuadCollection.EMPTY, ConnectedQuadBaker.material(baker, texture), true, null);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }
}

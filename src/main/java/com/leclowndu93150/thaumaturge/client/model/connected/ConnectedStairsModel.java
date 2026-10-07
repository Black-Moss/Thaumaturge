package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record ConnectedStairsModel(FrameKit side, FrameKit top, FrameKit bottom, Identifier particle, Optional<TagKey<Block>> group) implements CustomUnbakedBlockStateModel {
    public static final Identifier TYPE = TTIds.rl("connected_stairs");
    public static final MapCodec<ConnectedStairsModel> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(FrameKit.CODEC.fieldOf("side").forGetter(ConnectedStairsModel::side), FrameKit.CODEC.fieldOf("top").forGetter(ConnectedStairsModel::top),
                    FrameKit.CODEC.fieldOf("bottom").forGetter(ConnectedStairsModel::bottom), Identifier.CODEC.fieldOf("particle").forGetter(ConnectedStairsModel::particle),
                    TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("connects_with").forGetter(ConnectedStairsModel::group)).apply(instance, ConnectedStairsModel::new));

    private static final Direction[] FACES = Direction.values();

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        StairCellQuads[] quads = new StairCellQuads[FACES.length];
        for (Direction face : FACES) {
            quads[face.ordinal()] = StairCellQuads.bake(baker, face == Direction.UP ? top : face == Direction.DOWN ? bottom : side, face);
        }
        return new ConnectedStairsBlockStateModel(quads, ConnectedQuadBaker.material(baker, particle), group.orElse(null));
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }
}

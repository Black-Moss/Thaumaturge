package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.Nullable;

public final class ConnectedBlockStateModel implements DynamicBlockStateModel {
    private static final Direction[] FACES = Direction.values();

    private final ConnectedFaceQuads quads;
    private final QuadCollection fixed;
    private final Material.Baked particle;
    private final boolean ambientOcclusion;
    private final @Nullable TagKey<Block> group;

    public ConnectedBlockStateModel(ConnectedFaceQuads quads, QuadCollection fixed, Material.Baked particle, boolean ambientOcclusion, @Nullable TagKey<Block> group) {
        this.quads = quads;
        this.fixed = fixed;
        this.particle = particle;
        this.ambientOcclusion = ambientOcclusion;
        this.group = group;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        QuadCollection.Builder builder = new QuadCollection.Builder().addAll(fixed);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction face : FACES) {
            if (quads.connects(face)) {
                quads.addFace(face, FaceConnections.mask(level, pos, state, face, group, cursor), builder);
            }
        }
        parts.add(new SimpleModelWrapper(builder.build(), ambientOcclusion, particle));
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return quads.materialFlags() | fixed.materialFlags();
    }
}

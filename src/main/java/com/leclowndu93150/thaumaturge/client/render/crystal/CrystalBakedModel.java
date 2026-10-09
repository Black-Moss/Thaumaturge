package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalFaceTransforms;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalShards;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

public final class CrystalBakedModel implements DynamicBlockStateModel {
    private static final Direction[] FACES = Direction.values();
    private static final int TINT_INDEX = 0;
    private static final int MATERIAL_FLAGS = 0;
    private static final boolean AMBIENT_OCCLUSION = true;

    private final TTMesh mesh;
    private final Material.Baked material;

    public CrystalBakedModel(TTMesh mesh, Material.Baked material) {
        this.mesh = mesh;
        this.material = material;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int growth = state.hasProperty(BlockCrystal.SIZE) ? state.getValue(BlockCrystal.SIZE) : 0;
        long seed = CrystalShards.seed(state, pos);
        List<BakedQuad> quads = new ArrayList<>();
        boolean any = false;
        for (Direction face : FACES) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            any = true;
            List<Integer> order = CrystalShards.order(face, seed);
            for (int i = 0; i <= growth; i++) {
                bakeShard(order.get(i), face, quads);
            }
        }
        if (!any) {
            bakeShard(CrystalShards.unsupported(seed), Direction.DOWN, quads);
        }
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        parts.add(new SimpleModelWrapper(builder.build(), AMBIENT_OCCLUSION, material));
    }

    private void bakeShard(int index, Direction face, List<BakedQuad> output) {
        TTMeshPart part = mesh.parts().get(index);
        CrystalQuadBaker.bakePart(part, material, TINT_INDEX, CrystalFaceTransforms.forFace(face), output);
    }

    @Override
    public Material.Baked particleMaterial() {
        return material;
    }

    @Override
    public int materialFlags() {
        return MATERIAL_FLAGS;
    }
}

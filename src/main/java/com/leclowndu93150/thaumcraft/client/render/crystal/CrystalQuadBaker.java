package com.leclowndu93150.thaumcraft.client.render.crystal;

import com.leclowndu93150.thaumcraft.client.model.obj.MeshModel;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshPart;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshQuadBaker;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import org.joml.Matrix4f;

public final class CrystalQuadBaker {
    private static final int CRYSTAL_LIGHT_EMISSION = 15;

    private CrystalQuadBaker() {}

    public static void bakePart(MeshModel mesh, MeshPart part, Baked baked, int tintIndex, Matrix4f transform, java.util.List<BakedQuad> output) {
        BakedQuad.MaterialInfo info = new BakedQuad.MaterialInfo(
                baked.sprite(),
                ChunkSectionLayer.CUTOUT,
                Sheets.cutoutBlockItemSheet(),
                tintIndex,
                false,
                CRYSTAL_LIGHT_EMISSION);
        MeshQuadBaker.bakePart(mesh, part, baked, tintIndex, transform, info, output);
    }
}

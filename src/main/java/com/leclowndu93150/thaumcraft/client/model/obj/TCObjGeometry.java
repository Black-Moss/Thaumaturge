package com.leclowndu93150.thaumcraft.client.model.obj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class TCObjGeometry implements UnbakedGeometry {
    private static final float CENTER_OFFSET = 0.5F;

    private final Identifier model;
    private final boolean flipV;

    public TCObjGeometry(Identifier model, boolean flipV) {
        this.model = model;
        this.flipV = flipV;
    }

    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name) {
        MeshModel mesh = loadMesh();
        Matrix4f transform = new Matrix4f()
                .translate(CENTER_OFFSET, CENTER_OFFSET, CENTER_OFFSET)
                .mul(modelState.transformation().getMatrix());
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (MeshPart part : mesh.parts()) {
            String slot = part.material() == null ? "" : part.material().name();
            Material material = textureSlots.getMaterial(slot);
            if (material == null) {
                throw new IllegalStateException(
                        "Missing texture slot '" + slot + "' for OBJ model " + model + " in " + name.debugName());
            }
            Material.Baked baked = modelBaker.materials().get(material, name);
            boolean itemsAtlas = baked.sprite().atlasLocation().equals(TextureAtlas.LOCATION_ITEMS);
            boolean translucent = baked.forceTranslucent();
            RenderType sheet;
            if (itemsAtlas) {
                sheet = translucent ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
            } else {
                sheet = translucent ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
            }
            BakedQuad.MaterialInfo info = new BakedQuad.MaterialInfo(
                    baked.sprite(),
                    translucent ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT,
                    sheet,
                    part.tintIndex(),
                    false,
                    0);
            List<BakedQuad> quads = new ArrayList<>();
            MeshQuadBaker.bakePart(mesh, part, baked, part.tintIndex(), transform, info, flipV, quads);
            for (BakedQuad quad : quads) {
                builder.addUnculledFace(quad);
            }
        }
        return builder.build();
    }

    private MeshModel loadMesh() {
        try {
            return TCObjLoader.load(Minecraft.getInstance().getResourceManager(), model);
        } catch (IOException e) {
            throw new RuntimeException("Could not load OBJ model at " + model, e);
        }
    }
}

package com.leclowndu93150.thaumaturge.client.model.connected;

import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public final class ConnectedQuadBaker {
    public static final float FACE_SIZE = 16.0F;

    private ConnectedQuadBaker() {}

    public static Material.Baked material(ModelBaker baker, Identifier texture) {
        return baker.materials().get(new Material(texture), texture::toString);
    }

    public static BakedQuad bake(ModelBaker baker, Material.Baked material, Direction face, float inset, float minU, float minV, float maxU, float maxV, CuboidFace.UVs uvs) {
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f(FACE_SIZE, FACE_SIZE, FACE_SIZE);
        switch (face) {
            case DOWN -> {
                from.set(minU, 0.0F, FACE_SIZE - maxV);
                to.set(maxU, FACE_SIZE, FACE_SIZE - minV);
            }
            case UP -> {
                from.set(minU, 0.0F, minV);
                to.set(maxU, FACE_SIZE, maxV);
            }
            case NORTH -> {
                from.set(FACE_SIZE - maxU, FACE_SIZE - maxV, 0.0F);
                to.set(FACE_SIZE - minU, FACE_SIZE - minV, FACE_SIZE);
            }
            case SOUTH -> {
                from.set(minU, FACE_SIZE - maxV, 0.0F);
                to.set(maxU, FACE_SIZE - minV, FACE_SIZE);
            }
            case WEST -> {
                from.set(0.0F, FACE_SIZE - maxV, minU);
                to.set(FACE_SIZE, FACE_SIZE - minV, maxU);
            }
            case EAST -> {
                from.set(0.0F, FACE_SIZE - maxV, FACE_SIZE - maxU);
                to.set(FACE_SIZE, FACE_SIZE - minV, FACE_SIZE - minU);
            }
        }
        switch (face) {
            case DOWN -> from.y = inset;
            case UP -> to.y = FACE_SIZE - inset;
            case NORTH -> from.z = inset;
            case SOUTH -> to.z = FACE_SIZE - inset;
            case WEST -> from.x = inset;
            case EAST -> to.x = FACE_SIZE - inset;
        }
        CuboidFace cuboidFace = new CuboidFace(inset > 0.0F ? null : face, CuboidFace.NO_TINT, material.sprite().contents().name().toString(), uvs, Quadrant.R0);
        return FaceBakery.bakeQuad(baker, from, to, cuboidFace, material, face, BlockModelRotation.IDENTITY, null, true, 0);
    }

    public static BakedQuad bakeWhole(ModelBaker baker, Material.Baked material, Direction face, float inset, CuboidFace.UVs uvs) {
        return bake(baker, material, face, inset, 0.0F, 0.0F, FACE_SIZE, FACE_SIZE, uvs);
    }
}

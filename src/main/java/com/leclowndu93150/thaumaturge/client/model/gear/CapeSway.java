package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.context.ContextKey;
import org.joml.Quaternionf;

public record CapeSway(float flap, float lean, float sideLean) {
    public static final ContextKey<CapeSway> KEY = new ContextKey<>(TTIds.rl("cape_sway"));
    private static final CapeSway STILL = new CapeSway(0.0F, 0.0F, 0.0F);
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float LEAN_SHARE = 0.5F;

    static void swing(ModelPart cape, HumanoidRenderState state) {
        state.getRenderDataOrDefault(KEY, STILL).swing(cape);
    }

    private void swing(ModelPart cape) {
        float pitch = (lean * LEAN_SHARE + flap) * DEGREES_TO_RADIANS;
        float roll = sideLean * LEAN_SHARE * DEGREES_TO_RADIANS;
        cape.rotateBy(new Quaternionf().rotationX(pitch).rotateZ(roll).rotateY(-roll));
    }
}

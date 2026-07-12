package com.leclowndu93150.thaumcraft.client.casters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class WandTipTracker {
    private static final long STALE_MS = 250L;

    private static @Nullable Vec3 tip;
    private static long captureTime;

    private WandTipTracker() {}

    public static void capture(PoseStack poseStack, float tipModelY) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f offset = poseStack.last().pose().transformPosition(new Vector3f(0.0F, tipModelY, 0.0F));
        tip = camera.position().add(offset.x, offset.y, offset.z);
        captureTime = Util.getMillis();
    }

    public static @Nullable Vec3 firstPersonTip() {
        if (tip == null || Util.getMillis() - captureTime > STALE_MS) {
            return null;
        }
        return tip;
    }
}

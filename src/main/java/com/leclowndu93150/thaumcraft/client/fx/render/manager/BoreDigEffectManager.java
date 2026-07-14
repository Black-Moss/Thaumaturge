package com.leclowndu93150.thaumcraft.client.fx.render.manager;

import com.leclowndu93150.thaumcraft.client.fx.render.instance.BoreDigEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Camera;

public final class BoreDigEffectManager extends AbstractFXManager<BoreDigEffect> {
    public static final BoreDigEffectManager INSTANCE = new BoreDigEffectManager();

    private final List<BoreDigEffect> effects = new ArrayList<>();

    private BoreDigEffectManager() {}

    public void add(BoreDigEffect effect) {
        this.effects.add(effect);
    }

    @Override
    protected Collection<BoreDigEffect> activeInstances() {
        return this.effects;
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
    }
}

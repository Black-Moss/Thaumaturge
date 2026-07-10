package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumcraft.content.entity.boss.EntityEldritchGolem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class EldritchGolemRenderer extends MobRenderer<EntityEldritchGolem, EldritchGolemRenderState, EldritchGolemModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/eldritch_golem.png");
    private static final float SHADOW = 0.7F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(TCModelLayers.ELDRITCH_GOLEM)), SHADOW);
    }

    @Override
    public EldritchGolemRenderState createRenderState() {
        return new EldritchGolemRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchGolem entity, EldritchGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.headless = entity.isHeadless();
        state.attackTime = Math.max(0.0F, entity.getAttackTimer() - partialTicks);
        state.spawnTimer = entity.getSpawnTimer();
    }

    @Override
    public Identifier getTextureLocation(EldritchGolemRenderState state) {
        return TEXTURE;
    }
}

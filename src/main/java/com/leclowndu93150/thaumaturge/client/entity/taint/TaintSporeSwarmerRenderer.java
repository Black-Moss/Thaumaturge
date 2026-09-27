package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class TaintSporeSwarmerRenderer extends AbstractTaintSporeRenderer<EntityTaintSporeSwarmer, TaintSporeSwarmerModel> {
    public TaintSporeSwarmerRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeSwarmerModel(context.bakeLayer(TCModelLayers.TAINT_SPORE_SWARMER)));
        this.addLayer(new TaintSporeSwarmerCoreLayer(this, context.getModelSet()));
    }
}

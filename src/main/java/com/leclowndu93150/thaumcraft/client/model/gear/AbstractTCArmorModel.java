package com.leclowndu93150.thaumcraft.client.model.gear;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public abstract class AbstractTCArmorModel extends HumanoidModel<HumanoidRenderState> {
    protected AbstractTCArmorModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ZombieRenderState zombie) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive, zombie);
        }
    }
}

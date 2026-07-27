package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.client.champion.ChampionRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ChampionRenderState {
    @Unique
    private int thaumaturge$championType = -2;

    @Unique
    private int thaumaturge$entityId;

    @Override
    public int thaumaturge$championType() {
        return thaumaturge$championType;
    }

    @Override
    public void thaumaturge$setChampionType(int type) {
        this.thaumaturge$championType = type;
    }

    @Override
    public int thaumaturge$entityId() {
        return thaumaturge$entityId;
    }

    @Override
    public void thaumaturge$setEntityId(int id) {
        this.thaumaturge$entityId = id;
    }
}

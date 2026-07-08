package com.leclowndu93150.thaumcraft.mixin.client.renderer.entity;

import com.leclowndu93150.thaumcraft.client.champion.ChampionRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ChampionRenderState {
    @Unique
    private int thaumcraft$championType = -2;

    @Override
    public int thaumcraft$championType() {
        return thaumcraft$championType;
    }

    @Override
    public void thaumcraft$setChampionType(int type) {
        this.thaumcraft$championType = type;
    }
}

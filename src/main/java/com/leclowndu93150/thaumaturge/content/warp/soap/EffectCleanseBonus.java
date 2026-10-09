package com.leclowndu93150.thaumaturge.content.warp.soap;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;

public record EffectCleanseBonus(Holder<MobEffect> effect, int amount) implements CleanseBonus {
    @Override
    public int bonus(ServerPlayer player) {
        return player.hasEffect(effect) ? amount : 0;
    }
}

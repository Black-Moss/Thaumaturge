package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public abstract class AbstractChampionTrait implements MobTrait {
    private static final String NAME_KEY = "champion.thaumaturge.name";
    private static final double BONUS_HEALTH = 100.0;
    private static final double DAMAGE_MULTIPLIER_BONUS = 2.0;
    private static final float ASSIGNMENT_HEAL = 25.0F;

    @Override
    public final boolean isChampion() {
        return true;
    }

    @Override
    public final void modifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        modifiers.add(Attributes.MAX_HEALTH, BONUS_HEALTH, AttributeModifier.Operation.ADD_VALUE);
        modifiers.add(Attributes.ATTACK_DAMAGE, DAMAGE_MULTIPLIER_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        championModifiers(mob, modifiers);
    }

    @Override
    public final void onAdded(LivingEntity mob) {
        mob.heal(ASSIGNMENT_HEAL);
        mob.setCustomName(Component.translatable(NAME_KEY, MobTraitNames.of(TTMobTraits.registry().wrapAsHolder(this)), mob.getName()));
        onChampionAdded(mob);
    }

    protected void championModifiers(LivingEntity mob, MobTraitModifiers modifiers) {}

    protected void onChampionAdded(LivingEntity mob) {}
}

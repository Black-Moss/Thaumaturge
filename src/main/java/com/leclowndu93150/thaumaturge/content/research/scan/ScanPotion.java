package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

public final class ScanPotion implements IScanThing {
    private final Holder<MobEffect> effect;

    public ScanPotion(Holder<MobEffect> effect) {
        this.effect = effect;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target == null) {
            return false;
        }
        if (target instanceof LivingEntity living) {
            for (MobEffectInstance instance : living.getActiveEffects()) {
                if (instance.getEffect() == effect) {
                    return true;
                }
            }
            return false;
        }
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        if (stack.isEmpty()) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return false;
        }
        for (MobEffectInstance instance : contents.getAllEffects()) {
            if (instance.getEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return ScanKeys.effect(effect.unwrapKey().orElseThrow().identifier());
    }
}

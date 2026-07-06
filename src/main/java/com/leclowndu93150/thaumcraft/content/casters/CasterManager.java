package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.TreeMap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CasterManager {
    public static final String REMOVE_FOCUS = "REMOVE";
    private static final int TICKS_PER_SECOND = 20;
    private static final int EXHAUST_DISCOUNT_PENALTY = 10;
    private static final float SWAP_SOUND_VOLUME = 0.3F;
    private static final float SWAP_SOUND_PITCH = 1.0F;
    private static final float REMOVE_SOUND_PITCH = 0.9F;
    private static final int DEFAULT_AREA_SIZE = 3;
    private static final int DEFAULT_AREA_DIM = 0;

    private CasterManager() {}

    public static float getTotalVisDiscount(Player player) {
        if (player == null) {
            return 0.0F;
        }
        int total = GogglesAccess.totalVisDiscount(player);
        MobEffectInstance exhaust = player.getEffect(TCMobEffects.VIS_EXHAUST);
        MobEffectInstance infectious = player.getEffect(TCMobEffects.INFECTIOUS_VIS_EXHAUST);
        if (exhaust != null || infectious != null) {
            int level1 = exhaust != null ? exhaust.getAmplifier() : 0;
            int level2 = infectious != null ? infectious.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * EXHAUST_DISCOUNT_PENALTY;
        }
        return total / 100.0F;
    }

    public static boolean consumeVisFromInventory(Player player, float cost) {
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        for (int slot = main.size() - 1; slot >= 0; slot--) {
            ItemStack stack = main.get(slot);
            if (stack.getItem() instanceof ICaster caster && caster.consumeVis(stack, player, cost, true, false)) {
                return true;
            }
        }
        return false;
    }

    public static void changeFocus(ItemStack casterStack, Level level, Player player, String focusKey) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        TreeMap<String, Integer> foci = new TreeMap<>();
        for (int slot = 0; slot < main.size(); slot++) {
            ItemStack stack = main.get(slot);
            if (stack.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(stack);
                if (sortKey != null) {
                    foci.put(sortKey, slot);
                }
            }
        }
        if (REMOVE_FOCUS.equals(focusKey) || foci.isEmpty()) {
            ItemStack current = caster.getFocusStack(casterStack);
            if (!current.isEmpty() && player.getInventory().add(current.copy())) {
                caster.setFocus(casterStack, ItemStack.EMPTY);
                player.playSound(TCSounds.TICKS.get(), SWAP_SOUND_VOLUME, REMOVE_SOUND_PITCH);
            }
            return;
        }
        String newKey = focusKey;
        if (newKey == null || foci.get(newKey) == null) {
            newKey = newKey != null ? foci.higherKey(newKey) : null;
        }
        if (newKey == null || foci.get(newKey) == null) {
            newKey = foci.firstKey();
        }
        int slot = foci.get(newKey);
        ItemStack picked = main.get(slot).copy();
        if (picked.isEmpty()) {
            return;
        }
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        player.playSound(TCSounds.TICKS.get(), SWAP_SOUND_VOLUME, SWAP_SOUND_PITCH);
        ItemStack current = caster.getFocusStack(casterStack);
        if (!current.isEmpty() && player.getInventory().add(current.copy())) {
            caster.setFocus(casterStack, ItemStack.EMPTY);
        }
        if (caster.getFocusStack(casterStack).isEmpty()) {
            caster.setFocus(casterStack, picked);
        } else {
            player.getInventory().add(picked);
        }
    }

    public static int getAreaDim(ItemStack stack) {
        return DEFAULT_AREA_DIM;
    }

    public static int getAreaX(ItemStack stack) {
        return DEFAULT_AREA_SIZE;
    }

    public static int getAreaY(ItemStack stack) {
        return DEFAULT_AREA_SIZE;
    }

    public static int getAreaZ(ItemStack stack) {
        return DEFAULT_AREA_SIZE;
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        return entity.getData(TCAttachments.CASTER_COOLDOWN) > entity.level().getGameTime();
    }

    public static float getCooldown(LivingEntity entity) {
        long remaining = entity.getData(TCAttachments.CASTER_COOLDOWN) - entity.level().getGameTime();
        return remaining > 0 ? (float) remaining / TICKS_PER_SECOND : 0.0F;
    }

    public static void setCooldown(LivingEntity entity, int ticks) {
        if (ticks == 0) {
            entity.setData(TCAttachments.CASTER_COOLDOWN, 0L);
        } else {
            entity.setData(TCAttachments.CASTER_COOLDOWN, entity.level().getGameTime() + ticks);
        }
    }
}

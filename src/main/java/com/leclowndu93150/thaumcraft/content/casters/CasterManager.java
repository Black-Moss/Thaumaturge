package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.api.items.IArchitect;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
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
    private static final int DEFAULT_AREA_SIZE = CasterArea.DEFAULT_SIZE;
    private static final int AREA_DIM_ALL = 0;
    private static final int AREA_DIM_X = 1;
    private static final int AREA_DIM_Z = 2;
    private static final int AREA_DIM_Y = 3;

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

    public static void toggleMisc(ItemStack casterStack, Level level, Player player, int mod) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        FocusPackage core = ItemFocus.getPackage(caster.getFocusStack(casterStack));
        if (core == null || !containsArchitect(core)) {
            return;
        }
        int dim = getAreaDim(casterStack);
        if (mod == 0) {
            int areaX = getAreaX(casterStack);
            int areaY = getAreaY(casterStack);
            int areaZ = getAreaZ(casterStack);
            int max = getAreaSize(casterStack);
            switch (dim) {
                case AREA_DIM_ALL -> {
                    areaX++;
                    areaZ++;
                    areaY++;
                }
                case AREA_DIM_X -> areaX++;
                case AREA_DIM_Z -> areaZ++;
                case AREA_DIM_Y -> areaY++;
                default -> {}
            }
            if (areaX > max) {
                areaX = 0;
            }
            if (areaZ > max) {
                areaZ = 0;
            }
            if (areaY > max) {
                areaY = 0;
            }
            setArea(casterStack, new CasterArea(areaX, areaY, areaZ, dim));
        }
        if (mod == 1) {
            if (++dim > AREA_DIM_Y) {
                dim = AREA_DIM_ALL;
            }
            CasterArea area = area(casterStack);
            setArea(casterStack, new CasterArea(area.x(), area.y(), area.z(), dim));
        }
    }

    private static boolean containsArchitect(FocusPackage core) {
        for (IFocusElement element : core.getNodes()) {
            if (element instanceof IArchitect) {
                return true;
            }
        }
        return false;
    }

    private static CasterArea area(ItemStack stack) {
        CasterArea area = stack.get(TCDataComponents.CASTER_AREA.get());
        return area == null ? CasterArea.DEFAULT : area;
    }

    private static void setArea(ItemStack stack, CasterArea area) {
        stack.set(TCDataComponents.CASTER_AREA.get(), area);
    }

    private static int getAreaSize(ItemStack stack) {
        return stack.getItem() instanceof ICaster ? DEFAULT_AREA_SIZE : 0;
    }

    public static int getAreaDim(ItemStack stack) {
        return area(stack).dim();
    }

    public static int getAreaX(ItemStack stack) {
        return Math.min(area(stack).x(), getAreaSize(stack));
    }

    public static int getAreaY(ItemStack stack) {
        return Math.min(area(stack).y(), getAreaSize(stack));
    }

    public static int getAreaZ(ItemStack stack) {
        return Math.min(area(stack).z(), getAreaSize(stack));
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

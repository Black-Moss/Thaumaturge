package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TCAttributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Static accessor for detecting goggles, revealers, and vis-discount gear worn by an entity.
 *
 * <p>Side-agnostic: the head slot lookup uses {@link LivingEntity#getItemBySlot(EquipmentSlot)},
 * which works identically on client and server.
 *
 * @since 1.0.0
 */
public final class GogglesAccess {
    private GogglesAccess() {}

    /**
     * Returns whether the entity wears a head-slot stack implementing {@link IGoggles} with
     * popups enabled.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when the wearer should see in-game popups and HUD overlays
     */
    public static boolean wearsGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            if (ThaumaturgeCuriosCompat.checkForGoggles(entity)) return true;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof IGoggles g) {
            return g.showIngamePopups(head, entity);
        }
        return false;
    }

    /**
     * Returns whether the entity wears a stack implementing {@link IRevealer} that reveals
     * aura nodes. Held revealers do not count; the gear must occupy the head slot or an
     * equipped curio slot.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when nodes should be revealed for the entity
     */
    public static boolean revealsNodes(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty() && head.getItem() instanceof IRevealer r && r.showNodes(head, entity)) {
            return true;
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            return ThaumaturgeCuriosCompat.anyCurioMatches(entity,
                    stack -> stack.getItem() instanceof IRevealer r && r.showNodes(stack, entity));
        }
        return false;
    }

    /**
     * Sums the vis discount percentages contributed by ever held/worn item that has a modifier for the attribute {@link TCAttributes#VIS_DISCOUNT} by the player.
     * Each piece implementing {@link IVisDiscountGear} automatically obtain an attribute modifier for the {@link TCAttributes#VIS_DISCOUNT} attribute.
     *
     * @param player the player to query; null returns zero
     * @return the total discount in whole percent units, never negative
     */
    public static int totalVisDiscount(Player player) {
        if (player == null) {
            return 0;
        }
        AttributeInstance attribute = player.getAttribute(TCAttributes.VIS_DISCOUNT);
        if (attribute == null) return 0;
        return (int) (attribute.getValue() * 100);
    }
}

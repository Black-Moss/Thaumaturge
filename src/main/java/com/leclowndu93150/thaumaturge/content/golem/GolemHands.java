package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemHands;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class GolemHands implements IGolemHands {
    private static final int BASE_HAND_COUNT = 1;

    private final EntityThaumaturgeGolem golem;
    private final Hand primary;
    private final Hand secondary;
    private final List<Hand> allHands;

    public GolemHands(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        this.primary = new Hand(EquipmentSlot.MAINHAND);
        this.secondary = new Hand(EquipmentSlot.OFFHAND);
        this.allHands = List.of(primary, secondary);
    }

    private List<Hand> hands() {
        return golem.hasTrait(TTGolemTraits.HAULER) ? allHands : allHands.subList(0, BASE_HAND_COUNT);
    }

    @Override
    public ItemStack hold(ItemStack stack) {
        ItemStack leftover = stack;
        for (Hand hand : hands()) {
            leftover = hand.accept(leftover);
        }
        return leftover;
    }

    @Override
    public ItemStack release(ItemStack wanted) {
        ItemStack result = hands().stream().filter(hand -> hand.supplies(wanted)).findFirst().map(hand -> hand.withdraw(wanted)).orElse(ItemStack.EMPTY);
        refillPrimary();
        return result;
    }

    private void refillPrimary() {
        if (hands().size() > BASE_HAND_COUNT && primary.isIdle() && !secondary.isIdle()) {
            primary.set(secondary.get());
            secondary.set(ItemStack.EMPTY);
        }
    }

    @Override
    public int room(ItemStack stack) {
        return hands().stream().mapToInt(hand -> hand.space(stack)).sum();
    }

    @Override
    public boolean holds(ItemStack stack) {
        return !stack.isEmpty() && hands().stream().anyMatch(hand -> hand.carries(stack));
    }

    @Override
    public List<ItemStack> contents() {
        return hands().stream().map(Hand::get).toList();
    }

    private final class Hand {
        private final EquipmentSlot slot;

        private Hand(EquipmentSlot slot) {
            this.slot = slot;
        }

        private ItemStack get() {
            return golem.getItemBySlot(slot);
        }

        private void set(ItemStack stack) {
            golem.setItemSlot(slot, stack);
        }

        private boolean isIdle() {
            return get().isEmpty();
        }

        private boolean carries(ItemStack kind) {
            ItemStack held = get();
            return !held.isEmpty() && ItemStack.isSameItemSameComponents(held, kind);
        }

        private int space(ItemStack kind) {
            if (isIdle()) {
                return kind.getMaxStackSize();
            }
            return carries(kind) ? Math.max(0, kind.getMaxStackSize() - get().getCount()) : 0;
        }

        private ItemStack accept(ItemStack incoming) {
            if (incoming.isEmpty()) {
                return incoming;
            }
            if (isIdle()) {
                set(incoming);
                return ItemStack.EMPTY;
            }
            int added = Math.min(incoming.getCount(), space(incoming));
            get().grow(added);
            incoming.shrink(added);
            return incoming.isEmpty() ? ItemStack.EMPTY : incoming;
        }

        private boolean supplies(ItemStack wanted) {
            return !isIdle() && (wanted.isEmpty() || carries(wanted));
        }

        private ItemStack withdraw(ItemStack wanted) {
            ItemStack held = get();
            int amount = wanted.isEmpty() ? held.getCount() : Math.min(wanted.getCount(), held.getCount());
            ItemStack taken = held.split(amount);
            if (held.isEmpty()) {
                set(ItemStack.EMPTY);
            }
            return taken;
        }
    }
}

package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuPech extends AbstractContainerMenu {
    public static final int TRADE_BUTTON_ID = 0;
    private static final int OFFER_SLOT = 0;
    private static final int FIRST_PAYOUT_SLOT = 1;
    private static final int PAYOUT_COUNT = 4;
    private static final int PAYOUT_COLUMNS = 2;
    private static final int NO_SLOT = -1;
    private static final int TABLE_SLOTS = FIRST_PAYOUT_SLOT + PAYOUT_COUNT;
    private static final int PLAYER_SLOTS_END = TABLE_SLOTS + 36;
    private static final int OFFER_X = 36;
    private static final int OFFER_Y = 29;
    private static final int PAYOUT_X = 106;
    private static final int PAYOUT_Y = 20;
    private static final int SLOT_PITCH = 18;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final double INTERACTION_BUFFER = 4.0D;

    private final @Nullable EntityPech pech;
    private final Container table = new SimpleContainer(TABLE_SLOTS);

    public MenuPech(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, resolve(inventory, extraData.readVarInt()));
    }

    public MenuPech(int containerId, Inventory inventory, @Nullable EntityPech pech) {
        super(TTMenus.PECH.get(), containerId);
        this.pech = pech;
        addSlot(new Slot(table, OFFER_SLOT, OFFER_X, OFFER_Y));
        addPayoutGrid();
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
        if (pech != null) {
            pech.atTradeTable = true;
        }
    }

    private void addPayoutGrid() {
        for (int cell = 0; cell < PAYOUT_COUNT; cell++) {
            int x = PAYOUT_X + cell % PAYOUT_COLUMNS * SLOT_PITCH;
            int y = PAYOUT_Y + cell / PAYOUT_COLUMNS * SLOT_PITCH;
            addSlot(new PechPayoutSlot(table, FIRST_PAYOUT_SLOT + cell, x, y));
        }
    }

    private static @Nullable EntityPech resolve(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        return entity instanceof EntityPech found ? found : null;
    }

    public @Nullable EntityPech pech() {
        return pech;
    }

    public boolean canTrade() {
        if (pech == null) {
            return false;
        }
        ItemStack offer = table.getItem(OFFER_SLOT);
        return !offer.isEmpty() && pech.isPrizedItem(offer) && isPayoutAreaClear();
    }

    private boolean isPayoutAreaClear() {
        return IntStream.range(FIRST_PAYOUT_SLOT, TABLE_SLOTS).allMatch(this::isSlotBare);
    }

    private boolean isSlotBare(int slot) {
        return table.getItem(slot).isEmpty();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == TRADE_BUTTON_ID) {
            tryTrade(player);
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    private void tryTrade(Player player) {
        Optional.ofNullable(pech).filter(trader -> !player.level().isClientSide() && canTrade()).ifPresent(trader -> trade(player, trader));
    }

    private void trade(Player player, EntityPech trader) {
        ItemStack offered = table.removeItem(OFFER_SLOT, 1);
        for (ItemStack payout : PechBarter.haggle(trader, offered, player.level().getRandom(), player.level().registryAccess())) {
            if (!shelve(payout)) {
                player.getInventory().placeItemBackInInventory(payout);
            }
        }
        broadcastChanges();
    }

    private boolean shelve(ItemStack payout) {
        int target = findShelfSlot(payout);
        if (target == NO_SLOT) {
            return false;
        }
        ItemStack held = table.getItem(target);
        if (!held.isEmpty()) {
            held.grow(payout.getCount());
            table.setChanged();
            return true;
        }
        table.setItem(target, payout);
        return true;
    }

    private int findShelfSlot(ItemStack payout) {
        return IntStream.range(FIRST_PAYOUT_SLOT, TABLE_SLOTS).filter(slot -> acceptsPayout(table.getItem(slot), payout)).findFirst().orElse(NO_SLOT);
    }

    private static boolean acceptsPayout(ItemStack held, ItemStack payout) {
        return held.isEmpty() || canMergeInto(held, payout);
    }

    private static boolean canMergeInto(ItemStack held, ItemStack incoming) {
        int combined = held.getCount() + incoming.getCount();
        return combined <= held.getMaxStackSize() && ItemStack.isSameItemSameComponents(held, incoming);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        ItemStack moving = source.getItem();
        if (moving.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack snapshot = moving.copy();
        if (!relocate(moving, index)) {
            return ItemStack.EMPTY;
        }
        boolean changed = snapshot.getCount() != moving.getCount();
        refreshSource(source, moving);
        return changed ? completeTake(player, source, moving, snapshot) : ItemStack.EMPTY;
    }

    private static ItemStack completeTake(Player player, Slot source, ItemStack moving, ItemStack snapshot) {
        source.onTake(player, moving);
        return snapshot;
    }

    private static void refreshSource(Slot source, ItemStack remaining) {
        if (remaining.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
            return;
        }
        source.setChanged();
    }

    private boolean relocate(ItemStack moving, int fromIndex) {
        if (fromIndex >= TABLE_SLOTS) {
            return moveItemStackTo(moving, OFFER_SLOT, FIRST_PAYOUT_SLOT, false);
        }
        return moveItemStackTo(moving, TABLE_SLOTS, PLAYER_SLOTS_END, true);
    }

    @Override
    public boolean stillValid(Player player) {
        return Optional.ofNullable(pech).filter(trader -> trader.isAlive() && trader.isDomesticated()).filter(trader -> player.isWithinEntityInteractionRange(trader, INTERACTION_BUFFER)).isPresent();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (pech != null) {
            pech.atTradeTable = false;
        }
        if (player.level().isClientSide()) {
            return;
        }
        IntStream.range(0, TABLE_SLOTS).forEach(slot -> ejectTagged(player, table.removeItemNoUpdate(slot)));
    }

    private static void ejectTagged(Player player, ItemStack leftover) {
        if (leftover.isEmpty()) {
            return;
        }
        Optional.ofNullable(player.drop(leftover, true, false)).ifPresent(MenuPech::tagDropped);
    }

    private static void tagDropped(ItemEntity dropped) {
        dropped.addTag(EntityPech.DROPPED_BY_PECH_TAG);
    }
}

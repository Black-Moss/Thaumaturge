package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MenuTurretBasic extends AbstractContainerMenu {
    public static final int AMMO_X = 80;
    public static final int AMMO_Y = 29;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;

    private static final int AMMO_SLOT = 0;
    private static final int FIRST_INVENTORY_SLOT = 1;

    protected final @Nullable EntityTurretCrossbow turret;

    public MenuTurretBasic(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(TTMenus.TURRET_BASIC.get(), containerId, inventory, resolve(inventory, buf.readVarInt()), AMMO_X, AMMO_Y);
    }

    protected MenuTurretBasic(MenuType<?> type, int containerId, Inventory inventory, @Nullable EntityTurretCrossbow turret, int ammoX, int ammoY) {
        super(type, containerId);
        this.turret = turret;
        addSlot(new AmmoSlot(new MobEquipmentContainer(turret), AMMO_SLOT, ammoX, ammoY, turret));
        addInventoryExtendedSlots(inventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(inventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    static @Nullable EntityTurretCrossbow resolve(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        return entity instanceof EntityTurretCrossbow found ? found : null;
    }

    public static void open(Player player, EntityTurretCrossbow turret) {
        present(player, turret, (containerId, inventory, viewer) -> new MenuTurretBasic(TTMenus.TURRET_BASIC.get(), containerId, inventory, turret, AMMO_X, AMMO_Y));
    }

    static void present(Player player, EntityTurretCrossbow turret, MenuConstructor factory) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(factory, turret.getDisplayName()), buf -> buf.writeVarInt(turret.getId()));
        }
    }

    public @Nullable EntityTurretCrossbow turret() {
        return turret;
    }

    @Override
    public boolean stillValid(Player player) {
        return turret != null && turret.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index == AMMO_SLOT) {
            moved = moveItemStackTo(stack, FIRST_INVENTORY_SLOT, slots.size(), true);
        } else {
            moved = turret != null && turret.isValidAmmo(stack) && moveItemStackTo(stack, AMMO_SLOT, FIRST_INVENTORY_SLOT, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private static final class AmmoSlot extends Slot {
        private final @Nullable EntityTurretCrossbow owner;

        private AmmoSlot(MobEquipmentContainer container, int slot, int x, int y, @Nullable EntityTurretCrossbow owner) {
            super(container, slot, x, y);
            this.owner = owner;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return owner != null && owner.isValidAmmo(stack);
        }
    }
}

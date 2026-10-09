package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class MenuTurretAdvanced extends MenuTurretBasic {
    public static final int AMMO_X = 42;
    public static final int BUTTON_ANIMAL = 1;
    public static final int BUTTON_MOB = 2;
    public static final int BUTTON_PLAYER = 3;
    public static final int BUTTON_FRIENDLY = 4;

    public MenuTurretAdvanced(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        super(TTMenus.TURRET_ADVANCED.get(), containerId, inventory, resolve(inventory, buf.readVarInt()), AMMO_X, AMMO_Y);
    }

    private MenuTurretAdvanced(int containerId, Inventory inventory, EntityTurretCrossbowAdvanced turret) {
        super(TTMenus.TURRET_ADVANCED.get(), containerId, inventory, turret, AMMO_X, AMMO_Y);
    }

    public static void open(Player player, EntityTurretCrossbowAdvanced turret) {
        present(player, turret, (containerId, inventory, viewer) -> new MenuTurretAdvanced(containerId, inventory, turret));
    }

    public @Nullable EntityTurretCrossbowAdvanced advancedTurret() {
        return turret instanceof EntityTurretCrossbowAdvanced advanced ? advanced : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < BUTTON_ANIMAL || buttonId > BUTTON_FRIENDLY) {
            return super.clickMenuButton(player, buttonId);
        }
        EntityTurretCrossbowAdvanced advanced = advancedTurret();
        if (advanced != null && !player.level().isClientSide()) {
            toggle(advanced, buttonId);
        }
        return true;
    }

    private static void toggle(EntityTurretCrossbowAdvanced advanced, int buttonId) {
        switch (buttonId) {
            case BUTTON_ANIMAL -> advanced.flipFilter(TurretFilter.ANIMALS);
            case BUTTON_MOB -> advanced.flipFilter(TurretFilter.MOBS);
            case BUTTON_PLAYER -> advanced.flipFilter(TurretFilter.PLAYERS);
            default -> advanced.flipFilter(TurretFilter.FRIENDLY);
        }
    }
}

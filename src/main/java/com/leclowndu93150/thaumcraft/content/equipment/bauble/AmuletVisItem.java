package com.leclowndu93150.thaumcraft.content.equipment.bauble;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.items.RechargeAccess;
import com.leclowndu93150.thaumcraft.compat.curio.ThaumcraftCuriosCompat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.fml.ModList;

public final class AmuletVisItem extends Item {
    private final int rechargeInterval;

    public AmuletVisItem(Properties properties, int rechargeInterval) {
        super(properties);
        this.rechargeInterval = rechargeInterval;
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof Player player)
                || player.level().isClientSide()
                || player.tickCount % rechargeInterval != 0) {
            return;
        }
        for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
            if (RechargeAccess.rechargeItem(player.level(), player.getInventory().getItem(slot),
                    player.blockPosition(), player, 1) > 0.0F) {
                return;
            }
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)
                && ThaumcraftCuriosCompat.rechargeFirstCurio(player)) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor() && RechargeAccess.rechargeItem(player.level(),
                    player.getItemBySlot(slot), player.blockPosition(), player, 1) > 0.0F) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.thaumcraft.amulet_vis.text")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}

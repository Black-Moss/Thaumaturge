package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.fml.ModList;

public final class AmuletVisItem extends Item {
    private static final String TEXT_KEY = "item.thaumaturge.amulet_vis.text";
    private static final int HOTBAR_SLOTS = 9;
    private static final int PULSE_AMOUNT = 1;
    private static final float WAND_DRAIN = 1.0F;
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    private static final List<RechargeSource> SOURCES = List.of(new HotbarStackSource(), new HotbarWandSource(), new OffhandStackSource(), new OffhandWandSource(), new CurioSource(), new ArmorSource());

    private final int interval;

    public AmuletVisItem(Properties properties, int interval) {
        super(properties);
        this.interval = interval;
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof ServerPlayer player) || player.tickCount % interval != 0) {
            return;
        }
        for (RechargeSource source : SOURCES) {
            if (source.recharge(player)) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(TEXT_KEY).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    private static boolean rechargeStack(ServerPlayer player, ItemStack stack) {
        return RechargeAccess.drawFromAura(player.level(), stack, player.blockPosition(), player, PULSE_AMOUNT) > 0;
    }

    private static boolean rechargeWand(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemWand)) {
            return false;
        }
        int max = WandVisHelper.capacityOf(stack);
        ResourceKey<IAspect> lowest = null;
        int lowestAmount = max;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int amount = WandVisHelper.storedIn(stack, primal);
            if (amount < lowestAmount) {
                lowest = primal;
                lowestAmount = amount;
            }
        }
        if (lowest == null) {
            return false;
        }
        BlockPos pos = player.blockPosition();
        if (AuraHelper.drainVis(player.level(), pos, WAND_DRAIN, false) <= 0.0F) {
            return false;
        }
        WandVisHelper.topUpCentivis(stack, lowest, WandEconomy.CENTIVIS_PER_VIS, true);
        return true;
    }

    private interface RechargeSource {
        boolean recharge(ServerPlayer player);
    }

    private record HotbarStackSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
                if (rechargeStack(player, player.getInventory().getItem(slot))) {
                    return true;
                }
            }
            return false;
        }
    }

    private record HotbarWandSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
                if (rechargeWand(player, player.getInventory().getItem(slot))) {
                    return true;
                }
            }
            return false;
        }
    }

    private record OffhandStackSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            return rechargeStack(player, player.getOffhandItem());
        }
    }

    private record OffhandWandSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            return rechargeWand(player, player.getOffhandItem());
        }
    }

    private record CurioSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            return ModList.get().isLoaded(TTIds.CURIOS) && ThaumaturgeCuriosCompat.rechargeFirstCurio(player);
        }
    }

    private record ArmorSource() implements RechargeSource {
        @Override
        public boolean recharge(ServerPlayer player) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                if (rechargeStack(player, player.getItemBySlot(slot))) {
                    return true;
                }
            }
            return false;
        }
    }
}

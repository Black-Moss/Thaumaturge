package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    private static final List<RechargeSource> SOURCES = sorted(new WandSource(0), new OffhandSource(1), new HotbarSource(2), new CurioSource(3), new ArmorSource(4));

    private final int interval;

    public AmuletVisItem(Properties properties, int interval) {
        super(properties);
        this.interval = interval;
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof ServerPlayer player) || player.tickCount % interval != 0) {
            return;
        }
        List<Target> targets = new ArrayList<>();
        for (RechargeSource source : SOURCES) {
            source.collect(player, targets);
        }
        targets.sort(Comparator.comparingDouble(Target::fill));
        for (Target target : targets) {
            if (target.recharge(player)) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(TEXT_KEY).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    private static List<RechargeSource> sorted(RechargeSource... sources) {
        List<RechargeSource> list = new ArrayList<>(List.of(sources));
        list.sort(Comparator.comparingInt(RechargeSource::priority));
        return List.copyOf(list);
    }

    private static boolean canAccept(ItemStack stack) {
        ChargeProfile profile = RechargeAccess.profile(stack);
        return profile != null && RechargeAccess.getCharge(stack) < profile.capacity();
    }

    private interface Target {
        float fill();

        boolean recharge(ServerPlayer player);
    }

    private interface RechargeSource {
        int priority();

        void collect(ServerPlayer player, List<Target> targets);
    }

    private record StackTarget(ItemStack stack, float fill) implements Target {
        @Override
        public boolean recharge(ServerPlayer player) {
            return RechargeAccess.rechargeItem(player.level(), stack, player.blockPosition(), player, PULSE_AMOUNT) > 0.0F;
        }
    }

    private record WandTarget(ItemStack wand, ResourceKey<IAspect> primal, float fill) implements Target {
        @Override
        public boolean recharge(ServerPlayer player) {
            BlockPos pos = player.blockPosition();
            if (AuraHelper.drainVis(player.level(), pos, WAND_DRAIN, false) <= 0.0F) {
                return false;
            }
            WandVisHelper.topUpCentivis(wand, primal, WandEconomy.CENTIVIS_PER_VIS, true);
            return true;
        }
    }

    private static void collectStack(ItemStack stack, List<Target> targets) {
        if (canAccept(stack)) {
            targets.add(new StackTarget(stack, RechargeAccess.getChargePercentage(stack, null)));
        }
    }

    private record WandSource(int priority) implements RechargeSource {
        @Override
        public void collect(ServerPlayer player, List<Target> targets) {
            for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
                collectWand(player.getInventory().getItem(slot), targets);
            }
            collectWand(player.getOffhandItem(), targets);
        }

        private static void collectWand(ItemStack stack, List<Target> targets) {
            if (!(stack.getItem() instanceof ItemWand)) {
                return;
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
            if (lowest != null) {
                targets.add(new WandTarget(stack, lowest, (float) lowestAmount / max));
            }
        }
    }

    private record OffhandSource(int priority) implements RechargeSource {
        @Override
        public void collect(ServerPlayer player, List<Target> targets) {
            collectStack(player.getOffhandItem(), targets);
        }
    }

    private record HotbarSource(int priority) implements RechargeSource {
        @Override
        public void collect(ServerPlayer player, List<Target> targets) {
            for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
                collectStack(player.getInventory().getItem(slot), targets);
            }
        }
    }

    private record CurioSource(int priority) implements RechargeSource {
        @Override
        public void collect(ServerPlayer player, List<Target> targets) {
            if (!ModList.get().isLoaded(TTIds.CURIOS)) {
                return;
            }
            for (ItemStack curio : ThaumaturgeCuriosCompat.equippedCurios(player)) {
                collectStack(curio, targets);
            }
        }
    }

    private record ArmorSource(int priority) implements RechargeSource {
        @Override
        public void collect(ServerPlayer player, List<Target> targets) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                collectStack(player.getItemBySlot(slot), targets);
            }
        }
    }
}

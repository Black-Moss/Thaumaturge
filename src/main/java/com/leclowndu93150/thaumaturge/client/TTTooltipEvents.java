package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTTooltipEvents {
    private static final String NAME_LEVEL_SEPARATOR = " ";
    private static final int INSERT_INDEX = 1;
    private static final int NO_VALUE = 0;
    private static final int SINGLE_LEVEL = 1;
    private static final String ENCHANTMENT_KEY_PREFIX = "enchantment." + TTIds.MODID + ".";
    private static final String LEVEL_KEY_PREFIX = "enchantment.level.";
    private static final String RUNIC_KEY = "tooltip." + TTIds.MODID + ".runic_charge";
    private static final String WARPING_KEY = "item." + TTIds.MODID + ".warping";
    private static final String CHARGE_KEY = "tooltip." + TTIds.MODID + ".charge";
    private static final String STABILISER_KEY = "tooltip." + TTIds.MODID + ".infusion_stabiliser";
    private static final Identifier UNLOCK_INFUSION = TTIds.rl("unlock_infusion");

    private TTTooltipEvents() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player viewer = event.getEntity();
        if (viewer != null) {
            decorate(event.getToolTip(), event.getItemStack(), viewer);
        }
    }

    private static void decorate(List<Component> tooltip, ItemStack subject, Player viewer) {
        List<Component> headline = new ArrayList<>(enchantmentLines(subject));
        Stream.of(runicLine(subject), warpingLine(subject, viewer), chargeLine(subject)).filter(Objects::nonNull).forEach(headline::add);
        Collections.reverse(headline);
        tooltip.addAll(headline.isEmpty() ? tooltip.size() : INSERT_INDEX, headline);
        if (isUnlockedStabiliser(subject, viewer)) {
            tooltip.add(Component.translatable(STABILISER_KEY).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static List<Component> enchantmentLines(ItemStack subject) {
        List<Component> lines = new ArrayList<>();
        InfusionEnchantmentHelper.get(subject).levels().forEach((enchantment, level) -> {
            MutableComponent label = Component.translatable(ENCHANTMENT_KEY_PREFIX + enchantment.getSerializedName());
            if (enchantment.maxLevel() > SINGLE_LEVEL) {
                label = Component.empty().append(label).append(NAME_LEVEL_SEPARATOR).append(Component.translatable(LEVEL_KEY_PREFIX + level));
            }
            lines.add(Component.empty().append(label).withStyle(ChatFormatting.GOLD));
        });
        return lines;
    }

    private static @Nullable Component runicLine(ItemStack subject) {
        int runic = InfusionRunicAugmentRecipe.charge(subject);
        return runic > NO_VALUE ? Component.translatable(RUNIC_KEY, runic).withStyle(ChatFormatting.GOLD) : null;
    }

    private static @Nullable Component warpingLine(ItemStack subject, Player viewer) {
        boolean warped = WarpHelper.getFinalWarp(subject, viewer) > NO_VALUE;
        return warped ? Component.translatable(WARPING_KEY).withStyle(ChatFormatting.DARK_PURPLE) : null;
    }

    private static @Nullable Component chargeLine(ItemStack subject) {
        ChargeProfile profile = RechargeAccess.profile(subject);
        if (profile == null) {
            return null;
        }
        return Component.translatable(CHARGE_KEY, RechargeAccess.getCharge(subject), profile.capacity()).withStyle(ChatFormatting.AQUA);
    }

    private static boolean isUnlockedStabiliser(ItemStack subject, Player viewer) {
        return subject.getItem() instanceof BlockItem blockItem && isStabiliser(blockItem.getBlock()) && KnowledgeAccess.of(viewer).isResearchComplete(UNLOCK_INFUSION);
    }

    private static boolean isStabiliser(Block block) {
        return block instanceof IInfusionStabiliser || block.defaultBlockState().is(TTBlockTags.INFUSION_STABILISERS);
    }
}

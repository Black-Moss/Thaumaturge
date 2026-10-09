package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class FocusPouchItem extends Item {
    public static final int SIZE = 18;

    private static final Identifier RESEARCH = TTIds.rl("focus_pouch");

    public FocusPouchItem(Item.Properties properties) {
        super(properties);
    }

    public static NonNullList<ItemStack> getInventory(ItemStack stack) {
        NonNullList<ItemStack> contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ItemContainerContents stored = stack.get(TTDataComponents.POUCH_CONTENTS.get());
        if (stored != null) {
            stored.copyInto(contents);
        }
        return contents;
    }

    public static void setInventory(ItemStack stack, NonNullList<ItemStack> contents) {
        stack.set(TTDataComponents.POUCH_CONTENTS.get(), ItemContainerContents.fromItems(contents));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int foci = 0;
        for (ItemStack held : getInventory(stack)) {
            if (FocusItems.isFocus(held)) {
                foci++;
            }
        }
        tooltip.accept(Component.translatable("tooltip.thaumaturge.focus_pouch.count", foci, SIZE).withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!DeviceGate.passes(player, RESEARCH)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, menuPlayer) -> new MenuFocusPouch(containerId, inventory, hand), player.getItemInHand(hand).getHoverName()),
                    buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResult.SUCCESS;
    }
}

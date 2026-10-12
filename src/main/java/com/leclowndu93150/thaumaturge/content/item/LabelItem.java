package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.blocks.LabelTarget;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class LabelItem extends Item implements ILabel {
    private static final String MARKED_NAME_KEY = "item." + TTIds.MODID + ".marked_label";

    public LabelItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack withAspect(Holder<IAspect> aspect) {
        ResourceKey<IAspect> key = aspect.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Aspect holder has no registry key: " + aspect));
        return withAspect(key);
    }

    public static ItemStack withAspect(ResourceKey<IAspect> aspect) {
        ItemStack stack = new ItemStack(TTItems.LABEL.get());
        stack.set(TTDataComponents.ASPECT_FILTER.get(), aspect);
        return stack;
    }

    @Override
    public @Nullable ResourceKey<IAspect> getFilteredAspect(ItemStack stack) {
        return stack.get(TTDataComponents.ASPECT_FILTER.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(TTDataComponents.ASPECT_FILTER.get())) {
            return Component.translatable(MARKED_NAME_KEY);
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        LabelTarget target = findTarget(level, pos);
        if (player == null || target == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack label = context.getItemInHand();
        if (!target.attachLabel(level, pos, context.getClickedFace(), player, label)) {
            return InteractionResult.PASS;
        }
        label.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static @Nullable LabelTarget findTarget(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof LabelTarget block) {
            return block;
        }
        return level.getBlockEntity(pos) instanceof LabelTarget blockEntity ? blockEntity : null;
    }
}

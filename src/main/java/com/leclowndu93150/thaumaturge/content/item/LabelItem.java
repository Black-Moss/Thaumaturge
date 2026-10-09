package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
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
import net.minecraft.world.level.block.entity.BlockEntity;
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = context.getItemInHand();
        ILabelable target = findTarget(level, context.getClickedPos());
        if (target == null || !target.applyLabel(player, context.getClickedPos(), context.getClickedFace(), held)) {
            return InteractionResult.PASS;
        }
        if (!player.hasInfiniteMaterials()) {
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
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

    private static @Nullable ILabelable findTarget(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof ILabelable block) {
            return block;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof ILabelable labelable ? labelable : null;
    }
}

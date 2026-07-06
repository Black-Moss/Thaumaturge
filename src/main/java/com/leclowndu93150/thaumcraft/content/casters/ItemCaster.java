package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.casters.CasterTriggerRegistry;
import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.leclowndu93150.thaumcraft.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumcraft.api.items.IArchitect;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class ItemCaster extends Item implements ICaster, IArchitect {
    public static final int AREA_SINGLE_CHUNK = 0;
    public static final int AREA_CROSS_CHUNKS = 1;
    public static final int AREA_NINE_CHUNKS = 2;
    private static final float MIN_CONSUMPTION_MODIFIER = 0.1F;
    private static final int CHUNK_OFFSET_BLOCKS = 16;

    private final int area;

    public ItemCaster(Properties properties, int area) {
        super(properties);
        this.area = area;
    }

    public @Nullable ItemFocus getFocus(ItemStack stack) {
        return getFocusStack(stack).getItem() instanceof ItemFocus focus ? focus : null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        ItemStackTemplate template = stack.get(TCDataComponents.SOCKETED_FOCUS.get());
        return template == null ? ItemStack.EMPTY : template.create();
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (focus == null || focus.isEmpty()) {
            stack.remove(TCDataComponents.SOCKETED_FOCUS.get());
        } else {
            stack.set(TCDataComponents.SOCKETED_FOCUS.get(), ItemStackTemplate.fromNonEmptyStack(focus));
        }
    }

    @Override
    public float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
        float modifier = 1.0F;
        if (player != null) {
            modifier -= CasterManager.getTotalVisDiscount(player);
        }
        return Math.max(modifier, MIN_CONSUMPTION_MODIFIER);
    }

    private List<BlockPos> auraPositions(Player player) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos base = player.blockPosition();
        switch (area) {
            case AREA_CROSS_CHUNKS -> {
                positions.add(base);
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    positions.add(base.relative(face, CHUNK_OFFSET_BLOCKS));
                }
            }
            case AREA_NINE_CHUNKS -> {
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        positions.add(base.offset(xx * CHUNK_OFFSET_BLOCKS, 0, zz * CHUNK_OFFSET_BLOCKS));
                    }
                }
            }
            default -> positions.add(base);
        }
        return positions;
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
        amount *= getConsumptionModifier(stack, player, crafting);
        Level level = player.level();
        List<BlockPos> positions = auraPositions(player);
        float pool = 0.0F;
        for (BlockPos pos : positions) {
            pool += AuraHelper.getVis(level, pos);
        }
        if (pool < amount) {
            return false;
        }
        if (simulate) {
            return true;
        }
        if (positions.size() == 1) {
            amount -= AuraHelper.drainVis(level, positions.get(0), amount, false);
            return amount <= 0.0F;
        }
        float slice = amount / positions.size();
        while (amount > 0.0F) {
            float before = amount;
            for (BlockPos pos : positions) {
                amount -= AuraHelper.drainVis(level, pos, Math.min(slice, amount), false);
                if (amount <= 0.0F) {
                    return true;
                }
            }
            if (amount >= before) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable BlockState getPickedBlock(ItemStack stack) {
        ItemStack focusStack = getFocusStack(stack);
        FocusPackage core = ItemFocus.getPackage(focusStack);
        if (core == null) {
            return null;
        }
        for (IFocusElement element : core.getNodes()) {
            if (element instanceof IFocusBlockPicker) {
                return stack.get(TCDataComponents.PICKED_BLOCK.get());
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack casterStack = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(casterStack);
        ItemFocus focus = getFocus(casterStack);
        if (focus == null || CasterManager.isOnCooldown(player)) {
            return InteractionResult.PASS;
        }
        FocusPackage core = ItemFocus.getPackage(focusStack);
        if (core == null) {
            return InteractionResult.PASS;
        }
        CasterManager.setCooldown(player, focus.getActivationTime(focusStack));
        if (player.isShiftKeyDown()) {
            for (IFocusElement element : core.getNodes()) {
                if (element instanceof IFocusBlockPicker) {
                    return InteractionResult.PASS;
                }
            }
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (consumeVis(casterStack, player, focus.getVisCost(focusStack), false, false)) {
            FocusEngine.castFocusPackage(player, core);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IInteractWithCaster target
                && target.onCasterRightClick(level, stack, player, pos, side, hand)) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IInteractWithCaster target
                && target.onCasterRightClick(level, stack, player, pos, side, hand)) {
            return InteractionResult.SUCCESS;
        }
        if (CasterTriggerRegistry.hasTrigger(state)) {
            return CasterTriggerRegistry.performTrigger(level, stack, player, pos, side, state)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        }
        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && player.isShiftKeyDown() && blockEntity == null) {
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core != null) {
                for (IFocusElement element : core.getNodes()) {
                    if (element instanceof IFocusBlockPicker) {
                        if (!level.isClientSide()) {
                            if (!state.isAir()) {
                                stack.set(TCDataComponents.PICKED_BLOCK.get(), state);
                            }
                            return InteractionResult.SUCCESS;
                        }
                        player.swing(hand);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    private @Nullable IArchitect architectElement(ItemStack stack) {
        FocusPackage core = ItemFocus.getPackage(getFocusStack(stack));
        if (core == null) {
            return null;
        }
        for (IFocusElement element : core.getNodes()) {
            if (element instanceof IArchitect architect) {
                return architect;
            }
        }
        return null;
    }

    @Override
    public @Nullable HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster) {
        IArchitect architect = architectElement(stack);
        return architect == null ? null : architect.getArchitectMOP(stack, level, caster);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        IArchitect architect = architectElement(stack);
        return architect == null ? List.of() : architect.getArchitectBlocks(stack, level, pos, side, player);
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis) {
        IArchitect architect = architectElement(stack);
        return architect != null && architect.showAxis(stack, level, player, side, axis);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == this && newStack.getItem() == this) {
            return sortingHash(oldStack) != sortingHash(newStack);
        }
        return oldStack.getItem() != newStack.getItem();
    }

    private int sortingHash(ItemStack casterStack) {
        ItemFocus focus = getFocus(casterStack);
        if (focus == null) {
            return 0;
        }
        String sortKey = focus.getSortingHelper(getFocusStack(casterStack));
        return sortKey != null ? sortKey.hashCode() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag flag) {
        ItemStack focusStack = getFocusStack(stack);
        if (!(focusStack.getItem() instanceof ItemFocus focus)) {
            return;
        }
        builder.accept(Component.translatable("tooltip.thaumcraft.caster.vis_cost",
                        ItemFocus.formatVis(focus.getVisCost(focusStack)))
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
        builder.accept(focusStack.getHoverName().copy()
                .withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));
        focus.addFocusInformation(focusStack, builder);
    }
}

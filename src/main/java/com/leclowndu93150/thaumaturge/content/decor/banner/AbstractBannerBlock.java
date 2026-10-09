package com.leclowndu93150.thaumaturge.content.decor.banner;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBannerBlock extends Block implements EntityBlock {
    private static final float EDIT_VOLUME = 1.0F;
    private static final float EDIT_PITCH = 1.0F;

    private final @Nullable DyeColor dye;

    protected AbstractBannerBlock(@Nullable DyeColor dye, Properties properties) {
        super(properties);
        this.dye = dye;
    }

    public @Nullable DyeColor dye() {
        return dye;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBanner(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (dye == null || !(level.getBlockEntity(pos) instanceof BlockEntityBanner banner)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                banner.setAspect(null);
                playEditSound(level, pos);
            }
            return InteractionResult.SUCCESS;
        }
        ResourceKey<IAspect> phialAspect = phialAspect(stack);
        if (phialAspect == null) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide()) {
            banner.setAspect(phialAspect);
            stack.shrink(1);
            playEditSound(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static @Nullable ResourceKey<IAspect> phialAspect(ItemStack stack) {
        if (!(stack.getItem() instanceof PhialItem)) {
            return null;
        }
        AspectList aspects = ComponentEssentia.phial(stack).getAspects();
        if (aspects.isEmpty()) {
            return null;
        }
        AspectInstance first = aspects.entries().getFirst();
        return first.aspect().getKey();
    }

    private static void playEditSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, EDIT_VOLUME, EDIT_PITCH);
    }
}

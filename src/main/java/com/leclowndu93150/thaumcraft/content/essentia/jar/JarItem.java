package com.leclowndu93150.thaumcraft.content.essentia.jar;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaList;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class JarItem extends BlockItem implements IEssentiaContainerItem {


    public JarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        EssentiaList essentia = stack.get(TCDataComponents.ESSENTIA_CONTENTS);
        return essentia == null ? AspectList.EMPTY : essentia.contents();
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            stack.remove(TCDataComponents.ESSENTIA_CONTENTS);
            return;
        }
        stack.set(TCDataComponents.ESSENTIA_CONTENTS, new EssentiaList(aspects));
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !getAspects(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AspectList list = getAspects(stack);
        if (list.isEmpty()) {
            return 0;
        }
        int amount = list.totalAmount();
        int max = BlockEntityJar.CAPACITY;
        return Mth.clamp(Math.round((float) amount * 13.0F / (float) max), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        AspectList list = getAspects(stack);
        if (list.isEmpty()) {
            return 0;
        }
        AspectInstance first = list.entries().getFirst();
        if (first == null) {
            return 0;
        }
        return first.aspect().value().color();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return super.useOn(context);
        if (level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic) {
            AspectList aspects = getAspects(stack);
            // We use Direction.UP to allow insertion/extraction from all faces with fials
            if (alembic.aspectKey() != null) {
                Holder<IAspect> aspect = EssentiaTransportHelper.resolve(level, alembic.aspectKey());
                if (aspect != null && (aspects.isEmpty() || aspect == aspects.entries().getFirst().aspect())) {
                    int alembicAmount = Math.min(alembic.amount(), BlockEntityJar.CAPACITY - aspects.totalAmount());
                    if (alembicAmount >= 0) {
                        if (level.isClientSide()) {
                            player.swing(context.getHand());
                            return InteractionResult.SUCCESS;
                        }
                        int taken = alembic.takeEssentia(aspect, alembicAmount, Direction.UP);
                        if (taken > 0) {
                            setAspects(stack, aspects.add(aspect, taken));
                            level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.25f, 1.0f);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.useOn(context);
    }


}

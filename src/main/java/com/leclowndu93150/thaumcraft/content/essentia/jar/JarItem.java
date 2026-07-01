package com.leclowndu93150.thaumcraft.content.essentia.jar;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaList;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

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
        return Mth.clamp(Math.round((float)amount * 13.0F / (float)max), 0, 13);
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
}

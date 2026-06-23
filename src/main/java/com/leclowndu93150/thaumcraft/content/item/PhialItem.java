package com.leclowndu93150.thaumcraft.content.item;

import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PhialItem extends Item implements IEssentiaContainerItem {
    public static final int BASE_AMOUNT = 10;

    public PhialItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect, int amount) {
        ItemStack stack = new ItemStack(com.leclowndu93150.thaumcraft.registry.items.TCItemsHContainers.PHIAL.get());
        stack.set(TCDataComponents.ASPECTS.get(), AspectList.of(new AspectInstance(aspect, amount)));
        return stack;
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect) {
        return makeFilled(aspect, BASE_AMOUNT);
    }

    @Override
    public Component getName(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects.isEmpty()) {
            return Component.translatable(this.getDescriptionId() + ".empty");
        }
        Holder<IAspect> first = aspects.entries().getFirst().aspect();
        MutableComponent aspectName = AspectComponents.name(first);
        return Component.translatable(this.getDescriptionId() + ".filled", aspectName);
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        AspectList stored = stack.get(TCDataComponents.ASPECTS.get());
        return stored == null ? AspectList.EMPTY : stored;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            stack.remove(TCDataComponents.ASPECTS.get());
            return;
        }
        stack.set(TCDataComponents.ASPECTS.get(), aspects);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
}

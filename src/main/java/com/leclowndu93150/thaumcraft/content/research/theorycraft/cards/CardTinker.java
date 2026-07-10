package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CardTinker extends TheorycraftCard {
    private static final List<Supplier<ItemStack>> OPTIONS = List.of(
            () -> new ItemStack(TCItems.VIS_RESONATOR.get()),
            () -> new ItemStack(TCItems.THAUMOMETER.get()),
            () -> new ItemStack(Items.ANVIL),
            () -> new ItemStack(Items.ACTIVATOR_RAIL),
            () -> new ItemStack(Items.DISPENSER),
            () -> new ItemStack(Items.DROPPER),
            () -> new ItemStack(Items.ENCHANTING_TABLE),
            () -> new ItemStack(Items.ENDER_CHEST),
            () -> new ItemStack(Items.JUKEBOX),
            () -> new ItemStack(Items.DAYLIGHT_DETECTOR),
            () -> new ItemStack(Items.PISTON),
            () -> new ItemStack(Items.HOPPER),
            () -> new ItemStack(Items.STICKY_PISTON),
            () -> new ItemStack(Items.MAP),
            () -> new ItemStack(Items.COMPASS),
            () -> new ItemStack(Items.TNT_MINECART),
            () -> new ItemStack(Items.COMPARATOR),
            () -> new ItemStack(Items.CLOCK));

    private ItemStack stack = ItemStack.EMPTY;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.ARTIFICE;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.tinker.name");
    }

    @Override
    public Component description() {
        int base = baseValue() * 2;
        return Component.translatable("card.thaumcraft.tinker.text", base, base + 10);
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        stack = OPTIONS.get(rng.nextInt(OPTIONS.size())).get();
        return !stack.isEmpty();
    }

    private int baseValue() {
        return (int) Math.sqrt(CardHelper.visSize(stack));
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        return stack.isEmpty() ? List.of() : List.of(new CardItemRequirement(stack.copy(), false));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        int base = baseValue() * 2;
        data.addCategoryTotal(TCResearchCategories.ARTIFICE, Mth.nextInt(player.getRandom(), base, base + 10));
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (!stack.isEmpty()) {
            output.store("stack", ItemStack.CODEC, stack);
        }
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        stack = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}

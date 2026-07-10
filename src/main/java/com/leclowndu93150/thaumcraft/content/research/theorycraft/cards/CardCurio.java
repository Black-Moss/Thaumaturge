package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.content.misc.ItemCurio;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CardCurio extends TheorycraftCard {
    private ItemStack curio = ItemStack.EMPTY;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.curio.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.curio.text");
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        return curio.isEmpty() ? List.of() : List.of(new CardItemRequirement(curio.copy(), true));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        List<ItemStack> curios = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCurio) {
                curios.add(stack.copyWithCount(1));
            }
        }
        if (!curios.isEmpty()) {
            curio = curios.get(rng.nextInt(curios.size()));
        }
        return !curio.isEmpty();
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.BASICS, 5);
        data.addCategoryTotal(CardHelper.randomCategory(player), 5);
        ItemCurio.Variant variant = curio.getItem() instanceof ItemCurio item ? item.variant() : null;
        if (variant == null) {
            data.addCategoryTotal(TCResearchCategories.BASICS, Mth.nextInt(player.getRandom(), 25, 35));
        } else {
            switch (variant) {
                case ARCANE -> data.addCategoryTotal(TCResearchCategories.AUROMANCY, Mth.nextInt(player.getRandom(), 25, 35));
                case PRESERVED -> data.addCategoryTotal(TCResearchCategories.ALCHEMY, Mth.nextInt(player.getRandom(), 25, 35));
                case ANCIENT -> data.addCategoryTotal(TCResearchCategories.GOLEMANCY, Mth.nextInt(player.getRandom(), 25, 35));
                case ELDRITCH -> data.addCategoryTotal(TCResearchCategories.ELDRITCH, Mth.nextInt(player.getRandom(), 25, 35));
                case KNOWLEDGE -> data.addCategoryTotal(TCResearchCategories.INFUSION, Mth.nextInt(player.getRandom(), 25, 35));
                case TWISTED -> data.addCategoryTotal(TCResearchCategories.ARTIFICE, Mth.nextInt(player.getRandom(), 25, 35));
                case RITES -> {
                    data.addCategoryTotal(TCResearchCategories.ELDRITCH, Mth.nextInt(player.getRandom(), 15, 20));
                    data.addCategoryTotal(TCResearchCategories.AUROMANCY, Mth.nextInt(player.getRandom(), 10, 15));
                }
            }
        }
        if (player.getRandom().nextBoolean()) {
            data.addBonusDraws(1);
        }
        if (player.getRandom().nextBoolean()) {
            data.addBonusDraws(1);
        }
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (!curio.isEmpty()) {
            output.store("stack", ItemStack.CODEC, curio);
        }
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        curio = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}

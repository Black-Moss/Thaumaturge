package com.leclowndu93150.thaumcraft.data.datamap;

import com.leclowndu93150.thaumcraft.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumcraft.registry.TCItemTags;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class InfernalBonusProvider extends DataMapProvider {
    public InfernalBonusProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        Builder<List<InfernalBonus>, Item> b = builder(InfernalBonus.DATA_MAP);

        add(b,Tags.Items.ORES_IRON, InfernalBonus.builder(items,Tags.Items.NUGGETS_IRON).chance(0.33F).build());
        add(b,Tags.Items.ORES_COPPER, InfernalBonus.builder(items,Tags.Items.NUGGETS_COPPER).chance(0.33F).build());
        add(b,Tags.Items.ORES_GOLD, InfernalBonus.builder(items,Tags.Items.NUGGETS_GOLD).chance(0.33F).build());
        add(b,Tags.Items.ORES_QUARTZ, InfernalBonus.builder(items,TCItemTags.NUGGETS_QUARTZ).chance(0.33F).build());
        add(b, TCItemTags.ORES_CINNABAR, InfernalBonus.builder(items,TCItemTags.NUGGETS_QUICKSILVER).chance(0.33F).build());
        addConditional(b,TCItemTags.ORES_LEAD, new NotCondition(new TagEmptyCondition<>(TCItemTags.NUGGETS_LEAD)),InfernalBonus.builder(items,TCItemTags.NUGGETS_LEAD).chance(0.33F).build());
        addConditional(b,TCItemTags.ORES_SILVER, new NotCondition(new TagEmptyCondition<>(TCItemTags.NUGGETS_SILVER)),InfernalBonus.builder(items,TCItemTags.NUGGETS_SILVER).chance(0.33F).build());
        addConditional(b,TCItemTags.ORES_TIN, new NotCondition(new TagEmptyCondition<>(TCItemTags.NUGGETS_TIN)),InfernalBonus.builder(items,TCItemTags.NUGGETS_TIN).chance(0.33F).build());

        add(b, TCItemTags.RARE_EARTH_CHANCE_HIGH, InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.025F).build());
        add(b, TCItemTags.RARE_EARTH_CHANCE_NORMAL, InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.02F).build());
        add(b, TCItemTags.RARE_EARTH_CHANCE_LOW, InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.01F).build());

    }

    @Override
    public String getName() {
        return "Infernal Furnace Bonus Data Map";
    }

    private static void add(Builder<List<InfernalBonus>, Item> b, ItemLike key, InfernalBonus... values) {
        b.add(key.asItem().builtInRegistryHolder(), Arrays.stream(values).toList(), false);
    }

    private static void add(Builder<List<InfernalBonus>, Item> b, TagKey<Item> key, InfernalBonus... values) {
        b.add(key, Arrays.stream(values).toList(), false);
    }

    private static void addConditional(Builder<List<InfernalBonus>, Item> b, TagKey<Item> key, ICondition condition, InfernalBonus... values) {
        b.add(key, Arrays.stream(values).toList(), false, condition);
    }
}

package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksAOres;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsAOres;
import java.util.List;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TCBlocksAOresLoot {
    private TCBlocksAOresLoot() {}

    public static LootTable.Builder amber(HolderLookup.Provider lookup) {
        HolderLookup.RegistryLookup<Enchantment> enchants = lookup.lookupOrThrow(Registries.ENCHANTMENT);
        LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(TCItemsAOres.AMBER.get())
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                .apply(ApplyBonusCount.addOreBonusCount(enchants.getOrThrow(Enchantments.FORTUNE)))
                .apply(ApplyExplosionDecay.explosionDecay());
        return silkOrDrop(TCBlocksAOres.ORE_AMBER.get(), silkCondition(enchants), entry);
    }

    public static LootTable.Builder cinnabar(HolderLookup.Provider lookup) {
        HolderLookup.RegistryLookup<Enchantment> enchants = lookup.lookupOrThrow(Registries.ENCHANTMENT);
        LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(TCItemsAOres.CINNABAR.get())
                .apply(ApplyBonusCount.addOreBonusCount(enchants.getOrThrow(Enchantments.FORTUNE)))
                .apply(ApplyExplosionDecay.explosionDecay());
        return silkOrDrop(TCBlocksAOres.ORE_CINNABAR.get(), silkCondition(enchants), entry);
    }

    public static LootTable.Builder quartz(HolderLookup.Provider lookup) {
        HolderLookup.RegistryLookup<Enchantment> enchants = lookup.lookupOrThrow(Registries.ENCHANTMENT);
        LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(Items.QUARTZ)
                .apply(ApplyBonusCount.addOreBonusCount(enchants.getOrThrow(Enchantments.FORTUNE)))
                .apply(ApplyExplosionDecay.explosionDecay());
        return silkOrDrop(TCBlocksAOres.ORE_QUARTZ.get(), silkCondition(enchants), entry);
    }

    private static LootTable.Builder silkOrDrop(Block block, LootItemCondition.Builder silk, LootPoolEntryContainer.Builder<?> entry) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block).when(silk).otherwise(entry))
        );
    }

    private static LootItemCondition.Builder silkCondition(HolderLookup.RegistryLookup<Enchantment> enchants) {
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withComponents(
                                DataComponentMatchers.Builder.components()
                                        .partial(
                                                DataComponentPredicates.ENCHANTMENTS,
                                                EnchantmentsPredicate.enchantments(List.of(
                                                        new EnchantmentPredicate(
                                                                enchants.getOrThrow(Enchantments.SILK_TOUCH),
                                                                MinMaxBounds.Ints.atLeast(1)
                                                        )
                                                ))
                                        )
                                        .build()
                        )
        );
    }
}

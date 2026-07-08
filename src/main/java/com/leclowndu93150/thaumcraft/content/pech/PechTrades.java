package com.leclowndu93150.thaumcraft.content.pech;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.entity.EntityPech;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public final class PechTrades {
    public record Entry(int tier, ItemStack stack) {}

    private PechTrades() {}

    public static List<Entry> tradesFor(int pechType, HolderLookup.Provider registries) {
        return switch (pechType) {
            case EntityPech.TYPE_MAGE -> mageTrades(registries);
            case EntityPech.TYPE_STALKER -> stalkerTrades(registries);
            default -> foragerTrades();
        };
    }

    private static List<Entry> foragerTrades() {
        List<Entry> trades = new ArrayList<>();
        add(trades, 1, TCItems.CLUSTER_IRON.get());
        add(trades, 1, TCItems.CLUSTER_GOLD.get());
        add(trades, 1, TCItems.CLUSTER_CINNABAR.get());
        add(trades, 1, TCItems.CLUSTER_QUARTZ.get());
        add(trades, 1, TCItems.CLUSTER_COPPER.get());
        add(trades, 2, Items.BLAZE_ROD);
        add(trades, 2, TCBlocks.SAPLING_GREATWOOD.get());
        add(trades, 2, Items.DRAGON_BREATH);
        add(trades, 2, Items.COMPASS);
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        add(trades, 3, Items.GOLDEN_APPLE);
        add(trades, 4, TCItems.THAUMIUM_PICKAXE.get());
        add(trades, 4, TCItems.THAUMIUM_AXE.get());
        add(trades, 4, TCItems.THAUMIUM_HOE.get());
        add(trades, 4, Items.SPECTRAL_ARROW);
        add(trades, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(trades, 5, TCBlocks.SAPLING_SILVERWOOD.get());
        add(trades, 5, Items.TOTEM_OF_UNDYING);
        return trades;
    }

    private static List<Entry> mageTrades(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        List<Entry> trades = new ArrayList<>();
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.AER))));
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.TERRA))));
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.IGNIS))));
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.AQUA))));
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.ORDO))));
        trades.add(new Entry(1, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.PERDITIO))));
        trades.add(new Entry(2, PotionContents.createItemStack(Items.POTION, Potions.REGENERATION)));
        trades.add(new Entry(2, PotionContents.createItemStack(Items.POTION, Potions.HEALING)));
        trades.add(new Entry(2, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.VITIUM))));
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        trades.add(new Entry(3, EssentiaCrystalFactory.of(aspects.getOrThrow(TCAspects.AURAM))));
        add(trades, 3, Items.GOLDEN_APPLE);
        add(trades, 4, TCItems.CLOTH_BOOTS.get());
        add(trades, 4, TCItems.CLOTH_CHEST.get());
        add(trades, 4, TCItems.CLOTH_LEGS.get());
        add(trades, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(trades, 5, TCItems.PECH_WAND.get());
        return trades;
    }

    private static List<Entry> stalkerTrades(HolderLookup.Provider registries) {
        List<Entry> trades = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            if (dye != DyeColor.WHITE) {
                add(trades, 1, TCBlocks.CANDLES.get(dye).get());
            }
        }
        add(trades, 2, Items.GHAST_TEAR);
        trades.add(new Entry(2, enchantedBook(registries, Enchantments.POWER, 1)));
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        add(trades, 3, Items.EXPERIENCE_BOTTLE);
        add(trades, 3, Items.GOLDEN_APPLE);
        add(trades, 4, Items.ENCHANTED_GOLDEN_APPLE);
        trades.add(new Entry(5, enchantedBook(registries, Enchantments.FLAME, 1)));
        trades.add(new Entry(5, enchantedBook(registries, Enchantments.INFINITY, 1)));
        return trades;
    }

    private static ItemStack enchantedBook(HolderLookup.Provider registries,
                                           ResourceKey<Enchantment> enchantment, int level) {
        Holder<Enchantment> holder = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(holder, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
        return book;
    }

    private static void add(List<Entry> trades, int tier, ItemLike item) {
        trades.add(new Entry(tier, new ItemStack(item)));
    }
}

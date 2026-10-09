package com.leclowndu93150.thaumaturge.data.worldgen.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable.PechTrade;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public final class PechTradeBootstrap {
    private static final int TIER_COMMON = 1;
    private static final int TIER_UNCOMMON = 2;
    private static final int TIER_FAIR = 3;
    private static final int TIER_RARE = 4;
    private static final int TIER_EPIC = 5;
    private static final int CRYSTAL_AMOUNT = 1;
    private static final int BOOK_LEVEL = 1;

    private PechTradeBootstrap() {}

    public static void bootstrap(BootstrapContext<PechTradeTable> ctx) {
        HolderGetter<IAspect> aspects = ctx.lookup(IAspect.REGISTRY_KEY);
        HolderGetter<Enchantment> enchantments = ctx.lookup(Registries.ENCHANTMENT);

        TradeListBuilder forager = new TradeListBuilder(aspects, enchantments);
        forager.items(TIER_COMMON, TTItems.CLUSTER_IRON.get(), TTItems.CLUSTER_GOLD.get(), TTItems.CLUSTER_CINNABAR.get(), TTItems.CLUSTER_QUARTZ.get(), TTItems.CLUSTER_COPPER.get())
                .items(TIER_UNCOMMON, Items.BLAZE_ROD, TTBlocks.SAPLING_GREATWOOD.get(), Items.DRAGON_BREATH, Items.COMPASS)
                .items(TIER_FAIR, Items.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE, Items.GOLDEN_APPLE)
                .items(TIER_RARE, TTItems.THAUMIUM_PICKAXE.get(), TTItems.THAUMIUM_AXE.get(), TTItems.THAUMIUM_HOE.get(), Items.SPECTRAL_ARROW)
                .items(TIER_EPIC, Items.ENCHANTED_GOLDEN_APPLE, TTBlocks.SAPLING_SILVERWOOD.get(), Items.TOTEM_OF_UNDYING, TTItems.CURIO_KNOWLEDGE.get());
        forager.registerAs(ctx, "forager");

        TradeListBuilder mage = new TradeListBuilder(aspects, enchantments);
        for (ResourceKey<IAspect> primal : List.of(TTAspects.AER, TTAspects.TERRA, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.ORDO, TTAspects.PERDITIO)) {
            mage.crystal(TIER_COMMON, primal);
        }
        mage.potion(TIER_UNCOMMON, Potions.REGENERATION).potion(TIER_UNCOMMON, Potions.HEALING).crystal(TIER_UNCOMMON, TTAspects.VITIUM)
                .items(TIER_FAIR, Items.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE).crystal(TIER_FAIR, TTAspects.AURAM).items(TIER_FAIR, Items.GOLDEN_APPLE)
                .items(TIER_RARE, TTItems.CLOTH_BOOTS.get(), TTItems.CLOTH_CHEST.get(), TTItems.CLOTH_LEGS.get())
                .items(TIER_EPIC, Items.ENCHANTED_GOLDEN_APPLE, TTItems.PECH_WAND.get(), TTItems.CURIO_KNOWLEDGE.get());
        mage.registerAs(ctx, "mage");

        TradeListBuilder stalker = new TradeListBuilder(aspects, enchantments);
        Arrays.stream(DyeColor.values()).filter(dye -> dye != DyeColor.WHITE).forEach(dye -> stalker.items(TIER_COMMON, TTBlocks.CANDLES.get(dye).get()));
        stalker.items(TIER_UNCOMMON, Items.GHAST_TEAR).book(TIER_UNCOMMON, Enchantments.POWER).items(TIER_FAIR, Items.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE, Items.GOLDEN_APPLE)
                .items(TIER_RARE, TTItems.ELDRITCH_EYE.get(), Items.ENCHANTED_GOLDEN_APPLE).book(TIER_EPIC, Enchantments.FLAME).book(TIER_EPIC, Enchantments.INFINITY)
                .items(TIER_EPIC, TTItems.CURIO_KNOWLEDGE.get());
        stalker.registerAs(ctx, "stalker");
    }

    private static ResourceKey<PechTradeTable> tableKey(String name) {
        return ResourceKey.create(PechTradeTable.REGISTRY_KEY, TTIds.rl(name));
    }

    private static final class TradeListBuilder {
        private final HolderGetter<IAspect> aspects;
        private final HolderGetter<Enchantment> enchantments;
        private final List<PechTrade> entries = new ArrayList<>();

        private TradeListBuilder(HolderGetter<IAspect> aspects, HolderGetter<Enchantment> enchantments) {
            this.aspects = aspects;
            this.enchantments = enchantments;
        }

        private TradeListBuilder items(int tier, ItemLike... goods) {
            Arrays.stream(goods).map(good -> new PechTrade(tier, new ItemStackTemplate(good.asItem()))).forEach(entries::add);
            return this;
        }

        private TradeListBuilder crystal(int tier, ResourceKey<IAspect> aspect) {
            AspectInstance contained = new AspectInstance(aspects.getOrThrow(aspect), CRYSTAL_AMOUNT);
            return withPatch(tier, TTItems.ESSENTIA_CRYSTAL.get(), DataComponentPatch.builder().set(TTDataComponents.CRYSTAL_ASPECT.get(), contained));
        }

        private TradeListBuilder potion(int tier, Holder<Potion> potion) {
            return withPatch(tier, Items.POTION, DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(potion)));
        }

        private TradeListBuilder book(int tier, ResourceKey<Enchantment> enchantment) {
            ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            stored.set(enchantments.getOrThrow(enchantment), BOOK_LEVEL);
            return withPatch(tier, Items.ENCHANTED_BOOK, DataComponentPatch.builder().set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable()));
        }

        private TradeListBuilder withPatch(int tier, Item item, DataComponentPatch.Builder patch) {
            entries.add(new PechTrade(tier, new ItemStackTemplate(item, patch.build())));
            return this;
        }

        private void registerAs(BootstrapContext<PechTradeTable> ctx, String name) {
            ctx.register(tableKey(name), new PechTradeTable(entries));
        }
    }
}

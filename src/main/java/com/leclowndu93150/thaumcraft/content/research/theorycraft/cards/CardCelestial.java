package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.CategoryComponents;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.TCResearchEntries;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import com.leclowndu93150.thaumcraft.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import java.util.List;
import java.util.Random;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CardCelestial extends TheorycraftCard {
    private static final int NOTE_VARIANTS = 13;
    private static final int SUN_INDEX = 0;
    private static final int FIRST_MOON_INDEX = 5;
    private static final int BASE_POINTS_MIN = 25;
    private static final int BASE_POINTS_SPREAD = 26;
    private static final int STAR_WARP_SPREAD = 6;

    private int note1 = -1;
    private int note2 = -1;
    private @Nullable ResourceKey<IResearchCategory> category;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public @Nullable ResourceKey<IResearchCategory> category() {
        return category;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.celestial.name");
    }

    @Override
    public Component description() {
        return category == null
                ? Component.translatable("card.thaumcraft.celestial.text", Component.empty())
                : Component.translatable("card.thaumcraft.celestial.text", CategoryComponents.emphasised(category));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        if (data.categoriesWithTotal().isEmpty()
                || !KnowledgeAccess.of(player).isResearchKnown(TCResearchEntries.CELESTIAL_SCANNING)) {
            return false;
        }
        Random rng = new Random(seed());
        note1 = rng.nextInt(NOTE_VARIANTS);
        note2 = note1;
        while (note2 == note1) {
            note2 = rng.nextInt(NOTE_VARIANTS);
        }
        int highest = 0;
        ResourceKey<IResearchCategory> highestKey = null;
        for (ResourceKey<IResearchCategory> key : data.categoriesWithTotal()) {
            int total = data.categoryTotal(key);
            if (total > highest) {
                highest = total;
                highestKey = key;
            }
        }
        category = highestKey;
        return category != null;
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        return List.of(
                new CardItemRequirement(noteStack(note1), true),
                new CardItemRequirement(noteStack(note2), true));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (category == null) {
            return false;
        }
        data.addCategoryTotal(category, BASE_POINTS_MIN + player.getRandom().nextInt(BASE_POINTS_SPREAD));
        boolean sun = note1 == SUN_INDEX || note2 == SUN_INDEX;
        boolean moon = note1 >= FIRST_MOON_INDEX || note2 >= FIRST_MOON_INDEX;
        boolean stars = isStar(note1) || isStar(note2);
        if (stars) {
            int amount = player.getRandom().nextInt(STAR_WARP_SPREAD);
            data.addCategoryTotal(TCResearchCategories.ELDRITCH, amount * 2);
            ResearchManager.applyWarp(player, amount);
        }
        if (sun) {
            data.incrementPenalty();
        }
        if (moon) {
            data.addBonusDraws(1);
        }
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        output.putInt("note1", note1);
        output.putInt("note2", note2);
        if (category != null) {
            output.store("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY), category);
        }
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        note1 = input.getIntOr("note1", -1);
        note2 = input.getIntOr("note2", -1);
        category = input.read("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY)).orElse(null);
    }

    private static boolean isStar(int note) {
        return note > SUN_INDEX && note < FIRST_MOON_INDEX;
    }

    private static ItemStack noteStack(int note) {
        return CelestialNotesItem.stackOf(CelestialBody.byIndex(note));
    }
}

package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public final class SalisMundusCraftingExtension implements ICraftingCategoryExtension<SalisMundusRecipe> {
    public static final SalisMundusCraftingExtension INSTANCE = new SalisMundusCraftingExtension();

    private SalisMundusCraftingExtension() {}

    private static final int CRYSTAL_SLOTS = 3;

    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<SalisMundusRecipe> recipeHolder) {
        List<SlotDisplay> slots = Stream.of(Items.FLINT, Items.BOWL, Items.REDSTONE).<SlotDisplay>map(item -> new SlotDisplay.ItemSlotDisplay(item)).collect(Collectors.toCollection(ArrayList::new));
        List<SlotDisplay> crystals = crystalDisplays();
        IntStream.range(0, CRYSTAL_SLOTS).mapToObj(offset -> rotated(crystals, offset)).forEach(slots::add);
        return slots;
    }

    private static SlotDisplay rotated(List<SlotDisplay> displays, int offset) {
        if (displays.isEmpty()) {
            return SlotDisplay.Empty.INSTANCE;
        }
        if (displays.size() == 1) {
            return displays.get(0);
        }
        List<SlotDisplay> shifted = new ArrayList<>(displays);
        Collections.rotate(shifted, -offset);
        return new SlotDisplay.Composite(shifted);
    }

    private static List<SlotDisplay> crystalDisplays() {
        Player player = Minecraft.getInstance().player;
        List<SlotDisplay> variants = player == null
                ? List.of()
                : EssentiaCrystalFactory.discoveredCrystals(player).stream().<SlotDisplay>map(crystal -> new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(crystal))).toList();
        if (variants.isEmpty()) {
            return List.of(new SlotDisplay.ItemSlotDisplay(TTItems.ESSENTIA_CRYSTAL.get()));
        }
        return variants;
    }
}

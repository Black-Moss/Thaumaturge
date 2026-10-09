package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public final class SalisMundusRecipe extends CustomRecipe {
    public static final SalisMundusRecipe INSTANCE = new SalisMundusRecipe();

    public static final MapCodec<SalisMundusRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);

    public static final StreamCodec<RegistryFriendlyByteBuf, SalisMundusRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private static final int CRYSTAL_COUNT = 3;

    private static final int RETURNED_COUNT = 1;

    private static final Set<Part> RETURNED_PARTS = EnumSet.of(Part.BOWL, Part.FLINT);

    private static final List<Part> SINGLE_PARTS = List.of(Part.BOWL, Part.FLINT, Part.DUST);

    private SalisMundusRecipe() {}

    @Override
    public RecipeSerializer<SalisMundusRecipe> getSerializer() {
        return TTRecipeSerializers.SALIS_MUNDUS.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        SlotDisplay crystal = new SlotDisplay.ItemSlotDisplay(TTItems.ESSENTIA_CRYSTAL.get());
        List<SlotDisplay> ingredients = List.of(new SlotDisplay.ItemSlotDisplay(Items.FLINT), new SlotDisplay.ItemSlotDisplay(Items.BOWL), new SlotDisplay.TagSlotDisplay(Tags.Items.DUSTS_REDSTONE),
                crystal, crystal, crystal);
        return List.of(new ShapelessCraftingRecipeDisplay(ingredients, new SlotDisplay.ItemSlotDisplay(TTItems.SALIS_MUNDUS.get()), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    private static ItemStack leftoverOf(ItemStack stack) {
        Part part = Part.classify(stack);
        if (part != null && RETURNED_PARTS.contains(part)) {
            return stack.copyWithCount(RETURNED_COUNT);
        }
        ItemStackTemplate remainder = stack.getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> leftovers = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < leftovers.size(); slot++) {
            leftovers.set(slot, leftoverOf(input.getItem(slot)));
        }
        return leftovers;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return TTItems.SALIS_MUNDUS.get().getDefaultInstance();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> filled = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
        if (filled.stream().anyMatch(stack -> Part.classify(stack) == null)) {
            return false;
        }
        Map<Part, List<ItemStack>> grouped = filled.stream().collect(Collectors.groupingBy(Part::classify, () -> new EnumMap<>(Part.class), Collectors.toList()));
        for (Part single : SINGLE_PARTS) {
            if (grouped.getOrDefault(single, List.of()).size() != 1) {
                return false;
            }
        }
        List<ItemStack> crystals = grouped.getOrDefault(Part.CRYSTAL, List.of());
        Set<ResourceKey<IAspect>> distinct = new HashSet<>();
        for (ItemStack crystal : crystals) {
            AspectInstance carried = crystal.get(TTDataComponents.CRYSTAL_ASPECT.get());
            ResourceKey<IAspect> key = carried == null ? null : carried.aspect().getKey();
            if (key == null || !distinct.add(key)) {
                return false;
            }
        }
        return distinct.size() == CRYSTAL_COUNT;
    }

    private enum Part implements Predicate<ItemStack> {
        BOWL(stack -> stack.is(Items.BOWL)), FLINT(stack -> stack.is(Items.FLINT)), DUST(stack -> stack.is(Tags.Items.DUSTS_REDSTONE)), CRYSTAL(stack -> stack.is(TTItems.ESSENTIA_CRYSTAL));

        private final Predicate<ItemStack> filter;

        Part(Predicate<ItemStack> filter) {
            this.filter = filter;
        }

        @Override
        public boolean test(ItemStack stack) {
            return filter.test(stack);
        }

        static @Nullable Part classify(ItemStack stack) {
            return Arrays.stream(values()).filter(part -> part.test(stack)).findFirst().orElse(null);
        }
    }
}

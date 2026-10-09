package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class InfusionRunicAugmentRecipe implements InfusionJobRecipe {
    public static final int BASE_INSTABILITY = 5;
    private static final int MAX_COMPONENTS = 64;
    private static final int MAX_CHARGE = 120;
    private static final int DISPLAY_CHARGES = 5;
    private static final int INSTABILITY_CHARGE_DIVISOR = 2;
    private static final float COST_BASE = 1.0F;
    private static final float COST_DIVISOR = 2.0F;
    private static final double COST_GROWTH = 2.0;

    public static final MapCodec<InfusionRunicAugmentRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Ingredient.CODEC.listOf(1, MAX_COMPONENTS).fieldOf("components").forGetter(r -> r.base), Ingredient.CODEC.fieldOf("per_level").forGetter(r -> r.perLevel),
                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects), Ingredient.CODEC.fieldOf("display_catalyst").forGetter(r -> r.displayCatalyst),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, InfusionRunicAugmentRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRunicAugmentRecipe> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.base,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.perLevel, AspectList.STREAM_CODEC, r -> r.aspects, Ingredient.CONTENTS_STREAM_CODEC, r -> r.displayCatalyst,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, InfusionRunicAugmentRecipe::new);

    public static final RecipeSerializer<InfusionRunicAugmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final List<Ingredient> base;
    private final Ingredient perLevel;
    private final AspectList aspects;
    private final Ingredient displayCatalyst;
    private final Optional<ResearchGate> research;

    public InfusionRunicAugmentRecipe(List<Ingredient> base, Ingredient perLevel, AspectList aspects, Ingredient displayCatalyst, Optional<ResearchGate> research) {
        this.base = List.copyOf(base);
        this.perLevel = perLevel;
        this.aspects = aspects;
        this.displayCatalyst = displayCatalyst;
        this.research = research;
    }

    public static int charge(ItemStack stack) {
        return stack.getOrDefault(TTDataComponents.RUNIC_CHARGE.get(), 0);
    }

    public static boolean isShieldable(ItemStack stack) {
        if (stack.is(TTItemTags.RUNIC_SHIELDABLE)) {
            return true;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    public List<Ingredient> scaledComponents(ItemStack catalyst) {
        List<Ingredient> scaled = new ArrayList<>(base);
        for (int i = 0; i < charge(catalyst); i++) {
            scaled.add(perLevel);
        }
        return scaled;
    }

    public @Nullable List<ItemStack> matchScaled(ItemStack catalyst, List<ItemStack> available) {
        List<Ingredient> required = scaledComponents(catalyst);
        if (available.size() != required.size()) {
            return null;
        }
        List<ItemStack> remaining = new ArrayList<>(available);
        List<ItemStack> chosen = new ArrayList<>(required.size());
        for (Ingredient ingredient : required) {
            ItemStack found = null;
            for (ItemStack candidate : remaining) {
                if (ingredient.test(candidate)) {
                    found = candidate;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            remaining.remove(found);
            chosen.add(found.copyWithCount(1));
        }
        return chosen;
    }

    public AspectList scaledAspects(ItemStack catalyst) {
        float factor = (COST_BASE + (float) Math.pow(COST_GROWTH, charge(catalyst))) / COST_DIVISOR;
        AspectList scaled = AspectList.EMPTY;
        for (AspectInstance entry : aspects.entries()) {
            int amount = (int) (entry.amount() * factor);
            if (amount > 0) {
                scaled = scaled.add(entry.aspect(), amount);
            }
        }
        return scaled;
    }

    public int scaledInstability(ItemStack catalyst) {
        return BASE_INSTABILITY + charge(catalyst) / INSTABILITY_CHARGE_DIVISOR;
    }

    @Override
    public @Nullable List<ItemStack> jobComponents(InfusionInput input) {
        return matchScaled(input.catalyst(), input.components());
    }

    @Override
    public AspectList jobEssentia(InfusionInput input) {
        return scaledAspects(input.catalyst());
    }

    @Override
    public int jobInstability(InfusionInput input) {
        return scaledInstability(input.catalyst());
    }

    public ItemStack augmentedResult(ItemStack catalyst) {
        ItemStack result = catalyst.copyWithCount(1);
        result.set(TTDataComponents.RUNIC_CHARGE.get(), Math.min(MAX_CHARGE, charge(catalyst) + 1));
        return result;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        ItemStack catalyst = input.catalyst();
        return !catalyst.isEmpty() && isShieldable(catalyst) && matchScaled(catalyst, input.components()) != null;
    }

    @Override
    public Ingredient catalyst() {
        return displayCatalyst;
    }

    @Override
    public List<Ingredient> components() {
        return base;
    }

    @Override
    public AspectList aspects() {
        return aspects;
    }

    @Override
    public int instability() {
        return BASE_INSTABILITY;
    }

    @Override
    public ItemStack resultItem() {
        ItemStack display = displayCatalyst.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        return display.isEmpty() ? ItemStack.EMPTY : augmentedResult(display);
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack sample = displayCatalyst.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        if (sample.isEmpty()) {
            return List.of(new InfusionRecipeDisplay(displayCatalyst.display(), componentDisplays(base), aspects, BASE_INSTABILITY, SlotDisplay.Empty.INSTANCE));
        }
        List<RecipeDisplay> displays = new ArrayList<>(DISPLAY_CHARGES);
        for (int level = 0; level < DISPLAY_CHARGES; level++) {
            ItemStack catalystAtLevel = sample.copy();
            catalystAtLevel.set(TTDataComponents.RUNIC_CHARGE.get(), level);
            displays.add(new InfusionRecipeDisplay(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(catalystAtLevel)), componentDisplays(scaledComponents(catalystAtLevel)),
                    scaledAspects(catalystAtLevel), scaledInstability(catalystAtLevel), new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(augmentedResult(catalystAtLevel)))));
        }
        return displays;
    }

    private static List<SlotDisplay> componentDisplays(List<Ingredient> ingredients) {
        return ingredients.stream().map(Ingredient::display).map(display -> (SlotDisplay) display).toList();
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        return augmentedResult(input.catalyst());
    }

    @Override
    public RecipeSerializer<InfusionRunicAugmentRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionRunicAugmentRecipe> getType() {
        return TTRecipeTypes.RUNIC_AUGMENT.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }
}

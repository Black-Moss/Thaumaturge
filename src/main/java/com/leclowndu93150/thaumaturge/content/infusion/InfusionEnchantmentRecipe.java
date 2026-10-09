package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.Level;

public final class InfusionEnchantmentRecipe implements InfusionJobRecipe {
    public static final int INSTABILITY = 4;
    private static final int MAX_COMPONENTS = 64;
    private static final int WARP_ROLL_BOUND = 10;
    private static final float OTHER_ENCHANTMENT_WEIGHT = 0.33F;
    private static final int DISPLAY_LEVEL = 1;

    public static final MapCodec<InfusionEnchantmentRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i
                    .group(InfusionEnchantment.CODEC.fieldOf("enchantment").forGetter(r -> r.enchantment),
                            Ingredient.CODEC.listOf(1, MAX_COMPONENTS).fieldOf("components").forGetter(r -> r.components), AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                            Ingredient.CODEC.fieldOf("display_catalyst").forGetter(r -> r.displayCatalyst), ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research))
                    .apply(i, InfusionEnchantmentRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(InfusionEnchantment.STREAM_CODEC, r -> r.enchantment,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.components, AspectList.STREAM_CODEC, r -> r.aspects, Ingredient.CONTENTS_STREAM_CODEC, r -> r.displayCatalyst,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, InfusionEnchantmentRecipe::new);

    public static final RecipeSerializer<InfusionEnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final InfusionEnchantment enchantment;
    private final List<Ingredient> components;
    private final AspectList aspects;
    private final Ingredient displayCatalyst;
    private final Optional<ResearchGate> research;

    public InfusionEnchantmentRecipe(InfusionEnchantment enchantment, List<Ingredient> components, AspectList aspects, Ingredient displayCatalyst, Optional<ResearchGate> research) {
        this.enchantment = enchantment;
        this.components = List.copyOf(components);
        this.aspects = aspects;
        this.displayCatalyst = displayCatalyst;
        this.research = research;
    }

    public InfusionEnchantment enchantment() {
        return enchantment;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        ItemStack catalyst = input.catalyst();
        return !catalyst.isEmpty() && InfusionEnchantmentHelper.canApply(catalyst, enchantment) && InfusionEnchantmentHelper.level(catalyst, enchantment) < enchantment.maxLevel()
                && matchComponents(input.components()) != null;
    }

    public AspectList scaledAspects(ItemStack catalyst) {
        int next = InfusionEnchantmentHelper.level(catalyst, enchantment) + 1;
        if (next > enchantment.maxLevel()) {
            return AspectList.EMPTY;
        }
        int others = InfusionEnchantmentHelper.list(catalyst).size() - (InfusionEnchantmentHelper.has(catalyst, enchantment) ? 1 : 0);
        float modifier = next + OTHER_ENCHANTMENT_WEIGHT * others;
        AspectList scaled = AspectList.EMPTY;
        for (AspectInstance entry : aspects.entries()) {
            int amount = (int) (entry.amount() * modifier);
            if (amount > 0) {
                scaled = scaled.add(entry.aspect(), amount);
            }
        }
        return scaled;
    }

    @Override
    public AspectList jobEssentia(InfusionInput input) {
        return scaledAspects(input.catalyst());
    }

    @Override
    public ItemStack jobResult(InfusionInput input, RandomSource random) {
        return enchantedResult(input.catalyst(), random);
    }

    @Override
    public boolean exactResult() {
        return false;
    }

    public ItemStack enchantedResult(ItemStack catalyst, RandomSource random) {
        ItemStack result = catalyst.copyWithCount(1);
        int current = InfusionEnchantmentHelper.level(result, enchantment);
        if (current >= enchantment.maxLevel()) {
            return result;
        }
        if (random.nextInt(WARP_ROLL_BOUND) < InfusionEnchantmentHelper.list(result).size()) {
            result.set(TTDataComponents.STACK_WARP.get(), result.getOrDefault(TTDataComponents.STACK_WARP.get(), 0) + 1);
        }
        InfusionEnchantmentHelper.add(result, enchantment, current + 1);
        return result;
    }

    @Override
    public Ingredient catalyst() {
        return displayCatalyst;
    }

    @Override
    public List<Ingredient> components() {
        return components;
    }

    @Override
    public AspectList aspects() {
        return aspects;
    }

    @Override
    public int instability() {
        return INSTABILITY;
    }

    @Override
    public ItemStack resultItem() {
        ItemStack display = displayCatalyst.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        if (!display.isEmpty()) {
            InfusionEnchantmentHelper.add(display, enchantment, DISPLAY_LEVEL);
        }
        return display;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack out = resultItem();
        SlotDisplay resultDisplay = out.isEmpty() ? SlotDisplay.Empty.INSTANCE : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(out));
        List<SlotDisplay> componentDisplays = components.stream().map(Ingredient::display).map(display -> (SlotDisplay) display).toList();
        return List.of(new InfusionRecipeDisplay(displayCatalyst.display(), componentDisplays, aspects, INSTABILITY, resultDisplay));
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        ItemStack result = input.catalyst().copyWithCount(1);
        InfusionEnchantmentHelper.add(result, enchantment, InfusionEnchantmentHelper.level(result, enchantment) + 1);
        return result;
    }

    @Override
    public RecipeSerializer<InfusionEnchantmentRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionEnchantmentRecipe> getType() {
        return TTRecipeTypes.INFUSION_ENCHANTMENT.get();
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

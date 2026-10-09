package com.leclowndu93150.thaumaturge.content.recipe.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGated;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class CrucibleRecipe implements Recipe<CrucibleRecipeInput>, ResearchGated {
    public static final MapCodec<CrucibleRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i
                    .group(Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst), AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result), ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research))
                    .apply(i, CrucibleRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst, AspectList.STREAM_CODEC,
            r -> r.aspects, ItemStackTemplate.STREAM_CODEC, r -> r.result, ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, CrucibleRecipe::new);

    public static final RecipeSerializer<CrucibleRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Optional<ResearchGate> research;
    private final ItemStackTemplate result;
    private final AspectList aspects;
    private final Ingredient catalyst;

    public CrucibleRecipe(Ingredient catalyst, AspectList aspects, ItemStackTemplate result, Optional<ResearchGate> research) {
        this.research = research;
        this.result = result;
        this.aspects = aspects;
        this.catalyst = catalyst;
    }

    @Override
    public boolean matches(CrucibleRecipeInput input, Level level) {
        return catalyst.test(input.catalyst()) && covers(input.availableAspects());
    }

    private boolean covers(AspectList stock) {
        return !stock.isEmpty() && aspects.entries().stream().allMatch(need -> stock.amountOf(need.aspect()) >= need.amount());
    }

    public AspectList removeMatching(AspectList available) {
        return aspects.entries().stream().reduce(available, (left, need) -> left.remove(need.aspect(), need.amount()), (first, second) -> second);
    }

    @Override
    public ItemStack assemble(CrucibleRecipeInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CrucibleRecipeInput>> getSerializer() {
        return TTRecipeSerializers.CRUCIBLE.get();
    }

    @Override
    public RecipeType<? extends Recipe<CrucibleRecipeInput>> getType() {
        return TTRecipeTypes.CRUCIBLE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CrucibleRecipeDisplay(catalyst.display(), aspects, new SlotDisplay.ItemSlotDisplay(result.item())));
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    public Ingredient catalyst() {
        return catalyst;
    }

    public AspectList aspects() {
        return aspects;
    }

    public ItemStackTemplate rawResult() {
        return result;
    }
}

package com.leclowndu93150.thaumcraft.content.infusion;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
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
import net.minecraft.world.level.Level;

public final class InfusionRecipe implements Recipe<InfusionInput>, IInfusionRecipe {
    public static final MapCodec<InfusionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst),
            Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(r -> r.components),
            AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
            Codec.intRange(0, 100).optionalFieldOf("instability", 0).forGetter(r -> r.instability),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)
    ).apply(i, InfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.catalyst,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            r -> r.components,
            AspectList.STREAM_CODEC,
            r -> r.aspects,
            ByteBufCodecs.VAR_INT,
            r -> r.instability,
            ItemStackTemplate.STREAM_CODEC,
            r -> r.result,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
            r -> r.research,
            InfusionRecipe::new
    );

    public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient catalyst;
    private final List<Ingredient> components;
    private final AspectList aspects;
    private final int instability;
    private final ItemStackTemplate result;
    private final Optional<ResearchGate> research;

    public InfusionRecipe(Ingredient catalyst, List<Ingredient> components, AspectList aspects,
                          int instability, ItemStackTemplate result, Optional<ResearchGate> research) {
        this.catalyst = catalyst;
        this.components = List.copyOf(components);
        this.aspects = aspects;
        this.instability = instability;
        this.result = result;
        this.research = research;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        if (!catalyst.test(input.catalyst())) {
            return false;
        }
        return matchComponents(input.components()) != null;
    }

    public List<ItemStack> matchComponents(List<ItemStack> available) {
        if (available.size() != components.size()) {
            return null;
        }
        List<ItemStack> remaining = new ArrayList<>(available);
        List<ItemStack> consumed = new ArrayList<>(components.size());
        for (Ingredient component : components) {
            ItemStack found = null;
            for (ItemStack candidate : remaining) {
                if (component.test(candidate)) {
                    found = candidate;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            remaining.remove(found);
            consumed.add(found.copyWithCount(1));
        }
        return consumed;
    }

    @Override
    public Ingredient catalyst() {
        return catalyst;
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
        return instability;
    }

    @Override
    public ItemStack resultItem() {
        return result.create();
    }

    public ItemStackTemplate rawResult() {
        return result;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<InfusionRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionRecipe> getType() {
        return TCRecipeTypes.INFUSION.get();
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

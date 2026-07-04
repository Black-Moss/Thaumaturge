package com.leclowndu93150.thaumcraft.content.recipe.workbench;

import com.google.common.annotations.VisibleForTesting;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class ArcaneShapedCraftingRecipe extends ArcaneCraftingRecipe{

    public static final MapCodec<ArcaneShapedCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((i) ->
            i.group(
                    CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
                    ExtraCodecs.POSITIVE_INT.fieldOf("vis").forGetter(o->o.vis),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(o->o.gate),
                    ArcaneCraftingRecipe.PRIMAL_ASPECTS_CODEC.fieldOf("crystals").forGetter(o->o.aspects),
                    ArcaneShapedRecipePattern.MAP_CODEC.forGetter((o) -> o.pattern),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter((o) -> o.result)
            ).apply(i, ArcaneShapedCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapedCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, (o) -> o.commonInfo,
            ByteBufCodecs.VAR_INT, (o) -> o.vis,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), o -> o.gate,
            AspectList.STREAM_CODEC, o -> o.aspects,
            ArcaneShapedRecipePattern.STREAM_CODEC, (o) -> o.pattern,
            ItemStackTemplate.STREAM_CODEC, (o) -> o.result,
            ArcaneShapedCraftingRecipe::new);

    public static final RecipeSerializer<ArcaneShapedCraftingRecipe> SERIALIZER= new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);
    public final ArcaneShapedRecipePattern pattern;
    private final ItemStackTemplate result;

    public ArcaneShapedCraftingRecipe(Recipe.CommonInfo commonInfo, int vis, Optional<ResearchGate> researchGate, AspectList aspects, ArcaneShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo,vis,researchGate,aspects);
        this.pattern = pattern;
        this.result = result;
    }

    public RecipeSerializer<? extends ArcaneCraftingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @VisibleForTesting
    public List<Optional<Ingredient>> getIngredients() {
        return this.pattern.ingredients();
    }

    public ItemStackTemplate result() {
        return this.result;
    }

    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    public boolean matches(ArcaneCraftingInput input, Level level) {
        return super.matches(input, level) && this.pattern.matches(input);
    }

    public ItemStack assemble(ArcaneCraftingInput input) {
        return this.result.create();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(this.pattern.width(), this.pattern.height(), this.pattern.ingredients().stream().map((e) -> (SlotDisplay)e.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(), new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}

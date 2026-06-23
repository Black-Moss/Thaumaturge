package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class DustTriggerCategory implements IRecipeCategory<RecipeHolder<DustTrigger>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(TCIds.MODID, "dust_trigger");
    public static final IRecipeHolderType<DustTrigger> RECIPE_TYPE = IRecipeHolderType.create(UID);

    private static final int WIDTH = 144;
    private static final int HEIGHT = 54;

    private static final int DUST_SLOT_X = 6;
    private static final int DUST_SLOT_Y = 18;
    private static final int TARGET_SLOT_X = 56;
    private static final int TARGET_SLOT_Y = 18;
    private static final int RESULT_SLOT_X = 118;
    private static final int RESULT_SLOT_Y = 18;

    private final IDrawable icon;

    public DustTriggerCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.SALIS_MUNDUS.get()));
    }

    @Override
    public IRecipeType<RecipeHolder<DustTrigger>> getRecipeType() {
        return RECIPE_TYPE;
    }

    public static IRecipeType<RecipeHolder<DustTrigger>> type() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumcraft.category.dust_trigger");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DustTrigger> holder, IFocusGroup focuses) {
        builder.setShapeless();

        Component usage = Component.translatable("jei.thaumcraft.dust_trigger.usage");
        builder.addSlot(RecipeIngredientRole.INPUT, DUST_SLOT_X + 1, DUST_SLOT_Y + 1)
                .add(TCItems.SALIS_MUNDUS.get())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(usage));

        DustTrigger recipe = holder.value();
        IRecipeSlotBuilder targetSlot = builder.addSlot(RecipeIngredientRole.CRAFTING_STATION,
                TARGET_SLOT_X + 1, TARGET_SLOT_Y + 1);
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            targetSlot.add(new ItemStack(simple.target()));
        } else if (recipe instanceof DustTriggerTagRecipe tagRecipe) {
            TagKey<Block> tag = tagRecipe.targetTag();
            List<ItemStack> stacks = stacksFromBlockTag(tag);
            if (!stacks.isEmpty()) {
                targetSlot.addItemStacks(stacks);
            }
            Component tagLabel = Component.translatable(
                    "jei.thaumcraft.dust_trigger.target.tag",
                    Component.literal("#" + tag.location()));
            targetSlot.addRichTooltipCallback((view, tooltip) -> tooltip.add(tagLabel));
        } else if (recipe instanceof DustTriggerMultiblockRecipe multi) {
            Component blueprintLabel = Component.translatable(
                    "jei.thaumcraft.dust_trigger.target.multiblock",
                    Component.literal(multi.blueprintId().toString()));
            targetSlot.addRichTooltipCallback((view, tooltip) -> tooltip.add(blueprintLabel));
        }

        ItemStack result = resultStack(recipe);
        if (!result.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1)
                    .add(result);
        }
    }

    private static List<ItemStack> stacksFromBlockTag(TagKey<Block> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            ItemStack stack = new ItemStack(blockHolder.value());
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static @Nullable TagKey<Block> targetTag(DustTrigger recipe) {
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return tag.targetTag();
        }
        return null;
    }

    private static ItemStack resultStack(DustTrigger recipe) {
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            return simple.result();
        }
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return tag.result();
        }
        if (recipe instanceof DustTriggerMultiblockRecipe multi) {
            return multi.result();
        }
        return ItemStack.EMPTY;
    }
}

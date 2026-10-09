package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviewRenderState;
import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.mixin.client.gui.GuiGraphicsExtractorAccessor;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public final class MultiblockCategory implements IRecipeCategory<RecipeHolder<DustTrigger>> {
    public static final IRecipeHolderType<DustTrigger> RECIPE_TYPE = IRecipeHolderType.create(TTIds.rl("multiblock_dust_trigger"));

    private static final int PANEL_HEIGHT = 108;
    private static final int PANEL_WIDTH = 144;
    private static final Identifier OVERLAY_TEXTURE = TTIds.rl("textures/gui/gui_researchbook_overlay.png");

    private static final IDrawable arrow = new AlphaDrawable(OVERLAY_TEXTURE, 199, 168, 26, 26);
    private static final IDrawable resultIcon = new AlphaDrawable(OVERLAY_TEXTURE, 41, 7, 30, 30);

    private static final int ARROW_X = PANEL_WIDTH / 2 - arrow.getWidth() / 2 - 20;
    private static final int ARROW_LOCK_OFFSET_X = 6;
    private static final int ARROW_LOCK_Y = 4;
    private static final int LOCK_HIT_LEFT = PANEL_WIDTH / 2 - arrow.getWidth() / 2 - 14;
    private static final int LOCK_HIT_RIGHT = PANEL_WIDTH / 2 - arrow.getWidth() / 2 + 4;
    private static final int LOCK_HIT_TOP = 4;
    private static final int LOCK_HIT_BOTTOM = 20;
    private static final int DUST_SLOT_X = ARROW_X - 18;
    private static final int DUST_SLOT_Y = -3;
    private static final int RESULT_SLOT_X = 118;
    private static final int RESULT_SLOT_Y = PANEL_HEIGHT / 2 - 9;
    private static final int RESULT_FRAME_MARGIN = 6;
    private static final int INPUT_ROW_X = 5;
    private static final int INPUT_SLOT_STRIDE = 20;
    private static final int INPUT_ROW_BOTTOM_INSET = 20;
    private static final int PREVIEW_SCALE = 25;
    private static final float ROTATION_DIVISOR = 8F;
    private static final float PREVIEW_YAW_OFFSET = 90F;
    private static final int PREVIEW_PITCH = 15;
    private static final int PREVIEW_LEFT_SHIFT = 35;
    private static final int PREVIEW_TOP_SHIFT = 5;

    private final IDrawable icon;
    private int rotation = 0;

    public MultiblockCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TTItems.SALIS_MUNDUS.get()));
    }

    public static IRecipeType<RecipeHolder<DustTrigger>> type() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<RecipeHolder<DustTrigger>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.multiblock_dust_trigger");
    }

    @Override
    public int getWidth() {
        return PANEL_WIDTH;
    }

    @Override
    public int getHeight() {
        return PANEL_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DustTrigger> holder, IFocusGroup focuses) {
        addDustSlot(builder);
        Plan plan = Plan.of(holder.value());
        if (plan.showsResult()) {
            addResultSlot(builder, plan.result());
        }
        addRequirementSlots(builder, plan.requirementsLargestFirst());
    }

    private static void addDustSlot(IRecipeLayoutBuilder builder) {
        Component hint = Component.translatable("jei.thaumaturge.dust_trigger.target.multiblock");
        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, DUST_SLOT_X + 1, DUST_SLOT_Y + 1);
        slot.add(TTItems.SALIS_MUNDUS.get());
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(hint));
    }

    private static void addResultSlot(IRecipeLayoutBuilder builder, ItemStack result) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1).add(result);
    }

    private static void addRequirementSlots(IRecipeLayoutBuilder builder, List<Object2IntMap.Entry<BlueprintSource>> requirements) {
        int column = 0;
        for (Object2IntMap.Entry<BlueprintSource> requirement : requirements) {
            int slotX = INPUT_ROW_X + INPUT_SLOT_STRIDE * column++;
            builder.addInputSlot(slotX, PANEL_HEIGHT - INPUT_ROW_BOTTOM_INSET).addItemStacks(stacksOf(requirement));
        }
    }

    private static List<ItemStack> stacksOf(Object2IntMap.Entry<BlueprintSource> requirement) {
        int count = requirement.getIntValue();
        return requirement.getKey().getRepresentations().stream().map(representation -> representation.copyWithCount(count)).collect(Collectors.toList());
    }

    @Override
    public void draw(RecipeHolder<DustTrigger> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        DustTrigger shown = holder.value();
        Plan plan = Plan.of(shown);
        if (plan.showsResult()) {
            resultIcon.draw(guiGraphics, RESULT_SLOT_X - RESULT_FRAME_MARGIN, RESULT_SLOT_Y - RESULT_FRAME_MARGIN);
        }
        arrow.draw(guiGraphics, ARROW_X, 0);
        drawLock(guiGraphics, shown);
        drawPreview(guiGraphics, plan);
    }

    private static void drawLock(GuiGraphicsExtractor guiGraphics, DustTrigger trigger) {
        if (trigger.doesPassGate(Minecraft.getInstance().player)) {
            return;
        }
        guiGraphics.item(Items.BARRIER.getDefaultInstance(), ARROW_X + ARROW_LOCK_OFFSET_X, ARROW_LOCK_Y);
    }

    private void drawPreview(GuiGraphicsExtractor guiGraphics, Plan plan) {
        if (plan.blueprint() == null) {
            return;
        }
        Matrix3x2f offset = guiGraphics.pose();
        submitPreview(guiGraphics, plan.previewBlocks(), Math.round(offset.m20), Math.round(offset.m21));
        rotation++;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<DustTrigger> holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (!isOverLock(mouseX, mouseY)) {
            return;
        }
        if (holder.value().doesPassGate(Minecraft.getInstance().player)) {
            return;
        }
        tooltip.addAll(ResearchUtils.generateMissingResearchList(holder.value().researchGate().get()));
    }

    private static boolean isOverLock(double mouseX, double mouseY) {
        boolean insideX = LOCK_HIT_LEFT < mouseX && mouseX < LOCK_HIT_RIGHT;
        boolean insideY = LOCK_HIT_TOP < mouseY && mouseY < LOCK_HIT_BOTTOM;
        return insideX && insideY;
    }

    private void submitPreview(GuiGraphicsExtractor guiGraphics, Map<BlockPos, BlockState> blocks, int originX, int originY) {
        float yaw = rotation / ROTATION_DIVISOR + PREVIEW_YAW_OFFSET;
        int left = originX - PREVIEW_LEFT_SHIFT;
        int top = originY + PREVIEW_TOP_SHIFT;
        int right = originX + PANEL_WIDTH;
        int bottom = originY + PANEL_HEIGHT;
        BlockPreviewRenderState preview = new BlockPreviewRenderState(blocks, PREVIEW_SCALE, yaw, 1, PREVIEW_PITCH, 0, 0, left, top, right, bottom, null);
        GuiGraphicsExtractorAccessor accessor = (GuiGraphicsExtractorAccessor) guiGraphics;
        accessor.thaumaturge$getGuiRenderState().addPicturesInPictureState(preview);
    }

    private static @Nullable Blueprint lookupBlueprint(Identifier blueprintId) {
        RegistryAccess access = ThaumaturgeJEIPlugin.clientRegistryAccess();
        if (access == null) {
            return null;
        }
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId);
        return access.lookup(Blueprint.REGISTRY_KEY).flatMap(registry -> registry.get(key)).map(Holder::value).orElse(null);
    }

    private record Cell(int x, int y, int z, BlueprintPart part) {
    }

    private record Plan(@Nullable Blueprint blueprint, ItemStack result) {
        static Plan of(DustTrigger trigger) {
            Blueprint found = lookupBlueprint(((DustTriggerMultiblockRecipe) trigger).blueprintId());
            return new Plan(found, DustTriggerCategory.resultStack(trigger));
        }

        boolean showsResult() {
            return !result.isEmpty() && !keepsResult();
        }

        boolean keepsResult() {
            return cells().filter(cell -> cell.part().target() instanceof BlueprintTarget.Keep).flatMap(cell -> cell.part().source().getRepresentations().stream())
                    .anyMatch(representation -> ItemStack.isSameItemSameComponents(result, representation));
        }

        Stream<Cell> cells() {
            return Optional.ofNullable(blueprint).stream().flatMap(Plan::cellsOf);
        }

        private static Stream<Cell> cellsOf(Blueprint blueprint) {
            return IntStream.range(0, blueprint.ySize()).boxed().flatMap(y -> columnsOf(blueprint, y));
        }

        private static Stream<Cell> columnsOf(Blueprint blueprint, int y) {
            return IntStream.range(0, blueprint.xSize()).boxed().flatMap(x -> IntStream.range(0, blueprint.zSize()).mapToObj(z -> cellAt(blueprint, x, y, z)).flatMap(Optional::stream));
        }

        private static Optional<Cell> cellAt(Blueprint blueprint, int x, int y, int z) {
            return Optional.ofNullable(blueprint.cell(y, x, z)).map(part -> new Cell(x, y, z, part));
        }

        Map<BlockPos, BlockState> previewBlocks() {
            int topLayer = blueprint.ySize() - 1;
            return cells().collect(Collectors.toMap(cell -> new BlockPos(cell.x(), topLayer - cell.y(), cell.z()), cell -> cell.part().source().getState()));
        }

        List<Object2IntMap.Entry<BlueprintSource>> requirementsLargestFirst() {
            Object2IntOpenHashMap<BlueprintSource> tally = new Object2IntOpenHashMap<>();
            cells().map(cell -> cell.part().source()).filter(source -> !source.getRepresentations().isEmpty()).forEach(source -> tally.addTo(source, 1));
            List<Object2IntMap.Entry<BlueprintSource>> entries = new ArrayList<>(tally.object2IntEntrySet());
            entries.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));
            Collections.reverse(entries);
            return entries;
        }
    }
}

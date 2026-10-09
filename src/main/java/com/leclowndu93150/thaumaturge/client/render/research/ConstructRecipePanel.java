package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviews;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockRecipeDisplay;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

final class ConstructRecipePanel implements RecipePanel {
    private static final String LABEL_KEY = "gui.thaumaturge.recipe_type.construct";
    private static final int PREVIEW_OFFSET_Y = -12;
    private static final int PREVIEW_WIDTH = 96;
    private static final int PREVIEW_HEIGHT = 100;
    private static final float PREVIEW_SCALE = 16.0F;
    private static final int STRIP_X = -85;
    private static final int STRIP_STRIDE = 17;
    private static final int STRIP_Y = 90;
    private static final int ALL_LAYERS = -1;
    private static final int BOOKMARK_CENTER = 8;
    private static final int BOOKMARK_SIZE = 16;
    private static final float BOOKMARK_ROTATION = -35.0F;

    private final MultiblockRecipeDisplay display;

    ConstructRecipePanel(MultiblockRecipeDisplay display) {
        this.display = display;
    }

    static int layerCount(MultiblockRecipeDisplay display) {
        Blueprint blueprint = blueprint(display.blueprint());
        return blueprint == null ? 0 : blueprint.ySize();
    }

    static void renderBookmark(GuiGraphicsExtractor graphics, int x, int y, MultiblockRecipeDisplay display) {
        BlockPreviews.render(graphics, x + BOOKMARK_CENTER, y + BOOKMARK_CENTER, blocks(blueprint(display.blueprint())), BOOKMARK_SIZE, BOOKMARK_SIZE, PREVIEW_SCALE, BOOKMARK_ROTATION, ALL_LAYERS);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
        context.drawLabel(graphics, LABEL_KEY);
        context.drawOutputFrame(graphics);
        BlockPreviews.render(graphics, context.cx, context.cy + PREVIEW_OFFSET_Y, blocks(blueprint(display.blueprint())), PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_SCALE, rotation, layer);
        for (RecipeDisplayWidget.ItemHit hit : hits(context)) {
            context.drawItem(graphics, hit);
        }
    }

    @Override
    public List<RecipeDisplayWidget.ItemHit> hits(PanelContext context) {
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        context.collect(hits, context.first(display.result()), PanelContext.OUTPUT_X, PanelContext.OUTPUT_Y);
        hits.addAll(strip(context));
        return hits;
    }

    @Override
    public @Nullable List<Component> popup(PanelContext context, double mouseX, double mouseY) {
        return null;
    }

    private List<RecipeDisplayWidget.ItemHit> strip(PanelContext context) {
        Blueprint blueprint = blueprint(display.blueprint());
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        if (blueprint == null) {
            return hits;
        }
        Map<BlueprintSource, Integer> counts = new LinkedHashMap<>();
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null && !part.source().getRepresentations().isEmpty()) {
                        counts.merge(part.source(), 1, Integer::sum);
                    }
                }
            }
        }
        List<Map.Entry<BlueprintSource, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Map.Entry.<BlueprintSource, Integer>comparingByValue().reversed());
        for (int i = 0; i < sorted.size(); i++) {
            ItemStack stack = sorted.get(i).getKey().getRepresentations().get(0).copy();
            stack.setCount(sorted.get(i).getValue());
            hits.add(context.hit(stack, STRIP_X + STRIP_STRIDE * i, STRIP_Y));
        }
        return hits;
    }

    private static @Nullable Blueprint blueprint(Identifier id) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return level.registryAccess().lookup(Blueprint.REGISTRY_KEY).flatMap(lookup -> lookup.get(ResourceKey.create(Blueprint.REGISTRY_KEY, id))).map(Holder.Reference::value).orElse(null);
    }

    private static Map<BlockPos, BlockState> blocks(@Nullable Blueprint blueprint) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        if (blueprint == null) {
            return blocks;
        }
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null && !part.source().getState().isAir()) {
                        blocks.put(new BlockPos(x, blueprint.ySize() - y - 1, z), part.source().getState());
                    }
                }
            }
        }
        return blocks;
    }
}

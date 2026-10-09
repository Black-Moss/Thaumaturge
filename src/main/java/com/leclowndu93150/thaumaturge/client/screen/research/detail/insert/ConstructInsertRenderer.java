package com.leclowndu93150.thaumaturge.client.screen.research.detail.insert;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviews;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.PreviewController;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ConstructInsertRenderer {
    private static final String TAG_PREFIX = "#";
    private static final long TAG_CYCLE_TICKS = 20L;
    private static final int TITLE_Y = 26;
    private static final int TITLE_COLOR = 0xFF505050;
    private static final int PREVIEW_CENTER_Y = 118;
    private static final int PREVIEW_WIDTH = 96;
    private static final int PREVIEW_HEIGHT = 100;
    private static final float PREVIEW_MAX_SCALE = 16.0F;
    private static final int CONTROLS_CENTER_Y = 130;
    private static final float WAND_U = 136.0F;
    private static final float WAND_V = 152.0F;
    private static final int WAND_SIZE = 24;
    private static final int WAND_Y = 174;
    private static final int WAND_GAP = 2;
    private static final float WAND_ALPHA = 0.4F;
    private static final int OVERLAY_TEXTURE_SIZE = 512;
    private static final int COST_Y = 182;
    private static final int COST_STRIDE = 18;

    private ConstructInsertRenderer() {}

    public static boolean render(GuiGraphicsExtractor graphics, DetailFrame frame, IResearchStage stage, PreviewController preview, int mouseX, int mouseY) {
        ResearchConstruct construct = stage.construct().orElse(null);
        if (construct == null) {
            return false;
        }
        int paperTop = frame.paperTop();
        BookBlit.paper(graphics, frame.paperLeft(), paperTop);
        Font font = frame.font();
        int centerX = frame.paperLeft() + BookSprites.PAPER_SIZE / 2;
        int titleY = paperTop + TITLE_Y;
        Component title = Component.translatable("gui.thaumaturge.recipe_type.construct");
        graphics.text(font, title, centerX - font.width(title) / 2, titleY, TITLE_COLOR, false);
        BlockPreviews.render(graphics, centerX, paperTop + PREVIEW_CENTER_Y, constructBlocks(construct, frame.gameTime()), PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MAX_SCALE, preview.currentRotation(),
                preview.visibleLayer());
        preview.configure(graphics, font, centerX, paperTop + CONTROLS_CENTER_Y, construct.ySize());
        AspectList cost = construct.cost();
        if (!cost.isEmpty()) {
            drawCost(graphics, frame, cost, centerX, titleY, mouseX, mouseY);
        }
        return true;
    }

    private static void drawCost(GuiGraphicsExtractor graphics, DetailFrame frame, AspectList cost, int centerX, int titleY, int mouseX, int mouseY) {
        List<AspectInstance> entries = cost.entries();
        int rowWidth = COST_STRIDE * (entries.size() - 1) + BookSprites.SLOT_SIZE;
        int rowLeft = centerX - rowWidth / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK_OVERLAY, rowLeft - WAND_SIZE - WAND_GAP, titleY + WAND_Y, WAND_U, WAND_V, WAND_SIZE, WAND_SIZE, WAND_SIZE, WAND_SIZE,
                OVERLAY_TEXTURE_SIZE, OVERLAY_TEXTURE_SIZE, BookBlit.alphaTint(WAND_ALPHA));
        for (int index = 0; index < entries.size(); index++) {
            AspectInstance entry = entries.get(index);
            int tileX = rowLeft + COST_STRIDE * index;
            int tileY = titleY + COST_Y;
            AspectTagRenderer.render(graphics, frame.font(), tileX, tileY, entry.aspect(), entry.amount());
            if (Rect.inside(tileX, tileY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(frame.font(), AspectComponents.name(entry.aspect()), mouseX, mouseY);
            }
        }
    }

    public static Map<BlockPos, BlockState> constructBlocks(ResearchConstruct construct, long gameTime) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int cellIndex = 0;
        for (int y = 0; y < construct.ySize(); y++) {
            for (int z = construct.zSize() - 1; z >= 0; z--) {
                for (int x = construct.xSize() - 1; x >= 0; x--) {
                    ItemStack stack = resolveCell(construct.cells().get(cellIndex++), gameTime);
                    Block block = Block.byItem(stack.getItem());
                    if (block != Blocks.AIR) {
                        blocks.put(new BlockPos(x, construct.ySize() - y - 1, z), block.defaultBlockState());
                    }
                }
            }
        }
        return blocks;
    }

    private static ItemStack resolveCell(String spec, long gameTime) {
        if (spec.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!spec.startsWith(TAG_PREFIX)) {
            return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(spec)));
        }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(spec.substring(TAG_PREFIX.length())));
        List<Holder<Item>> members = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(members::add);
        if (members.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(members.get((int) (gameTime / TAG_CYCLE_TICKS % members.size())));
    }
}

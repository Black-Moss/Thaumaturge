package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.client.screen.casters.SpellPartIcons;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellAnalyzer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;

public final class NodeInspector {
    public static final int LABEL_STEP = 11;
    private static final int CARD_H = 26;
    private static final int CARD_STEP = 31;
    private static final int CARD_ICON = 13;
    private static final int CARD_BACK = 22;
    private static final int CARD_GLYPH = 14;
    private static final int CARD_TEXT_X = 27;
    private static final int CARD_NAME_Y = 4;
    private static final int CARD_KIND_Y = 15;
    private static final int ASPECT_COLUMNS = 6;
    private static final int ASPECT_PITCH_X = 21;
    private static final int ASPECT_PITCH_Y = 20;
    private static final int ASPECT_ICON = 16;
    private static final int ASPECT_TEXTURE = 32;
    private static final int ASPECT_SELECT_OFFSET = 3;
    private static final int ASPECT_TAIL = 2;
    private static final int ROW_STEP = 15;
    private static final int ROW_TEXT_Y = 2;
    private static final int SPIN = 12;
    private static final int SPIN_GAP = 1;
    private static final int VALUE_MIN_W = 24;
    private static final int VALUE_PAD = 6;
    private static final int COST_STEP = 13;
    private static final int COST_TEXT_Y = 1;
    private static final int DIVIDER_STEP = 6;

    private final DraftHost session;
    private final List<StepHit> stepHits = new ArrayList<>();
    private final List<AspectHit> aspectHits = new ArrayList<>();

    public NodeInspector(DraftHost session) {
        this.session = session;
    }

    public int render(GuiGraphicsExtractor graphics, FocalLayout layout, int mouseX, int mouseY) {
        aspectHits.clear();
        stepHits.clear();
        Pane pane = new Pane(graphics, layout.rightX0(), layout.rightX1(), mouseX, mouseY);
        int cardY = layout.bodyY0() + LABEL_STEP;
        pane.label(session.font(), SpellText.inspectorLabel("selected"), layout.bodyY0());
        FocalDraw.field(graphics, pane.left(), cardY, FocalLayout.RIGHT_W, CARD_H);
        Selection selection = session.selection();
        SpellNode node = SpellPaths.at(session.draft().root(), selection.path());
        Optional<SpellPart> part = selection.ghost() ? Optional.empty() : session.part(node.part());
        int y = cardY + CARD_STEP;
        if (part.isPresent()) {
            drawCard(pane, cardY, node, part.get());
            y = drawBody(pane, y, node, part.get());
        } else {
            FocalDraw.text(graphics, session.font(), SpellText.emptySlot(), pane.left() + CARD_TEXT_X, cardY + CARD_NAME_Y, FocalColors.GREY);
        }
        FocalDraw.sprite(graphics, FocalSprites.DIVIDER, pane.left(), y, FocalLayout.RIGHT_W, FocalSprites.DIVIDER_H);
        return y + DIVIDER_STEP;
    }

    private void drawCard(Pane pane, int cardY, SpellNode node, SpellPart part) {
        Font font = session.font();
        int iconX = pane.left() + CARD_ICON;
        int iconY = cardY + CARD_ICON;
        SpellPartIcons.draw(pane.graphics(), session.registries(), node, iconX, iconY, CARD_BACK, CARD_GLYPH, CARD_BACK, 1.0F);
        int textX = pane.left() + CARD_TEXT_X;
        FocalDraw.text(pane.graphics(), font, SpellText.partName(node.part()), textX, cardY + CARD_NAME_Y, FocalColors.WHITE);
        FocalDraw.text(pane.graphics(), font, kindLine(node, part), textX, cardY + CARD_KIND_Y, FocalColors.GREY);
    }

    private Component kindLine(SpellNode node, SpellPart part) {
        if (!part.aspect().selectable()) {
            return SpellText.kind(part.kind());
        }
        return part.aspect().resolve(node.aspect(), session.registries()).map(key -> SpellText.kindWithAspect(part.kind(), key)).orElseGet(() -> SpellText.kind(part.kind()));
    }

    private int drawBody(Pane pane, int startY, SpellNode node, SpellPart part) {
        int y = part.aspect().selectable() ? aspectGrid(pane, startY, node, part) : startY;
        for (SettingSpec spec : part.settings()) {
            settingRow(pane, y, node, spec);
            y += ROW_STEP;
        }
        int cost = Math.round(SpellAnalyzer.nodeComplexity(node, part, session.registries()));
        int costY = y + COST_TEXT_Y;
        FocalDraw.text(pane.graphics(), session.font(), SpellText.inspectorLabel("node_cost"), pane.left(), costY, FocalColors.GREY);
        FocalDraw.textRight(pane.graphics(), session.font(), Component.literal(Integer.toString(cost)), pane.right(), costY, FocalColors.WHITE);
        return y + COST_STEP;
    }

    private List<Holder.Reference<IAspect>> discoveredAspects(SpellPart part) {
        return part.aspect().options(session.registries()).stream().flatMap(key -> session.registries().lookupOrThrow(IAspect.REGISTRY_KEY).get(key).stream())
                .filter(holder -> AspectPoolAccess.isDiscovered(session.player(), holder)).toList();
    }

    private int aspectGrid(Pane pane, int startY, SpellNode node, SpellPart part) {
        pane.label(session.font(), SpellText.inspectorLabel("aspect"), startY);
        int gridY = startY + LABEL_STEP;
        List<Holder.Reference<IAspect>> options = discoveredAspects(part);
        Optional<ResourceKey<IAspect>> chosen = part.aspect().resolve(node.aspect(), session.registries());
        int slot = 0;
        for (Holder.Reference<IAspect> option : options) {
            int cellX = pane.left() + slot % ASPECT_COLUMNS * ASPECT_PITCH_X;
            int cellY = gridY + slot / ASPECT_COLUMNS * ASPECT_PITCH_Y;
            boolean picked = chosen.map(option.key()::equals).orElse(false);
            drawAspectCell(pane, option, cellX, cellY, picked);
            slot++;
        }
        int rowCount = Math.max(1, Math.ceilDiv(options.size(), ASPECT_COLUMNS));
        return gridY + rowCount * ASPECT_PITCH_Y + ASPECT_TAIL;
    }

    private void drawAspectCell(Pane pane, Holder.Reference<IAspect> option, int cellX, int cellY, boolean selected) {
        GuiGraphicsExtractor graphics = pane.graphics();
        IAspect value = option.value();
        FocalDraw.sprite(graphics, FocalSprites.SLOT, cellX, cellY, FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, value.texture(), cellX + 1, cellY + 1, 0.0F, 0.0F, ASPECT_ICON, ASPECT_ICON, ASPECT_TEXTURE, ASPECT_TEXTURE, ASPECT_TEXTURE, ASPECT_TEXTURE,
                ARGB.opaque(value.color()));
        if (selected) {
            int ringX = cellX - ASPECT_SELECT_OFFSET;
            int ringY = cellY - ASPECT_SELECT_OFFSET;
            FocalDraw.sprite(graphics, FocalSprites.SELECTION, ringX, ringY, FocalSprites.SELECTION_SIZE, FocalSprites.SELECTION_SIZE);
        }
        aspectHits.add(new AspectHit(option.key(), cellX, cellY));
        if (FocalDraw.over(pane.mouseX(), pane.mouseY(), cellX, cellY, FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE)) {
            graphics.setTooltipForNextFrame(session.font(), SpellText.aspectName(option.key()), pane.mouseX(), pane.mouseY());
        }
    }

    private void settingRow(Pane pane, int y, SpellNode node, SettingSpec spec) {
        FocalDraw.text(pane.graphics(), session.font(), SpellText.setting(spec), pane.left(), y + ROW_TEXT_Y, FocalColors.GREY);
        Component label = spec.label(currentValue(node, spec));
        int labelWidth = session.font().width(label);
        int valueW = Math.max(VALUE_MIN_W, labelWidth + VALUE_PAD);
        int incX = pane.right() - SPIN;
        int valueX = incX - SPIN_GAP - valueW;
        int decX = valueX - SPIN_GAP - SPIN;
        stepButton(pane, decX, y, FocalSprites.GLYPH_LEFT);
        FocalDraw.field(pane.graphics(), valueX, y, valueW, SPIN);
        FocalDraw.text(pane.graphics(), session.font(), label, valueX + (valueW - labelWidth) / 2, y + ROW_TEXT_Y, FocalColors.WHITE);
        stepButton(pane, incX, y, FocalSprites.GLYPH_RIGHT);
        stepHits.add(new StepHit(spec, decX, y, -1));
        stepHits.add(new StepHit(spec, incX, y, 1));
    }

    private void stepButton(Pane pane, int x, int y, Identifier glyph) {
        boolean hovered = FocalDraw.over(pane.mouseX(), pane.mouseY(), x, y, SPIN, SPIN);
        FocalDraw.button(pane.graphics(), x, y, SPIN, SPIN, glyph, FocalSprites.GLYPH_SIZE, hovered, false);
    }

    private static int currentValue(SpellNode node, SettingSpec spec) {
        return spec.clamp(node.settings().getOrDefault(spec.key(), spec.defaultValue()));
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        Selection selection = session.selection();
        if (selection.ghost()) {
            return false;
        }
        Optional<AspectHit> aspectHit = aspectHits.stream().filter(hit -> FocalDraw.over(mouseX, mouseY, hit.x(), hit.y(), FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE)).findFirst();
        if (aspectHit.isPresent()) {
            ResourceKey<IAspect> picked = aspectHit.get().aspect();
            editNode(selection, node -> node.withAspect(Optional.of(picked)));
            return true;
        }
        Optional<StepHit> stepHit = stepHits.stream().filter(hit -> FocalDraw.over(mouseX, mouseY, hit.x(), hit.y(), SPIN, SPIN)).findFirst();
        if (stepHit.isPresent()) {
            SettingSpec spec = stepHit.get().spec();
            int delta = stepHit.get().delta();
            editNode(selection, node -> {
                int current = node.settings().getOrDefault(spec.key(), spec.defaultValue());
                return node.withSetting(spec.key(), spec.offset(current, delta));
            });
            return true;
        }
        return false;
    }

    private void editNode(Selection selection, UnaryOperator<SpellNode> change) {
        session.edit(spell -> spell.withRoot(SpellPaths.update(spell.root(), selection.path(), change)));
        session.playClick();
    }

    private record Pane(GuiGraphicsExtractor graphics, int left, int right, int mouseX, int mouseY) {
        void label(Font font, Component title, int y) {
            FocalDraw.text(graphics, font, title, left, y, FocalColors.LABEL);
        }
    }

    private record StepHit(SettingSpec spec, int x, int y, int delta) {
    }

    private record AspectHit(ResourceKey<IAspect> aspect, int x, int y) {
    }
}

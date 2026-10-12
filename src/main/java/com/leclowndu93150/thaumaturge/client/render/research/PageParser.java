package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public final class PageParser {
    public static final int PAGE_WIDTH = 140;
    public static final int PAGE_HEIGHT = 210;
    public static final int LINE_HEIGHT = 9;

    private static final int BREAK_GAP_DRAWN = 5;
    private static final int BREAK_GAP_BUDGET = 6;
    private static final int IMAGE_GAP = 2;
    private static final int IMAGE_MAX_HEIGHT = PAGE_HEIGHT - IMAGE_GAP;
    private static final int IMAGE_SHEET_SIZE = 256;
    private static final int ORNAMENT_SPACE = 8;
    private static final int ORNAMENT_DRAW_Y = 1;
    private static final int IMAGE_DRAW_Y = 1;
    private static final int HALF = 2;
    private static final int MAX_WHITESPACE = 0x20;

    private static final int BODY_RULE_U = 24;
    private static final int BODY_RULE_V = 184;
    private static final int BODY_RULE_WIDTH = 95;
    private static final int BODY_RULE_HEIGHT = 6;
    private static final int BODY_RULE_OFFSET_X = 22;
    private static final int DIVIDER_U = 28;
    private static final int DIVIDER_V = 192;
    private static final int DIVIDER_WIDTH = 140;
    private static final int DIVIDER_HEIGHT = 6;

    private static final String BREAK_TAG = "<BR>";
    private static final String PAGE_TAG = "<PAGE>";
    private static final String RULE_TAG = "<LINE>";
    private static final String DIVIDER_TAG = "<DIV>";
    private static final String IMAGE_OPEN = "<IMG>";
    private static final String IMAGE_CLOSE = "</IMG>";
    private static final Pattern IMAGE_DESCRIPTOR = Pattern.compile("^\\s*(.+?):(\\d{1,4}):(\\d{1,4}):(\\d{1,4}):(\\d{1,4}):(\\d{0,4}\\.?\\d{1,6})\\s*$");

    private PageParser() {}

    public static String headed(String heading, String text) {
        return heading + BREAK_TAG + text;
    }

    public static List<Page> paginate(Font font, List<String> texts, int firstPageTop, int firstPageBudget) {
        Paginator paginator = new Paginator(font, firstPageTop, firstPageBudget);
        for (int index = 0; index < texts.size(); index++) {
            if (index > 0) {
                paginator.forcedBreak();
            }
            for (Block block : read(texts.get(index))) {
                block.layout(paginator);
            }
        }
        return paginator.finish();
    }

    private static List<Block> read(String text) {
        List<Block> blocks = new ArrayList<>();
        StringBuilder run = new StringBuilder();
        int cursor = 0;
        while (cursor < text.length()) {
            if (text.startsWith(IMAGE_OPEN, cursor)) {
                int close = text.indexOf(IMAGE_CLOSE, cursor + IMAGE_OPEN.length());
                int descriptorEnd = close < 0 ? text.length() : close;
                closeRun(blocks, run);
                blocks.add(imageBlock(text.substring(cursor + IMAGE_OPEN.length(), descriptorEnd)));
                cursor = close < 0 ? text.length() : close + IMAGE_CLOSE.length();
            } else if (text.startsWith(BREAK_TAG, cursor)) {
                if (run.isEmpty()) {
                    blocks.add(Blank.INSTANCE);
                } else {
                    blocks.add(new Paragraph(run.toString(), true));
                    run.setLength(0);
                }
                cursor += BREAK_TAG.length();
            } else if (text.startsWith(PAGE_TAG, cursor)) {
                closeRun(blocks, run);
                blocks.add(PageBreak.INSTANCE);
                cursor += PAGE_TAG.length();
            } else if (text.startsWith(RULE_TAG, cursor)) {
                closeRun(blocks, run);
                blocks.add(Rule.INSTANCE);
                cursor += RULE_TAG.length();
            } else if (text.startsWith(DIVIDER_TAG, cursor)) {
                closeRun(blocks, run);
                blocks.add(Divider.INSTANCE);
                cursor += DIVIDER_TAG.length();
            } else {
                run.append(text.charAt(cursor));
                cursor++;
            }
        }
        closeRun(blocks, run);
        while (!blocks.isEmpty() && blocks.getLast().trailingOrnament()) {
            blocks.removeLast();
        }
        return blocks;
    }

    private static void closeRun(List<Block> blocks, StringBuilder run) {
        if (!run.isEmpty()) {
            blocks.add(new Paragraph(run.toString(), false));
            run.setLength(0);
        }
    }

    private static Block imageBlock(String descriptor) {
        PageImage image = PageImage.parse(descriptor);
        return image != null && image.fitsPage() ? new Picture(image) : Skipped.INSTANCE;
    }

    public interface PageElement {
        void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop);
    }

    public record Page(List<PageElement> elements) {
        public Page {
            elements = List.copyOf(elements);
        }

        public void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop) {
            for (PageElement element : elements) {
                element.draw(graphics, font, columnLeft, pageTop);
            }
        }
    }

    public record PageImage(Identifier texture, int u, int v, int width, int height, float scale) {
        public static @Nullable PageImage parse(String descriptor) {
            Matcher matcher = IMAGE_DESCRIPTOR.matcher(descriptor);
            if (!matcher.matches()) {
                return null;
            }
            Identifier texture = Identifier.tryParse(matcher.group(1).trim());
            if (texture == null) {
                return null;
            }
            return new PageImage(texture, Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)),
                    Float.parseFloat(matcher.group(6)));
        }

        public int renderedWidth() {
            return (int) (width * scale);
        }

        public int renderedHeight() {
            return (int) (height * scale);
        }

        boolean fitsPage() {
            int drawnWidth = renderedWidth();
            int drawnHeight = renderedHeight();
            return drawnWidth > 0 && drawnHeight > 0 && drawnWidth <= PAGE_WIDTH && drawnHeight <= IMAGE_MAX_HEIGHT;
        }

        int footprint() {
            return renderedHeight() + IMAGE_GAP;
        }
    }

    private record StyledChar(Style style, int codePoint) {}

    private record TextLine(FormattedCharSequence text, int y) implements PageElement {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop) {
            graphics.text(font, text, columnLeft, pageTop + y, BookSprites.INK_COLOR, false);
        }
    }

    private record RuleMark(int y) implements PageElement {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, columnLeft + BODY_RULE_OFFSET_X, pageTop + y + ORNAMENT_DRAW_Y, (float) BODY_RULE_U, (float) BODY_RULE_V,
                    BODY_RULE_WIDTH, BODY_RULE_HEIGHT, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
        }
    }

    private record DividerMark(int y) implements PageElement {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, columnLeft, pageTop + y + ORNAMENT_DRAW_Y, (float) DIVIDER_U, (float) DIVIDER_V, DIVIDER_WIDTH, DIVIDER_HEIGHT,
                    TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
        }
    }

    private record ImageMark(PageImage image, int y) implements PageElement {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Font font, int columnLeft, int pageTop) {
            int left = columnLeft + (PAGE_WIDTH - image.renderedWidth()) / HALF;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) left, (float) (pageTop + y + IMAGE_DRAW_Y));
            graphics.pose().scale(image.scale(), image.scale());
            graphics.blit(RenderPipelines.GUI_TEXTURED, image.texture(), 0, 0, (float) image.u(), (float) image.v(), image.width(), image.height(), IMAGE_SHEET_SIZE, IMAGE_SHEET_SIZE);
            graphics.pose().popMatrix();
        }
    }

    private interface Block {
        void layout(Paginator paginator);

        default boolean trailingOrnament() {
            return false;
        }
    }

    private record Paragraph(String text, boolean closedByBreak) implements Block {
        @Override
        public void layout(Paginator paginator) {
            paginator.paragraph(text, closedByBreak);
        }
    }

    private enum Blank implements Block {
        INSTANCE;

        @Override
        public void layout(Paginator paginator) {
            paginator.blankLine();
        }
    }

    private enum Skipped implements Block {
        INSTANCE;

        @Override
        public void layout(Paginator paginator) {}
    }

    private enum PageBreak implements Block {
        INSTANCE;

        @Override
        public void layout(Paginator paginator) {
            paginator.forcedBreak();
        }
    }

    private enum Rule implements Block {
        INSTANCE;

        @Override
        public void layout(Paginator paginator) {
            paginator.ornament(new RuleMark(paginator.cursorAfterRoom(ORNAMENT_SPACE)), ORNAMENT_SPACE);
        }

        @Override
        public boolean trailingOrnament() {
            return true;
        }
    }

    private enum Divider implements Block {
        INSTANCE;

        @Override
        public void layout(Paginator paginator) {
            paginator.ornament(new DividerMark(paginator.cursorAfterRoom(ORNAMENT_SPACE)), ORNAMENT_SPACE);
        }

        @Override
        public boolean trailingOrnament() {
            return true;
        }
    }

    private record Picture(PageImage image) implements Block {
        @Override
        public void layout(Paginator paginator) {
            paginator.image(image);
        }
    }

    private static final class Paginator {
        private final Font font;
        private final List<Page> pages = new ArrayList<>();
        private final Deque<PageImage> waiting = new ArrayDeque<>();
        private List<PageElement> current = new ArrayList<>();
        private int cursor;
        private int remaining;
        private boolean bodyEmpty = true;

        private Paginator(Font font, int firstPageTop, int firstPageBudget) {
            this.font = font;
            this.cursor = firstPageTop;
            this.remaining = firstPageBudget;
        }

        void paragraph(String text, boolean closedByBreak) {
            List<FormattedCharSequence> lines = trimmed(font.split(FormattedText.of(text), PAGE_WIDTH));
            if (lines.isEmpty()) {
                if (closedByBreak) {
                    blankLine();
                }
                return;
            }
            for (FormattedCharSequence line : lines) {
                makeRoom(LINE_HEIGHT);
                current.add(new TextLine(line, cursor));
                advance(LINE_HEIGHT, LINE_HEIGHT);
            }
            if (closedByBreak) {
                advance(BREAK_GAP_DRAWN, BREAK_GAP_BUDGET);
            }
        }

        void blankLine() {
            makeRoom(LINE_HEIGHT);
            advance(LINE_HEIGHT + BREAK_GAP_DRAWN, LINE_HEIGHT + BREAK_GAP_BUDGET);
        }

        int cursorAfterRoom(int space) {
            makeRoom(space);
            return cursor;
        }

        void ornament(PageElement mark, int space) {
            current.add(mark);
            advance(space, space);
        }

        void image(PageImage image) {
            if (waiting.isEmpty() && image.footprint() <= remaining) {
                place(image);
            } else {
                waiting.addLast(image);
            }
        }

        void forcedBreak() {
            if (!bodyEmpty) {
                turnPage();
            }
        }

        List<Page> finish() {
            while (!waiting.isEmpty()) {
                turnPage();
            }
            if (!current.isEmpty() || pages.isEmpty()) {
                pages.add(new Page(current));
            }
            return List.copyOf(pages);
        }

        private void makeRoom(int space) {
            if (remaining < space) {
                turnPage();
            }
        }

        private void place(PageImage image) {
            current.add(new ImageMark(image, cursor));
            advance(image.footprint(), image.footprint());
        }

        private void advance(int drawn, int charged) {
            cursor += drawn;
            remaining -= charged;
            bodyEmpty = false;
        }

        private static List<FormattedCharSequence> trimmed(List<FormattedCharSequence> lines) {
            List<FormattedCharSequence> result = new ArrayList<>(lines.size());
            for (FormattedCharSequence line : lines) {
                List<StyledChar> chars = new ArrayList<>();
                line.accept((index, style, codePoint) -> chars.add(new StyledChar(style, codePoint)));
                int start = 0;
                int end = chars.size();
                while (start < end && chars.get(start).codePoint() <= MAX_WHITESPACE) {
                    start++;
                }
                while (end > start && chars.get(end - 1).codePoint() <= MAX_WHITESPACE) {
                    end--;
                }
                if (start == end) {
                    continue;
                }
                List<FormattedCharSequence> parts = new ArrayList<>(end - start);
                for (StyledChar styled : chars.subList(start, end)) {
                    parts.add(FormattedCharSequence.codepoint(styled.codePoint(), styled.style()));
                }
                result.add(FormattedCharSequence.composite(parts));
            }
            return result;
        }

        private void turnPage() {
            pages.add(new Page(current));
            current = new ArrayList<>();
            cursor = 0;
            remaining = PAGE_HEIGHT;
            bodyEmpty = true;
            while (!waiting.isEmpty() && waiting.peekFirst().footprint() <= remaining) {
                place(waiting.pollFirst());
            }
        }
    }
}

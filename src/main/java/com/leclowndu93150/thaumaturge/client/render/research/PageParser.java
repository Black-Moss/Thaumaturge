package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class PageParser {
    public static final int PAGE_HEIGHT = 210;
    public static final int PAGE_WIDTH = 140;

    private static final int REQUIREMENT_ROW_HEIGHT = 18;
    private static final int KNOWLEDGE_ROW_HEIGHT = 20;
    private static final int KNOWLEDGE_TOP_PADDING = 2;
    private static final int FIRST_PAGE_HEIGHT = 182;
    private static final int KNOWLEDGE_DIVIDER_ALLOWANCE = 12;
    private static final int REQUIREMENT_DIVIDER_ALLOWANCE = 24;
    private static final int HISTORY_GAP = 5;
    private static final int IMAGE_GAP = 2;
    private static final int MAX_IMAGE_HEIGHT = 208;
    private static final int MAX_IMAGE_WIDTH = PAGE_WIDTH;
    private static final int DESCRIPTOR_FIELDS = 6;
    private static final float PARAGRAPH_EXTRA = 0.66F;
    private static final Identifier KNOWLEDGE_TYPES = TTIds.rl("knowledge_types");
    private static final String ADDENDUM_KEY = "gui.thaumaturge.thaumonomicon.addendum";
    private static final char TAG_OPEN = '<';
    private static final String IMAGE_OPEN = "<IMG>";
    private static final String IMAGE_CLOSE = "</IMG>";
    private static final String DESCRIPTOR_SEPARATOR = ":";
    private static final Map<String, Kind> SIMPLE_TAGS = Map.of("<BR>", Kind.PARAGRAPH, "<LINE>", Kind.RULE, "<DIV>", Kind.DIVIDER, "<PAGE>", Kind.PAGE);

    private enum Kind {
        TEXT, NEWLINE, PARAGRAPH, RULE, DIVIDER, PAGE, IMAGE
    }

    private record Token(Kind kind, String text, @Nullable PageImage image, Style style) {
        static Token of(Kind kind) {
            return new Token(kind, "", null, Style.EMPTY);
        }

        boolean divider() {
            return kind == Kind.RULE || kind == Kind.DIVIDER;
        }

        boolean blank() {
            return kind == Kind.PARAGRAPH || kind == Kind.NEWLINE || kind == Kind.TEXT && text.isBlank();
        }
    }

    public static final class Page {
        private final List<PageElement> elements;

        public Page() {
            this.elements = new ArrayList<>();
        }

        void add(PageElement element) {
            elements.add(element);
        }

        public List<PageElement> elements() {
            return elements;
        }
    }

    public sealed interface PageElement permits PageElement.Text, PageElement.Image {
        record Text(String content, Style style, boolean paragraphBreak) implements PageElement {
        }

        record Image(PageImage image) implements PageElement {
        }
    }

    public static final class PageImage {
        public static final PageImage LINE_DIVIDER = new PageImage(TTScreenTextures.RESEARCH_BOOK, 24, 184, 95, 6, 1.0F);
        public static final PageImage SECTION_DIVIDER = new PageImage(TTScreenTextures.RESEARCH_BOOK, 28, 192, 140, 6, 1.0F);

        public final Identifier texture;
        public final int u;
        public final int v;
        public final int w;
        public final int h;
        public final float scale;
        public final int renderedWidth;
        public final int renderedHeight;

        public PageImage(Identifier texture, int u, int v, int w, int h, float scale) {
            this.renderedWidth = (int) (w * scale);
            this.renderedHeight = (int) (h * scale);
            this.scale = scale;
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.w = w;
            this.h = h;
        }

        public int renderedHeight() {
            return renderedHeight;
        }

        public int renderedWidth() {
            return renderedWidth;
        }

        public static @Nullable PageImage parse(String descriptor) {
            String[] parts = descriptor.split(DESCRIPTOR_SEPARATOR, -1);
            if (parts.length != DESCRIPTOR_FIELDS + 1) {
                return null;
            }
            Identifier texture = Identifier.tryParse(parts[0].trim() + DESCRIPTOR_SEPARATOR + parts[1].trim());
            if (texture == null) {
                return null;
            }
            PageImage candidate = readNumbers(texture, parts);
            return candidate != null && candidate.fitsPage() ? candidate : null;
        }

        private static @Nullable PageImage readNumbers(Identifier texture, String[] parts) {
            try {
                int[] ints = new int[4];
                for (int slot = 0; slot < ints.length; slot++) {
                    ints[slot] = Integer.parseInt(parts[slot + 2].trim());
                }
                float scale = Float.parseFloat(parts[DESCRIPTOR_FIELDS].trim());
                return new PageImage(texture, ints[0], ints[1], ints[2], ints[3], scale);
            } catch (NumberFormatException invalid) {
                return null;
            }
        }

        private boolean fitsPage() {
            if (renderedWidth <= 0 || renderedHeight <= 0) {
                return false;
            }
            return renderedHeight <= MAX_IMAGE_HEIGHT && renderedWidth <= MAX_IMAGE_WIDTH;
        }
    }

    private PageParser() {}

    public static List<Page> parse(Font font, String textKey, int budget) {
        return new Layout(font, budget).run(tokens(textKey, List.of()));
    }

    public static List<Page> parse(Font font, Identifier entryId, String stageTextKey, List<String> addendaTextKeys, int knowledgeRows, boolean complete, boolean hasRequiredResearch, boolean hasObtain, boolean hasCraft, boolean hasKnowledge, boolean reserveHistoryGap) {
        boolean knowledgeTypes = KNOWLEDGE_TYPES.equals(entryId);
        int reserved = 0;
        if (knowledgeTypes) {
            reserved += KNOWLEDGE_TOP_PADDING + KNOWLEDGE_ROW_HEIGHT * knowledgeRows;
        }
        boolean anyRequirement = false;
        if (!complete) {
            for (boolean present : new boolean[]{hasCraft, hasObtain, hasKnowledge, hasRequiredResearch}) {
                if (present) {
                    reserved += REQUIREMENT_ROW_HEIGHT;
                    anyRequirement = true;
                }
            }
        }
        if (anyRequirement) {
            reserved += REQUIREMENT_DIVIDER_ALLOWANCE;
        } else if (knowledgeTypes) {
            reserved += KNOWLEDGE_DIVIDER_ALLOWANCE;
        }
        if (reserveHistoryGap) {
            reserved += HISTORY_GAP;
        }
        return new Layout(font, FIRST_PAGE_HEIGHT - reserved).run(tokens(stageTextKey, addendaTextKeys));
    }

    private static List<Token> tokens(String textKey, List<String> addendaTextKeys) {
        List<Token> tokens = new ArrayList<>();
        appendMarkup(tokens, Component.translatable(textKey).getString());
        for (int i = 0; i < addendaTextKeys.size(); i++) {
            tokens.add(Token.of(Kind.PAGE));
            tokens.add(new Token(Kind.TEXT, Component.translatable(ADDENDUM_KEY, i + 1).getString(), null, Style.EMPTY.withItalic(true)));
            tokens.add(Token.of(Kind.PARAGRAPH));
            appendMarkup(tokens, Component.translatable(addendaTextKeys.get(i)).getString());
        }
        return tokens;
    }

    private static void appendMarkup(List<Token> tokens, String markup) {
        List<Token> parsed = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int i = 0;
        while (i < markup.length()) {
            int consumed = 0;
            if (markup.charAt(i) == TAG_OPEN) {
                consumed = tagLength(markup, i, text, parsed);
            }
            if (consumed > 0) {
                i += consumed;
            } else {
                text.append(markup.charAt(i));
                i++;
            }
        }
        flushText(text, parsed);
        for (int last = parsed.size() - 1; last >= 0; last--) {
            if (!parsed.get(last).blank()) {
                if (parsed.get(last).divider()) {
                    parsed.remove(last);
                }
                break;
            }
        }
        tokens.addAll(parsed);
    }

    private static int tagLength(String markup, int at, StringBuilder text, List<Token> out) {
        for (Map.Entry<String, Kind> tag : SIMPLE_TAGS.entrySet()) {
            if (markup.startsWith(tag.getKey(), at)) {
                flushText(text, out);
                out.add(Token.of(tag.getValue()));
                return tag.getKey().length();
            }
        }
        if (markup.startsWith(IMAGE_CLOSE, at)) {
            return IMAGE_CLOSE.length();
        }
        if (markup.startsWith(IMAGE_OPEN, at)) {
            int bodyStart = at + IMAGE_OPEN.length();
            int close = markup.indexOf(IMAGE_CLOSE, bodyStart);
            if (close < 0) {
                return IMAGE_OPEN.length();
            }
            flushText(text, out);
            PageImage image = PageImage.parse(markup.substring(bodyStart, close));
            out.add(image == null ? Token.of(Kind.NEWLINE) : new Token(Kind.IMAGE, "", image, Style.EMPTY));
            return close + IMAGE_CLOSE.length() - at;
        }
        return 0;
    }

    private static void flushText(StringBuilder text, List<Token> out) {
        if (!text.isEmpty()) {
            out.add(new Token(Kind.TEXT, text.toString(), null, Style.EMPTY));
            text.setLength(0);
        }
    }

    private record Line(String content, Style style) {
    }

    private static final class Layout {
        private static final Line BLANK_LINE = new Line("", Style.EMPTY);

        private final List<Page> pages = new ArrayList<>();
        private final Deque<PageImage> queue = new ArrayDeque<>();
        private final StringSplitter splitter;
        private final int lineHeight;
        private final StringBuilder paragraph = new StringBuilder();
        private Style paragraphStyle = Style.EMPTY;
        private Page page = new Page();
        private int remaining;
        private boolean breakPending;

        Layout(Font font, int firstPageHeight) {
            this.splitter = font.getSplitter();
            this.lineHeight = font.lineHeight;
            this.remaining = firstPageHeight;
            pages.add(page);
        }

        List<Page> run(List<Token> tokens) {
            for (Token token : tokens) {
                switch (token.kind) {
                    case TEXT -> {
                        if (paragraph.isEmpty()) {
                            paragraphStyle = token.style;
                        }
                        paragraph.append(token.text);
                    }
                    case NEWLINE -> paragraph.append('\n');
                    case PARAGRAPH -> endParagraph();
                    case PAGE -> {
                        flushParagraph(false);
                        breakPending = true;
                    }
                    case RULE -> enqueue(PageImage.LINE_DIVIDER);
                    case DIVIDER -> enqueue(PageImage.SECTION_DIVIDER);
                    case IMAGE -> enqueue(token.image);
                }
            }
            flushParagraph(false);
            while (!queue.isEmpty()) {
                drain();
                if (!queue.isEmpty()) {
                    openNewPage();
                }
            }
            return pages;
        }

        private void enqueue(PageImage image) {
            flushParagraph(false);
            queue.add(image);
            drain();
        }

        private boolean closed() {
            return !page.elements().isEmpty() && (breakPending || remaining < lineHeight);
        }

        private void openNewPage() {
            page = new Page();
            pages.add(page);
            remaining = PAGE_HEIGHT;
            breakPending = false;
            drain();
        }

        private void drain() {
            while (!queue.isEmpty() && !closed()) {
                PageImage head = queue.peek();
                int need = head.renderedHeight + IMAGE_GAP;
                if (need > remaining) {
                    return;
                }
                page.add(new PageElement.Image(head));
                remaining -= need;
                queue.poll();
            }
        }

        private void endParagraph() {
            if (paragraph.isEmpty()) {
                placeLine(BLANK_LINE, true);
            } else {
                flushParagraph(true);
            }
        }

        private void flushParagraph(boolean endsParagraph) {
            if (paragraph.isEmpty()) {
                return;
            }
            String text = paragraph.toString();
            Style startStyle = paragraphStyle;
            paragraph.setLength(0);
            paragraphStyle = Style.EMPTY;
            List<Line> lines = new ArrayList<>();
            splitter.splitLines(text, PAGE_WIDTH, startStyle, false, (style, start, end) -> {
                String content = text.substring(start, end).trim();
                if (!content.isEmpty()) {
                    lines.add(new Line(content, style));
                }
            });
            for (int i = 0; i < lines.size(); i++) {
                placeLine(lines.get(i), endsParagraph && i == lines.size() - 1);
            }
        }

        private void placeLine(Line line, boolean paragraphBreak) {
            while (closed()) {
                openNewPage();
            }
            page.add(new PageElement.Text(line.content, line.style, paragraphBreak));
            float used = paragraphBreak ? lineHeight + lineHeight * PARAGRAPH_EXTRA : lineHeight;
            remaining = (int) (remaining - used);
        }
    }
}

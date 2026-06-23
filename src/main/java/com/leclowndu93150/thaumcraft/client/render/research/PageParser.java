package com.leclowndu93150.thaumcraft.client.render.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class PageParser {
    public static final int PAGE_WIDTH = 140;
    public static final int PAGE_HEIGHT = 210;

    private static final int BASE_HEIGHT_REMAINING = 182;
    private static final int PAGE_RESET_HEIGHT = 210;
    private static final int KNOWLEDGETYPES_TOP_PAD = 2;
    private static final int KNOWLEDGETYPES_ROW_STEP = 20;
    private static final int KNOWLEDGETYPES_DIVIDER = 12;
    private static final int REQUIREMENT_ROW = 18;
    private static final int REQUIREMENT_DIVIDER = 15;
    private static final float BONUS_BREAK_FRACTION = 0.66F;
    private static final int IMAGE_GAP = 2;

    private static final Identifier KNOWLEDGETYPES_ID = Identifier.fromNamespaceAndPath("thaumcraft", "knowledgetypes");
    private static final String ADDENDUM_TEXT_KEY = "tc.addendumtext";

    private PageParser() {}

    public static List<Page> parse(Font font, String stageTextKey, int contentHeightBudget) {
        return paginate(font, stageTextKey, List.of(), contentHeightBudget);
    }

    public static List<Page> parse(
            Font font,
            Identifier entryId,
            String stageTextKey,
            List<String> addendaTextKeys,
            int knowledgeTypeRowCount,
            boolean isComplete,
            boolean hasRequiredResearch,
            boolean hasObtain,
            boolean hasCraft,
            boolean hasKnowledge) {
        int heightRemaining = BASE_HEIGHT_REMAINING;
        int dividerSpace = 0;
        if (entryId != null && entryId.equals(KNOWLEDGETYPES_ID)) {
            heightRemaining -= KNOWLEDGETYPES_TOP_PAD;
            heightRemaining -= KNOWLEDGETYPES_ROW_STEP * knowledgeTypeRowCount;
            dividerSpace = KNOWLEDGETYPES_DIVIDER;
        }
        if (!isComplete) {
            if (hasCraft) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasObtain) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasKnowledge) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasRequiredResearch) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
        }
        heightRemaining -= dividerSpace;
        return paginate(font, stageTextKey, addendaTextKeys, heightRemaining);
    }

    private static List<Page> paginate(Font font, String stageTextKey, List<String> addendaTextKeys, int initialBudget) {
        String rawText = Component.translatable(stageTextKey).getString();
        for (int i = 0; i < addendaTextKeys.size(); i++) {
            String key = addendaTextKeys.get(i);
            String addendumHeader = Component.translatable(ADDENDUM_TEXT_KEY, i + 1).getString();
            String addendumBody = Component.translatable(key).getString();
            rawText = rawText + "<PAGE>" + addendumHeader + "<BR>" + addendumBody;
        }
        rawText = rawText.replaceAll("<BR>", "~B\n\n");
        rawText = rawText.replaceAll("<BR/>", "~B\n\n");
        rawText = rawText.replaceAll("<LINE>", "~L");
        rawText = rawText.replaceAll("<LINE/>", "~L");
        rawText = rawText.replaceAll("<DIV>", "~D");
        rawText = rawText.replaceAll("<DIV/>", "~D");
        rawText = rawText.replaceAll("<PAGE>", "~P");
        rawText = rawText.replaceAll("<PAGE/>", "~P");
        List<PageImage> images = new ArrayList<>();
        String[] imgSplit = rawText.split("<IMG>");
        for (String s : imgSplit) {
            int i = s.indexOf("</IMG>");
            if (i >= 0) {
                String clean = s.substring(0, i);
                PageImage pi = PageImage.parse(clean);
                if (pi == null) {
                    rawText = rawText.replaceFirst(clean, "\n");
                } else {
                    images.add(pi);
                    rawText = rawText.replaceFirst(clean, "~I");
                }
            }
        }
        rawText = rawText.replaceAll("<IMG>", "");
        rawText = rawText.replaceAll("</IMG>", "");
        List<String> firstPassText = new ArrayList<>();
        String[] temp = rawText.split("~P");
        for (int a = 0; a < temp.length; a++) {
            String t = temp[a];
            String[] temp1 = t.split("~D");
            for (int x = 0; x < temp1.length; x++) {
                String t1 = temp1[x];
                String[] temp2 = t1.split("~L");
                for (int b = 0; b < temp2.length; b++) {
                    String t2 = temp2[b];
                    String[] temp3 = t2.split("~I");
                    for (int c = 0; c < temp3.length; c++) {
                        String t3 = temp3[c];
                        firstPassText.add(t3);
                        if (c != temp3.length - 1) {
                            firstPassText.add("~I");
                        }
                    }
                    if (b != temp2.length - 1) {
                        firstPassText.add("~L");
                    }
                }
                if (x != temp1.length - 1) {
                    firstPassText.add("~D");
                }
            }
            if (a != temp.length - 1) {
                firstPassText.add("~P");
            }
        }
        List<String> parsedText = new ArrayList<>();
        for (String sx : firstPassText) {
            List<FormattedText> split = font.getSplitter().splitLines(sx, PAGE_WIDTH, Style.EMPTY);
            for (FormattedText ln : split) {
                parsedText.add(ln.getString());
            }
        }
        int lineHeight = font.lineHeight;
        int heightRemaining = initialBudget;
        List<Page> pages = new ArrayList<>();
        Page page1 = new Page();
        List<PageImage> tempImages = new ArrayList<>();
        for (String original : parsedText) {
            String line = original;
            if (line.contains("~I")) {
                if (!images.isEmpty()) {
                    tempImages.add(images.remove(0));
                }
                line = "";
            }
            if (line.contains("~L")) {
                tempImages.add(PageImage.LINE_DIVIDER);
                line = "";
            }
            if (line.contains("~D")) {
                tempImages.add(PageImage.SECTION_DIVIDER);
                line = "";
            }
            if (line.contains("~P")) {
                heightRemaining = PAGE_RESET_HEIGHT;
                pages.add(page1);
                page1 = new Page();
                line = "";
            }
            if (!line.isEmpty()) {
                line = line.trim();
                page1.add(new PageElement.Text(line));
                heightRemaining -= lineHeight;
                if (line.endsWith("~B")) {
                    heightRemaining = (int) (heightRemaining - lineHeight * BONUS_BREAK_FRACTION);
                }
            }
            while (!tempImages.isEmpty() && heightRemaining >= tempImages.get(0).renderedHeight() + IMAGE_GAP) {
                heightRemaining -= tempImages.get(0).renderedHeight() + IMAGE_GAP;
                page1.add(new PageElement.Image(tempImages.remove(0)));
            }
            if (heightRemaining < lineHeight && !page1.elements().isEmpty()) {
                heightRemaining = PAGE_RESET_HEIGHT;
                pages.add(page1);
                page1 = new Page();
            }
        }
        if (!page1.elements().isEmpty()) {
            pages.add(page1);
        }
        page1 = new Page();
        heightRemaining = PAGE_RESET_HEIGHT;
        while (!tempImages.isEmpty()) {
            PageImage head = tempImages.get(0);
            if (heightRemaining < head.renderedHeight() + IMAGE_GAP) {
                heightRemaining = PAGE_RESET_HEIGHT;
                pages.add(page1);
                page1 = new Page();
            } else {
                heightRemaining -= head.renderedHeight() + IMAGE_GAP;
                page1.add(new PageElement.Image(tempImages.remove(0)));
            }
        }
        if (!page1.elements().isEmpty()) {
            pages.add(page1);
        }
        if (pages.isEmpty()) {
            pages.add(new Page());
        }
        return pages;
    }

    public static final class Page {
        private final List<PageElement> elements = new ArrayList<>();

        public List<PageElement> elements() {
            return elements;
        }

        void add(PageElement element) {
            elements.add(element);
        }
    }

    public sealed interface PageElement permits PageElement.Text, PageElement.Image {
        record Text(String content) implements PageElement {}
        record Image(PageImage image) implements PageElement {}
    }

    public static final class PageImage {
        public static final PageImage LINE_DIVIDER = new PageImage(
                Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_researchbook.png"),
                24, 184, 95, 6, 1.0F);
        public static final PageImage SECTION_DIVIDER = new PageImage(
                Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_researchbook.png"),
                28, 192, 140, 6, 1.0F);

        public final Identifier texture;
        public final int u;
        public final int v;
        public final int w;
        public final int h;
        public final float scale;
        public final int renderedWidth;
        public final int renderedHeight;

        public PageImage(Identifier texture, int u, int v, int w, int h, float scale) {
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.w = w;
            this.h = h;
            this.scale = scale;
            this.renderedWidth = (int) (w * scale);
            this.renderedHeight = (int) (h * scale);
        }

        public int renderedHeight() {
            return renderedHeight;
        }

        public int renderedWidth() {
            return renderedWidth;
        }

        public static PageImage parse(String descriptor) {
            String[] parts = descriptor.split(":");
            if (parts.length != 7) return null;
            try {
                Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
                int u = Integer.parseInt(parts[2]);
                int v = Integer.parseInt(parts[3]);
                int w = Integer.parseInt(parts[4]);
                int h = Integer.parseInt(parts[5]);
                float scale = Float.parseFloat(parts[6]);
                PageImage pi = new PageImage(id, u, v, w, h, scale);
                return pi.renderedHeight <= 208 && pi.renderedWidth <= PAGE_WIDTH ? pi : null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}

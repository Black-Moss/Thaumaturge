package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;

public final class NavigationLayout {
    public static final int BACK_U = 38;
    public static final int BACK_V = 202;
    public static final int BACK_WIDTH = 20;
    public static final int BACK_HEIGHT = 12;

    private static final int ARROW_ROW_OFFSET = 190;
    private static final int PAGE_LEFT_DRAW_OFFSET = -16;
    private static final int PAGE_RIGHT_DRAW_OFFSET = 262;
    private static final int PAGE_HIT_ROW_OFFSET = 189;
    private static final int PAGE_LEFT_HIT_OFFSET = -17;
    private static final int PAGE_RIGHT_HIT_OFFSET = 261;
    private static final int PAGE_HIT_WIDTH = 14;
    private static final int PAGE_HIT_HEIGHT = 10;
    private static final int BACK_OFFSET = 118;
    private static final int STAGE_LEFT_OFFSET = 6;
    private static final int STAGE_RIGHT_OFFSET = 86;
    private static final int STAGE_DRAW_ROW_OFFSET = 185;
    private static final int STAGE_HIT_ROW_OFFSET = 184;
    private static final int STAGE_HIT_SIZE = 12;
    private static final int STAGE_HIT_INSET = 1;
    private static final int PAGER_LEFT_HIT_OFFSET = 38;
    private static final int PAGER_RIGHT_HIT_OFFSET = 205;
    private static final int PAGER_HIT_ROW_OFFSET = 192;
    private static final int PAGER_HIT_SIZE = 14;

    private NavigationLayout() {}

    public static int arrowRow(DetailFrame frame) {
        return frame.top() + ARROW_ROW_OFFSET;
    }

    public static int pageLeftDrawX(DetailFrame frame) {
        return frame.left() + PAGE_LEFT_DRAW_OFFSET;
    }

    public static int pageRightDrawX(DetailFrame frame) {
        return frame.left() + PAGE_RIGHT_DRAW_OFFSET;
    }

    public static Rect pageLeftHit(DetailFrame frame) {
        return new Rect(frame.left() + PAGE_LEFT_HIT_OFFSET, frame.top() + PAGE_HIT_ROW_OFFSET, PAGE_HIT_WIDTH, PAGE_HIT_HEIGHT);
    }

    public static Rect pageRightHit(DetailFrame frame) {
        return new Rect(frame.left() + PAGE_RIGHT_HIT_OFFSET, frame.top() + PAGE_HIT_ROW_OFFSET, PAGE_HIT_WIDTH, PAGE_HIT_HEIGHT);
    }

    public static Rect backButton(DetailFrame frame) {
        return new Rect(frame.left() + BACK_OFFSET, arrowRow(frame), BACK_WIDTH, BACK_HEIGHT);
    }

    public static int stageLeftDrawX(DetailFrame frame) {
        return frame.left() + STAGE_LEFT_OFFSET;
    }

    public static int stageRightDrawX(DetailFrame frame) {
        return frame.left() + STAGE_RIGHT_OFFSET;
    }

    public static int stageDrawRow(DetailFrame frame) {
        return frame.top() + STAGE_DRAW_ROW_OFFSET;
    }

    public static Rect stageLeftHit(DetailFrame frame) {
        return new Rect(stageLeftDrawX(frame) - STAGE_HIT_INSET, frame.top() + STAGE_HIT_ROW_OFFSET, STAGE_HIT_SIZE, STAGE_HIT_SIZE);
    }

    public static Rect stageRightHit(DetailFrame frame) {
        return new Rect(stageRightDrawX(frame) - STAGE_HIT_INSET, frame.top() + STAGE_HIT_ROW_OFFSET, STAGE_HIT_SIZE, STAGE_HIT_SIZE);
    }

    public static Rect pagerLeftHit(DetailFrame frame) {
        return new Rect(frame.left() + PAGER_LEFT_HIT_OFFSET, frame.top() + PAGER_HIT_ROW_OFFSET, PAGER_HIT_SIZE, PAGER_HIT_SIZE);
    }

    public static Rect pagerRightHit(DetailFrame frame) {
        return new Rect(frame.left() + PAGER_RIGHT_HIT_OFFSET, frame.top() + PAGER_HIT_ROW_OFFSET, PAGER_HIT_SIZE, PAGER_HIT_SIZE);
    }
}

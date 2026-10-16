package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainRolls;

public final class EntryPoseCalculator {
    public static final float ICON_SIZE = 16.0F;

    private static final float POP_SHARE = 0.34F;
    private static final float POP_NORMALISER = 0.33F;
    private static final float FLIGHT_SHARE = 0.66F;
    private static final int START_WIDTH_DIVISOR = 4;
    private static final int START_HEIGHT_DIVISOR = 3;
    private static final int BOOK_CENTRE_INSET = 12;

    private EntryPoseCalculator() {}

    public static EntryPose pose(int life, float remaining) {
        if (life <= 0) {
            return new EntryPose(0.0F, 0.0F, 0.0F);
        }
        float left = Math.max(0.0F, remaining);
        float elapsed = life - left;
        if (elapsed < POP_SHARE * life) {
            return new EntryPose(ICON_SIZE * GainEasing.popScale(elapsed / (POP_NORMALISER * life)), 1.0F, 1.0F);
        }
        float flight = left / (FLIGHT_SHARE * life);
        return new EntryPose(ICON_SIZE * GainEasing.flightScale(flight), GainEasing.horizontalShare(flight), GainEasing.verticalShare(flight));
    }

    public static ScreenPoint locate(GainRolls rolls, EntryPose pose, int guiWidth, int guiHeight) {
        float endX = guiWidth - BOOK_CENTRE_INSET + rolls.endX();
        float endY = guiHeight - BOOK_CENTRE_INSET + rolls.endY();
        float reachX = guiWidth / START_WIDTH_DIVISOR + rolls.startX();
        float reachY = guiHeight / START_HEIGHT_DIVISOR + rolls.startY();
        return new ScreenPoint(endX - reachX * pose.horizontal(), endY - reachY * pose.vertical());
    }
}

package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainRolls;

public final class EntryPoseCalculator {
    public static final float ICON_SIZE = 16.0F;

    private static final float FLIGHT_START_FRACTION = 0.66F;
    private static final float POP_SPAN_FRACTION = 0.33F;
    private static final float NO_TRAVEL = 1.0F;
    private static final int CORNER_MARGIN = 12;
    private static final int WIDTH_DIVISOR = 4;
    private static final int HEIGHT_DIVISOR = 3;

    private EntryPoseCalculator() {}

    public static EntryPose pose(int life, float remaining) {
        float flightStart = FLIGHT_START_FRACTION * life;
        if (remaining >= flightStart) {
            float progress = (life - remaining) / (POP_SPAN_FRACTION * life);
            return new EntryPose(ICON_SIZE * GainEasing.popScale(progress), NO_TRAVEL, NO_TRAVEL);
        }
        float flight = remaining / flightStart;
        float drop = GainEasing.flightDrop(flight);
        return new EntryPose(ICON_SIZE * flight, GainEasing.flightSweep(drop), drop);
    }

    public static ScreenPoint locate(GainRolls rolls, EntryPose pose, int guiWidth, int guiHeight) {
        float x = guiWidth - CORNER_MARGIN + rolls.endX() - (guiWidth / WIDTH_DIVISOR + rolls.startX()) * pose.horizontal();
        float y = guiHeight - CORNER_MARGIN + rolls.endY() - (guiHeight / HEIGHT_DIVISOR + rolls.startY()) * pose.vertical();
        return new ScreenPoint(x, y);
    }
}

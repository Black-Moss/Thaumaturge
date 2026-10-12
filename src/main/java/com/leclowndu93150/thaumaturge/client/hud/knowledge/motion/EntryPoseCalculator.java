package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainRolls;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.RollSalt;

public final class EntryPoseCalculator {
    public static final float ICON_SIZE = 16.0F;

    private static final float POP_SHARE = 0.4F;
    private static final int START_WIDTH_DIVISOR = 4;
    private static final int START_HEIGHT_DIVISOR = 3;
    private static final int BOOK_CENTRE_INSET = 9;
    private static final int HALF_DIVISOR = 2;

    private EntryPoseCalculator() {}

    public static EntryPose pose(int life, float remaining) {
        if (life <= 0) {
            return new EntryPose(0.0F, 0.0F, 0.0F);
        }
        float progress = (life - remaining) / life;
        if (progress < POP_SHARE) {
            return new EntryPose(ICON_SIZE * GainEasing.popScale(progress / POP_SHARE), 1.0F, 1.0F);
        }
        float flight = (progress - POP_SHARE) / (1.0F - POP_SHARE);
        return new EntryPose(ICON_SIZE * GainEasing.flightScale(1.0F - flight), 1.0F - GainEasing.sweep(flight), 1.0F - GainEasing.drop(flight));
    }

    public static ScreenPoint locate(GainRolls rolls, EntryPose pose, int guiWidth, int guiHeight) {
        float endX = guiWidth - BOOK_CENTRE_INSET + centred(rolls.endX(), RollSalt.END_X);
        float endY = guiHeight - BOOK_CENTRE_INSET + centred(rolls.endY(), RollSalt.END_Y);
        float reachX = guiWidth / START_WIDTH_DIVISOR + centred(rolls.startX(), RollSalt.START_X);
        float reachY = guiHeight / START_HEIGHT_DIVISOR + centred(rolls.startY(), RollSalt.START_Y);
        return new ScreenPoint(endX - reachX * pose.horizontal(), endY - reachY * pose.vertical());
    }

    private static int centred(int roll, RollSalt salt) {
        return roll - salt.range() / HALF_DIVISOR;
    }
}

package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import com.leclowndu93150.thaumaturge.content.warp.spawn.PortalSummons;

public final class CrimsonPortalOutcome implements WarpOutcome {
    private static final String NOTICE = "warp.thaumaturge.text.16";

    @Override
    public void apply(WarpRoll roll) {
        if (PortalSummons.summon(roll.level(), roll.player(), roll.random())) {
            WarpNotices.send(roll.player(), NOTICE);
        }
    }
}

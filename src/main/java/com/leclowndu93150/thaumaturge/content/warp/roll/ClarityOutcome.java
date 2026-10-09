package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.WarpLedger;
import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;

public final class ClarityOutcome implements WarpOutcome {
    private static final int CLEANSED_WARP = 1;
    private static final String NOTICE = "warp.thaumaturge.text.14";

    @Override
    public void apply(WarpRoll roll) {
        if (roll.normalWarp() > 0) {
            WarpLedger.change(roll.player(), -CLEANSED_WARP, WarpType.NORMAL);
        }
        WarpNotices.send(roll.player(), NOTICE);
    }
}

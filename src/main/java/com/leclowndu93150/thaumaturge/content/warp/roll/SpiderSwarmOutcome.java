package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import com.leclowndu93150.thaumaturge.content.warp.spawn.SpiderSummons;

public record SpiderSwarmOutcome(boolean illusory) implements WarpOutcome {
    private static final int MAX_SPIDERS = 50;
    private static final String NOTICE = "warp.thaumaturge.text.7";

    @Override
    public void apply(WarpRoll roll) {
        int count = Math.min(MAX_SPIDERS, roll.effectiveWarp());
        for (int i = 0; i < count; i++) {
            boolean creatable = SpiderSummons.summon(roll.level(), roll.player(), roll.random(), illusory);
            if (!creatable) {
                return;
            }
        }
        WarpNotices.send(roll.player(), NOTICE);
    }
}

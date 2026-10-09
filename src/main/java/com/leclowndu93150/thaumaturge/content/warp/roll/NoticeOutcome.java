package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;

public record NoticeOutcome(String key) implements WarpOutcome {
    @Override
    public void apply(WarpRoll roll) {
        WarpNotices.send(roll.player(), key);
    }
}

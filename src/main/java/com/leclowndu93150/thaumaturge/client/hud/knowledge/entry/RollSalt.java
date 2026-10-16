package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import it.unimi.dsi.fastutil.HashCommon;

public enum RollSalt {
    TILT(0x5DEECE66DL, RollRanges.TILT), START_X(0x2545F4914F6CDD1DL, RollRanges.START_SCATTER), START_Y(0x7A3C5B1E9D24F86BL, RollRanges.START_SCATTER), END_X(0x1B873593CC9E2D51L,
            RollRanges.END_SCATTER), END_Y(0x68E31DA4B5297CC3L, RollRanges.END_SCATTER), APPEAR_FRAME(0x3C6EF372FE94F82BL, ParticleTextures.STAR_GLINT_FRAMES), APPEAR_SPIN(0x510E527FADE682D1L,
                    RollRanges.FULL_TURN), APPEAR_GREEN(0x1F83D9ABFB41BD6BL, RollRanges.GREEN), APPEAR_BLUE(0x5BE0CD19137E2179L, RollRanges.BLUE), ARRIVAL_FRAME(0x6A09E667F3BCC909L,
                            ParticleTextures.STAR_GLINT_FRAMES), ARRIVAL_SPIN(0x3243F6A8885A308DL,
                                    RollRanges.FULL_TURN), ARRIVAL_GREEN(0x13198A2E03707344L, RollRanges.GREEN), ARRIVAL_BLUE(0x0A4093822299F31DL, RollRanges.BLUE);

    private final long salt;
    private final int range;

    RollSalt(long salt, int range) {
        this.salt = salt;
        this.range = range;
    }

    public int roll(long seed) {
        return (int) Long.remainderUnsigned(HashCommon.mix(seed ^ salt), range);
    }

    public int range() {
        return range;
    }

    private static final class RollRanges {
        static final int TILT = 12;
        static final int START_SCATTER = 32;
        static final int END_SCATTER = 8;
        static final int FULL_TURN = 360;
        static final int GREEN = 67;
        static final int BLUE = 192;
    }
}

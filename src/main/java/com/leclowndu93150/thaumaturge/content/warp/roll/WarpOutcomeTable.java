package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.effect.MobEffects;

public final class WarpOutcomeTable {
    private static final int EFFECT_TICKS = 5000;
    private static final int LONG_EFFECT_TICKS = 6000;
    private static final int FATIGUE_TICKS = 1200;
    private static final int SCALED_TICKS_CAP = 32000;
    private static final int SCALED_TICKS_FACTOR = 10;
    private static final int BLINDNESS_TICKS_FACTOR = 5;
    private static final int NIGHT_VISION_TICKS_FACTOR = 40;
    private static final int NIGHT_VISION_TICKS_CAP = 6000;
    private static final int LONE_GUARDIAN = 1;
    private static final int MIST_GUARDIAN_STEP = 30;
    private static final int DEEP_MIST_GUARDIAN_STEP = 15;

    private static final String NOISE = "warp.thaumaturge.text.11";
    private static final String DRAINED = "warp.thaumaturge.text.1";
    private static final String GURGLE = "warp.thaumaturge.text.15";
    private static final String HUNGER = "warp.thaumaturge.text.2";
    private static final String FOLLOWED = "warp.thaumaturge.text.12";
    private static final String SCORCHED = "warp.thaumaturge.text.5";
    private static final String RELUCTANT = "warp.thaumaturge.text.9";
    private static final String PERCEPTION = "warp.thaumaturge.text.10";
    private static final String GRIM = "warp.thaumaturge.text.4";
    private static final String WATCHED = "warp.thaumaturge.text.13";

    private static final WarpOutcome LINGERING_HUNGER = EffectOutcome.amplified(TTMobEffects.UNNATURAL_HUNGER, LONG_EFFECT_TICKS, HUNGER);

    private static final List<WarpOutcomeBand> OMENS = List.of(WarpOutcomeBand.between(1, 4, new CreeperHissOutcome()), WarpOutcomeBand.between(5, 8, new DistantBlastOutcome()),
            WarpOutcomeBand.between(9, 12, new NoticeOutcome(NOISE)), WarpOutcomeBand.between(25, 28, new NoticeOutcome(FOLLOWED)), WarpOutcomeBand.between(61, 64, new NoticeOutcome(WATCHED)));

    private static final List<WarpOutcomeBand> AFFLICTIONS = List.of(WarpOutcomeBand.between(13, 16, EffectOutcome.amplified(TTMobEffects.VIS_EXHAUST, EFFECT_TICKS, DRAINED)),
            WarpOutcomeBand.between(17, 20, EffectOutcome.scaled(TTMobEffects.THAUMARHIA, SCALED_TICKS_FACTOR, SCALED_TICKS_CAP, Optional.of(GURGLE))),
            WarpOutcomeBand.between(21, 24, EffectOutcome.amplified(TTMobEffects.UNNATURAL_HUNGER, EFFECT_TICKS, HUNGER)),
            WarpOutcomeBand.between(33, 36, EffectOutcome.scaled(TTMobEffects.BLURRED_VISION, SCALED_TICKS_FACTOR, SCALED_TICKS_CAP, Optional.empty())),
            WarpOutcomeBand.between(37, 40, EffectOutcome.amplified(TTMobEffects.SUN_SCORNED, EFFECT_TICKS, SCORCHED)),
            WarpOutcomeBand.between(41, 44, EffectOutcome.amplified(MobEffects.MINING_FATIGUE, FATIGUE_TICKS, RELUCTANT)),
            WarpOutcomeBand.between(45, 48, EffectOutcome.amplified(TTMobEffects.INFECTIOUS_VIS_EXHAUST, LONG_EFFECT_TICKS, DRAINED)),
            WarpOutcomeBand.between(49, 52, EffectOutcome.scaled(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS_FACTOR, NIGHT_VISION_TICKS_CAP, Optional.of(PERCEPTION))),
            WarpOutcomeBand.between(53, 56, EffectOutcome.amplified(TTMobEffects.DEATH_GAZE, LONG_EFFECT_TICKS, GRIM)),
            WarpOutcomeBand.between(69, 72, EffectOutcome.scaled(MobEffects.BLINDNESS, BLINDNESS_TICKS_FACTOR, SCALED_TICKS_CAP, Optional.empty())), WarpOutcomeBand.between(73, 75, LINGERING_HUNGER),
            WarpOutcomeBand.between(77, 80, LINGERING_HUNGER));

    private static final List<WarpOutcomeBand> RESPITE = List.of(WarpOutcomeBand.exactly(76, new ClarityOutcome()));

    private static final List<WarpOutcomeBand> INCURSIONS = List.of(WarpOutcomeBand.between(29, 32, new MistOutcome(roll -> LONE_GUARDIAN)),
            WarpOutcomeBand.between(57, 60, new SpiderSwarmOutcome(true)), WarpOutcomeBand.between(65, 68, new MistOutcome(roll -> roll.effectiveWarp() / MIST_GUARDIAN_STEP)),
            WarpOutcomeBand.between(81, 88, new CrimsonPortalOutcome()), WarpOutcomeBand.between(89, 92, new SpiderSwarmOutcome(false)),
            WarpOutcomeBand.atLeast(93, new MistOutcome(roll -> roll.effectiveWarp() / DEEP_MIST_GUARDIAN_STEP)));

    private static final WarpOutcomeTable STANDARD = of(List.of(OMENS, AFFLICTIONS, RESPITE, INCURSIONS));

    private final List<WarpOutcomeBand> bands;

    private WarpOutcomeTable(List<WarpOutcomeBand> bands) {
        this.bands = bands;
    }

    public static WarpOutcomeTable standard() {
        return STANDARD;
    }

    public static WarpOutcomeTable of(List<List<WarpOutcomeBand>> groups) {
        List<WarpOutcomeBand> merged = new ArrayList<>();
        groups.forEach(merged::addAll);
        merged.sort(Comparator.comparingInt(WarpOutcomeBand::min));
        for (int i = 1; i < merged.size(); i++) {
            WarpOutcomeBand previous = merged.get(i - 1);
            WarpOutcomeBand current = merged.get(i);
            if (previous.overlaps(current)) {
                throw new IllegalArgumentException("Warp outcome bands " + previous.min() + ".." + previous.max() + " and " + current.min() + ".." + current.max() + " overlap");
            }
        }
        return new WarpOutcomeTable(List.copyOf(merged));
    }

    public Optional<WarpOutcome> select(int strength) {
        for (WarpOutcomeBand band : bands) {
            if (band.contains(strength)) {
                return Optional.of(band.outcome());
            }
            if (band.min() > strength) {
                break;
            }
        }
        return Optional.empty();
    }
}

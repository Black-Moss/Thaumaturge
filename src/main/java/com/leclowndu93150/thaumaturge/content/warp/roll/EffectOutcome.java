package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record EffectOutcome(Holder<MobEffect> effect, ToIntFunction<WarpRoll> duration, ToIntFunction<WarpRoll> amplifier, Optional<String> notice) implements WarpOutcome {
    private static final int BASE_AMPLIFIER = 0;

    public static EffectOutcome amplified(Holder<MobEffect> effect, int duration, String notice) {
        return new EffectOutcome(effect, roll -> duration, WarpRoll::amplifier, Optional.of(notice));
    }

    public static EffectOutcome scaled(Holder<MobEffect> effect, int factor, int cap, Optional<String> notice) {
        return new EffectOutcome(effect, roll -> roll.scaled(factor, cap), roll -> BASE_AMPLIFIER, notice);
    }

    @Override
    public void apply(WarpRoll roll) {
        roll.player().addEffect(new MobEffectInstance(effect, duration.applyAsInt(roll), amplifier.applyAsInt(roll), true, true));
        notice.ifPresent(key -> WarpNotices.send(roll.player(), key));
    }
}

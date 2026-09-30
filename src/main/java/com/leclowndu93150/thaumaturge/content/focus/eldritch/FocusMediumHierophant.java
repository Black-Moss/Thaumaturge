package com.leclowndu93150.thaumaturge.content.focus.eldritch;

import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.AbstractHierophantSpell;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class FocusMediumHierophant implements FocusMedium {
    private final Identifier id;
    private final Function<Level, AbstractHierophantSpell> factory;

    public FocusMediumHierophant(Identifier id, Function<Level, AbstractHierophantSpell> factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public Identifier id() {
        return id;
    }
    @Override
    public int complexity(FocusSettings settings) {
        return 0;
    }
    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_NOTHING;
    }
    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("left", "focus.thaumaturge.eldritch_crescent.left", new SettingDefinition.IntRange(0, 1)));
    }

    @Override
    public @Nullable CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        final FocusPackage continuation = ctx.continuation();
        if (!(ctx.level() instanceof ServerLevel level) || !(ctx.caster() instanceof EntityEldritchHierophant caster) || continuation == null || incoming.trajectories() == null) {
            return null;
        }
        for (Trajectory trajectory : incoming.trajectories()) {
            final AbstractHierophantSpell spell = factory.apply(level);
            spell.cast(caster, continuation, trajectory.source(), trajectory.direction(), settings.value("left") == 1);
            level.addFreshEntity(spell);
        }
        return null;
    }
}

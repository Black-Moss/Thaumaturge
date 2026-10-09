package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record GainEntry(GainSubject subject, int life, GainRolls rolls) {
    private static final int THEORY_BONUS_TICKS = 10;

    public static GainEntry knowledge(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, int duration, long seed) {
        int life = type == KnowledgeType.THEORY ? duration + THEORY_BONUS_TICKS : duration;
        return new GainEntry(new KnowledgeSubject(type, category), life, GainRolls.of(seed));
    }

    public static GainEntry aspect(Holder<IAspect> aspect, int duration, long seed) {
        return new GainEntry(new AspectSubject(aspect), duration, GainRolls.of(seed));
    }
}

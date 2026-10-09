package com.leclowndu93150.thaumaturge.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * An extra page of text and recipes unlocked on a completed research entry.
 *
 * <p>The addendum is shown once the owning entry is complete and every research in
 * {@code requiredResearch} is complete. An empty required list means it is shown as soon as the
 * entry is complete. The record performs no validation or copying of the supplied lists.
 *
 * @param textKey the translation key of the page text
 * @param recipes the recipe identifiers appended to the recipes shown for the entry
 * @param requiredResearch the research entries that must be complete before the page is shown
 * @since 1.0.0
 */
public record ResearchAddendum(String textKey, List<Identifier> recipes, List<Identifier> requiredResearch) {
    /** Datapack codec. Requires {@code text}; {@code recipes} and {@code required_research} default to empty. */
    public static final Codec<ResearchAddendum> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.STRING.fieldOf("text").forGetter(ResearchAddendum::textKey), Identifier.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchAddendum::recipes),
                    Identifier.CODEC.listOf().optionalFieldOf("required_research", List.of()).forGetter(ResearchAddendum::requiredResearch))
            .apply(instance, ResearchAddendum::new));
}

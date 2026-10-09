package com.leclowndu93150.thaumaturge.api.capability;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The two kinds of category knowledge a player accumulates.
 *
 * <p>Each type keeps an independent raw counter per research category plus an uncategorised
 * counter. The visible level is the raw counter divided by {@link #progression()} with truncating
 * integer division. See {@link IPlayerKnowledge} for how the counters are stored.
 *
 * <p>The declaration order is load-bearing: {@link #STREAM_CODEC} encodes the ordinal, so
 * {@link #THEORY} is wire value 0 and {@link #OBSERVATION} is wire value 1. Appending a constant
 * is safe, reordering is not.
 *
 * @since 1.0.0
 */
public enum KnowledgeType implements StringRepresentable {
    /** Knowledge gained by study, one visible level per 32 raw points. */
    THEORY("theory", "T", 32),
    /** Knowledge gained by scanning, one visible level per 16 raw points. */
    OBSERVATION("observation", "O", 16);

    private static final String TRANSLATION_PREFIX = "knowledge_type.thaumaturge.";

    /**
     * Codec for datapack and save serialization. Accepts and emits the lowercase serialized name
     * only; an unknown string fails decoding.
     */
    public static final Codec<KnowledgeType> CODEC = StringRepresentable.fromEnum(KnowledgeType::values);

    /**
     * Network codec holding the ordinal as one variable-length integer. A decoded ordinal outside
     * the constant range fails instead of wrapping.
     */
    public static final StreamCodec<ByteBuf, KnowledgeType> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], KnowledgeType::ordinal);

    private final String serializedName;
    private final String abbreviation;
    private final int progression;

    KnowledgeType(String serializedName, String abbreviation, int progression) {
        this.serializedName = serializedName;
        this.abbreviation = abbreviation;
        this.progression = progression;
    }

    /**
     * Returns the lowercase name used in saves, commands and texture paths.
     *
     * @return the stable serialized name
     */
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Returns the single capital letter used by HUD overlays and persisted map keys.
     *
     * @return the stable abbreviation
     */
    public String abbreviation() {
        return abbreviation;
    }

    /**
     * Returns the number of raw points that make up one visible level.
     *
     * @return the positive progression divisor
     */
    public int progression() {
        return progression;
    }

    /**
     * Returns the translation key of the display name.
     *
     * @return {@code knowledge_type.thaumaturge.} followed by the serialized name
     */
    public String translationKey() {
        return TRANSLATION_PREFIX + serializedName;
    }
}

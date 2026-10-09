package com.leclowndu93150.thaumaturge.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Per-entry display and unlock flags of a research entry.
 *
 * <p>The declaration order is the iteration order of an entry's meta set and the order of the
 * written {@code meta} list in data. The ordinal is never persisted or transmitted.
 *
 * @since 1.0.0
 */
public enum ResearchEntryMeta implements StringRepresentable {
    /** Draws the node frame round. Wins over {@link #HEX} when both are set. */
    ROUND("round"),
    /** Draws an additional spiky overlay frame on top of the base shape. */
    SPIKY("spiky"),
    /** Reverses the direction of the connector lines of the entry. */
    REVERSE("reverse"),
    /** Draws the hidden frame variant and hides the entry until it can be unlocked. */
    HIDDEN("hidden"),
    /** Adds the entry to the player record without player action when its conditions pass. */
    AUTOUNLOCK("autounlock"),
    /** Draws the node frame hexagonal. */
    HEX("hex");

    /** Codec reading and writing the lowercase serialized name. */
    public static final Codec<ResearchEntryMeta> CODEC = StringRepresentable.fromEnum(ResearchEntryMeta::values);

    private final String serialized;

    ResearchEntryMeta(String serialized) {
        this.serialized = serialized;
    }

    /**
     * The lowercase name used in data.
     *
     * @return the serialized name
     */
    @Override
    public String getSerializedName() {
        return serialized;
    }
}

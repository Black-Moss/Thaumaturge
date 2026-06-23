package com.leclowndu93150.thaumcraft.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Per-entry meta flag controlling Thaumonomicon display and unlock behaviour.
 *
 * @since 1.0.0
 */
public enum ResearchEntryMeta implements StringRepresentable {
    /** Entry icon is drawn with a circular frame. */
    ROUND("round"),
    /** Entry icon is drawn with a spiky frame, used by eldritch entries. */
    SPIKY("spiky"),
    /** Entry connector lines are drawn in reverse order. */
    REVERSE("reverse"),
    /** Entry is hidden from the grid until unlocked. */
    HIDDEN("hidden"),
    /** Entry is automatically added to the player's record on load. */
    AUTOUNLOCK("autounlock"),
    /** Entry icon is drawn with a hexagonal frame. */
    HEX("hex");

    /** Codec for datapack serialization. */
    public static final Codec<ResearchEntryMeta> CODEC = StringRepresentable.fromEnum(ResearchEntryMeta::values);

    private final String name;

    ResearchEntryMeta(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

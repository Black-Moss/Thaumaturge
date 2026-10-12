package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;

/**
 * A reference from a research entry to one of its parent entries.
 *
 * <p>The serialized form is a single string: {@code "namespace:path"} requires the parent to be
 * fully complete, {@code "namespace:path@2"} requires the parent to have reached stage 2
 * (one-based: {@code @1} means merely started), and a leading {@code "~"} marks the reference as
 * inherit-only, suppressing the connector line in the Thaumonomicon while keeping the progression
 * requirement.
 *
 * @param id the parent entry identifier
 * @param stage the minimum parent stage required, or {@code 0} to require full completion
 * @param inherit whether the Thaumonomicon suppresses the connector line for this reference
 * @since 1.0.0
 */
public record ResearchParent(Identifier id, int stage, boolean inherit) {
    /** String codec using the {@code ~}/{@code @stage} prefix and suffix conventions. */
    public static final Codec<ResearchParent> CODEC = Codec.STRING.comapFlatMap(ResearchParent::parse, ResearchParent::serialize);

    /**
     * Creates a reference requiring full completion of the parent.
     *
     * @param id the parent entry identifier
     * @return the reference
     */
    public static ResearchParent of(Identifier id) {
        return new ResearchParent(id, 0, false);
    }

    /**
     * Tests whether a player's research progress meets this reference. A reference with a stage is met by reaching
     * that stage or by completing the parent outright; one without a stage only by completing the parent.
     *
     * @param research the player's research progress
     * @return {@code true} when the reference is met
     * @since 1.1.0
     */
    public boolean isSatisfiedBy(PlayerResearch research) {
        if (research.hasCompleted(id)) {
            return true;
        }
        return stage > 0 && research.hasReachedStage(id, stage - 1);
    }

    /**
     * Tests whether the given knowledge satisfies this reference. A stage-qualified reference is
     * satisfied by either reaching the stage or completing the parent outright.
     *
     * @param knowledge the player knowledge to test
     * @return {@code true} when satisfied
     * @deprecated use {@link #isSatisfiedBy(PlayerResearch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public boolean isSatisfiedBy(IPlayerKnowledge knowledge) {
        return isSatisfiedBy((PlayerResearch) knowledge);
    }

    private static DataResult<ResearchParent> parse(String value) {
        boolean inherit = value.startsWith("~");
        String body = inherit ? value.substring(1) : value;
        int at = body.lastIndexOf('@');
        int stage = 0;
        if (at >= 0) {
            try {
                stage = Integer.parseInt(body.substring(at + 1));
            } catch (NumberFormatException e) {
                return DataResult.error(() -> "Invalid parent stage in '" + value + "'");
            }
            body = body.substring(0, at);
        }
        Identifier id = Identifier.tryParse(body);
        if (id == null) {
            return DataResult.error(() -> "Invalid parent identifier '" + value + "'");
        }
        return DataResult.success(new ResearchParent(id, stage, inherit));
    }

    private String serialize() {
        return (inherit ? "~" : "") + id + (stage > 0 ? "@" + stage : "");
    }
}

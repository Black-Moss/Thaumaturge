package com.leclowndu93150.thaumcraft.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

/**
 * Icon shown on a research entry's node in the Thaumonomicon. An icon is either a full texture
 * path or an item identifier; entries may declare several icons, which the browser cycles
 * through while the node is visible.
 *
 * <p>The serialized form is a single string. Strings ending in {@code .png} are treated as
 * texture paths; anything else is treated as an item identifier, matching the legacy research
 * JSON convention.
 *
 * @param id the texture path or item identifier
 * @param texture whether {@link #id} names a texture rather than an item
 * @since 1.0.0
 */
public record ResearchIcon(Identifier id, boolean texture) {
    /** String codec using the {@code .png}-suffix convention to distinguish textures from items. */
    public static final Codec<ResearchIcon> CODEC = Codec.STRING.xmap(ResearchIcon::parse, ResearchIcon::serialize);

    /**
     * Creates a texture icon.
     *
     * @param id the full texture path, e.g. {@code thaumcraft:textures/research/r_wisp.png}
     * @return the icon
     */
    public static ResearchIcon ofTexture(Identifier id) {
        return new ResearchIcon(id, true);
    }

    /**
     * Creates an item icon.
     *
     * @param id the item identifier
     * @return the icon
     */
    public static ResearchIcon ofItem(Identifier id) {
        return new ResearchIcon(id, false);
    }

    private static ResearchIcon parse(String value) {
        Identifier id = Identifier.parse(value);
        return new ResearchIcon(id, id.getPath().endsWith(".png"));
    }

    private static String serialize(ResearchIcon icon) {
        return icon.id.toString();
    }
}

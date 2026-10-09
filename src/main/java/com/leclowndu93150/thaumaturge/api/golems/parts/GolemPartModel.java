package com.leclowndu93150.thaumaturge.api.golems.parts;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * The visual description of a golem part: a mesh, an optional fallback texture and the place it attaches to.
 *
 * <p>Object groups of the mesh whose names start with {@code bm} take the golem material's texture;
 * all other groups take the fallback texture, unless the mesh names a texture slot itself.
 *
 * @apiNote instances use identity equality. Client render hooks are keyed by model instance, so a model must be created
 *          once and reused.
 * @since 1.0.0
 */
public final class GolemPartModel {
    private static final String MATERIAL_GROUP_PREFIX = "bm";

    private final Descriptor descriptor;

    /**
     * @param objModel    the location of the mesh, in the mod's open mesh format
     * @param texture     the fallback texture, or null when the mesh supplies every texture
     * @param attachPoint where the part attaches to the golem
     */
    public GolemPartModel(Identifier objModel, @Nullable Identifier texture, AttachPoint attachPoint) {
        this.descriptor = new Descriptor(objModel, texture, attachPoint);
    }

    /**
     * @return the location of the mesh
     */
    public Identifier objModel() {
        return descriptor.mesh();
    }

    /**
     * @return the fallback texture, or null
     */
    public @Nullable Identifier texture() {
        return descriptor.fallback();
    }

    /**
     * @return where the part attaches to the golem
     */
    public AttachPoint attachPoint() {
        return descriptor.anchor();
    }

    /**
     * @param groupName the name of a mesh object group
     * @return whether the group is drawn with the golem material's texture
     */
    public boolean useMaterialTextureForObjectPart(String groupName) {
        return groupName.regionMatches(0, MATERIAL_GROUP_PREFIX, 0, MATERIAL_GROUP_PREFIX.length());
    }

    private record Descriptor(Identifier mesh, @Nullable Identifier fallback, AttachPoint anchor) {
    }

    /**
     * The part of the golem a model attaches to.
     *
     * @since 1.0.0
     */
    public enum AttachPoint {
        /** The arms. */
        ARMS,
        /** The legs. */
        LEGS,
        /** The body. */
        BODY,
        /** The head. */
        HEAD
    }

    /**
     * Which side of the golem a limb part sits on.
     *
     * @since 1.0.0
     */
    public enum LimbSide {
        /** The left limb. */
        LEFT,
        /** The right limb. */
        RIGHT,
        /** A single, unpaired part. */
        MIDDLE
    }
}

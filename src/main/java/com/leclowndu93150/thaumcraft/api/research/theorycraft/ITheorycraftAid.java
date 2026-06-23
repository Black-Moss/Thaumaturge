package com.leclowndu93150.thaumcraft.api.research.theorycraft;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

/**
 * A theorycraft aid. Aids are environmental boosts the research table grants when certain
 * structures are present nearby; each aid adds one or more {@link TheorycraftCard cards} to the
 * player's aid pool, which feeds draws alongside the base deck.
 *
 * <p>Aids are registered to the {@code thaumcraft:theorycraft_aid} registry. The manager scans
 * the table's surroundings on session start, asks each registered aid whether it
 * {@link #matches matches} the environment, and pools the matching aids' cards.
 *
 * @since 1.0.0
 */
public interface ITheorycraftAid {
    /**
     * Cards this aid contributes to the player's aid pool when matched.
     *
     * @return the contributed card registry keys, never empty
     */
    List<ResourceKey<CardFactory>> cards();

    /**
     * Whether this aid is satisfied by the environment around the research table at the given
     * position. Implementations must restrict their checks to chunks that are already loaded;
     * the manager guarantees the table's own chunk is loaded but neighbours may not be.
     *
     * @param level the level accessor
     * @param tablePos the position of the research table
     * @return {@code true} when this aid matches
     */
    boolean matches(LevelAccessor level, BlockPos tablePos);

    /**
     * Display stack used to render this aid in the research table UI.
     *
     * @return the icon stack; never null, may be empty
     */
    ItemStack displayStack();

    /**
     * Localized description shown on hover.
     *
     * @return the localized description
     */
    Component description();
}

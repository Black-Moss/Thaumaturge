package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * Something that shows, before it acts, the blocks an item is about to affect.
 *
 * <p>While a player holds an item that implements this interface, the client traces an anchor block with
 * {@link #traceAnchor}, asks {@link #previewArea} for the affected positions around it, outlines every one of them with
 * a white frame drawn through other blocks, and draws glowing arrows on the anchor for each axis that
 * {@link #showsAxisArrows} accepts. Thaumaturge's wands implement it and answer through the architect delivery of their
 * socketed focus; the elemental shovel implements it for the blocks it would place.
 *
 * <p>Spell behaviours may implement it too. A caster answers through the first behaviour in its focus's spell that
 * implements this interface.
 *
 * @implNote Thaumaturge calls every method on the logical client, once per frame or a few times a second while the
 * item is held, so implementations must be cheap and must not change the world. A spell behaviour that implements this
 * interface also lets the caster's area keys adjust the caster's stored area.
 * @since 1.1.0
 */
public interface AreaPreviewer {
    /**
     * Traces the block the preview is anchored on.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param holder the entity holding the stack
     * @return the trace result, or {@code null} when no preview can be anchored right now; only a block hit anchors a
     *     preview
     * @since 1.1.0
     */
    @Nullable
    HitResult traceAnchor(ItemStack stack, Level level, LivingEntity holder);

    /**
     * Tests whether the vanilla block outline is hidden while the preview has at least one position. The preview
     * itself is drawn either way.
     *
     * @param stack the held stack
     * @return {@code true} to hide the vanilla outline while a preview is shown
     * @since 1.1.0
     */
    boolean hidesBlockOutline(ItemStack stack);

    /**
     * Returns the positions the item would affect if used on the anchor block now.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param anchor the traced block position
     * @param face the traced block face
     * @param player the player holding the stack
     * @return the affected positions in any order, without duplicates; empty when nothing would be affected
     * @since 1.1.0
     */
    List<BlockPos> previewArea(ItemStack stack, Level level, BlockPos anchor, Direction face, Player player);

    /**
     * Tests whether the arrows for one world axis are drawn on the anchor block, showing that the area along that axis
     * is the one being adjusted.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param player the player holding the stack
     * @param face the traced block face
     * @param axis the axis being asked about
     * @return {@code true} to draw the arrows for that axis
     * @since 1.1.0
     */
    boolean showsAxisArrows(ItemStack stack, Level level, Player player, Direction face, Direction.Axis axis);
}

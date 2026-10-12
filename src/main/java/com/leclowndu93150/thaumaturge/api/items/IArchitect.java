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
 * An item that previews a set of block positions it is about to affect, rendered client-side
 * as a white grid overlay while the item is held.
 *
 * <p>Implemented by the caster's gauntlet, which delegates to an architect-capable focus medium
 * such as Plan.
 *
 * <p>Since 1.1.0 every method here and every {@link AreaPreviewer} method has a default that answers through its
 * counterpart, so an implementation overrides one set or the other. An implementation written against 1.0.0 overrides
 * the methods declared here and is an {@link AreaPreviewer} as well. Thaumaturge's wands implement the
 * {@link AreaPreviewer} methods and still implement this interface, so code that tests for it keeps finding them.
 *
 * @apiNote An implementation that overrides neither set recurses without end on the first call.
 * @since 1.0.0
 * @deprecated use {@link AreaPreviewer}. This interface extends it and is removed in 2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface IArchitect extends AreaPreviewer {
    /**
     * Traces the block this architect anchors its preview on. Called on the client every frame while the item is held.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param caster the entity holding the item
     * @return the trace result, or null when the item cannot anchor a preview right now
     * @deprecated use {@link AreaPreviewer#traceAnchor}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @Nullable
    default HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        return traceAnchor(stack, level, caster);
    }

    /**
     * Whether the preview replaces the vanilla block highlight instead of drawing during the level render pass.
     *
     * @param stack the architect item stack
     * @return true to draw in place of the vanilla block outline
     * @deprecated use {@link AreaPreviewer#hidesBlockOutline}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean replacesBlockHighlight(ItemStack stack) {
        return hidesBlockOutline(stack);
    }

    /**
     * The block positions the item would currently affect, anchored at the traced block.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param pos    the traced block position
     * @param side   the traced block face
     * @param player the player holding the item
     * @return the affected positions; empty when nothing would be affected
     * @deprecated use {@link AreaPreviewer#previewArea}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        return previewArea(stack, level, pos, side, player);
    }

    /**
     * Whether the axis indicator for the given axis should render at the anchor block.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param player the player holding the item
     * @param side   the traced block face
     * @param axis   the axis being queried
     * @return true to render the indicator for that axis
     * @deprecated use {@link AreaPreviewer#showsAxisArrows}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return showsAxisArrows(stack, level, player, side, axis);
    }

    /**
     * Answers {@link AreaPreviewer#traceAnchor} through {@link #aim}.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param holder the entity holding the stack
     * @return the result of {@link #aim}
     * @since 1.1.0
     */
    @Override
    @Nullable
    default HitResult traceAnchor(ItemStack stack, Level level, LivingEntity holder) {
        return aim(stack, level, holder);
    }

    /**
     * Answers {@link AreaPreviewer#hidesBlockOutline} through {@link #replacesBlockHighlight}.
     *
     * @param stack the held stack
     * @return the result of {@link #replacesBlockHighlight}
     * @since 1.1.0
     */
    @Override
    default boolean hidesBlockOutline(ItemStack stack) {
        return replacesBlockHighlight(stack);
    }

    /**
     * Answers {@link AreaPreviewer#previewArea} through {@link #previewBlocks}.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param anchor the traced block position
     * @param face the traced block face
     * @param player the player holding the stack
     * @return the result of {@link #previewBlocks}
     * @since 1.1.0
     */
    @Override
    default List<BlockPos> previewArea(ItemStack stack, Level level, BlockPos anchor, Direction face, Player player) {
        return previewBlocks(stack, level, anchor, face, player);
    }

    /**
     * Answers {@link AreaPreviewer#showsAxisArrows} through {@link #showsAxis}.
     *
     * @param stack the held stack
     * @param level the level of the holder
     * @param player the player holding the stack
     * @param face the traced block face
     * @param axis the axis being asked about
     * @return the result of {@link #showsAxis}
     * @since 1.1.0
     */
    @Override
    default boolean showsAxisArrows(ItemStack stack, Level level, Player player, Direction face, Direction.Axis axis) {
        return showsAxis(stack, level, player, face, axis);
    }
}

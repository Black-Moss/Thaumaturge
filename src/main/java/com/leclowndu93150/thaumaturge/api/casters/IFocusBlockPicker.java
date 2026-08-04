package com.leclowndu93150.thaumaturge.api.casters;

/**
 * Marker for focus items whose behavior depends on a remembered block. While a focus
 * implementing this marker is socketed, sneak-right-clicking a block with the caster stores
 * that block's state on the caster, retrievable through
 * {@link ICaster#getPickedBlock(net.minecraft.world.item.ItemStack)}.
 *
 * @since 1.0.0
 */
public interface IFocusBlockPicker {}

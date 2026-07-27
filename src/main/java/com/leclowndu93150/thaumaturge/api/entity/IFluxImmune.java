package com.leclowndu93150.thaumaturge.api.entity;

/**
 * Marker interface for items that grant their wearer immunity from flux-related effects when
 * equipped in any armor or hand slot.
 *
 * <p>An entity counts as flux-immune when any item in their equipment slots implements this
 * interface; see {@code FluxImmunityHelper.isImmune}.
 *
 * @since 1.0.0
 */
public interface IFluxImmune {
}

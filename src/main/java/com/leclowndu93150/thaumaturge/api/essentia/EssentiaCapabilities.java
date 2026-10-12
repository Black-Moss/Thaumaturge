package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

/**
 * Registry-style holder for the essentia capability types.
 *
 * <p>{@link #CONDUIT} is the sided block capability that exposes {@link IEssentiaConduit}; it
 * accepts a nullable {@link Direction} context the way vanilla item / fluid handlers do. Register
 * conduits through {@link #registerConduit} or {@link #registerConduitBlock}, which also answer
 * the deprecated {@link #TRANSPORT} capability so consumers written against 1.0 still find them.
 *
 * <p>{@link #STORAGE} is the sided block capability for {@link IEssentiaStorage}: listable,
 * transactional storage bound to the queried side. Querying with a {@code null} side returns no
 * view.
 *
 * <p>{@link #CONTAINER} is the item capability for {@link IItemEssentia}; it requires no context and returns a view bound to the
 * queried stack.
 *
 * <p>{@link #ITEM_STORAGE} is the item capability for {@link IEssentiaItemStorage}, present only
 * on items automation may fill and drain. Scan-only aspect items do not expose it.
 *
 * @since 1.0.0
 */
public final class EssentiaCapabilities {
    /**
     * Sided block capability for essentia transport.
     *
     * @deprecated Look up {@link #CONDUIT} through {@link EssentiaAccess#conduit} instead, and
     *     register providers with {@link #registerConduit} or {@link #registerConduitBlock}. Every
     *     conduit registered that way is also served here, wrapped as an {@link
     *     IEssentiaTransport}. Removed in 2.0.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public static final BlockCapability<IEssentiaTransport, Direction> TRANSPORT = BlockCapability.createSided(TTIds.rl("essentia_transport"), IEssentiaTransport.class);

    /**
     * Sided block capability for essentia conduits.
     *
     * @since 1.1.0
     */
    public static final BlockCapability<IEssentiaConduit, Direction> CONDUIT = BlockCapability.createSided(TTIds.rl("essentia_conduit"), IEssentiaConduit.class);

    /** Sided block capability for listable, transactional essentia storage. */
    public static final BlockCapability<IEssentiaStorage, Direction> STORAGE = BlockCapability.createSided(TTIds.rl("essentia_storage"), IEssentiaStorage.class);

    /** Item capability for the essentia a stack carries. */
    public static final ItemCapability<IItemEssentia, Void> CONTAINER = ItemCapability.createVoid(TTIds.rl("essentia_container"), IItemEssentia.class);

    /** Item capability for essentia items automation may fill and drain. */
    public static final ItemCapability<IEssentiaItemStorage, Void> ITEM_STORAGE = ItemCapability.createVoid(TTIds.rl("essentia_item_storage"), IEssentiaItemStorage.class);

    /** Sided block capability for synthetic aspect queries (filters, routing intents). */
    public static final BlockCapability<IAspectQuery, Direction> ASPECT_QUERY = BlockCapability.createSided(TTIds.rl("aspect_query"), IAspectQuery.class);

    private EssentiaCapabilities() {}

    /**
     * Registers a block entity's conduit under {@link #CONDUIT}, and under the deprecated {@link
     * #TRANSPORT} as a view that forwards every call to the same conduit.
     *
     * <p>The provider is called once per lookup of either capability. It receives a {@code null}
     * side when the caller asks about the whole device.
     *
     * @param event    the capability registration event
     * @param type     the block entity type
     * @param provider returns the conduit for a block entity and side, or {@code null} for none
     * @param <BE>     the block entity class
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    public static <BE extends BlockEntity> void registerConduit(RegisterCapabilitiesEvent event, BlockEntityType<BE> type, ICapabilityProvider<? super BE, @Nullable Direction, IEssentiaConduit> provider) {
        event.registerBlockEntity(CONDUIT, type, provider);
        event.registerBlockEntity(TRANSPORT, type, (be, side) -> ConduitAsTransport.view(provider.getCapability(be, side)));
    }

    /**
     * Registers a block's conduit under {@link #CONDUIT}, and under the deprecated {@link
     * #TRANSPORT} as a view that forwards every call to the same conduit. Use this for blocks whose
     * conduit is not their own block entity, such as one face of a multiblock.
     *
     * @param event    the capability registration event
     * @param provider returns the conduit for a position and side, or {@code null} for none
     * @param blocks   the blocks to register for
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    public static void registerConduitBlock(RegisterCapabilitiesEvent event, IBlockCapabilityProvider<IEssentiaConduit, @Nullable Direction> provider, Block... blocks) {
        event.registerBlock(CONDUIT, provider, blocks);
        event.registerBlock(TRANSPORT, (level, pos, state, be, side) -> ConduitAsTransport.view(provider.getCapability(level, pos, state, be, side)), blocks);
    }
}

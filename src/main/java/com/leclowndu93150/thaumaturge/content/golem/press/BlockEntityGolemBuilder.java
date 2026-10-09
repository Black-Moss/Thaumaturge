package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityGolemBuilder extends AbstractSyncedBlockEntity implements IEssentiaTransport, MenuProvider {
    public static final int SLOT_OUTPUT = 0;

    private static final String KEY_GOLEM = "golem";
    private static final String KEY_COST = "cost";
    private static final String KEY_MAX_COST = "mcost";
    private static final int OUTPUT_SLOTS = 1;
    private static final int CYCLE_INTERVAL_TICKS = 5;
    private static final int WORKING_SUCTION = 128;
    private static final int UNIT = 1;
    private static final int NOTHING = 0;
    private static final boolean PASSIVE_OUTPUT = false;
    private static final int COST_PER_TRAIT = 2;
    private static final int RANK_ZERO = 0;
    private static final float START_VOLUME = 0.25F;
    private static final float DELIVER_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final Direction[] DRAIN_ORDER = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final int PRESS_MAX = 90;
    private static final int PRESS_RISE = 6;
    private static final int PRESS_FALL = 3;
    private static final int BURST_THRESHOLD = 60;
    private static final int BURST_VENTS = 16;
    private static final int SETTLE_VENTS = 10;
    private static final int IDLE_VENT_ONE_IN = 8;
    private static final float BURST_VOLUME = 0.66F;
    private static final float IDLE_VOLUME = 0.1F;
    private static final float HISS_PITCH_SPREAD = 0.1F;
    private static final double VENT_SPREAD = 0.1;
    private static final int VENT_COLOR = 0xAAAAAA;
    private static final float VENT_SCALE = 1.0F;
    private static final double CENTER = 0.5;
    private static final int RENDER_REACH_LOW = 1;
    private static final int RENDER_REACH_HIGH = 2;
    private static final int RENDER_HEIGHT = 2;

    public int press;
    public boolean @Nullable [] hasStuff;

    private final OutputSlot outputHandler = new OutputSlot();
    private @Nullable GolemProperties pending;
    private int cost;
    private int totalCost;
    private int ticks;
    private boolean buffered;

    public BlockEntityGolemBuilder(BlockPos pos, BlockState state) {
        super(TTBlockEntities.GOLEM_BUILDER.get(), pos, state);
    }

    public ItemStacksResourceHandler outputHandler() {
        return outputHandler;
    }

    public int cost() {
        return cost;
    }

    public int totalCost() {
        return totalCost;
    }

    public @Nullable GolemProperties pendingGolem() {
        return pending;
    }

    private boolean isWorking() {
        return cost > 0 && pending != null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityGolemBuilder builder) {
        if (builder.ticks++ % CYCLE_INTERVAL_TICKS == 0) {
            builder.runCycle(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityGolemBuilder builder) {
        builder.animate(level, pos);
    }

    private void runCycle(Level level) {
        if (!isWorking() || !payUnit(level)) {
            return;
        }
        cost--;
        setChanged();
        if (cost <= 0) {
            deliver(level);
        }
    }

    private boolean payUnit(Level level) {
        if (buffered) {
            buffered = false;
            return true;
        }
        Holder<IAspect> machina = EssentiaTransportHelper.resolve(level, TTAspects.MACHINA);
        if (machina == null) {
            return false;
        }
        for (Direction side : DRAIN_ORDER) {
            IEssentiaTransport source = drainSource(level, side);
            if (source == null) {
                continue;
            }
            Direction facing = side.getOpposite();
            if (!source.canOutputTo(facing)) {
                break;
            }
            if (source.takeEssentia(machina, UNIT, facing) > 0) {
                return true;
            }
        }
        return false;
    }

    private @Nullable IEssentiaTransport drainSource(Level level, Direction side) {
        BlockPos neighbour = worldPosition.relative(side);
        if (!isConnectable(side) || !level.hasChunkAt(neighbour)) {
            return null;
        }
        Direction facing = side.getOpposite();
        IEssentiaTransport candidate = level.getCapability(EssentiaCapabilities.TRANSPORT, neighbour, facing);
        boolean usable = candidate != null && candidate.isConnectable(facing) && candidate.getSuctionAmount(facing) < WORKING_SUCTION;
        return usable ? candidate : null;
    }

    private void deliver(Level level) {
        GolemProperties design = pending;
        if (design == null) {
            return;
        }
        ItemStack placer = placerFor(design);
        if (!outputAccepts(placer)) {
            return;
        }
        outputHandler.set(SLOT_OUTPUT, ItemResource.of(placer), outputHandler.getAmountAsInt(SLOT_OUTPUT) + 1);
        level.playSound(null, worldPosition, TTSounds.WAND.get(), SoundSource.BLOCKS, DELIVER_VOLUME, SOUND_PITCH);
        resetJob();
        setChangedAndSync();
    }

    private void resetJob() {
        cost = 0;
        totalCost = 0;
        pending = null;
    }

    private static ItemStack placerFor(GolemProperties design) {
        ItemStack placer = new ItemStack(TTItems.GOLEM_PLACER.get());
        placer.set(TTDataComponents.GOLEM_PROPERTIES.get(), design);
        return placer;
    }

    private boolean outputAccepts(ItemStack placer) {
        int held = outputHandler.getAmountAsInt(SLOT_OUTPUT);
        return held == 0 || outputHandler.getResource(SLOT_OUTPUT).matches(placer) && held < placer.getMaxStackSize();
    }

    public boolean[] stockedComponents(GolemProperties props) {
        List<ItemStack> components = props.components();
        boolean[] available = new boolean[components.size()];
        Level world = level;
        if (world != null) {
            int slot = 0;
            for (ItemStack component : components) {
                available[slot++] = InvHelper.checkAdjacentChests(world, worldPosition, component);
            }
        }
        return available;
    }

    public boolean beginAssembly(GolemProperties props, Player player) {
        if (level == null || level.isClientSide() || cost != 0) {
            return false;
        }
        if (!props.isKnownBy(KnowledgeAccess.of(player))) {
            return false;
        }
        GolemProperties design = props.withRank(RANK_ZERO);
        if (!outputAccepts(placerFor(design))) {
            return abandonJob();
        }
        ItemStack[] components = props.components().toArray(ItemStack[]::new);
        if (!InvHelper.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player, true, components)) {
            return abandonJob();
        }
        InvHelper.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player, false, components);
        int total = COST_PER_TRAIT * props.traits().size();
        for (ItemStack component : components) {
            total += component.getCount();
        }
        totalCost = total;
        cost = total;
        pending = design;
        setChangedAndSync();
        level.playSound(null, worldPosition, TTSounds.WAND.get(), SoundSource.BLOCKS, START_VOLUME, SOUND_PITCH);
        return true;
    }

    private boolean abandonJob() {
        resetJob();
        setChanged();
        return false;
    }

    private void animate(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        double x = pos.getX() + CENTER;
        double y = pos.getY();
        double z = pos.getZ() + CENTER;
        if (isWorking()) {
            if (press < PRESS_MAX) {
                press = Math.min(PRESS_MAX, press + PRESS_RISE);
                if (press >= BURST_THRESHOLD) {
                    hiss(level, x, y + CENTER, z, BURST_VOLUME, random);
                    vent(level, pos, BURST_VENTS, random);
                }
            } else if (random.nextInt(IDLE_VENT_ONE_IN) == 0) {
                hiss(level, x, y + CENTER, z, IDLE_VOLUME, random);
                vent(level, pos, UNIT, random);
            }
            return;
        }
        if (press >= PRESS_MAX) {
            vent(level, pos, SETTLE_VENTS, random);
        }
        press = Math.max(0, press - PRESS_FALL);
    }

    private static void hiss(Level level, double x, double y, double z, float volume, RandomSource random) {
        level.playLocalSound(x, y, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, volume, SOUND_PITCH + random.nextFloat() * HISS_PITCH_SPREAD, false);
    }

    private static void vent(Level level, BlockPos pos, int count, RandomSource random) {
        for (int i = 0; i < count; i++) {
            VentParticleOptions options = new VentParticleOptions(random.nextGaussian() * VENT_SPREAD, 0.0, random.nextGaussian() * VENT_SPREAD, VENT_COLOR, VENT_SCALE, false);
            level.addParticle(options, pos.getX() + CENTER, pos.getY() + 1, pos.getZ() + CENTER, 0.0, 0.0, 0.0);
        }
    }

    public AABB renderBounds() {
        return new AABB(worldPosition.getX() - RENDER_REACH_LOW, worldPosition.getY(), worldPosition.getZ() - RENDER_REACH_LOW, worldPosition.getX() + RENDER_REACH_HIGH,
                worldPosition.getY() + RENDER_HEIGHT, worldPosition.getZ() + RENDER_REACH_HIGH);
    }

    @Override
    public Component getDisplayName() {
        return TTBlocks.GOLEM_BUILDER.get().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuGolemBuilder(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt(KEY_MAX_COST, totalCost);
        out.putInt(KEY_COST, cost);
        out.storeNullable(KEY_GOLEM, GolemProperties.CODEC, pending);
        outputHandler.serialize(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        totalCost = in.getIntOr(KEY_MAX_COST, 0);
        cost = in.getIntOr(KEY_COST, 0);
        pending = in.read(KEY_GOLEM, GolemProperties.CODEC).orElse(null);
        outputHandler.deserialize(in);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        boolean acceptable = isWorking() && !buffered && amount > 0 && aspect.is(TTAspects.MACHINA);
        buffered = buffered || acceptable;
        return acceptable ? UNIT : NOTHING;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return isWorking() ? WORKING_SUCTION : NOTHING;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return EssentiaTransportHelper.resolve(level, TTAspects.MACHINA);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return PASSIVE_OUTPUT;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return NOTHING;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return NOTHING;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getMinimumSuction() {
        return NOTHING;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    private final class OutputSlot extends ItemStacksResourceHandler {
        OutputSlot() {
            super(OUTPUT_SLOTS);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TTItems.GOLEM_PLACER.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}

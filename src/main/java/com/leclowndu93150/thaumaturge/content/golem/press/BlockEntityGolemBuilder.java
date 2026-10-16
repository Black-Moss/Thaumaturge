package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
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

public final class BlockEntityGolemBuilder extends AbstractSyncedBlockEntity implements IEssentiaConduit, MenuProvider {
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
    private static final double CENTER = 0.5;
    private static final int RENDER_REACH_LOW = 1;
    private static final int RENDER_REACH_HIGH = 2;
    private static final int RENDER_HEIGHT = 2;
    private static final int PRESS_DOWN = 90;
    private static final int PRESS_DESCENT_STEP = 6;
    private static final int PRESS_RISE_STEP = 3;
    private static final int PRESS_HISS_FROM = 60;
    private static final double STEAM_LIFT = 1.0D;
    private static final double STEAM_SIDE_SPREAD = 0.1D;
    private static final int STEAM_COLOR = 0xAAAAAA;
    private static final float STEAM_SCALE = 1.0F;
    private static final int STROKE_PUFFS = 16;
    private static final int SETTLE_PUFFS = 10;
    private static final int HELD_VENT_ONE_IN = 8;
    private static final float HISS_VOLUME = 0.66F;
    private static final float HELD_HISS_VOLUME = 0.1F;
    private static final float HISS_PITCH = 1.0F;
    private static final float HISS_PITCH_SPREAD = 0.1F;

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
        if (pending == null) {
            return;
        }
        if (cost > 0 && payUnit(level)) {
            cost--;
            if (cost > 0) {
                setChanged();
                return;
            }
            setChangedAndSync();
        }
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
            IEssentiaConduit source = drainSource(level, side);
            if (source != null && source.extract(machina, UNIT, side.getOpposite(), false) == UNIT) {
                return true;
            }
        }
        return false;
    }

    private void deliver(Level level) {
        GolemProperties finished = pending;
        if (finished == null) {
            return;
        }
        ItemStack placer = placerFor(finished);
        if (!outputAccepts(placer)) {
            return;
        }
        int held = outputHandler.getAmountAsInt(SLOT_OUTPUT);
        outputHandler.set(SLOT_OUTPUT, ItemResource.of(placer), held + UNIT);
        resetJob();
        setChangedAndSync();
        level.playSound(null, worldPosition, TTSounds.WAND.get(), SoundSource.BLOCKS, DELIVER_VOLUME, SOUND_PITCH);
    }

    private void animate(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        boolean working = isWorking();
        if (working && press < PRESS_DOWN) {
            press = Math.min(PRESS_DOWN, press + PRESS_DESCENT_STEP);
            if (press >= PRESS_HISS_FROM) {
                hiss(level, pos, random, HISS_VOLUME);
                vent(level, pos, random, STROKE_PUFFS);
            }
        }
        if (press >= PRESS_DOWN && random.nextInt(HELD_VENT_ONE_IN) == 0) {
            hiss(level, pos, random, HELD_HISS_VOLUME);
            vent(level, pos, random, 1);
        }
        if (press > 0 && !working) {
            if (press >= PRESS_DOWN) {
                vent(level, pos, random, SETTLE_PUFFS);
            }
            press = Math.max(0, press - PRESS_RISE_STEP);
        }
    }

    private static void hiss(Level level, BlockPos pos, RandomSource random, float volume) {
        float pitch = HISS_PITCH + random.nextFloat() * HISS_PITCH_SPREAD;
        level.playLocalSound(pos.getX() + CENTER, pos.getY() + CENTER, pos.getZ() + CENTER, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, volume, pitch, false);
    }

    private static void vent(Level level, BlockPos pos, RandomSource random, int puffs) {
        for (int i = 0; i < puffs; i++) {
            double driftX = random.nextGaussian() * STEAM_SIDE_SPREAD;
            double driftZ = random.nextGaussian() * STEAM_SIDE_SPREAD;
            VentParticleOptions steam = new VentParticleOptions(driftX, 0.0D, driftZ, STEAM_COLOR, STEAM_SCALE, false);
            level.addParticle(steam, pos.getX() + CENTER, pos.getY() + STEAM_LIFT, pos.getZ() + CENTER, 0.0D, 0.0D, 0.0D);
        }
    }

    private @Nullable IEssentiaConduit drainSource(Level level, Direction side) {
        BlockPos neighbour = worldPosition.relative(side);
        if (!connectsOn(side) || !level.hasChunkAt(neighbour)) {
            return null;
        }
        Direction facing = side.getOpposite();
        IEssentiaConduit candidate = EssentiaAccess.conduit(level, neighbour, facing);
        boolean usable = candidate != null && candidate.connectsOn(facing) && candidate.releasesOn(facing) && candidate.pull(facing).strength() < WORKING_SUCTION;
        return usable ? candidate : null;
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
                available[slot++] = InvHelper.neighboursHold(world, worldPosition, component);
            }
        }
        return available;
    }

    public boolean beginAssembly(GolemProperties props, Player player) {
        if (level == null || level.isClientSide() || cost != 0 || pending != null) {
            return false;
        }
        if (!props.isKnownBy(PlayerResearchAccess.of(player))) {
            return false;
        }
        GolemProperties design = props.withRank(RANK_ZERO);
        if (!outputAccepts(placerFor(design))) {
            return abandonJob();
        }
        ItemStack[] components = props.components().toArray(ItemStack[]::new);
        if (!InvHelper.takeFromNeighboursOrPlayer(level, worldPosition, player, true, components)) {
            return abandonJob();
        }
        InvHelper.takeFromNeighboursOrPlayer(level, worldPosition, player, false, components);
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
    public boolean connectsOn(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean receivesOn(Direction face) {
        return connectsOn(face);
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        boolean acceptable = isWorking() && !buffered && amount > 0 && aspect.is(TTAspects.MACHINA);
        if (!simulate) {
            buffered = buffered || acceptable;
        }
        return acceptable ? UNIT : NOTHING;
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        return isWorking() && !buffered && aspect.is(TTAspects.MACHINA) ? UNIT : NOTHING;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        return isWorking() ? EssentiaPull.of(EssentiaTransportHelper.resolve(level, TTAspects.MACHINA), WORKING_SUCTION) : EssentiaPull.NONE;
    }

    @Override
    public boolean releasesOn(Direction face) {
        return PASSIVE_OUTPUT;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return NOTHING;
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return NOTHING;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return null;
    }

    @Override
    public int pullThreshold() {
        return NOTHING;
    }

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

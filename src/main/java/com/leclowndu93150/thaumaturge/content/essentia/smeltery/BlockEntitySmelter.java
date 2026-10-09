package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class BlockEntitySmelter extends AbstractSyncedBlockEntity implements MenuProvider {
    public int fuelCapacity;
    public int fuelRemaining;
    public int cookElapsed;
    public int cookTarget;
    public AspectList essentiaStock = AspectList.EMPTY;
    public int vis;

    static final int INPUT_SLOT = 0;
    static final int FUEL_SLOT = 1;
    static final int SLOT_COUNT = 2;

    private static final int MAX_ESSENTIA = 256;
    private static final int DEFAULT_BURN_TIME = 200;
    private static final int BASE_COOK_PER_ESSENTIA = 2;
    private static final double BELLOWS_COOK_REDUCTION = 0.125;
    private static final double SPEED_BOOST_FACTOR = 0.8;
    private static final float VITIUM_RETENTION_FACTOR = 0.66F;
    private static final int VENT_ODDS = 3;
    private static final float VENT_SOUND_VOLUME = 0.25F;
    private static final float VENT_SOUND_PITCH_BASE = 2.6F;
    private static final float VENT_SOUND_PITCH_SPREAD = 0.8F;
    private static final int VENT_PUFFS = 4;
    private static final double VENT_PUFF_JITTER = 0.1;
    private static final double VENT_PUFF_SPEED = 0.25;
    private static final double VENT_PUFF_MOTION_JITTER = 0.1;
    private static final double VENT_FACE_OFFSET = 0.5;
    private static final int VENT_COLOR = 11184810;
    private static final String ASPECTS_KEY = "Aspects";
    private static final String BURN_TIME_KEY = "BurnTime";
    private static final String COOK_TIME_KEY = "CookTime";
    private static final String SMELT_TIME_KEY = "SmeltTime";
    private static final String CURRENT_BURN_TIME_KEY = "CurrentItemBurnTime";
    private static final String SPEED_BOOST_KEY = "SpeedBoost";
    private static final int UNKNOWN_BELLOWS = -1;

    private final SmelterItems items = new SmelterItems();
    private boolean speedBoost;
    private int tickCounter;
    private int bellows = UNKNOWN_BELLOWS;

    public BlockEntitySmelter(BlockPos pos, BlockState state) {
        this(TTBlockEntities.SMELTER.get(), pos, state);
    }

    protected BlockEntitySmelter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStacksResourceHandler itemSlots() {
        return items;
    }

    static boolean isInputValid(ItemStack stack) {
        return !stack.isEmpty() && !AspectIndexAccess.of(stack).isEmpty();
    }

    static boolean isFuelValid(Level level, ItemStack stack) {
        return !stack.isEmpty() && burnTimeOf(level, stack) > 0;
    }

    private static int burnTimeOf(Level level, ItemStack stack) {
        return stack.getBurnTime(null, level.fuelValues());
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntitySmelter smelter) {
        if (level instanceof ServerLevel server) {
            smelter.serverTick(server, pos, state);
        }
    }

    private void serverTick(ServerLevel server, BlockPos pos, BlockState state) {
        tickCounter++;
        if (bellows < 0) {
            refreshBellows();
        }
        boolean wasLit = state.getValue(BlockSmelter.LIT);
        boolean dirty = false;
        if (fuelRemaining > 0) {
            fuelRemaining--;
            dirty = true;
        }
        boolean canSmelt = canSmelt();
        if (fuelRemaining == 0 && canSmelt && igniteFuel(server)) {
            dirty = true;
        }
        if (fuelRemaining > 0 && canSmelt) {
            cookElapsed++;
            dirty = true;
            if (cookElapsed >= cookTarget) {
                cookElapsed = 0;
                smeltOne(server, pos, state);
                dirty = true;
            }
        } else {
            dirty |= cookElapsed != 0;
            cookElapsed = 0;
        }
        dirty |= handOverEssentia(server, pos);
        boolean lit = fuelRemaining > 0;
        if (lit != wasLit) {
            server.setBlock(pos, state.setValue(BlockSmelter.LIT, lit), Block.UPDATE_ALL);
            dirty = true;
        }
        if (dirty) {
            setChanged();
        }
        syncToClient();
    }

    private ItemStack stackIn(int slot) {
        return items.getResource(slot).toStack(items.getAmountAsInt(slot));
    }

    private void putStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            items.set(slot, ItemResource.EMPTY, 0);
        } else {
            items.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    private boolean canSmelt() {
        ItemStack input = stackIn(INPUT_SLOT);
        if (input.isEmpty()) {
            return false;
        }
        int total = AspectIndexAccess.of(input).totalAmount();
        int headroom = MAX_ESSENTIA - vis;
        if (total <= 0 || total > headroom) {
            return false;
        }
        double bellowsFactor = 1.0 - BELLOWS_COOK_REDUCTION * Math.max(bellows, 0);
        cookTarget = Math.max(1, (int) (total * BASE_COOK_PER_ESSENTIA * bellowsFactor));
        return true;
    }

    private boolean igniteFuel(Level level) {
        ItemStack fuel = stackIn(FUEL_SLOT);
        int burn = fuel.isEmpty() ? 0 : burnTimeOf(level, fuel);
        if (burn <= 0) {
            return false;
        }
        ItemStackTemplate leftover = fuel.getCraftingRemainder();
        boolean boosted = fuel.is(TTItems.ALUMENTUM.get());
        fuel.shrink(1);
        ItemStack remaining = fuel.isEmpty() && leftover != null ? leftover.create() : fuel;
        putStack(FUEL_SLOT, remaining);
        speedBoost = boosted;
        fuelCapacity = burn;
        fuelRemaining = burn;
        return true;
    }

    private void smeltOne(ServerLevel server, BlockPos pos, BlockState state) {
        ItemStack input = stackIn(INPUT_SLOT);
        RandomSource random = server.getRandom();
        float yield = essentiaYield();
        Direction[] vents = sidesWith(state.getValue(BlockSmelter.FACING), false);
        FateTally tally = new FateTally();
        for (AspectInstance entry : AspectIndexAccess.of(input).entries()) {
            float chance = entry.aspect().is(TTAspects.VITIUM) ? yield * VITIUM_RETENTION_FACTOR : yield;
            int keptBefore = tally.kept;
            for (int remaining = entry.amount(); remaining > 0; remaining--) {
                tally.record(rollFate(server, pos, vents, random, yield, chance));
            }
            int keptNow = tally.kept - keptBefore;
            if (keptNow > 0) {
                essentiaStock = essentiaStock.add(entry.aspect(), keptNow);
            }
        }
        input.shrink(1);
        putStack(INPUT_SLOT, input);
        vis = essentiaStock.totalAmount();
        pollute(server, pos, tally.polluted);
    }

    private static void pollute(ServerLevel server, BlockPos pos, int points) {
        if (points > 0) {
            AuraHelper.polluteAura(server, pos, points, true);
        }
    }

    private PointFate rollFate(ServerLevel server, BlockPos pos, Direction[] vents, RandomSource random, float yield, float chance) {
        if (yield >= 1.0F || random.nextFloat() < chance) {
            return PointFate.KEPT;
        }
        return ventAbsorbs(server, pos, vents, random) ? PointFate.VENTED : PointFate.POLLUTED;
    }

    private Direction[] sidesWith(Direction facing, boolean aux) {
        Direction[] found = new Direction[Direction.Plane.HORIZONTAL.length()];
        int count = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing && isPartFacingBack(side, aux)) {
                found[count++] = side;
            }
        }
        Direction[] result = new Direction[count];
        System.arraycopy(found, 0, result, 0, count);
        return result;
    }

    private boolean isPartFacingBack(Direction side, boolean aux) {
        BlockState neighbour = level.getBlockState(worldPosition.relative(side));
        if (aux) {
            return neighbour.getBlock() instanceof BlockSmelterAux && neighbour.getValue(BlockSmelterAux.FACING) == side.getOpposite();
        }
        return neighbour.getBlock() instanceof BlockSmelterVent && neighbour.getValue(BlockSmelterVent.FACING) == side.getOpposite();
    }

    private boolean ventAbsorbs(ServerLevel server, BlockPos pos, Direction[] vents, RandomSource random) {
        for (Direction side : vents) {
            if (random.nextInt(VENT_ODDS) == 0) {
                emitVent(server, pos, side, random);
                return true;
            }
        }
        return false;
    }

    private static void emitVent(ServerLevel server, BlockPos pos, Direction side, RandomSource random) {
        float pitch = VENT_SOUND_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * VENT_SOUND_PITCH_SPREAD;
        Vec3 outward = new Vec3(side.getStepX(), 0.0, side.getStepZ());
        Vec3 centre = Vec3.atCenterOf(pos);
        playVentSound(server, centre.add(outward), pitch);
        emitPuffs(server, centre, outward, random);
    }

    private static void playVentSound(ServerLevel server, Vec3 at, float pitch) {
        server.playSound(null, at.x, at.y, at.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, VENT_SOUND_VOLUME, pitch);
    }

    private static void emitPuffs(ServerLevel server, Vec3 centre, Vec3 outward, RandomSource random) {
        Vec3 mouth = centre.add(outward.scale(VENT_FACE_OFFSET));
        Vec3 base = new Vec3(outward.x * VENT_PUFF_SPEED, 0.0, outward.z * VENT_PUFF_SPEED);
        for (int puff = 0; puff < VENT_PUFFS; puff++) {
            emitPuff(server, mouth, base, random);
        }
    }

    private static void emitPuff(ServerLevel server, Vec3 mouth, Vec3 base, RandomSource random) {
        Vec3 offset = jitterVector(random, VENT_PUFF_JITTER);
        Vec3 motion = base.add(jitterVector(random, VENT_PUFF_MOTION_JITTER));
        Effects.vent(server, mouth.add(offset)).motion(motion.x, motion.y, motion.z).color(VENT_COLOR).send();
    }

    private static Vec3 jitterVector(RandomSource random, double range) {
        double x = jitter(random, range);
        double y = jitter(random, range);
        double z = jitter(random, range);
        return new Vec3(x, y, z);
    }

    private static double jitter(RandomSource random, double range) {
        return (random.nextDouble() * 2.0 - 1.0) * range;
    }

    private boolean handOverEssentia(ServerLevel server, BlockPos pos) {
        if (vis <= 0 || tickCounter % handOverInterval() != 0) {
            return false;
        }
        Direction[] auxSides = sidesWith(getBlockState().getValue(BlockSmelter.FACING), true);
        boolean moved = feedColumn(server, pos);
        for (int index = 0; index < auxSides.length; index++) {
            moved = feedColumn(server, pos.relative(auxSides[index])) || moved;
        }
        return moved;
    }

    private int handOverInterval() {
        int interval = ventInterval();
        return speedBoost ? Math.max(1, (int) (interval * SPEED_BOOST_FACTOR)) : interval;
    }

    private boolean feedColumn(ServerLevel server, BlockPos start) {
        for (AspectInstance entry : essentiaStock.sortedByAmount()) {
            if (BlockEntityAlembic.feedColumn(server, start, entry.aspect())) {
                essentiaStock = essentiaStock.remove(entry.aspect(), 1);
                vis = essentiaStock.totalAmount();
                setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ASPECTS_KEY, AspectList.CODEC, essentiaStock);
        output.putInt(BURN_TIME_KEY, fuelRemaining);
        output.putInt(COOK_TIME_KEY, cookElapsed);
        output.putInt(SMELT_TIME_KEY, cookTarget);
        output.putInt(CURRENT_BURN_TIME_KEY, fuelCapacity);
        output.putBoolean(SPEED_BOOST_KEY, speedBoost);
        items.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input);
        loadFurnaceState(input);
        essentiaStock = input.read(ASPECTS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        vis = essentiaStock.totalAmount();
    }

    private void loadFurnaceState(ValueInput input) {
        cookElapsed = input.getIntOr(COOK_TIME_KEY, 0);
        speedBoost = input.getBooleanOr(SPEED_BOOST_KEY, false);
        loadFuelState(input);
        cookTarget = input.getIntOr(SMELT_TIME_KEY, 0);
    }

    private void loadFuelState(ValueInput input) {
        fuelCapacity = input.getIntOr(CURRENT_BURN_TIME_KEY, 0);
        fuelRemaining = input.getIntOr(BURN_TIME_KEY, 0);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            spillItems(server, pos);
            releaseEssentia(server, pos);
        }
    }

    private void spillItems(ServerLevel server, BlockPos pos) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            Containers.dropItemStack(server, pos.getX(), pos.getY(), pos.getZ(), stackIn(slot));
            putStack(slot, ItemStack.EMPTY);
        }
    }

    private void releaseEssentia(ServerLevel server, BlockPos pos) {
        if (vis <= 0) {
            return;
        }
        AuraHelper.polluteAura(server, pos, vis, true);
        essentiaStock = AspectList.EMPTY;
        vis = 0;
        setChangedAndSync();
    }

    public boolean takeAspect(Holder<IAspect> aspect, int amount) {
        if (essentiaStock.amountOf(aspect) < amount) {
            return false;
        }
        essentiaStock = essentiaStock.remove(aspect, amount);
        vis = essentiaStock.totalAmount();
        setChangedAndSync();
        return true;
    }

    public void refreshBellows() {
        if (level == null) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockSmelter.FACING);
        Direction[] sides = new Direction[Direction.Plane.HORIZONTAL.length() - 1];
        int count = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing) {
                sides[count++] = side;
            }
        }
        bellows = BellowsHelper.countBellows(level, worldPosition, sides);
    }

    private SmelterStats stats() {
        SmelterStats stats = getBlockState().getBlock().builtInRegistryHolder().getData(SmelterDataMaps.SMELTER_STATS);
        return stats == null ? SmelterStats.DEFAULT : stats;
    }

    public int ventInterval() {
        return stats().smeltInterval();
    }

    public float essentiaYield() {
        return stats().efficiency();
    }

    public int scaled(SmelterGauge gauge, int scale) {
        return switch (gauge) {
            case COOK -> cookElapsed * scale / Math.max(cookTarget, 1);
            case ESSENTIA -> vis * scale / MAX_ESSENTIA;
            case FUEL -> fuelRemaining * scale / (fuelCapacity == 0 ? DEFAULT_BURN_TIME : fuelCapacity);
        };
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuSmelter(containerId, playerInventory, this);
    }

    private enum PointFate {
        KEPT, VENTED, POLLUTED
    }

    private static final class FateTally {
        private int kept;
        private int polluted;

        void record(PointFate fate) {
            switch (fate) {
                case KEPT -> kept++;
                case POLLUTED -> polluted++;
                case VENTED -> {
                }
            }
        }
    }

    private final class SmelterItems extends ItemStacksResourceHandler {
        SmelterItems() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            ItemStack stack = resource.toStack(1);
            return index == INPUT_SLOT ? isInputValid(stack) : level != null && isFuelValid(level, stack);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}

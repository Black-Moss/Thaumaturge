package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCondenser extends BlockEntity implements IEssentiaTransport {
    private static final int CAPACITY = 100;
    private static final int INTAKE_INTERVAL = 5;
    private static final int RECHECK_INTERVAL = 100;
    private static final int SUCTION = 128;
    private static final int DIRTY_CHANCE = 50;
    private static final int MAX_STRUCTURE_DISTANCE_SQR = 74;
    private static final float MAX_SCORE = 40.0F;
    private static final int CELL_VALUE = 100;
    private static final int PENALTY_PER_NEIGHBOUR = 15;
    private static final float VALUE_DIVISOR = 100.0F;
    private static final int ROOT_VALUE = CELL_VALUE - PENALTY_PER_NEIGHBOUR;
    private static final float BASE_INTERVAL = 600.0F;
    private static final float INTERVAL_PER_SCORE = 15.0F;
    private static final int MIN_INTERVAL = 5;
    private static final int BASE_COST = 4;
    private static final float FLUX_PER_CONVERSION = 1.0F;
    private static final float UNSCANNED = -1.0F;
    private static final int ABSORB_AMOUNT = 1;
    private static final String ESSENTIA_KEY = "essentia";
    private static final String FLUX_KEY = "flux";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

    private int essentia;
    private int flux;
    private int ticks;
    private float score = UNSCANNED;
    private int interval;
    private int cost;
    private List<BlockPos> dirtyCandidates = List.of();

    public BlockEntityCondenser(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CONDENSER.get(), pos, state);
    }

    public int interval() {
        return interval;
    }

    public int cost() {
        return cost;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCondenser condenser) {
        condenser.ticks++;
        if (condenser.score < 0.0F || condenser.ticks % RECHECK_INTERVAL == 0) {
            condenser.rescan(level);
        }
        if (!state.getValue(BlockStateProperties.ENABLED) || condenser.score <= 0.0F) {
            return;
        }
        if (condenser.ticks % INTAKE_INTERVAL == 0 && condenser.essentia < CAPACITY) {
            condenser.intake(level);
        }
        boolean stillValid = condenser.score > 0.0F && condenser.interval > 0;
        if (stillValid && condenser.ticks % condenser.interval == 0) {
            condenser.convert(level);
        }
    }

    public void triggerCheck() {
        if (level != null && !level.isClientSide()) {
            rescan(level);
        }
    }

    private boolean canConvert(Level level) {
        return essentia >= cost && flux < CAPACITY && AuraHelper.getFlux(level, worldPosition) >= FLUX_PER_CONVERSION;
    }

    private void convert(Level level) {
        if (!canConvert(level)) {
            return;
        }
        AuraHelper.drainFlux(level, worldPosition, FLUX_PER_CONVERSION, false);
        essentia -= cost;
        flux += 1;
        if (level.getRandom().nextInt(DIRTY_CHANCE) == 0) {
            makeDirty(level);
        }
        setChanged();
    }

    private void intake(Level level) {
        IEssentiaTransport[] around = new IEssentiaTransport[HORIZONTALS.length];
        for (int slot = 0; slot < around.length; slot++) {
            IEssentiaTransport found = neighbour(level, HORIZONTALS[slot]);
            if (found != null && !found.canOutputTo(HORIZONTALS[slot].getOpposite())) {
                return;
            }
            around[slot] = found;
        }
        for (int slot = 0; slot < around.length && essentia < CAPACITY; slot++) {
            if (around[slot] != null) {
                supplyFrom(level, around[slot], HORIZONTALS[slot]);
            }
        }
    }

    private @Nullable IEssentiaTransport neighbour(Level level, Direction side) {
        BlockPos pos = worldPosition.relative(side);
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        return EssentiaAccess.transport(level, pos, side.getOpposite());
    }

    private void supplyFrom(Level level, IEssentiaTransport source, Direction side) {
        Direction face = side.getOpposite();
        if (!isWilling(source, face, getSuctionAmount(side))) {
            return;
        }
        Holder<IAspect> offered = source.getEssentiaType(face);
        if (offered == null) {
            return;
        }
        if (offered.is(TTAspects.VITIUM)) {
            makeDirty(level);
            setChanged();
            return;
        }
        absorb(source.takeEssentia(offered, ABSORB_AMOUNT, face));
    }

    private static boolean isWilling(IEssentiaTransport source, Direction face, int ownSuction) {
        return source.getEssentiaAmount(face) > 0 && source.getSuctionAmount(face) < ownSuction && ownSuction >= source.getMinimumSuction();
    }

    private void absorb(int received) {
        if (received <= 0) {
            return;
        }
        essentia += received;
        setChanged();
    }

    private void makeDirty(Level level) {
        int count = dirtyCandidates.size();
        if (count == 0) {
            return;
        }
        int draw = level.getRandom().nextInt(count * count);
        BlockPos target = dirtyCandidates.get((draw + count) / (count + 1));
        if (isClean(level, target)) {
            level.setBlock(target, TTBlocks.CONDENSER_LATTICE_DIRTY.get().defaultBlockState(), Block.UPDATE_ALL);
            score = UNSCANNED;
        }
    }

    private void rescan(Level level) {
        Map<BlockPos, Integer> depth = floodStructure(level);
        if (depth.keySet().stream().anyMatch(cell -> sitsOnForeignCondenser(level, cell))) {
            dirtyCandidates = List.of();
            markIdle();
            return;
        }
        dirtyCandidates = depth.keySet().stream().filter(cell -> isClean(level, cell)).collect(Collectors.toCollection(ArrayList::new));
        evaluate(level, depth, new HashSet<>(dirtyCandidates));
    }

    private Map<BlockPos, Integer> floodStructure(Level level) {
        Map<BlockPos, Integer> depth = new LinkedHashMap<>();
        BlockPos entry = worldPosition.above();
        if (!isLattice(level, entry)) {
            return depth;
        }
        depth.put(entry, 0);
        List<BlockPos> layer = List.of(entry);
        for (int step = 1; !layer.isEmpty(); step++) {
            List<BlockPos> following = new ArrayList<>();
            for (BlockPos cell : layer) {
                for (Direction direction : DIRECTIONS) {
                    BlockPos adjacent = cell.relative(direction);
                    if (inRange(adjacent) && isLattice(level, adjacent) && depth.putIfAbsent(adjacent, step) == null) {
                        following.add(adjacent);
                    }
                }
            }
            layer = following;
        }
        return depth;
    }

    private boolean sitsOnForeignCondenser(Level level, BlockPos cell) {
        BlockPos below = cell.below();
        return !below.equals(worldPosition) && level.getBlockState(below).is(TTBlocks.CONDENSER.get());
    }

    private void markIdle() {
        score = 0.0F;
        interval = 0;
    }

    private void evaluate(Level level, Map<BlockPos, Integer> depth, Set<BlockPos> clean) {
        int value = 0;
        BlockPos entry = worldPosition.above();
        if (clean.contains(entry)) {
            value = ROOT_VALUE;
            for (BlockPos cell : cleanRouteFrom(entry, clean)) {
                if (!isTerminal(cell, depth)) {
                    value += CELL_VALUE - PENALTY_PER_NEIGHBOUR * latticeNeighbours(level, cell);
                }
            }
        }
        if (value <= 0) {
            markIdle();
            return;
        }
        score = Math.min(value / VALUE_DIVISOR, MAX_SCORE);
        interval = Math.max(MIN_INTERVAL, Math.round(BASE_INTERVAL - INTERVAL_PER_SCORE * score));
        cost = (int) (BASE_COST + Math.sqrt(depth.size()));
    }

    private static Set<BlockPos> cleanRouteFrom(BlockPos entry, Set<BlockPos> clean) {
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(entry);
        pending.push(entry);
        while (!pending.isEmpty()) {
            BlockPos cell = pending.pop();
            for (Direction direction : DIRECTIONS) {
                BlockPos adjacent = cell.relative(direction);
                if (clean.contains(adjacent) && reached.add(adjacent)) {
                    pending.push(adjacent);
                }
            }
        }
        return reached;
    }

    private static boolean isTerminal(BlockPos cell, Map<BlockPos, Integer> depth) {
        int own = depth.get(cell);
        return Arrays.stream(DIRECTIONS).map(direction -> depth.get(cell.relative(direction))).noneMatch(other -> other != null && other >= own);
    }

    private static int latticeNeighbours(Level level, BlockPos cell) {
        return (int) Arrays.stream(DIRECTIONS).filter(direction -> isLattice(level, cell.relative(direction))).count();
    }

    private static boolean isLattice(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof BlockCondenserLattice;
    }

    private static boolean isClean(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TTBlocks.CONDENSER_LATTICE.get());
    }

    private boolean inRange(BlockPos pos) {
        return worldPosition.distSqr(pos) <= MAX_STRUCTURE_DISTANCE_SQR && pos.getY() - worldPosition.getY() > 0;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return !Direction.UP.equals(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face.getAxis() != Direction.Axis.Y;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return Direction.DOWN.equals(face);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (Direction.DOWN.equals(face)) {
            return 0;
        }
        return essentia < CAPACITY ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        boolean bottomOrUnspecified = face == null || face == Direction.DOWN;
        return bottomOrUnspecified && flux > 0 ? Aspects.resolve(level, TTAspects.VITIUM) : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return face == null || face == Direction.DOWN ? flux : 0;
    }

    @Override
    public int takeEssentia(@Nullable Holder<IAspect> aspect, int amount, Direction face) {
        if (face != Direction.DOWN || aspect != null && !aspect.is(TTAspects.VITIUM)) {
            return 0;
        }
        int removed = Math.min(amount, flux);
        if (removed <= 0) {
            return removed;
        }
        flux -= removed;
        setChanged();
        return removed;
    }

    @Override
    public int addEssentia(@Nullable Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face)) {
            return 0;
        }
        if (aspect != null && aspect.is(TTAspects.VITIUM)) {
            if (level != null && !level.isClientSide()) {
                makeDirty(level);
            }
            return amount;
        }
        int room = CAPACITY - essentia;
        if (room <= 0 || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, room);
        absorb(accepted);
        return accepted;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && flux > 0) {
            AuraHelper.polluteAura(level, pos, (float) flux, true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = input.getIntOr(ESSENTIA_KEY, 0);
        flux = input.getIntOr(FLUX_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(ESSENTIA_KEY, essentia);
        output.putInt(FLUX_KEY, flux);
    }
}

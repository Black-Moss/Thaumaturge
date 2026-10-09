package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public class BlockEntitySpa extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 5000;

    private static final int WORK_INTERVAL_TICKS = 40;
    private static final int PLACEMENT_COST = FluidType.BUCKET_VOLUME;
    private static final int SALTS_COST = 1;
    private static final int SPREAD_RADIUS = 2;
    private static final int TANK_SLOT = 0;
    private static final int SALTS_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final String MIX_KEY = "Mix";
    private static final String TANK_KEY = "Tank";
    private static final String ITEMS_KEY = "Items";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final List<int[]> SPREAD_OFFSETS = buildSpreadOffsets();

    private final SpaTank tank = new SpaTank();
    private final SpaSalts items = new SpaSalts();
    private boolean mix = true;
    private int cooldown;

    public BlockEntitySpa(BlockPos pos, BlockState state) {
        super(TTBlockEntities.SPA.get(), pos, state);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    public ItemStacksResourceHandler getItems() {
        return items;
    }

    public boolean getMix() {
        return mix;
    }

    public void toggleMix() {
        mix = !mix;
        setChangedAndSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntitySpa spa) {
        if (spa.cooldown-- > 0) {
            return;
        }
        spa.cooldown = WORK_INTERVAL_TICKS - 1;
        spa.work(level, pos);
    }

    private void work(Level level, BlockPos pos) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        BlockState target = targetState();
        if (target == null) {
            return;
        }
        BlockPos above = pos.above();
        BlockPos destination = isSourceOf(level.getBlockState(above), target) ? nearestSpreadCell(level, above, target) : (isPlaceable(level, above, target) ? above : null);
        if (destination == null) {
            return;
        }
        level.setBlock(destination, target, Block.UPDATE_ALL);
        drainIngredients();
    }

    private @Nullable BlockState targetState() {
        if (tank.getAmountAsInt(TANK_SLOT) < PLACEMENT_COST) {
            return null;
        }
        Fluid stored = tank.getResource(TANK_SLOT).getFluid();
        return mix ? mixedTarget(stored) : dispensedTarget(stored);
    }

    private @Nullable BlockState mixedTarget(Fluid stored) {
        if (stored != Fluids.WATER || !hasSalts()) {
            return null;
        }
        return TTBlocks.PURIFYING_FLUID.get().defaultBlockState();
    }

    private boolean hasSalts() {
        return items.getResource(SALTS_SLOT).is(TTItems.BATH_SALTS.get()) && items.getAmountAsInt(SALTS_SLOT) >= SALTS_COST;
    }

    private static @Nullable BlockState dispensedTarget(Fluid stored) {
        if (!(stored instanceof FlowingFluid flowing)) {
            return null;
        }
        BlockState legacy = flowing.getSource().defaultFluidState().createLegacyBlock();
        return legacy.isAir() ? null : legacy.getBlock().defaultBlockState();
    }

    private static List<int[]> buildSpreadOffsets() {
        List<int[]> offsets = new ArrayList<>();
        for (int dz = -SPREAD_RADIUS; dz <= SPREAD_RADIUS; dz++) {
            for (int dx = -SPREAD_RADIUS; dx <= SPREAD_RADIUS; dx++) {
                offsets.add(new int[]{dx, dz});
            }
        }
        offsets.sort(Comparator.<int[]>comparingInt(o -> o[0] * o[0] + o[1] * o[1]).thenComparingInt(o -> o[0]).thenComparingInt(o -> o[1]));
        return List.copyOf(offsets);
    }

    private static @Nullable BlockPos nearestSpreadCell(Level level, BlockPos center, BlockState target) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int[] offset : SPREAD_OFFSETS) {
            cursor.setWithOffset(center, offset[0], 0, offset[1]);
            if (isPlaceable(level, cursor, target) && touchesSource(level, cursor, target)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isSourceOf(BlockState state, BlockState target) {
        return state.is(target.getBlock()) && state.getFluidState().isSource();
    }

    private static boolean isPlaceable(Level level, BlockPos pos, BlockState target) {
        BlockState current = level.getBlockState(pos);
        if (isSourceOf(current, target) || !current.canBeReplaced()) {
            return false;
        }
        if (!hasSupport(level, pos)) {
            return false;
        }
        FluidState fluid = target.getFluidState();
        FluidStack probe = FluidResource.of(fluid.getType()).toStack(FluidType.BUCKET_VOLUME);
        return !fluid.getFluidType().isVaporizedOnPlacement(level, pos, probe);
    }

    private static boolean hasSupport(Level level, BlockPos pos) {
        BlockPos floor = pos.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
    }

    private static boolean touchesSource(Level level, BlockPos pos, BlockState target) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction side : DIRECTIONS) {
            cursor.setWithOffset(pos, side);
            if (isSourceOf(level.getBlockState(cursor), target)) {
                return true;
            }
        }
        return false;
    }

    private void drainIngredients() {
        FluidResource stored = tank.getResource(TANK_SLOT);
        tank.set(TANK_SLOT, stored, tank.getAmountAsInt(TANK_SLOT) - PLACEMENT_COST);
        if (!mix) {
            return;
        }
        ItemResource salts = items.getResource(SALTS_SLOT);
        items.set(SALTS_SLOT, salts, items.getAmountAsInt(SALTS_SLOT) - SALTS_COST);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(MIX_KEY, mix);
        tank.serialize(output.child(TANK_KEY));
        items.serialize(output.child(ITEMS_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mix = input.getBooleanOr(MIX_KEY, true);
        input.child(TANK_KEY).ifPresent(tank::deserialize);
        input.child(ITEMS_KEY).ifPresent(items::deserialize);
    }

    @Override
    public Component getDisplayName() {
        return TTBlocks.SPA.get().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuSpa(containerId, playerInventory, this);
    }

    private final class SpaTank extends FluidStacksResourceHandler {
        SpaTank() {
            super(SLOT_COUNT, TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChangedAndSync();
        }
    }

    private final class SpaSalts extends ItemStacksResourceHandler {
        SpaSalts() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TTItems.BATH_SALTS.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}

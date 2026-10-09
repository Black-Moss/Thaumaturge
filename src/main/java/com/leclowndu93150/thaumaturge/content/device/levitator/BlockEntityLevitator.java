package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityLevitator extends AbstractSyncedBlockEntity {
    private static final String REACH_KEY = "reach";
    private static final String CHARGE_KEY = "charge";
    private static final int REFUEL_THRESHOLD = 10;
    private static final float REFUEL_DRAW = 1.0F;
    private static final int CHARGE_PER_VIS = 1200;
    private static final int CLEARANCE_INTERVAL = 10;
    private static final int SYNC_INTERVAL = 20;
    private static final double PUSH = 0.1;
    private static final double SPEED_CAP = 0.35;
    private static final double DAMPING = 0.9;
    private static final double LIFT = 0.08;
    private static final float IDLE_STREAM_CHANCE = 0.1F;
    private static final float STREAM_CHANCE = 0.6F;

    private LevitatorReach reach = LevitatorReach.MEDIUM;
    private int charge;
    private int clearance;
    private boolean clearanceStale = true;

    public BlockEntityLevitator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LEVITATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityLevitator levitator) {
        levitator.step(level, pos, state);
    }

    public void cycleReach(Player player) {
        reach = reach.next();
        clearance = 0;
        clearanceStale = true;
        if (level != null && !level.isClientSide()) {
            setChangedAndSync();
            player.sendSystemMessage(Component.translatable("gui.thaumaturge.levitator", reach.blocks(), reach.visCost()));
        }
    }

    void markClearanceStale() {
        clearanceStale = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(REACH_KEY, LevitatorReach.CODEC, reach);
        output.putInt(CHARGE_KEY, charge);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reach = input.read(REACH_KEY, LevitatorReach.CODEC).orElse(LevitatorReach.MEDIUM);
        charge = input.getIntOr(CHARGE_KEY, 0);
        clearance = Math.min(clearance, reach.blocks());
        clearanceStale = true;
    }

    private void step(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockLevitator.FACING);
        if (clearanceStale || level.getGameTime() % CLEARANCE_INTERVAL == 0) {
            clearance = measureClearance(level, pos, facing);
            clearanceStale = false;
        }
        if (!level.isClientSide()) {
            refuel(level, pos);
        }
        if (clearance <= 0 || charge <= 0 || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        int carried = carry(level, pos, facing);
        if (level.isClientSide()) {
            LevitatorMist.stream(level, pos, facing, IDLE_STREAM_CHANCE);
        } else if (carried > 0 && level.getGameTime() % SYNC_INTERVAL == 0) {
            setChangedAndSync();
        }
    }

    private int measureClearance(Level level, BlockPos pos, Direction facing) {
        int range = reach.blocks();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 1; distance <= range; distance++) {
            cursor.setWithOffset(pos, facing.getStepX() * distance, facing.getStepY() * distance, facing.getStepZ() * distance);
            if (!level.hasChunkAt(cursor) || level.getBlockState(cursor).isSolidRender()) {
                return distance;
            }
        }
        return range;
    }

    private void refuel(Level level, BlockPos pos) {
        if (charge >= REFUEL_THRESHOLD) {
            return;
        }
        int gain = (int) (AuraHelper.drainVis(level, pos, REFUEL_DRAW, false) * CHARGE_PER_VIS);
        if (gain > 0) {
            charge += gain;
            setChangedAndSync();
        }
    }

    private int carry(Level level, BlockPos pos, Direction facing) {
        AABB column = new AABB(pos).expandTowards(facing.getStepX() * (double) clearance, facing.getStepY() * (double) clearance, facing.getStepZ() * (double) clearance);
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class, column, BlockEntityLevitator::canCarry);
        int carried = 0;
        for (Entity entity : candidates) {
            if (charge <= 0) {
                break;
            }
            push(entity, facing);
            charge -= reach.visCost();
            carried++;
            if (level.isClientSide()) {
                LevitatorMist.cling(level, entity);
                LevitatorMist.stream(level, pos, facing, STREAM_CHANCE);
            }
        }
        return carried;
    }

    private static boolean canCarry(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return entity instanceof ItemEntity || entity instanceof AbstractHorse || entity instanceof Player || entity.isPushable();
    }

    private static void push(Entity entity, Direction facing) {
        Vec3 velocity = entity.getDeltaMovement();
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;
        if (facing == Direction.UP && entity.isShiftKeyDown()) {
            y = y < 0.0 ? y * DAMPING : y;
        } else {
            x = Math.clamp(x + facing.getStepX() * PUSH, -SPEED_CAP, SPEED_CAP);
            y = Math.clamp(y + facing.getStepY() * PUSH, -SPEED_CAP, SPEED_CAP);
            z = Math.clamp(z + facing.getStepZ() * PUSH, -SPEED_CAP, SPEED_CAP);
            if (facing.getAxis().isHorizontal() && !entity.onGround()) {
                y = (y < 0.0 ? y * DAMPING : y) + LIFT;
            }
        }
        entity.setDeltaMovement(x, y, z);
        entity.resetFallDistance();
    }
}

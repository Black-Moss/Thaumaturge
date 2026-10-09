package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class BlockEntityPatternCrafter extends AbstractSyncedBlockEntity {
    public static final int PATTERN_COUNT = 10;

    private static final String TYPE_KEY = "Type";
    private static final String POWER_KEY = "Power";
    private static final int GRID_SIZE = 3;
    private static final int GRID_CELLS = GRID_SIZE * GRID_SIZE;
    private static final int[][] PATTERNS = {{0, 1, 2, 3, 4, 5, 6, 7, 8}, {0}, {0, 1}, {0, 3}, {0, 1, 3, 4}, {0, 1, 2}, {0, 3, 6}, {0, 1, 2, 3, 4, 5}, {0, 1, 3, 4, 6, 7}, {0, 1, 2, 3, 5, 6, 7, 8}};
    private static final int WORK_INTERVAL = 20;
    private static final float DRAIN_AMOUNT = 5.0F;
    private static final float CRAFT_COST = 1.0F;
    private static final int SPIN_EVENT = 1;
    private static final int SPIN_DURATION = 10;
    private static final float SPIN_ACCELERATION = 1.0F;
    private static final float SPIN_DECAY = 0.8F;
    private static final float SPIN_REST_SPEED = 0.5F;
    private static final float CLACK_PITCH = 1.7F;
    private static final float CLACK_VOLUME = 0.2F;
    private static final Direction TAKE_FACE = Direction.DOWN;
    private static final Direction GIVE_FACE = Direction.UP;

    public int rotTicks;
    public float rotSpeed;
    public float rot;

    private int clackTimer;
    private int workTimer;
    private float power;
    private byte type;

    public BlockEntityPatternCrafter(BlockPos pos, BlockState state) {
        super(TTBlockEntities.PATTERN_CRAFTER.get(), pos, state);
    }

    public byte patternType() {
        return type;
    }

    public void cycle() {
        type = (byte) ((type + 1) % PATTERN_COUNT);
        setChangedAndSync();
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            animate(level, pos);
            return;
        }
        boolean due = workTimer == 0;
        workTimer = (workTimer + 1) % WORK_INTERVAL;
        if (due && state.getValue(BlockStateProperties.ENABLED) && level instanceof ServerLevel serverLevel) {
            work(serverLevel, pos);
        }
    }

    private void animate(Level level, BlockPos pos) {
        advanceSpin();
        if (rotSpeed <= 0.0F) {
            clackTimer = 0;
            return;
        }
        rot += rotSpeed;
        if (++clackTimer >= Math.max(1, (int) rotSpeed)) {
            clackTimer = 0;
            Vec3 center = Vec3.atCenterOf(pos);
            level.playLocalSound(center.x, center.y, center.z, TTSounds.CLACK.get(), SoundSource.BLOCKS, CLACK_VOLUME, CLACK_PITCH, false);
        }
    }

    private void advanceSpin() {
        if (rotTicks > 0) {
            rotTicks--;
            rotSpeed += SPIN_ACCELERATION;
            return;
        }
        if (rotSpeed > 0.0F) {
            rotSpeed *= SPIN_DECAY;
            if (rotSpeed < SPIN_REST_SPEED) {
                rotSpeed = 0.0F;
            }
        }
    }

    private void work(ServerLevel level, BlockPos pos) {
        ResourceHandler<ItemResource> input = InvHelper.getItemHandlerAt(level, pos.above(), TAKE_FACE);
        ResourceHandler<ItemResource> output = InvHelper.getItemHandlerAt(level, pos.below(), GIVE_FACE);
        if (input == null || output == null) {
            return;
        }
        refillPower(level, pos);
        if (power < CRAFT_COST) {
            return;
        }
        int slots = input.size();
        int slot = 0;
        boolean crafted = false;
        while (!crafted && slot < slots) {
            ItemResource resource = input.getResource(slot++);
            crafted = !resource.isEmpty() && craft(level, pos, input, output, resource.toStack());
        }
    }

    private void refillPower(ServerLevel level, BlockPos pos) {
        if (power > 0.0F) {
            return;
        }
        power = AuraHelper.drainVis(level, pos, DRAIN_AMOUNT, false);
        if (power > 0.0F) {
            setChanged();
        }
    }

    private static CraftingInput gridFor(int[] cells, ItemStack sample) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(GRID_CELLS, ItemStack.EMPTY);
        for (int cell : cells) {
            stacks.set(cell, sample.copy());
        }
        return CraftingInput.of(GRID_SIZE, GRID_SIZE, stacks);
    }

    private static boolean canStoreAll(ResourceHandler<ItemResource> output, ItemStack result, NonNullList<ItemStack> leftovers) {
        if (result.isEmpty() || !InvHelper.insertStack(output, result, true).isEmpty()) {
            return false;
        }
        for (ItemStack leftover : leftovers) {
            if (!leftover.isEmpty() && !InvHelper.insertStack(output, leftover, true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean craft(ServerLevel level, BlockPos pos, ResourceHandler<ItemResource> input, ResourceHandler<ItemResource> output, ItemStack sample) {
        int[] cells = PATTERNS[type];
        ItemStack required = sample.copyWithCount(cells.length);
        if (InvHelper.removeStackFrom(input, required, InvHelper.InvFilter.STRICT, true).getCount() < cells.length) {
            return false;
        }
        CraftingInput grid = gridFor(cells, sample);
        return level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, grid, level).map(holder -> finishCraft(level, pos, input, output, required, holder.value(), grid)).orElse(false);
    }

    private boolean finishCraft(ServerLevel level, BlockPos pos, ResourceHandler<ItemResource> input, ResourceHandler<ItemResource> output, ItemStack required, CraftingRecipe recipe, CraftingInput grid) {
        ItemStack result = recipe.assemble(grid);
        NonNullList<ItemStack> leftovers = recipe.getRemainingItems(grid);
        if (!canStoreAll(output, result, leftovers)) {
            return false;
        }
        InvHelper.removeStackFrom(input, required, InvHelper.InvFilter.STRICT, false);
        store(level, pos, output, result);
        for (ItemStack leftover : leftovers) {
            if (!leftover.isEmpty()) {
                store(level, pos, output, leftover);
            }
        }
        power -= CRAFT_COST;
        setChanged();
        level.blockEvent(pos, getBlockState().getBlock(), SPIN_EVENT, 0);
        return true;
    }

    private static void store(ServerLevel level, BlockPos pos, ResourceHandler<ItemResource> output, ItemStack stack) {
        ItemStack overflow = InvHelper.insertStack(output, stack, false);
        if (!overflow.isEmpty()) {
            InvHelper.ejectStackAt(level, pos, Direction.DOWN, overflow);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != SPIN_EVENT) {
            return super.triggerEvent(id, param);
        }
        startSpin();
        return true;
    }

    private void startSpin() {
        if (level != null && level.isClientSide()) {
            rotTicks = SPIN_DURATION;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte(TYPE_KEY, type);
        output.putFloat(POWER_KEY, power);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        byte stored = input.getByteOr(TYPE_KEY, (byte) 0);
        type = stored >= 0 && stored < PATTERN_COUNT ? stored : 0;
        power = input.getFloatOr(POWER_KEY, 0.0F);
    }
}

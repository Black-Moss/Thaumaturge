package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.mixin.world.entity.item.ItemEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class BlockEntityCrucible extends AbstractSyncedBlockEntity implements ReadOnlyAspectContainer {
    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_ASPECT = 100;

    static final int BOIL_HEAT = 150;

    private static final String KEY_ASPECTS = "Aspects";
    private static final String KEY_TANK = "Tank";
    private static final String KEY_HEAT = "Heat";

    private static final int TANK_SLOTS = 1;
    private static final int TANK_SLOT = 0;
    private static final int CRAFT_WATER_COST = 50;
    private static final int MAX_HEAT = 200;
    private static final int HEAT_SYNC_RISE = BOIL_HEAT + 1;
    private static final int HEAT_SYNC_FALL = BOIL_HEAT - 1;

    private static final int COUNTER_START = -100;
    private static final int COUNTER_DISSOLVE = -150;
    private static final int COUNTER_CRAFT = -250;
    private static final int LEAK_THRESHOLD = 100;
    private static final int OVERFLOW_INTERVAL = 5;
    private static final int LEAK_SPILL_CHANCE_NUMERATOR = 3;
    private static final int LEAK_SPILL_CHANCE_DENOMINATOR = 4;
    private static final int OVERFLOW_SPILL_ODDS = 4;
    private static final int POINT_SPILL_ODDS = 4;
    private static final float LEAK_FLUX = 0.25F;
    private static final float OVERFLOW_FLUX = 1.0F;

    private static final int EVENT_BOIL_BURST = 10;
    private static final int EVENT_CRAFT_COMPLETE = 11;
    private static final int DISSOLVE_BURST_INTENSITY = 1;
    private static final int SPILL_BURST_INTENSITY = 5;

    private static final float FLUID_BASE = 0.3F;
    private static final float FLUID_TANK_SPAN = 0.5F;
    private static final float FLUID_OVERFLOW_HEIGHT = 1.001F;
    private static final float FLUID_EXACT_HEIGHT = 0.9999F;

    private static final float BUBBLE_VOLUME = 0.2F;
    private static final float BUBBLE_PITCH_BASE = 1.0F;
    private static final float BUBBLE_PITCH_SPREAD = 0.4F;
    private static final float SPILL_VOLUME = 0.2F;
    private static final float SPILL_PITCH = 1.0F;
    private static final double BLOCK_CENTER = 0.5;

    private static final double EJECT_Y = 0.71;
    private static final double EJECT_LIFT = 0.075;
    private static final float EJECT_SCATTER = 0.01F;

    private final FluidStacksResourceHandler tank = new CrucibleTank();
    private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();
    private AspectList aspects = AspectList.EMPTY;
    private int heat;
    private int leakCounter = COUNTER_START;

    public BlockEntityCrucible(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CRUCIBLE.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        emptyOut();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putShort(KEY_HEAT, getHeat());
        tank.serialize(view.child(KEY_TANK));
        view.store(KEY_ASPECTS, AspectList.CODEC, aspects);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        restoreContents(view);
    }

    private void restoreContents(ValueInput view) {
        heat = readHeat(view);
        view.child(KEY_TANK).ifPresent(tank::deserialize);
        aspects = view.read(KEY_ASPECTS, AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    private static int readHeat(ValueInput view) {
        return Mth.clamp(view.getShortOr(KEY_HEAT, (short) 0), 0, MAX_HEAT);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    public float surfaceLevel() {
        float filled = (float) tank.getAmountAsInt(TANK_SLOT) / TANK_CAPACITY;
        float floor = FLUID_BASE + FLUID_TANK_SPAN * filled;
        float aspectShare = (float) aspects.totalAmount() / MAX_ASPECT;
        float height = floor + aspectShare * (1.0F - floor);
        int order = Float.compare(height, 1.0F);
        if (order > 0) {
            return FLUID_OVERFLOW_HEIGHT;
        }
        return order == 0 ? FLUID_EXACT_HEIGHT : height;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    public short getHeat() {
        return (short) heat;
    }

    boolean isBoiling() {
        return heat > BOIL_HEAT && hasWater();
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityCrucible crucible) {
        crucible.leakCounter++;
        if (level instanceof ServerLevel serverLevel) {
            crucible.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel serverLevel) {
        updateHeat(serverLevel);
        emitParticles(serverLevel);
        runLeaks(serverLevel);
    }

    private void runLeaks(ServerLevel serverLevel) {
        if (isOverfilled() && leakCounter % OVERFLOW_INTERVAL == 0) {
            shedOverflow(serverLevel);
        }
        if (leakCounter >= LEAK_THRESHOLD) {
            leakCounter = 0;
            leakDrop();
        }
    }

    private boolean hasWater() {
        return tank.getAmountAsInt(TANK_SLOT) > 0;
    }

    private boolean isOverfilled() {
        return aspects.totalAmount() > MAX_ASPECT;
    }

    private void updateHeat(ServerLevel serverLevel) {
        if (!hasWater()) {
            heat = Math.max(heat - 1, 0);
            return;
        }
        belowPos.setWithOffset(worldPosition, Direction.DOWN);
        boolean heated = serverLevel.getBlockState(belowPos).is(TTBlockTags.CRUCIBLE_HEAT_SOURCES);
        int previous = heat;
        heat = heated ? Math.min(heat + 1, MAX_HEAT) : Math.max(heat - 1, 0);
        boolean crossedUp = heat > previous && heat == HEAT_SYNC_RISE;
        boolean crossedDown = heat < previous && heat == HEAT_SYNC_FALL;
        if (crossedUp || crossedDown) {
            setChangedAndSync();
        }
    }

    private void emitParticles(ServerLevel serverLevel) {
        if (!hasWater()) {
            return;
        }
        float height = surfaceLevel();
        if (heat > BOIL_HEAT) {
            CrucibleFx.froth(serverLevel, worldPosition, height, isOverfilled());
        }
        CrucibleFx.bubble(serverLevel, worldPosition, height, aspects);
    }

    private void takeOnePoint(RandomSource random) {
        AspectInstance picked = aspects.entries().get(random.nextInt(aspects.size()));
        aspects = aspects.remove(picked.aspect(), 1);
    }

    private void shedOverflow(ServerLevel serverLevel) {
        if (aspects.isEmpty()) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        takeOnePoint(random);
        if (random.nextInt(OVERFLOW_SPILL_ODDS) != 0 || !PhysicalFlux.spill(serverLevel, worldPosition, random)) {
            AuraHelper.polluteAura(serverLevel, worldPosition, OVERFLOW_FLUX, true);
        }
        setChangedAndSync();
    }

    public void leakDrop() {
        if (level instanceof ServerLevel serverLevel && !aspects.isEmpty()) {
            RandomSource random = serverLevel.getRandom();
            takeOnePoint(random);
            boolean attempt = random.nextInt(LEAK_SPILL_CHANCE_DENOMINATOR) < LEAK_SPILL_CHANCE_NUMERATOR;
            if (attempt && PhysicalFlux.spill(serverLevel, worldPosition, random)) {
                AuraHelper.polluteAura(serverLevel, worldPosition, LEAK_FLUX, true);
            }
        }
        if (level instanceof ServerLevel) {
            setChangedAndSync();
        }
    }

    public void emptyOut() {
        if (level instanceof ServerLevel serverLevel) {
            spillEverything(serverLevel);
        }
    }

    private void spillEverything(ServerLevel serverLevel) {
        int points = aspects.totalAmount();
        if (points <= 0 && !hasWater()) {
            return;
        }
        tank.set(TANK_SLOT, FluidResource.EMPTY, 0);
        aspects = AspectList.EMPTY;
        int lost = points - spillPoints(serverLevel, points);
        if (lost > 0) {
            AuraHelper.polluteAura(serverLevel, worldPosition, lost, true);
        }
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL_BURST, SPILL_BURST_INTENSITY);
        setChangedAndSync();
    }

    private int spillPoints(ServerLevel serverLevel, int points) {
        RandomSource random = serverLevel.getRandom();
        int placed = 0;
        for (int point = 0; point < points; point++) {
            boolean attempt = random.nextInt(POINT_SPILL_ODDS) == 0;
            if (attempt && PhysicalFlux.spill(serverLevel, worldPosition, random)) {
                placed++;
            }
        }
        return placed;
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        boolean known = id == EVENT_BOIL_BURST || id == EVENT_CRAFT_COMPLETE;
        if (level == null || !known) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            runServerFx(serverLevel, id, data);
        } else {
            playSpillSound();
        }
        return true;
    }

    private void runServerFx(ServerLevel serverLevel, int id, int data) {
        if (id == EVENT_BOIL_BURST) {
            CrucibleFx.boilBurst(serverLevel, worldPosition, surfaceLevel(), aspects, data);
        } else {
            CrucibleFx.craftComplete(serverLevel, worldPosition);
        }
    }

    private void playSpillSound() {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        level.playLocalSound(center.x, center.y, center.z, TTSounds.SPILL.get(), SoundSource.BLOCKS, SPILL_VOLUME, SPILL_PITCH, false);
    }

    public void popOut(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        int pieceSize = stack.getMaxStackSize();
        for (int offset = 0; offset < stack.getCount(); offset += pieceSize) {
            int count = Math.min(pieceSize, stack.getCount() - offset);
            EntitySpecialItem entity = new EntitySpecialItem(serverLevel, worldPosition.getX() + BLOCK_CENTER, worldPosition.getY() + EJECT_Y, worldPosition.getZ() + BLOCK_CENTER,
                    stack.copyWithCount(count));
            if (offset == 0) {
                entity.setDeltaMovement(0.0, EJECT_LIFT, 0.0);
            } else {
                double driftX = (random.nextFloat() - random.nextFloat()) * EJECT_SCATTER;
                double driftZ = (random.nextFloat() - random.nextFloat()) * EJECT_SCATTER;
                entity.setDeltaMovement(driftX, EJECT_LIFT, driftZ);
            }
            serverLevel.addFreshEntity(entity);
        }
    }

    public @Nullable ItemStack dropIn(ItemStack stack, @Nullable Player player) {
        if (!(level instanceof ServerLevel serverLevel) || player == null || player.isDeadOrDying() || stack.isEmpty() || !hasWater()) {
            return stack;
        }
        int budget = (stack.getCount() + 1) / 2;
        int consumed = 0;
        while (consumed < budget) {
            if (!smeltOne(serverLevel, stack.copyWithCount(1), player)) {
                break;
            }
            consumed++;
        }
        setChanged();
        int left = stack.getCount() - consumed;
        return left > 0 ? stack.copyWithCount(left) : null;
    }

    public void absorbThrown(ItemEntity itemEntity) {
        Entity thrower = EntityReference.getEntity(((ItemEntityAccessor) itemEntity).thaumaturge$getThrower(), itemEntity.level());
        if (thrower instanceof Player player && player.isAlive()) {
            settleThrown(itemEntity, player);
        }
    }

    private void settleThrown(ItemEntity itemEntity, Player player) {
        ItemStack held = itemEntity.getItem();
        ItemStack remainder = dropIn(held, player);
        if (remainder == null || remainder.isEmpty()) {
            itemEntity.discard();
            return;
        }
        if (remainder != held) {
            itemEntity.setItem(remainder);
        }
    }

    private boolean smeltOne(ServerLevel serverLevel, ItemStack single, Player player) {
        CrucibleRecipe recipe = ThaumaturgeCraftingManager.findMatchingCrucibleRecipe(serverLevel, player, aspects, single);
        boolean consumed = recipe == null ? dissolve(serverLevel, single, player) : craft(serverLevel, recipe, single, player);
        if (consumed) {
            setChangedAndSync();
        }
        return consumed;
    }

    private boolean craft(ServerLevel serverLevel, CrucibleRecipe recipe, ItemStack single, Player player) {
        ItemStack result = recipe.assemble(new CrucibleRecipeInput(single, aspects));
        popOut(result);
        aspects = recipe.removeMatching(aspects);
        drawWater();
        NeoForge.EVENT_BUS.post(new CrucibleEvent.Crafted(player, worldPosition, getBlockState(), this, result.copy(), recipe.aspects()));
        leakCounter = COUNTER_CRAFT;
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRAFT_COMPLETE, 0);
        return true;
    }

    private void drawWater() {
        try (Transaction tx = Transaction.openRoot()) {
            FluidResource water = FluidResource.of(Fluids.WATER);
            tank.extract(TANK_SLOT, water, CRAFT_WATER_COST, tx);
            tx.commit();
        }
    }

    private boolean dissolve(ServerLevel serverLevel, ItemStack single, Player player) {
        AspectList gained = collectDissolved(single, player);
        if (gained.isEmpty()) {
            return false;
        }
        aspects = aspects.add(gained);
        leakCounter = COUNTER_DISSOLVE;
        float pitch = BUBBLE_PITCH_BASE + serverLevel.getRandom().nextFloat() * BUBBLE_PITCH_SPREAD;
        serverLevel.playSound(null, worldPosition, TTSounds.BUBBLE.get(), SoundSource.BLOCKS, BUBBLE_VOLUME, pitch);
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL_BURST, DISSOLVE_BURST_INTENSITY);
        return true;
    }

    private AspectList collectDissolved(ItemStack single, Player player) {
        AspectList natural = AspectIndexAccess.of(single);
        CrucibleEvent.Dissolve event = NeoForge.EVENT_BUS.post(new CrucibleEvent.Dissolve(player, worldPosition, getBlockState(), this, single, natural));
        return event.isCanceled() ? AspectList.EMPTY : event.getAspects();
    }

    private final class CrucibleTank extends FluidStacksResourceHandler {
        private CrucibleTank() {
            super(TANK_SLOTS, TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChangedAndSync();
        }
    }
}

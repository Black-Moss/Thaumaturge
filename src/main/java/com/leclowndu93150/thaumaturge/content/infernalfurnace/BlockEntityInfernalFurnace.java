package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class BlockEntityInfernalFurnace extends AbstractSyncedBlockEntity {
    private static final int INVENTORY_SIZE = 32;
    private static final String COOK_TIME_KEY = "CookTime";
    private static final String SPEEDY_TIME_KEY = "SpeedyTime";
    private static final float VIS_DRAIN_AMOUNT = 20.0F;
    private static final int BASE_COOK_TIME = 140;
    private static final int BOOSTED_COOK_TIME = 80;
    private static final int MIN_COOK_TIME = 10;
    private static final int BELLOWS_DISTANCE = 2;
    private static final int MAX_BELLOWS = 4;
    private static final int BELLOWS_SPEED_BASE = 20;
    private static final int FLUX_ODDS = 20;
    private static final float FLUX_AMOUNT = 1.0F;
    private static final double CENTER = 0.5;
    private static final double TOSS_SPEED = 0.3;
    private static final int OUTSIDE_DISTANCE = 2;
    private static final double ORB_DISTANCE = 1.2;
    private static final double ORB_HEIGHT = 0.4;
    private static final double ORB_RETURN_SPEED = 0.13;
    private static final double ORB_SIDE_SPEED = 0.025;
    private static final float ORB_MIN_AMOUNT = 1.0F;
    private static final int ORB_MIN_VALUE = 1;
    private static final float HISS_VOLUME = 0.3F;
    private static final float HISS_PITCH = 2.6F;
    private static final float HISS_PITCH_JITTER = 0.8F;
    private static final double TOP_FACE_HEIGHT = 1.0;
    private static final double REJECT_SPARK_SPEED = 1.0;
    private static final double SPARK_HEIGHT = 0.3;
    private static final double SPARK_JITTER = 0.3;
    private static final int SMELT_POPS = 1;
    private static final float POP_VOLUME = 0.1F;
    private static final float POP_VOLUME_JITTER = 0.1F;
    private static final float POP_PITCH = 0.9F;
    private static final float POP_PITCH_JITTER = 0.15F;
    private static final int SMELT_SPARKS = 4;
    private static final double SPARK_SPEED = 0.001;
    private static final double SPARK_SIDE_SPREAD = 0.5;
    private static final double SPARK_LIFT_SPREAD = 0.2;
    private static final int[] BELLOWS_SPEED_BONUS = speedBonuses();
    private static final Map<Direction, Direction[]> BELLOWS_SIDES = bellowsSides();

    private final FurnaceInventory inventory = new FurnaceInventory();
    private final CookTimer timer = new CookTimer();
    private final Set<UUID> ejected = new HashSet<>();
    private boolean contentsDirty = true;

    public int visCharge;
    public int smeltTicksTotal;
    public int smeltTicksLeft;

    public BlockEntityInfernalFurnace(BlockPos pos, BlockState state) {
        super(TTBlockEntities.INFERNAL_FURNACE.get(), pos, state);
    }

    private static int[] speedBonuses() {
        int[] bonuses = new int[MAX_BELLOWS + 1];
        for (int bellows = 1; bellows <= MAX_BELLOWS; bellows++) {
            bonuses[bellows] = (BELLOWS_SPEED_BASE - (bellows - 1)) * bellows;
        }
        return bonuses;
    }

    private static Map<Direction, Direction[]> bellowsSides() {
        Map<Direction, Direction[]> sides = new EnumMap<>(Direction.class);
        for (Direction output : Direction.Plane.HORIZONTAL) {
            Direction[] allowed = new Direction[Direction.Plane.HORIZONTAL.length()];
            allowed[0] = Direction.DOWN;
            int next = 1;
            for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                if (horizontal != output) {
                    allowed[next++] = horizontal;
                }
            }
            sides.put(output, allowed);
        }
        return sides;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityInfernalFurnace furnace) {
        if (level instanceof ServerLevel serverLevel) {
            furnace.serverTick(serverLevel);
        }
    }

    private void serverTick(ServerLevel serverLevel) {
        timer.load(smeltTicksTotal, smeltTicksLeft, visCharge);
        if (timer.needsTotal() && timer.left() > 0) {
            timer.setTotal(cookTime(serverLevel));
        }
        rechargeBoost(serverLevel);
        if (!ejected.isEmpty()) {
            pruneEjected(serverLevel);
        }
        switch (timer.step()) {
            case IDLE -> {
                if (contentsDirty) {
                    contentsDirty = false;
                    startCycle(serverLevel);
                }
            }
            case FINISHING -> completeSmelt(serverLevel);
            case COOKING -> {
            }
        }
        storeTimer();
    }

    private void pruneEjected(ServerLevel serverLevel) {
        AABB cell = new AABB(worldPosition);
        Iterator<UUID> iterator = ejected.iterator();
        while (iterator.hasNext()) {
            Entity thrown = serverLevel.getEntity(iterator.next());
            if (thrown == null || !thrown.isAlive() || !thrown.getBoundingBox().intersects(cell)) {
                iterator.remove();
            }
        }
    }

    public boolean isOwnThrow(ItemEntity item) {
        return ejected.contains(item.getUUID());
    }

    private void rechargeBoost(ServerLevel serverLevel) {
        if (!timer.boostDepleted()) {
            return;
        }
        int whole = Mth.floor(AuraHelper.drainVis(serverLevel, worldPosition, VIS_DRAIN_AMOUNT, true));
        if (whole <= 0) {
            return;
        }
        AuraHelper.drainVis(serverLevel, worldPosition, whole, false);
        timer.refill(whole);
    }

    private void completeSmelt(ServerLevel serverLevel) {
        for (int slot = inventory.nextOccupied(0); slot >= 0; slot = inventory.nextOccupied(slot + 1)) {
            ItemStack input = inventory.getResource(slot).toStack(1);
            Optional<RecipeHolder<SmeltingRecipe>> recipe = smelting(serverLevel, input);
            if (recipe.isPresent()) {
                inventory.takeOne(slot);
                finishSmelt(serverLevel, recipe.get().value(), input);
                return;
            }
        }
    }

    private void storeTimer() {
        boolean changed = smeltTicksLeft != timer.left() || visCharge != timer.boost();
        smeltTicksTotal = timer.total();
        smeltTicksLeft = timer.left();
        visCharge = timer.boost();
        if (changed) {
            setChanged();
        }
    }

    private void launchStack(ServerLevel level, Direction side, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        BlockPos outside = worldPosition.relative(side, OUTSIDE_DISTANCE);
        ItemStack rest = InvHelper.insertAt(level, outside, side.getOpposite(), stack, false);
        if (rest.isEmpty()) {
            return;
        }
        BlockPos adjacent = worldPosition.relative(side);
        BlockPos tile = level.getBlockState(adjacent).isAir() ? adjacent : worldPosition;
        ItemEntity thrown = eject(level, tile.getX() + CENTER, worldPosition.getY(), tile.getZ() + CENTER, side, rest);
        ejected.add(thrown.getUUID());
    }

    private static ItemEntity eject(Level level, double x, double y, double z, Direction side, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack, side.getStepX() * TOSS_SPEED, 0.0, side.getStepZ() * TOSS_SPEED);
        entity.setNoPickUpDelay();
        level.addFreshEntity(entity);
        return entity;
    }

    public ItemStacksResourceHandler items() {
        return inventory;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        visCharge = readCounter(input, SPEEDY_TIME_KEY);
        smeltTicksLeft = readCounter(input, COOK_TIME_KEY);
    }

    private static int readCounter(ValueInput input, String key) {
        return input.getShortOr(key, (short) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putShort(SPEEDY_TIME_KEY, (short) visCharge);
        output.putShort(COOK_TIME_KEY, (short) smeltTicksLeft);
    }

    public ItemStack feed(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return stack;
        }
        if (smelting(serverLevel, stack).isEmpty()) {
            new FurnaceEffects(serverLevel, worldPosition).destroy();
            return ItemStack.EMPTY;
        }
        int leftover = stack.getCount() - insertCommitted(stack);
        return leftover > 0 ? stack.copyWithCount(leftover) : ItemStack.EMPTY;
    }

    private int insertCommitted(ItemStack stack) {
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = inventory.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return accepted;
        }
    }

    public void yieldResult(ItemStack result, ItemStack inputCopy, AbstractCookingRecipe recipe) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        OutputContext context = new OutputContext(serverLevel, this, worldPosition, outputSide(), bellowsCount(serverLevel));
        new FurnaceOutput(context).deliver(result, inputCopy, recipe.experience() * result.getCount());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) {
            return;
        }
        Direction output = state.getValue(BlockInfernalFurnace.FACING).getOpposite();
        for (ItemStack stack : inventory.drain()) {
            eject(level, pos.getX() + output.getStepX(), pos.getY(), pos.getZ() + output.getStepZ(), output, stack);
        }
    }

    private void startCycle(ServerLevel serverLevel) {
        if (hasSmeltable(serverLevel)) {
            timer.setTotal(cookTime(serverLevel));
            timer.start();
        }
    }

    private void finishSmelt(ServerLevel serverLevel, AbstractCookingRecipe recipe, ItemStack input) {
        yieldResult(recipe.assemble(new SingleRecipeInput(input)), input, recipe);
        Direction side = outputSide();
        new FurnaceEffects(serverLevel, worldPosition).smelt(side);
        if (serverLevel.getRandom().nextInt(FLUX_ODDS) == 0) {
            AuraHelper.polluteAura(serverLevel, worldPosition.relative(side), FLUX_AMOUNT, true);
        }
        timer.spendBoost();
    }

    private boolean hasSmeltable(ServerLevel serverLevel) {
        for (int slot = inventory.nextOccupied(0); slot >= 0; slot = inventory.nextOccupied(slot + 1)) {
            if (smelting(serverLevel, inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot))).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static Optional<RecipeHolder<SmeltingRecipe>> smelting(ServerLevel serverLevel, ItemStack stack) {
        return serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), serverLevel);
    }

    private int cookTime(ServerLevel serverLevel) {
        int base = timer.boost() > 0 ? BOOSTED_COOK_TIME : BASE_COOK_TIME;
        return Math.max(MIN_COOK_TIME, base - BELLOWS_SPEED_BONUS[bellowsCount(serverLevel)]);
    }

    private int bellowsCount(ServerLevel serverLevel) {
        return Math.min(MAX_BELLOWS, BellowsHelper.countBellows(serverLevel, worldPosition, BELLOWS_SIDES.get(outputSide()), BELLOWS_DISTANCE));
    }

    private Direction outputSide() {
        return getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite();
    }

    private enum CookPhase {
        IDLE, COOKING, FINISHING
    }

    private static final class CookTimer {
        private int total;
        private int left;
        private int boost;

        void load(int total, int left, int boost) {
            this.total = total;
            this.left = left;
            this.boost = boost;
        }

        boolean needsTotal() {
            return total <= 0;
        }

        void setTotal(int total) {
            this.total = total;
        }

        CookPhase step() {
            left = Mth.clamp(left, 0, total);
            if (left <= 0) {
                return CookPhase.IDLE;
            }
            left--;
            return left == 0 ? CookPhase.FINISHING : CookPhase.COOKING;
        }

        void start() {
            left = total;
        }

        void spendBoost() {
            boost--;
        }

        boolean boostDepleted() {
            return boost <= 0;
        }

        void refill(int amount) {
            boost = amount;
        }

        int total() {
            return total;
        }

        int left() {
            return left;
        }

        int boost() {
            return boost;
        }
    }

    private record OutputContext(ServerLevel level, BlockEntityInfernalFurnace furnace, BlockPos pos, Direction side, int bellows) {
    }

    private static final class FurnaceOutput {
        private final OutputContext context;

        private FurnaceOutput(OutputContext context) {
            this.context = context;
        }

        void deliver(ItemStack result, ItemStack input, float experience) {
            context.furnace().launchStack(context.level(), context.side(), result);
            RandomSource random = context.level().getRandom();
            for (int rolls = context.bellows(); rolls >= 0; rolls--) {
                rollBonuses(input, random);
            }
            spawnExperience(experience, random);
        }

        private void spawnExperience(float experience, RandomSource random) {
            if (experience <= 0.0F) {
                return;
            }
            if (experience < ORB_MIN_AMOUNT) {
                if (random.nextFloat() < experience) {
                    spawnOrb(ORB_MIN_VALUE, random);
                }
                return;
            }
            float remaining = experience;
            while (remaining > 0.0F) {
                int size = ExperienceOrb.getExperienceValue((int) remaining);
                remaining -= size;
                spawnOrb(size, random);
            }
        }

        private void spawnOrb(int value, RandomSource random) {
            BlockPos core = context.pos();
            Direction side = context.side();
            double x = core.getX() + CENTER + ORB_DISTANCE * side.getStepX();
            double y = core.getY() + ORB_HEIGHT;
            double z = core.getZ() + CENTER + ORB_DISTANCE * side.getStepZ();
            double spread = (random.nextDouble() - random.nextDouble()) * ORB_SIDE_SPEED;
            double speedX = side.getStepX() != 0 ? -ORB_RETURN_SPEED * side.getStepX() : spread;
            double speedZ = side.getStepZ() != 0 ? -ORB_RETURN_SPEED * side.getStepZ() : spread;
            ExperienceOrb orb = new ExperienceOrb(context.level(), x, y, z, value);
            orb.setPos(x, y, z);
            orb.setDeltaMovement(speedX, 0.0, speedZ);
            context.level().addFreshEntity(orb);
        }

        private void rollBonuses(ItemStack input, RandomSource random) {
            List<InfernalBonus> bonuses = input.getItem().builtInRegistryHolder().getData(InfernalBonus.DATA_MAP);
            if (bonuses == null) {
                return;
            }
            for (InfernalBonus bonus : bonuses) {
                ItemStack drop = bonusDrop(bonus, random);
                if (!drop.isEmpty()) {
                    context.furnace().launchStack(context.level(), context.side(), drop);
                }
            }
        }

        private static ItemStack bonusDrop(InfernalBonus bonus, RandomSource random) {
            if (random.nextFloat() >= bonus.chance()) {
                return ItemStack.EMPTY;
            }
            Item item = pickItem(bonus.items(), random);
            if (item == null) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item, Math.max(1, bonus.count().sample(random)));
        }

        private static @Nullable Item pickItem(HolderSet<Item> items, RandomSource random) {
            if (!items.isBound()) {
                return null;
            }
            List<Holder<Item>> members = items.stream().filter(Holder::isBound).toList();
            return members.isEmpty() ? null : members.get(random.nextInt(members.size())).value();
        }

    }

    private static final class FurnaceEffects {
        private final ServerLevel level;
        private final BlockPos pos;

        private FurnaceEffects(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        void destroy() {
            RandomSource random = level.getRandom();
            double h = random.nextDouble();
            double i = random.nextDouble();
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, HISS_VOLUME, HISS_PITCH + (float) (h - i) * HISS_PITCH_JITTER);
            level.sendParticles(ParticleTypes.LAVA, pos.getX() + h, pos.getY() + TOP_FACE_HEIGHT, pos.getZ() + i, 1, 0.0, 0.0, 0.0, REJECT_SPARK_SPEED);
        }

        void smelt(Direction side) {
            RandomSource random = level.getRandom();
            for (int pop = 0; pop < SMELT_POPS; pop++) {
                level.playSound(null, pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, POP_VOLUME + random.nextFloat() * POP_VOLUME_JITTER, POP_PITCH + random.nextFloat() * POP_PITCH_JITTER);
            }
            double x = pos.getX() + CENTER + side.getStepX() + (random.nextDouble() - random.nextDouble()) * SPARK_JITTER;
            double y = pos.getY() + SPARK_HEIGHT;
            double z = pos.getZ() + CENTER + side.getStepZ() + (random.nextDouble() - random.nextDouble()) * SPARK_JITTER;
            double outputSpread = random.nextDouble();
            double sideSpread = Math.abs(random.nextDouble() - random.nextDouble()) * SPARK_SIDE_SPREAD;
            double liftSpread = random.nextDouble() * SPARK_LIFT_SPREAD;
            boolean alongX = side.getStepX() != 0;
            level.sendParticles(ParticleTypes.LAVA, x, y, z, SMELT_SPARKS, alongX ? outputSpread : sideSpread, liftSpread, alongX ? sideSpread : outputSpread, SPARK_SPEED);
        }
    }

    private final class FurnaceInventory extends ItemStacksResourceHandler {
        private FurnaceInventory() {
            super(INVENTORY_SIZE);
        }

        int nextOccupied(int from) {
            for (int slot = from; slot < INVENTORY_SIZE; slot++) {
                if (!getResource(slot).isEmpty() && getAmountAsInt(slot) > 0) {
                    return slot;
                }
            }
            return -1;
        }

        void takeOne(int slot) {
            int left = getAmountAsInt(slot) - 1;
            set(slot, left > 0 ? getResource(slot) : ItemResource.EMPTY, Math.max(0, left));
        }

        List<ItemStack> drain() {
            List<ItemStack> drained = new ArrayList<>();
            for (int slot = nextOccupied(0); slot >= 0; slot = nextOccupied(slot + 1)) {
                ItemResource resource = getResource(slot);
                int amount = getAmountAsInt(slot);
                set(slot, ItemResource.EMPTY, 0);
                drained.add(resource.toStack(amount));
            }
            return drained;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            contentsDirty = true;
            setChanged();
        }
    }
}

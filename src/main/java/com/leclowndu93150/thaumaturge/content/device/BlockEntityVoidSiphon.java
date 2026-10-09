package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class BlockEntityVoidSiphon extends AbstractSyncedBlockEntity {
    public static final int PROGRESS_REQUIRED = 2000;

    private static final int WORK_INTERVAL = 20;
    private static final double RIFT_RANGE = 8.0;
    private static final int MIN_RIFT_SIZE = 2;
    private static final float STABILITY_DIVISOR = 15.0F;
    private static final int SHRINK_CHANCE = 33;
    private static final double BLOCK_CENTER = 0.5;
    private static final double TOP_CLEARANCE = 1.01;
    private static final double FOOTPRINT_EDGE = 0.5;
    private static final int SLOT_COUNT = 1;
    private static final String PROGRESS_KEY = "progress";
    private static final String OUTPUT_KEY = "Output";

    private final SeedOutput output = new SeedOutput(this);
    private int progress;
    private int ticks;

    public BlockEntityVoidSiphon(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    public ItemStacksResourceHandler output() {
        return output;
    }

    public int progress() {
        return progress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        boolean due = ++siphon.ticks % WORK_INTERVAL == 0;
        if (due && state.getValue(BlockStateProperties.ENABLED) && siphon.trySeed(false)) {
            siphon.drain(level, pos);
            siphon.produce();
        }
    }

    private boolean trySeed(boolean commit) {
        try (Transaction transaction = Transaction.openRoot()) {
            long accepted = output.insert(ItemResource.of(TTItems.VOID_SEED.get()), 1, transaction);
            if (commit && accepted == 1) {
                transaction.commit();
            }
            return accepted == 1;
        }
    }

    private void drain(Level level, BlockPos pos) {
        Vec3 origin = new Vec3(pos.getX() + BLOCK_CENTER, pos.getY() + TOP_CLEARANCE, pos.getZ() + BLOCK_CENTER);
        int tapped = 0;
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, new AABB(pos).inflate(RIFT_RANGE))) {
            if (isTappable(level, origin, rift)) {
                progress += harvest(level.getRandom(), rift);
                tapped++;
            }
        }
        if (tapped > 0) {
            setChanged();
        }
    }

    private static boolean isTappable(Level level, Vec3 origin, EntityFluxRift rift) {
        if (rift.isRemoved() || rift.currentSize() < MIN_RIFT_SIZE) {
            return false;
        }
        Vec3 riftPos = rift.position();
        return isUnobstructed(level, edgePoint(origin, riftPos), riftPos);
    }

    private static int harvest(RandomSource random, EntityFluxRift rift) {
        int size = rift.currentSize();
        float root = Mth.sqrt(size);
        rift.adjustStability(-root / STABILITY_DIVISOR);
        if (random.nextInt(SHRINK_CHANCE) != 0) {
            return (int) root;
        }
        rift.resize(size - 1);
        return (int) root;
    }

    private static Vec3 edgePoint(Vec3 origin, Vec3 target) {
        Vec3 flat = new Vec3(target.x - origin.x, 0.0, target.z - origin.z);
        double span = flat.length();
        return span <= FOOTPRINT_EDGE ? origin : origin.add(flat.scale(FOOTPRINT_EDGE / span));
    }

    private static boolean isUnobstructed(Level level, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        return level.clip(context).getType() == HitResult.Type.MISS;
    }

    private void produce() {
        int made = 0;
        while (progress >= PROGRESS_REQUIRED && trySeed(true)) {
            progress -= PROGRESS_REQUIRED;
            made++;
        }
        if (made > 0) {
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr(PROGRESS_KEY, 0);
        input.child(OUTPUT_KEY).ifPresent(output::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putInt(PROGRESS_KEY, progress);
        output.serialize(valueOutput.child(OUTPUT_KEY));
    }

    private static final class SeedOutput extends ItemStacksResourceHandler {
        private final BlockEntity owner;

        private SeedOutput(BlockEntity owner) {
            super(SLOT_COUNT);
            this.owner = owner;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TTItems.VOID_SEED.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            owner.setChanged();
        }
    }
}

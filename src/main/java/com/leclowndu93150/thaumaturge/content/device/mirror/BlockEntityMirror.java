package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirror extends BlockEntityMirrorBase {
    public static final int TRANSPORT_EVENT = 1;

    private static final String ITEMS_KEY = "Items";
    private static final String INTAKE_KEY = "Intake";
    private static final int INTAKE_SLOT = 0;
    private static final int INTAKE_SLOT_COUNT = 1;
    private static final int INSTABILITY_THRESHOLD = 128;
    private static final int EJECT_MIN_AGE = 20;
    private static final double EJECT_HEIGHT = 0.25;
    private static final double EJECT_SPEED = 0.15;
    private static final double CENTER = 0.5;
    private static final int PORTAL_COOLDOWN = 20;

    private final List<ItemStack> queue = new ArrayList<>();
    private final MirrorIntake intake = new MirrorIntake();
    private int age;

    public BlockEntityMirror(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MIRROR.get(), pos, state);
    }

    @Override
    protected int instabilityThreshold() {
        return INSTABILITY_THRESHOLD;
    }

    @Override
    protected boolean isSameKind(BlockEntity other) {
        return other instanceof BlockEntityMirror;
    }

    public ResourceHandler<ItemResource> intake() {
        return intake;
    }

    public void serverTick(Level level, BlockPos pos) {
        age++;
        forwardIntake(level);
        if (!queue.isEmpty() && age > EJECT_MIN_AGE) {
            eject(level, pos);
        }
        tickLink();
    }

    public boolean transport(ItemEntity entity) {
        Level world = level;
        BlockEntityMirror target = world == null ? null : linkedMirror();
        if (target == null) {
            return false;
        }
        ItemStack carried = entity.getItem().copy();
        entity.discard();
        handOver(world, target, carried);
        return true;
    }

    public boolean transportDirect(ItemStack stack) {
        boolean accepted = !stack.isEmpty();
        if (accepted) {
            addStack(stack);
        }
        return accepted;
    }

    public void addStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            queue.add(stack.copy());
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        queue.clear();
        input.listOrEmpty(ITEMS_KEY, ItemStack.CODEC).forEach(queue::add);
        input.child(INTAKE_KEY).ifPresent(intake::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<ItemStack> items = output.list(ITEMS_KEY, ItemStack.CODEC);
        queue.stream().filter(stack -> !stack.isEmpty()).forEach(items::add);
        intake.serialize(output.child(INTAKE_KEY));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        dropIntake(pos);
        super.preRemoveSideEffects(pos, state);
    }

    private void dropIntake(BlockPos pos) {
        Level world = level;
        int held = intake.getAmountAsInt(INTAKE_SLOT);
        if (world == null || world.isClientSide() || held <= 0) {
            return;
        }
        ItemStack stack = intake.getResource(INTAKE_SLOT).toStack(held);
        Containers.dropItemStack(world, pos.getX() + CENTER, pos.getY() + CENTER, pos.getZ() + CENTER, stack);
    }

    private @Nullable BlockEntityMirror linkedMirror() {
        if (!partnerAvailable() || !verifyPairing()) {
            return null;
        }
        return partner() instanceof BlockEntityMirror mirror ? mirror : null;
    }

    private boolean intakeOpen() {
        Level world = level;
        return world != null && !world.isClientSide() && partnerAvailable() && pairingIntact();
    }

    private void forwardIntake(Level world) {
        int held = intake.getAmountAsInt(INTAKE_SLOT);
        if (held <= 0 || world.isClientSide()) {
            return;
        }
        ItemStack batch = intake.getResource(INTAKE_SLOT).toStack(held);
        intake.set(INTAKE_SLOT, ItemResource.EMPTY, 0);
        BlockEntityMirror target = linkedMirror();
        if (target == null) {
            world.addFreshEntity(launch(world, worldPosition, batch));
            return;
        }
        handOver(world, target, batch);
    }

    private void handOver(Level world, BlockEntityMirror target, ItemStack stack) {
        target.addStack(stack);
        pileOn(stack.getCount());
        setChanged();
        announceTransport(world, worldPosition);
    }

    private void announceTransport(Level world, BlockPos at) {
        world.blockEvent(at, getBlockState().getBlock(), TRANSPORT_EVENT, 0);
    }

    private void eject(Level level, BlockPos pos) {
        int slot = level.getRandom().nextInt(queue.size());
        ItemStack entry = queue.get(slot);
        if (entry.isEmpty()) {
            queue.remove(slot);
            setChanged();
            return;
        }
        level.addFreshEntity(launch(level, pos, entry.copyWithCount(1)));
        entry.shrink(1);
        if (entry.isEmpty()) {
            queue.remove(slot);
        }
        pileOn(1);
        setChanged();
        announceTransport(level, pos);
    }

    private ItemEntity launch(Level level, BlockPos pos, ItemStack stack) {
        Vec3 origin = Vec3.atLowerCornerOf(pos).add(CENTER, EJECT_HEIGHT, CENTER);
        ItemEntity dropped = new ItemEntity(level, origin.x, origin.y, origin.z, stack);
        dropped.setDeltaMovement(getBlockState().getValue(BlockMirror.FACING).getUnitVec3().scale(EJECT_SPEED));
        dropped.setPortalCooldown(PORTAL_COOLDOWN);
        return dropped;
    }

    private final class MirrorIntake extends ItemStacksResourceHandler {
        private MirrorIntake() {
            super(INTAKE_SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return intakeOpen();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}

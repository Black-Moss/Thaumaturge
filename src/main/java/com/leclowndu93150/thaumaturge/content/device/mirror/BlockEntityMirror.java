package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirror extends BlockEntityMirrorBase {
    public static final int TRANSPORT_EVENT = 1;

    private static final String ITEMS_KEY = "Items";
    private static final int INSTABILITY_THRESHOLD = 128;
    private static final int EJECT_MIN_AGE = 20;
    private static final double EJECT_HEIGHT = 0.25;
    private static final double EJECT_SPEED = 0.15;
    private static final double CENTER = 0.5;
    private static final int PORTAL_COOLDOWN = 20;

    private final List<ItemStack> queue = new ArrayList<>();
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

    public void serverTick(Level level, BlockPos pos) {
        age++;
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
        target.addStack(carried);
        pileOn(carried.getCount());
        setChanged();
        announceTransport(world, worldPosition);
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
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<ItemStack> items = output.list(ITEMS_KEY, ItemStack.CODEC);
        queue.stream().filter(stack -> !stack.isEmpty()).forEach(items::add);
    }

    private @Nullable BlockEntityMirror linkedMirror() {
        if (!verifyPairing()) {
            return null;
        }
        return partner() instanceof BlockEntityMirror mirror ? mirror : null;
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

    private ItemEntity launch(Level level, BlockPos pos, ItemStack single) {
        Vec3 origin = Vec3.atLowerCornerOf(pos).add(CENTER, EJECT_HEIGHT, CENTER);
        ItemEntity dropped = new ItemEntity(level, origin.x, origin.y, origin.z, single);
        dropped.setDeltaMovement(getBlockState().getValue(BlockMirror.FACING).getUnitVec3().scale(EJECT_SPEED));
        dropped.setPortalCooldown(PORTAL_COOLDOWN);
        return dropped;
    }
}

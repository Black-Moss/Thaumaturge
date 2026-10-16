package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirror extends BlockEntityMirrorBase {
    private static final String ITEMS_KEY = "Items";
    private static final int INSTABILITY_THRESHOLD = 128;
    private static final int INSTABILITY_PER_EJECTED_ITEM = 1;
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

    public void transport(ItemEntity dropped) {
        if (!(level instanceof ServerLevel) || dropped.getItem().isEmpty()) {
            return;
        }
        BlockEntityMirror partner = linkedMirror();
        if (partner == null) {
            return;
        }
        int count = dropped.getItem().getCount();
        partner.addStack(dropped.getItem());
        dropped.discard();
        pileOn(count);
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

    private void eject(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int index = server.getRandom().nextInt(queue.size());
        ItemStack source = queue.get(index);
        ItemStack single = source.split(1);
        if (source.isEmpty()) {
            queue.remove(index);
        }
        setChanged();
        if (single.isEmpty()) {
            return;
        }
        launch(server, pos, single);
        pileOn(INSTABILITY_PER_EJECTED_ITEM);
    }

    private void launch(ServerLevel level, BlockPos pos, ItemStack stack) {
        Direction facing = getBlockState().getValue(BlockMirror.FACING);
        ItemEntity thrown = new ItemEntity(level, pos.getX() + CENTER, pos.getY() + EJECT_HEIGHT, pos.getZ() + CENTER, stack, facing.getStepX() * EJECT_SPEED, facing.getStepY() * EJECT_SPEED,
                facing.getStepZ() * EJECT_SPEED);
        thrown.setPortalCooldown(PORTAL_COOLDOWN);
        level.addFreshEntity(thrown);
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
        if (!partnerAvailable() || !verifyPairing()) {
            return null;
        }
        return partner() instanceof BlockEntityMirror mirror ? mirror : null;
    }
}

package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class BlockEntityArcaneWorkbench extends BlockEntity implements MenuProvider {
    private static final int CHARGER_RADIUS_CHUNKS = 1;
    private static final float MIN_ANCHOR_REQUEST = 1.0F;
    private static final double DRAIN_EPSILON = 0.01;
    private static final String HOST_SEPARATOR = ":";

    public int auraVis;

    private final InventoryArcaneWorkbench inventory = new InventoryArcaneWorkbench();

    public BlockEntityArcaneWorkbench(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ARCANE_WORKBENCH.get(), pos, state);
        inventory.addChangedListener(this::setChanged);
    }

    public void refreshAura() {
        if (level instanceof ServerLevel serverLevel) {
            auraVis = anchors(serverLevel).stream().mapToInt(anchor -> (int) AuraHelper.getVis(serverLevel, anchor)).sum();
        }
    }

    public InventoryArcaneWorkbench getInventory() {
        return inventory;
    }

    public boolean spendAura(int vis, TransactionContext transaction) {
        if (vis <= 0) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        List<BlockPos> anchors = anchors(serverLevel);
        if (anchors.isEmpty()) {
            return false;
        }
        float perAnchor = Math.max(MIN_ANCHOR_REQUEST, (float) vis / anchors.size());
        double owed = vis;
        while (owed > DRAIN_EPSILON) {
            double progress = 0.0;
            for (BlockPos anchor : anchors) {
                if (owed <= DRAIN_EPSILON) {
                    break;
                }
                float request = (float) Math.min(owed, perAnchor);
                float drained = AuraHelper.drainVis(serverLevel, anchor, request, transaction);
                owed -= drained;
                progress += drained;
            }
            if (progress < DRAIN_EPSILON && owed > DRAIN_EPSILON) {
                return false;
            }
        }
        return true;
    }

    public UUID hostIdentity() {
        Level current = level;
        if (current == null) {
            throw new IllegalStateException("Workbench host identity requires a level");
        }
        String key = current.dimension().identifier() + HOST_SEPARATOR + worldPosition.asLong();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        writeSlots(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        readSlots(input);
    }

    private void writeSlots(ValueOutput output) {
        ContainerHelper.saveAllItems(output, inventory.getItems());
    }

    private void readSlots(ValueInput input) {
        Collections.fill(inventory.getItems(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, inventory.getItems());
        inventory.setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, inventory);
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuArcaneWorkbench(containerId, playerInventory, this);
    }

    private List<BlockPos> anchors(ServerLevel serverLevel) {
        List<BlockPos> anchors = new ArrayList<>();
        if (serverLevel.getBlockState(worldPosition.above()).is(TTBlocks.ARCANE_WORKBENCH_CHARGER)) {
            int y = worldPosition.getY();
            ChunkPos.rangeClosed(ChunkPos.containing(worldPosition), CHARGER_RADIUS_CHUNKS).forEach(chunk -> anchors.add(chunk.getMiddleBlockPosition(y)));
        } else {
            anchors.add(worldPosition);
        }
        anchors.removeIf(anchor -> !serverLevel.hasChunkAt(anchor));
        anchors.sort(Comparator.comparingLong(BlockPos::asLong));
        return anchors;
    }
}

package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

public final class BlockEntityEldritchReliquary extends BlockEntity {
    private static final String MAZE = "maze";
    private static final String ROLE = "role";
    private static final String LOOT = "loot";
    private static final String CACHE_PREFIX = "cache/";
    private static final int VIEW_INTERVAL = 20;

    private Optional<MazeId> maze = Optional.empty();
    private ReliquaryRole role = ReliquaryRole.CACHE;
    private Optional<ResourceKey<LootTable>> loot = Optional.empty();
    private final Map<UUID, ReliquaryView> sent = new HashMap<>();

    public BlockEntityEldritchReliquary(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_RELIQUARY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEldritchReliquary reliquary) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % VIEW_INTERVAL == 0) {
            ReliquaryViews.push(serverLevel, reliquary);
        }
    }

    public void configure(MazeId maze, ReliquaryRole role, Optional<ResourceKey<LootTable>> loot) {
        this.maze = Optional.of(maze);
        this.role = role;
        this.loot = loot;
        sync();
    }

    public Optional<MazeId> maze() {
        return maze;
    }

    public ReliquaryRole role() {
        return role;
    }

    Optional<ResourceKey<LootTable>> loot() {
        return loot;
    }

    String claimKey() {
        return role == ReliquaryRole.CACHE ? CACHE_PREFIX + worldPosition.asLong() : role.getSerializedName();
    }

    Map<UUID, ReliquaryView> sentViews() {
        return sent;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        maze = input.read(MAZE, MazeId.CODEC);
        role = input.read(ROLE, ReliquaryRole.CODEC).orElse(ReliquaryRole.CACHE);
        loot = input.read(LOOT, LootTable.KEY_CODEC);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(MAZE, MazeId.CODEC, maze.orElse(null));
        output.store(ROLE, ReliquaryRole.CODEC, role);
        output.storeNullable(LOOT, LootTable.KEY_CODEC, loot.orElse(null));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

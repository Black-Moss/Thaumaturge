package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityEldritchAltar extends BlockEntity {
    static final int MAX_EYES = 4;
    private static final int SITE_INTERVAL = 20;
    private static final String EYES = "eyes";
    private static final String SITE = "site";
    private static final String GARRISON = "garrison";
    private static final String RITUAL = "ritual";
    private static final String LINK = "link";
    private static final String AWAKENED_AT = "awakened_at";

    private int eyes;
    private Optional<ResourceKey<ObeliskSite>> site = Optional.empty();
    private GarrisonState garrison = GarrisonState.FRESH;
    private Optional<AltarRitual> ritual = Optional.empty();
    private Optional<MazeId> link = Optional.empty();
    private long awakenedAt;

    public BlockEntityEldritchAltar(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ritual.ifPresent(active -> AltarRituals.tick(serverLevel, this, active));
        if (serverLevel.getGameTime() % SITE_INTERVAL != 0) {
            return;
        }
        AltarRituals.updateSeal(serverLevel, this);
        if (!portalOpen()) {
            AltarSite.tick(serverLevel, this);
        }
    }

    boolean portalOpen() {
        return level != null && level.getBlockState(worldPosition.above()).is(TCBlocks.ELDRITCH_PORTAL.get());
    }

    public int getEyes() {
        return eyes;
    }

    void setEyes(int eyes) {
        this.eyes = eyes;
        changed();
    }

    public ResourceKey<ObeliskSite> site() {
        return site.orElse(ObeliskSite.DORMANT);
    }

    public void assignSite(ResourceKey<ObeliskSite> site) {
        this.site = Optional.of(site);
        setChanged();
    }

    GarrisonState garrison() {
        return garrison;
    }

    void setGarrison(GarrisonState garrison) {
        this.garrison = garrison;
        setChanged();
    }

    Optional<AltarRitual> ritual() {
        return ritual;
    }

    void replaceRitual(AltarRitual ritual) {
        this.ritual = Optional.of(ritual);
        setChanged();
    }

    void setRitual(Optional<AltarRitual> ritual) {
        this.ritual = ritual;
        changed();
    }

    Optional<MazeId> link() {
        return link;
    }

    void setLink(Optional<MazeId> link) {
        this.link = link;
        changed();
    }

    long awakenedAt() {
        return awakenedAt;
    }

    void setAwakenedAt(long awakenedAt) {
        this.awakenedAt = awakenedAt;
        setChanged();
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && level.getBlockState(pos.above()).is(TCBlocks.ELDRITCH_PORTAL.get())) {
            level.removeBlock(pos.above(), false);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        eyes = input.getIntOr(EYES, 0);
        site = input.read(SITE, ResourceKey.codec(ObeliskSite.REGISTRY_KEY));
        garrison = input.read(GARRISON, GarrisonState.CODEC).orElse(GarrisonState.FRESH);
        ritual = input.read(RITUAL, AltarRitual.CODEC);
        link = input.read(LINK, MazeId.CODEC);
        awakenedAt = input.getLongOr(AWAKENED_AT, 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(EYES, eyes);
        site.ifPresent(value -> output.store(SITE, ResourceKey.codec(ObeliskSite.REGISTRY_KEY), value));
        output.store(GARRISON, GarrisonState.CODEC, garrison);
        ritual.ifPresent(value -> output.store(RITUAL, AltarRitual.CODEC, value));
        link.ifPresent(value -> output.store(LINK, MazeId.CODEC, value));
        output.putLong(AWAKENED_AT, awakenedAt);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            tag.merge(output.buildResult());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

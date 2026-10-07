package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityEldritchAltar extends AbstractSyncedBlockEntity {
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
        super(TTBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
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
        return level != null && level.getBlockState(worldPosition.above()).is(TTBlocks.ELDRITCH_PORTAL.get());
    }

    public int getEyes() {
        return eyes;
    }

    void setEyes(int eyes) {
        this.eyes = eyes;
        setChangedAndSync();
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
        setChangedAndSync();
    }

    Optional<MazeId> link() {
        return link;
    }

    void setLink(Optional<MazeId> link) {
        this.link = link;
        setChangedAndSync();
    }

    long awakenedAt() {
        return awakenedAt;
    }

    void setAwakenedAt(long awakenedAt) {
        this.awakenedAt = awakenedAt;
        setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && level.getBlockState(pos.above()).is(TTBlocks.ELDRITCH_PORTAL.get())) {
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

}

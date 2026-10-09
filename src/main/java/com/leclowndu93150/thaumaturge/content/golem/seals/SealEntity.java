package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterSpec;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealEntity implements ISealEntity {
    public static final Codec<SealEntity> CODEC = new Format().codec();

    private static final String LEGACY_NAMESPACE = "thaumcraft";
    private static final String KEY_DATA = "data";
    private static final String KEY_AREA = "area";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_REDSTONE = "redstone";
    private static final String KEY_LOCKED = "locked";
    private static final String KEY_COLOR = "color";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_POS = "pos";
    private static final String KEY_TYPE = "type";
    private static final BlockPos UNIT_AREA = new BlockPos(1, 1, 1);
    private static final int OPENING_WIDTH = 3;
    private static final int OPENING_DEPTH = 1;
    private static final byte NEUTRAL = 0;

    private final Contents contents;
    private final Kind kind;
    private final SealPos pos;
    private Traits traits;
    private boolean stopped;

    private SealEntity(SealPos pos, Kind kind, Contents contents, Traits traits) {
        this.traits = traits;
        this.contents = contents;
        this.kind = kind;
        this.pos = pos;
    }

    public static SealEntity place(SealPos pos, Identifier typeId, SealType type, UUID owner) {
        BlockPos area = type.hasArea() ? openingAt(pos.face()) : UNIT_AREA;
        Traits traits = Traits.initial(owner, area);
        return new SealEntity(pos, new Kind(typeId, type), Contents.fresh(type), traits);
    }

    private static BlockPos openingAt(Direction face) {
        return new BlockPos(extent(face, Direction.Axis.X), extent(face, Direction.Axis.Y), extent(face, Direction.Axis.Z));
    }

    private static int extent(Direction face, Direction.Axis axis) {
        return face.getAxis() == axis ? OPENING_DEPTH : OPENING_WIDTH;
    }

    @Override
    public @Nullable UUID owner() {
        return traits.owner();
    }

    @Override
    public void setOwner(@Nullable UUID incoming) {
        adjust(current -> current.withOwner(incoming));
    }

    @Override
    public void setLocked(boolean incoming) {
        adjust(current -> current.withLocked(incoming));
    }

    @Override
    public void setArea(BlockPos incoming) {
        adjust(current -> current.withArea(incoming));
    }

    @Override
    public boolean isLocked() {
        return traits.locked();
    }

    @Override
    public BlockPos area() {
        return traits.area();
    }

    @Override
    public byte color() {
        return traits.color();
    }

    @Override
    public void setColor(byte incoming) {
        adjust(current -> current.withColor(incoming));
    }

    @Override
    public SealPos pos() {
        return pos;
    }

    @Override
    public void setRedstoneControlled(boolean incoming) {
        adjust(current -> current.withRedstone(incoming));
    }

    @Override
    public byte priority() {
        return traits.priority();
    }

    @Override
    public void setPriority(byte incoming) {
        adjust(current -> current.withPriority(incoming));
    }

    @Override
    public Optional<ISealFilter> filter() {
        return contents.filter().map(ISealFilter.class::cast);
    }

    @Override
    public ISealBehavior behavior() {
        return contents.behavior();
    }

    @Override
    public boolean setting(SealSetting setting) {
        return contents.settings().get(setting);
    }

    @Override
    public void setSetting(SealSetting setting, boolean value) {
        contents.settings().set(setting, value);
    }

    private void adjust(UnaryOperator<Traits> change) {
        traits = change.apply(traits);
    }

    public void tick(ServerLevel level) {
        boolean halted = isStoppedByRedstone(level);
        if (halted && !stopped) {
            TaskBoard.of(level).endAllFrom(pos);
        }
        stopped = halted;
        if (halted) {
            return;
        }
        contents.behavior().tick(level, this);
    }

    @Override
    public boolean isStoppedByRedstone(Level level) {
        return traits.redstone() && (level.hasNeighborSignal(pos.pos()) || level.hasNeighborSignal(pos.pos().relative(pos.face())));
    }

    @Override
    public boolean isRedstoneControlled() {
        return traits.redstone();
    }

    public Identifier typeId() {
        return kind.id();
    }

    @Override
    public SealType type() {
        return kind.type();
    }

    @Override
    public void markChanged(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            publish(serverLevel);
        }
    }

    private void publish(ServerLevel level) {
        ChunkPos chunk = ChunkPos.containing(pos.pos());
        SealHandler.flagUnsaved(level, pos.pos());
        PacketDistributor.sendToPlayersTrackingChunk(level, chunk, ClientboundSealPayload.update(this));
    }

    private static Identifier migrate(Identifier id) {
        return LEGACY_NAMESPACE.equals(id.getNamespace()) ? TTIds.rl(id.getPath()) : id;
    }

    private record Kind(Identifier id, SealType type) {
    }

    private record Traits(byte priority, byte color, boolean locked, boolean redstone, @Nullable UUID owner, BlockPos area) {
        static Traits initial(@Nullable UUID owner, BlockPos area) {
            return new Traits(NEUTRAL, NEUTRAL, false, false, owner, area);
        }

        Traits withPriority(byte value) {
            return new Traits(value, color, locked, redstone, owner, area);
        }

        Traits withColor(byte value) {
            return new Traits(priority, value, locked, redstone, owner, area);
        }

        Traits withLocked(boolean value) {
            return new Traits(priority, color, value, redstone, owner, area);
        }

        Traits withRedstone(boolean value) {
            return new Traits(priority, color, locked, value, owner, area);
        }

        Traits withOwner(@Nullable UUID value) {
            return new Traits(priority, color, locked, redstone, value, area);
        }

        Traits withArea(BlockPos value) {
            return new Traits(priority, color, locked, redstone, owner, value);
        }
    }

    private record Contents(Optional<SealFilterState> filter, SealSettingValues settings, ISealBehavior behavior) {
        static Contents fresh(SealType type) {
            return new Contents(type.filter().map(SealFilterState::new), new SealSettingValues(type), type.newBehavior());
        }

        static MapCodec<Contents> codec(SealType type) {
            return RecordCodecBuilder.mapCodec(instance -> instance
                    .group(filterCodec(type).forGetter(Contents::filter), SealSettingValues.codec(type).forGetter(Contents::settings), type.behaviorCodec().forGetter(Contents::behavior))
                    .apply(instance, Contents::new));
        }

        private static MapCodec<Optional<SealFilterState>> filterCodec(SealType type) {
            Optional<SealFilterSpec> spec = type.filter();
            if (spec.isEmpty()) {
                return MapCodec.unit(Optional.empty());
            }
            return SealFilterState.codec(spec.get()).xmap(Optional::of, stored -> stored.orElseThrow());
        }
    }

    private static final class Format extends MapCodec<SealEntity> {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(KEY_TYPE, KEY_POS, KEY_PRIORITY, KEY_COLOR, KEY_LOCKED, KEY_REDSTONE, KEY_OWNER, KEY_AREA, KEY_DATA).map(ops::createString);
        }

        @Override
        public <T> DataResult<SealEntity> decode(DynamicOps<T> ops, MapLike<T> input) {
            T stored = input.get(KEY_TYPE);
            if (stored == null) {
                return DataResult.error(() -> "Seal has no type");
            }
            return Identifier.CODEC.parse(ops, stored).map(SealEntity::migrate)
                    .flatMap(id -> TTSeals.registry().getOptional(id).map(type -> body(id, type).decode(ops, input)).orElseGet(() -> DataResult.error(() -> "Unknown seal type " + id)));
        }

        @Override
        public <T> RecordBuilder<T> encode(SealEntity input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            RecordBuilder<T> typed = prefix.add(KEY_TYPE, Identifier.CODEC.encodeStart(ops, input.kind.id()));
            return body(input.kind.id(), input.kind.type()).encode(input, ops, typed);
        }

        private static MapCodec<SealEntity> body(Identifier typeId, SealType type) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(SealPos.CODEC.fieldOf(KEY_POS).forGetter(SealEntity::pos),
                    Codec.BYTE.optionalFieldOf(KEY_PRIORITY, NEUTRAL).forGetter(SealEntity::priority), Codec.BYTE.optionalFieldOf(KEY_COLOR, NEUTRAL).forGetter(SealEntity::color),
                    Codec.BOOL.optionalFieldOf(KEY_LOCKED, false).forGetter(SealEntity::isLocked), Codec.BOOL.optionalFieldOf(KEY_REDSTONE, false).forGetter(SealEntity::isRedstoneControlled),
                    UUIDUtil.CODEC.optionalFieldOf(KEY_OWNER).forGetter(seal -> Optional.ofNullable(seal.owner())), BlockPos.CODEC.optionalFieldOf(KEY_AREA, UNIT_AREA).forGetter(SealEntity::area),
                    Contents.codec(type).codec().lenientOptionalFieldOf(KEY_DATA).forGetter(seal -> Optional.of(seal.contents)))
                    .apply(instance, (pos, priority, color, locked, redstone, owner, area, data) -> assemble(typeId, type, pos, priority, color, locked, redstone, owner, area, data)));
        }

        private static SealEntity assemble(Identifier typeId, SealType type, SealPos pos, byte priority, byte color, boolean locked, boolean redstone, Optional<UUID> owner, BlockPos area, Optional<Contents> data) {
            Traits traits = new Traits(priority, color, locked, redstone, owner.orElse(null), area);
            return new SealEntity(pos, new Kind(typeId, type), data.orElseGet(() -> Contents.fresh(type)), traits);
        }
    }
}

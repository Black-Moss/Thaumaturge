package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.capability.ResearchStatus;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class PlayerKnowledge implements IPlayerKnowledge {
    private static final String RESEARCH_FIELD = "research";
    private static final String KNOWLEDGE_FIELD = "knowledge";
    private static final String NO_CATEGORY_SORT_KEY = "";

    public static final MapCodec<PlayerKnowledge> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResearchRecord.CODEC.listOf().optionalFieldOf(RESEARCH_FIELD).forGetter(knowledge -> Optional.of(knowledge.researchRecords())),
                    KnowledgeRecord.CODEC.listOf().optionalFieldOf(KNOWLEDGE_FIELD).forGetter(knowledge -> Optional.of(knowledge.knowledgeRecords())))
            .apply(instance, (research, points) -> fromRecords(research.orElse(List.of()), points.orElse(List.of()))));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private boolean pendingSync;
    private final Object2IntMap<KnowledgeKey> points = new Object2IntOpenHashMap<>();
    private final Map<Identifier, ResearchDetail> details = new HashMap<>();
    private final Set<Identifier> researchKeys = new HashSet<>();
    private final Set<Identifier> researchKeyView = Collections.unmodifiableSet(researchKeys);

    public PlayerKnowledge() {}

    private PlayerKnowledge(PlayerKnowledge source) {
        absorb(source);
    }

    private static final class ResearchDetail {
        private boolean staged;
        private int stage;
        private boolean finished;
        private int flags;

        ResearchDetail copy() {
            ResearchDetail twin = new ResearchDetail();
            twin.staged = staged;
            twin.stage = stage;
            twin.finished = finished;
            twin.flags = flags;
            return twin;
        }

        boolean reopen() {
            boolean was = finished;
            finished = false;
            return was;
        }

        boolean finish() {
            boolean was = finished;
            finished = true;
            return !was;
        }

        boolean restage(int value) {
            boolean differs = !staged || stage != value;
            assignStage(value);
            return differs;
        }

        boolean raiseFlag(int mask) {
            int updated = flags | mask;
            boolean differs = updated != flags;
            flags = updated;
            return differs;
        }

        boolean dropFlag(int mask) {
            int updated = flags & ~mask;
            boolean differs = updated != flags;
            flags = updated;
            return differs;
        }

        int stageOrZero() {
            return staged ? stage : 0;
        }

        Optional<Integer> stageIfSet() {
            return staged ? Optional.of(stage) : Optional.empty();
        }

        void assignStage(int value) {
            staged = true;
            stage = value;
        }
    }

    private record KnowledgeKey(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) {
    }

    private record ResearchRecord(Identifier id, Optional<Integer> stage, Optional<List<ResearchFlag>> flags, boolean complete) {
        static final Codec<ResearchRecord> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(LegacyIds.IDENTIFIER_CODEC.fieldOf("id").forGetter(ResearchRecord::id), Codec.INT.optionalFieldOf("stage").forGetter(ResearchRecord::stage),
                        ResearchFlag.CODEC.listOf().optionalFieldOf("flags").forGetter(ResearchRecord::flags), Codec.BOOL.optionalFieldOf("complete", false).forGetter(ResearchRecord::complete))
                .apply(instance, ResearchRecord::new));
    }

    private record KnowledgeRecord(KnowledgeType type, Optional<ResourceKey<IResearchCategory>> category, int amount) {
        static final Codec<KnowledgeRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeRecord::type),
                LegacyIds.resourceKeyCodec(IResearchCategory.REGISTRY_KEY).optionalFieldOf("category").forGetter(KnowledgeRecord::category),
                Codec.INT.fieldOf("amount").forGetter(KnowledgeRecord::amount)).apply(instance, KnowledgeRecord::new));

        String categorySortKey() {
            return category.map(key -> key.identifier().toString()).orElse(NO_CATEGORY_SORT_KEY);
        }
    }

    private static int maskOf(ResearchFlag flag) {
        return 1 << flag.ordinal();
    }

    private static int maskOf(Collection<ResearchFlag> flags) {
        int mask = 0;
        for (ResearchFlag flag : flags) {
            mask |= maskOf(flag);
        }
        return mask;
    }

    private static List<ResearchFlag> flagsOf(int mask) {
        return Arrays.stream(ResearchFlag.values()).filter(flag -> (mask & maskOf(flag)) != 0).toList();
    }

    private static PlayerKnowledge fromRecords(List<ResearchRecord> research, List<KnowledgeRecord> knowledge) {
        PlayerKnowledge restored = new PlayerKnowledge();
        research.forEach(restored::restoreResearch);
        knowledge.stream().filter(record -> record.amount() > 0).forEach(restored::restorePoints);
        return restored;
    }

    private void restoreResearch(ResearchRecord record) {
        Identifier id = record.id();
        researchKeys.add(id);
        ResearchDetail detail = detailFor(id);
        detail.finished |= record.complete();
        record.stage().ifPresent(detail::assignStage);
        int restoredMask = maskOf(record.flags().orElse(List.of()));
        if (restoredMask != 0) {
            detail.flags = restoredMask;
        }
    }

    private void restorePoints(KnowledgeRecord record) {
        points.put(new KnowledgeKey(record.type(), record.category().orElse(null)), record.amount());
    }

    private ResearchDetail detailFor(Identifier id) {
        return details.computeIfAbsent(id, key -> new ResearchDetail());
    }

    private int maskFor(Identifier id) {
        return Optional.ofNullable(details.get(id)).map(detail -> detail.flags).orElse(0);
    }

    private List<ResearchRecord> researchRecords() {
        return researchKeys.stream().sorted().map(this::recordOf).toList();
    }

    private ResearchRecord recordOf(Identifier id) {
        ResearchDetail stored = details.get(id);
        ResearchDetail detail = stored == null ? new ResearchDetail() : stored;
        return new ResearchRecord(id, detail.stageIfSet(), Optional.of(flagsOf(detail.flags)), detail.finished);
    }

    private List<KnowledgeRecord> knowledgeRecords() {
        Comparator<KnowledgeRecord> order = Comparator.comparingInt((KnowledgeRecord record) -> record.type().ordinal()).thenComparing(KnowledgeRecord::categorySortKey);
        return points.object2IntEntrySet().stream().filter(entry -> entry.getIntValue() > 0)
                .map(entry -> new KnowledgeRecord(entry.getKey().type(), Optional.ofNullable(entry.getKey().category()), entry.getIntValue())).sorted(order).toList();
    }

    @Override
    public void clear() {
        researchKeys.clear();
        details.clear();
        points.clear();
    }

    @Override
    public void sync(ServerPlayer player) {
        pendingSync = true;
    }

    public boolean takeSyncPending() {
        boolean pending = pendingSync;
        pendingSync = false;
        return pending;
    }

    @Override
    public boolean isResearchKnown(Identifier research) {
        return research != null && researchKeys.contains(research);
    }

    @Override
    public boolean addResearch(Identifier research) {
        return research != null && researchKeys.add(research);
    }

    @Override
    public boolean removeResearch(Identifier research) {
        boolean wasKnown = researchKeys.remove(research);
        if (wasKnown) {
            details.remove(research);
        }
        return wasKnown;
    }

    @Override
    public Set<Identifier> researchList() {
        return researchKeyView;
    }

    @Override
    public int researchStage(Identifier research) {
        if (!isResearchKnown(research)) {
            return -1;
        }
        ResearchDetail detail = details.get(research);
        return detail == null ? 0 : detail.stageOrZero();
    }

    @Override
    public boolean isResearchKnown(Identifier research, int minimumStage) {
        return isResearchKnown(research) && researchStage(research) >= minimumStage;
    }

    @Override
    public boolean setResearchStage(Identifier research, int stage) {
        return stage > 0 && isResearchKnown(research) && detailFor(research).restage(stage);
    }

    @Override
    public ResearchStatus researchStatus(Identifier research) {
        if (!isResearchKnown(research)) {
            return ResearchStatus.UNKNOWN;
        }
        ResearchDetail detail = details.get(research);
        boolean done = detail != null && detail.finished;
        return done ? ResearchStatus.COMPLETE : ResearchStatus.IN_PROGRESS;
    }

    @Override
    public boolean isResearchComplete(Identifier research) {
        return researchStatus(research) == ResearchStatus.COMPLETE;
    }

    public boolean markComplete(Identifier research) {
        return isResearchKnown(research) && detailFor(research).finish();
    }

    public boolean clearComplete(Identifier research) {
        return details.containsKey(research) && details.get(research).reopen();
    }

    @Override
    public boolean hasResearchFlag(Identifier research, ResearchFlag flag) {
        return (maskFor(research) & maskOf(flag)) != 0;
    }

    @Override
    public boolean setResearchFlag(Identifier research, ResearchFlag flag) {
        return detailFor(research).raiseFlag(maskOf(flag));
    }

    @Override
    public boolean clearResearchFlag(Identifier research, ResearchFlag flag) {
        return details.containsKey(research) && details.get(research).dropFlag(maskOf(flag));
    }

    @Override
    public int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return type == null ? 0 : points.getInt(new KnowledgeKey(type, category));
    }

    @Override
    public int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return type == null ? 0 : rawKnowledge(type, category) / type.progression();
    }

    @Override
    public boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) {
            return false;
        }
        int previous = rawKnowledge(type, category);
        KnowledgeKey slot = new KnowledgeKey(type, category);
        int updated = Math.max(0, previous + amount);
        if (updated == 0) {
            points.removeInt(slot);
        } else {
            points.put(slot, updated);
        }
        return updated != previous;
    }

    public static PlayerKnowledge snapshotOf(PlayerKnowledge source) {
        return new PlayerKnowledge(source);
    }

    public void mergeResearchFrom(PlayerKnowledge other) {
        researchKeys.addAll(other.researchKeys);
        other.details.forEach((id, incoming) -> {
            if (incoming.finished || incoming.staged) {
                ResearchDetail mine = detailFor(id);
                mine.finished |= incoming.finished;
                if (incoming.staged) {
                    mine.assignStage(mine.staged ? Math.max(mine.stage, incoming.stage) : incoming.stage);
                }
            }
        });
    }

    public void copyFrom(PlayerKnowledge other) {
        clear();
        absorb(other);
    }

    private void absorb(PlayerKnowledge other) {
        researchKeys.addAll(other.researchKeys);
        other.details.forEach((id, detail) -> details.put(id, detail.copy()));
        points.putAll(other.points);
    }

    public void applyAutoUnlock(ServerPlayer player) {
        player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(holder -> {
            Identifier id = holder.key().identifier();
            if (holder.value().hasMeta(ResearchEntryMeta.AUTOUNLOCK) && !researchKeys.contains(id) && ResearchUnlockConditions.passes(player, this, id)) {
                researchKeys.add(id);
            }
        }));
    }
}

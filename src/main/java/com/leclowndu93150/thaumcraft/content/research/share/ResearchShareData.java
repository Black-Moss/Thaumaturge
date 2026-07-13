package com.leclowndu93150.thaumcraft.content.research.share;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public final class ResearchShareData extends SavedData {

    public static final class ShareLink {
        final UUID first;
        final UUID second;
        final Set<Identifier> union;

        ShareLink(UUID first, UUID second, Set<Identifier> union) {
            this.first = first;
            this.second = second;
            this.union = new LinkedHashSet<>(union);
        }

        public UUID first() {
            return first;
        }

        public UUID second() {
            return second;
        }

        public Set<Identifier> union() {
            return union;
        }

        public boolean involves(UUID player) {
            return first.equals(player) || second.equals(player);
        }

        static final Codec<ShareLink> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                UUIDUtil.CODEC.fieldOf("first").forGetter(link -> link.first),
                UUIDUtil.CODEC.fieldOf("second").forGetter(link -> link.second),
                Identifier.CODEC.listOf().fieldOf("union")
                        .xmap(list -> (Set<Identifier>) new LinkedHashSet<>(list), List::copyOf)
                        .forGetter(link -> link.union)
        ).apply(builder, ShareLink::new));
    }

    public static final Codec<ResearchShareData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ShareLink.CODEC.listOf().fieldOf("links").forGetter(data -> data.links)
    ).apply(builder, ResearchShareData::new));

    public static final SavedDataType<ResearchShareData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "research_share"),
            ResearchShareData::new,
            CODEC,
            DataFixTypes.LEVEL);

    private final List<ShareLink> links;

    public ResearchShareData() {
        this(List.of());
    }

    private ResearchShareData(List<ShareLink> links) {
        this.links = new ArrayList<>(links);
    }

    public static ResearchShareData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<ShareLink> links() {
        return links;
    }

    public @Nullable ShareLink linkBetween(UUID a, UUID b) {
        for (ShareLink link : links) {
            if (link.involves(a) && link.involves(b)) {
                return link;
            }
        }
        return null;
    }

    public ShareLink link(UUID a, UUID b) {
        ShareLink existing = linkBetween(a, b);
        if (existing != null) {
            return existing;
        }
        ShareLink link = new ShareLink(a, b, Set.of());
        links.add(link);
        setDirty();
        return link;
    }

    public int unlinkAll(UUID player) {
        int removed = 0;
        for (int i = links.size() - 1; i >= 0; i--) {
            if (links.get(i).involves(player)) {
                links.remove(i);
                removed++;
            }
        }
        if (removed > 0) {
            setDirty();
        }
        return removed;
    }
}

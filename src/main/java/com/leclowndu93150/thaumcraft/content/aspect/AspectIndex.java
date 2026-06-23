package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AspectIndex implements IAspectIndex {
    public static final AspectIndex EMPTY = new AspectIndex(Map.of());

    public static final Codec<AspectIndex> CODEC = Codec.unboundedMap(Identifier.CODEC, AspectList.CODEC)
            .xmap(AspectIndex::fromIdMap, AspectIndex::toIdMap);

    public static final StreamCodec<RegistryFriendlyByteBuf, AspectIndex> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            AspectIndex::entries,
            AspectIndex::fromEntries
    );

    private final Reference2ObjectMap<Item, AspectList> byItem;

    private AspectIndex(Map<Item, AspectList> source) {
        Reference2ObjectMap<Item, AspectList> map = new Reference2ObjectOpenHashMap<>(source.size());
        for (Map.Entry<Item, AspectList> entry : source.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        this.byItem = map;
    }

    public static AspectIndex of(Map<Item, AspectList> source) {
        return source.isEmpty() ? EMPTY : new AspectIndex(source);
    }

    @Override
    public AspectList of(Item item) {
        AspectList result = byItem.get(item);
        return result == null ? AspectList.EMPTY : result;
    }

    @Override
    public AspectList of(ItemStack stack) {
        if (stack.isEmpty()) {
            return AspectList.EMPTY;
        }
        AspectList override = stack.get(TCDataComponents.ASPECTS.get());
        if (override != null) {
            return override;
        }
        return of(stack.getItem());
    }

    public int size() {
        return byItem.size();
    }

    public boolean isEmpty() {
        return byItem.isEmpty();
    }

    private List<Entry> entries() {
        List<Entry> list = new java.util.ArrayList<>(byItem.size());
        byItem.forEach((item, aspects) -> list.add(new Entry(BuiltInRegistries.ITEM.getKey(item), aspects)));
        return list;
    }

    private static AspectIndex fromEntries(List<Entry> entries) {
        Map<Item, AspectList> map = new HashMap<>(entries.size());
        for (Entry entry : entries) {
            Item item = BuiltInRegistries.ITEM.getValue(entry.itemId);
            if (item != null) {
                map.put(item, entry.aspects);
            }
        }
        return of(map);
    }

    private Map<Identifier, AspectList> toIdMap() {
        Map<Identifier, AspectList> out = new HashMap<>(byItem.size());
        byItem.forEach((item, aspects) -> out.put(BuiltInRegistries.ITEM.getKey(item), aspects));
        return out;
    }

    private static AspectIndex fromIdMap(Map<Identifier, AspectList> map) {
        Map<Item, AspectList> out = new HashMap<>(map.size());
        map.forEach((id, aspects) -> {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null) {
                out.put(item, aspects);
            }
        });
        return of(out);
    }

    private record Entry(Identifier itemId, AspectList aspects) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Identifier.CODEC.fieldOf("item").forGetter(Entry::itemId),
                AspectList.CODEC.fieldOf("aspects").forGetter(Entry::aspects)
        ).apply(builder, Entry::new));

        static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Entry::itemId,
                AspectList.STREAM_CODEC, Entry::aspects,
                Entry::new
        );
    }
}

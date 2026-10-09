package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemComponent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class GolemProperties implements IGolemProperties {
    private static final String MATERIAL_KEY = "material";
    private static final String HEAD_KEY = "head";
    private static final String ARMS_KEY = "arms";
    private static final String LEGS_KEY = "legs";
    private static final String ADDON_KEY = "addon";
    private static final String RANK_KEY = "rank";
    private static final int MATERIAL_BASE_MULTIPLIER = 2;

    public static final Codec<GolemProperties> CODEC = Codec
            .lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance
                    .group(partCodec(TTGolemParts::materials).fieldOf(MATERIAL_KEY).forGetter(GolemProperties::material),
                            partCodec(TTGolemParts::heads).fieldOf(HEAD_KEY).forGetter(GolemProperties::head), partCodec(TTGolemParts::arms).fieldOf(ARMS_KEY).forGetter(GolemProperties::arms),
                            partCodec(TTGolemParts::legs).fieldOf(LEGS_KEY).forGetter(GolemProperties::legs), partCodec(TTGolemParts::addons).fieldOf(ADDON_KEY).forGetter(GolemProperties::addon),
                            Codec.INT.xmap(GolemProperties::clampRank, GolemProperties::clampRank).optionalFieldOf(RANK_KEY, 0).forGetter(GolemProperties::rank))
                    .apply(instance, GolemProperties::new)));

    public static final StreamCodec<ByteBuf, GolemProperties> STREAM_CODEC = StreamCodec.composite(partStream(TTGolemParts::materials), GolemProperties::material, partStream(TTGolemParts::heads),
            GolemProperties::head, partStream(TTGolemParts::arms), GolemProperties::arms, partStream(TTGolemParts::legs), GolemProperties::legs, partStream(TTGolemParts::addons),
            GolemProperties::addon, ByteBufCodecs.VAR_INT.map(GolemProperties::clampRank, GolemProperties::clampRank), GolemProperties::rank, GolemProperties::new);

    private final GolemMaterial material;
    private final GolemHead head;
    private final GolemArm arms;
    private final GolemLeg legs;
    private final GolemAddon addon;
    private final int rank;
    private @Nullable Set<GolemTrait> traits;

    public GolemProperties(GolemMaterial material, GolemHead head, GolemArm arms, GolemLeg legs, GolemAddon addon, int rank) {
        this.material = material;
        this.head = head;
        this.arms = arms;
        this.legs = legs;
        this.addon = addon;
        this.rank = rank;
    }

    public static GolemProperties createDefault() {
        return new GolemProperties(TTGolemParts.WOOD.get(), TTGolemParts.HEAD_BASIC.get(), TTGolemParts.ARMS_BASIC.get(), TTGolemParts.LEGS_WALKER.get(), TTGolemParts.ADDON_NONE.get(), 0);
    }

    public static GolemProperties of(IGolemProperties properties) {
        if (properties instanceof GolemProperties direct) {
            return direct;
        }
        return new GolemProperties(properties.material(), properties.head(), properties.arms(), properties.legs(), properties.addon(), properties.rank());
    }

    public boolean isKnownBy(IPlayerKnowledge knowledge) {
        List<List<Identifier>> requirements = List.of(material.research(), head.research(), arms.research(), legs.research(), addon.research());
        for (List<Identifier> research : requirements) {
            for (Identifier id : research) {
                if (!knowledge.isResearchComplete(id)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<GolemTrait> traits() {
        Set<GolemTrait> resolved = traits;
        if (resolved == null) {
            resolved = resolveTraits();
            traits = resolved;
        }
        return resolved;
    }

    @Override
    public List<ItemStack> components() {
        List<ItemStack> bill = new ArrayList<>();
        ItemStack base = material.base();
        base.setCount(base.getCount() * MATERIAL_BASE_MULTIPLIER);
        mergeInto(bill, base);
        mergeInto(bill, material.mechanism());
        for (GolemPart part : List.of(arms, legs, head, addon)) {
            for (GolemComponent component : part.components()) {
                mergeInto(bill, component.resolve(material));
            }
        }
        return bill;
    }

    @Override
    public GolemMaterial material() {
        return material;
    }

    @Override
    public GolemHead head() {
        return head;
    }

    @Override
    public GolemArm arms() {
        return arms;
    }

    @Override
    public GolemLeg legs() {
        return legs;
    }

    @Override
    public GolemAddon addon() {
        return addon;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public GolemProperties withMaterial(GolemMaterial material) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withHead(GolemHead head) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withArms(GolemArm arms) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withLegs(GolemLeg legs) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withAddon(GolemAddon addon) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withRank(int rank) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof GolemProperties that && material == that.material && head == that.head && arms == that.arms && legs == that.legs && addon == that.addon && rank == that.rank;
    }

    @Override
    public int hashCode() {
        int hash = System.identityHashCode(material);
        hash = 31 * hash + System.identityHashCode(head);
        hash = 31 * hash + System.identityHashCode(arms);
        hash = 31 * hash + System.identityHashCode(legs);
        hash = 31 * hash + System.identityHashCode(addon);
        return 31 * hash + rank;
    }

    private Set<GolemTrait> resolveTraits() {
        Map<GolemTrait, Holder<GolemTrait>> active = new LinkedHashMap<>();
        List<List<Holder<GolemTrait>>> sources = List.of(material.traits(), head.traits(), arms.traits(), legs.traits(), addon.traits());
        for (List<Holder<GolemTrait>> source : sources) {
            for (Holder<GolemTrait> holder : source) {
                GolemTrait trait = holder.value();
                Holder<GolemTrait> clash = findOpposite(active, trait.opposite());
                if (clash != null) {
                    active.remove(clash.value());
                } else {
                    active.putIfAbsent(trait, holder);
                }
            }
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(active.keySet()));
    }

    private static @Nullable Holder<GolemTrait> findOpposite(Map<GolemTrait, Holder<GolemTrait>> active, @Nullable ResourceKey<GolemTrait> opposite) {
        if (opposite == null) {
            return null;
        }
        for (Holder<GolemTrait> candidate : active.values()) {
            if (candidate.is(opposite)) {
                return candidate;
            }
        }
        return null;
    }

    private static void mergeInto(List<ItemStack> bill, ItemStack stack) {
        for (ItemStack line : bill) {
            if (ItemStack.isSameItemSameComponents(line, stack)) {
                line.grow(stack.getCount());
                return;
            }
        }
        bill.add(stack);
    }

    private static int clampRank(int rank) {
        return Mth.clamp(rank, 0, EntityThaumaturgeGolem.MAX_RANK);
    }

    private static <T> Codec<T> partCodec(Supplier<Registry<T>> registry) {
        return Codec.lazyInitialized(() -> registry.get().byNameCodec());
    }

    private static <T> StreamCodec<ByteBuf, T> partStream(Supplier<Registry<T>> registry) {
        return StreamCodec.of((buf, part) -> Identifier.STREAM_CODEC.encode(buf, keyOf(registry.get(), part)), buf -> lookup(registry.get(), Identifier.STREAM_CODEC.decode(buf)));
    }

    private static <T> Identifier keyOf(Registry<T> registry, T part) {
        Identifier key = registry.getKey(part);
        if (key == null) {
            throw new EncoderException("Unregistered golem part " + part);
        }
        return key;
    }

    private static <T> T lookup(Registry<T> registry, Identifier id) {
        return registry.getOptional(id).orElseThrow(() -> new DecoderException("Unknown golem part " + id));
    }
}

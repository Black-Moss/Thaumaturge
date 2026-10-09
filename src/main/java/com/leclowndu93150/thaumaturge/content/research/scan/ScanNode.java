package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.research.scan.IScannable;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ScanNode implements IScannable {
    private static final int MIN_POINTS = 4;
    private static final int POINT_DIVISOR = 10;
    private static final int STRONG_FLOOR = 4;
    private static final int WEAK_FLOOR = 2;
    private static final int NO_FLOOR = 0;
    private static final int NO_BONUS = 0;
    private static final int TYPE_BONUS = 2;
    private static final String NODE_KEY_PREFIX = "node/";
    private static final String ERROR_KEY = "message.thaumaturge.research.discovery_error";
    private static final List<ResourceKey<IAspect>> CANONICAL_ORDER = List.of(TTAspects.AER, TTAspects.TERRA, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.ORDO, TTAspects.PERDITIO, TTAspects.DESIDERIUM,
            TTAspects.VITIUM, TTAspects.VICTUS, TTAspects.MORTUUS, TTAspects.TENEBRAE);
    private static final Map<NodeType, List<TypeRule>> TYPE_RULES = typeRules();

    public ScanNode() {}

    private record TypeRule(ResourceKey<IAspect> aspect, int floor, int bonus) {
        void applyTo(Map<ResourceKey<IAspect>, Integer> points) {
            int current = points.getOrDefault(aspect, 0);
            points.put(aspect, Math.max(current, floor) + bonus);
        }
    }

    private static Map<NodeType, List<TypeRule>> typeRules() {
        Map<NodeType, List<TypeRule>> rules = new EnumMap<>(NodeType.class);
        rules.put(NodeType.UNSTABLE, List.of(new TypeRule(TTAspects.PERDITIO, STRONG_FLOOR, NO_BONUS)));
        rules.put(NodeType.HUNGRY, List.of(new TypeRule(TTAspects.DESIDERIUM, STRONG_FLOOR, NO_BONUS)));
        rules.put(NodeType.TAINTED, List.of(new TypeRule(TTAspects.VITIUM, STRONG_FLOOR, NO_BONUS)));
        rules.put(NodeType.PURE, List.of(new TypeRule(TTAspects.VICTUS, WEAK_FLOOR, NO_BONUS), new TypeRule(TTAspects.ORDO, NO_FLOOR, TYPE_BONUS)));
        rules.put(NodeType.DARK, List.of(new TypeRule(TTAspects.MORTUUS, WEAK_FLOOR, NO_BONUS), new TypeRule(TTAspects.TENEBRAE, NO_FLOOR, TYPE_BONUS)));
        return rules;
    }

    @Override
    public boolean matches(Player player, ScanTarget target) {
        return nodeOf(player, target) != null;
    }

    @Override
    public @Nullable Component refusal(Player player, ScanTarget target) {
        return Optional.ofNullable(nodeOf(player, target)).map(node -> firstUndiscoveredComponent(player, scanList(player.level(), node)))
                .map(missing -> Component.translatable(ERROR_KEY, AspectComponents.help(missing))).orElse(null);
    }

    private static @Nullable Holder<IAspect> firstUndiscoveredComponent(Player player, AspectList list) {
        for (AspectInstance instance : list.entries()) {
            for (Holder<IAspect> component : instance.aspect().value().components()) {
                if (!AspectPools.isDiscovered(player, component)) {
                    return component;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable Identifier research(Player player, ScanTarget target) {
        if (nodeOf(player, target) == null || !(target instanceof ScannedBlock(BlockPos pos))) {
            return null;
        }
        return researchKey(player.level(), pos);
    }

    public static Identifier researchKey(Level level, BlockPos pos) {
        String dimension = level.dimension().identifier().getPath();
        return TTIds.rl(NODE_KEY_PREFIX + dimension + "/" + pos.asLong());
    }

    @Override
    public void onScanned(Player player, ScanTarget target) {
        BlockEntityNode node = nodeOf(player, target);
        if (node == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AspectPools.grantAll(serverPlayer, scanList(player.level(), node));
    }

    private static @Nullable BlockEntityNode nodeOf(Player player, ScanTarget target) {
        if (!(target instanceof ScannedBlock(BlockPos pos))) {
            return null;
        }
        return player.level().getBlockEntity(pos) instanceof BlockEntityNode node ? node : null;
    }

    private static Map<ResourceKey<IAspect>, Integer> basePoints(BlockEntityNode node) {
        Map<ResourceKey<IAspect>, Integer> points = new HashMap<>();
        node.getAspects().entries().forEach(instance -> {
            ResourceKey<IAspect> key = instance.aspect().getKey();
            if (key != null) {
                points.put(key, Math.max(MIN_POINTS, instance.amount() / POINT_DIVISOR));
            }
        });
        return points;
    }

    private static Stream<ResourceKey<IAspect>> orderedKeys(HolderLookup.RegistryLookup<IAspect> lookup) {
        return Stream.concat(CANONICAL_ORDER.stream(), lookup.listElements().map(Holder.Reference::key)).distinct();
    }

    private static AspectList scanList(Level level, BlockEntityNode node) {
        Map<ResourceKey<IAspect>, Integer> points = basePoints(node);
        TYPE_RULES.getOrDefault(node.kind(), List.of()).forEach(rule -> rule.applyTo(points));
        HolderLookup.RegistryLookup<IAspect> lookup = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        List<ResourceKey<IAspect>> keys = orderedKeys(lookup).filter(key -> points.getOrDefault(key, 0) > 0).toList();
        AspectList result = AspectList.EMPTY;
        for (ResourceKey<IAspect> key : keys) {
            Optional<Holder.Reference<IAspect>> holder = lookup.get(key);
            if (holder.isPresent()) {
                result = result.add(holder.get(), points.get(key));
            }
        }
        return result;
    }
}

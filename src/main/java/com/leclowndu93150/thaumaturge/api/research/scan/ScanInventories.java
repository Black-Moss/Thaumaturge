package com.leclowndu93150.thaumaturge.api.research.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

final class ScanInventories {
    private ScanInventories() {}

    static boolean hasInventory(Player player, ScanTarget target) {
        return !handlers(player, target).isEmpty() || container(player, target) != null;
    }

    static List<ItemStack> contents(Player player, ScanTarget target) {
        Map<ItemResource, ItemStack> stacks = new LinkedHashMap<>();
        List<ResourceHandler<ItemResource>> handlers = handlers(player, target);
        for (ResourceHandler<ItemResource> handler : handlers) {
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                int amount = handler.getAmountAsInt(slot);
                if (!resource.isEmpty() && amount > 0) {
                    stacks.putIfAbsent(resource, resource.toStack(amount));
                }
            }
        }
        if (handlers.isEmpty() && container(player, target) instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    stacks.putIfAbsent(ItemResource.of(stack), stack.copy());
                }
            }
        }
        return List.copyOf(stacks.values());
    }

    private static List<ResourceHandler<ItemResource>> handlers(Player player, ScanTarget target) {
        Set<ResourceHandler<ItemResource>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
        addHandler(player, target, null, seen, handlers);
        for (Direction side : Direction.values()) {
            addHandler(player, target, side, seen, handlers);
        }
        return handlers;
    }

    private static void addHandler(Player player, ScanTarget target, @Nullable Direction side, Set<ResourceHandler<ItemResource>> seen, List<ResourceHandler<ItemResource>> handlers) {
        ResourceHandler<ItemResource> handler = switch (target) {
            case ScannedBlock(var pos) -> player.level().hasChunkAt(pos) ? player.level().getCapability(Capabilities.Item.BLOCK, pos, side) : null;
            case ScannedEntity(var entity) -> side == null ? entity.getCapability(Capabilities.Item.ENTITY) : null;
            default -> null;
        };
        if (handler != null && seen.add(handler)) {
            handlers.add(handler);
        }
    }

    private static @Nullable Container container(Player player, ScanTarget target) {
        Object candidate = switch (target) {
            case ScannedBlock(var pos) -> player.level().hasChunkAt(pos) ? player.level().getBlockEntity(pos) : null;
            case ScannedEntity(var entity) -> entity;
            default -> null;
        };
        return candidate instanceof Container container ? container : null;
    }
}

package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class CraftReferenceHolder {
    private static volatile Set<Item> items = Set.of();

    private CraftReferenceHolder() {}

    public static void resetSession() {
        items = Set.of();
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null)
            return;
        rebuild(event.getPlayerList().getServer().registryAccess());
    }

    public static void rebuild(RegistryAccess access) {
        publish(access);
    }

    public static boolean isReference(RegistryAccess access, Item item) {
        Set<Item> cached = items;
        return (cached.isEmpty() ? publish(access) : cached).contains(item);
    }

    private static Set<Item> publish(RegistryAccess access) {
        Set<Item> collected = collectCraftItems(access);
        items = collected;
        return collected;
    }

    private static Set<Item> collectCraftItems(RegistryAccess access) {
        return access.lookup(IResearchEntry.REGISTRY_KEY).map(lookup -> lookup.listElements().flatMap(entry -> entry.value().stages().stream()).flatMap(stage -> stage.craft().stream())
                .flatMap(requirement -> requirement.items().stream()).map(Holder::value).collect(Collectors.toUnmodifiableSet())).orElse(Set.of());
    }
}

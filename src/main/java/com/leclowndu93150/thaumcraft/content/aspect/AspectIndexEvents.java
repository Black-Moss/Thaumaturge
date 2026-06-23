package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.network.ClientboundAspectIndexPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class AspectIndexEvents {
    private static volatile boolean datamapsReady = false;
    private static volatile MinecraftServer pendingServer = null;

    private AspectIndexEvents() {}

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }
        if (event.getCause() != DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
            return;
        }
        datamapsReady = true;
        MinecraftServer server = pendingServer;
        if (server != null) {
            buildAndBroadcast(server);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (datamapsReady) {
            buildAndBroadcast(server);
        } else {
            pendingServer = server;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ClientboundAspectIndexPayload(AspectIndexHolder.getConcrete()));
        }
    }

    private static void buildAndBroadcast(MinecraftServer server) {
        AspectIndex index = AspectIndexBuilder.build(server.getRecipeManager(), server.registryAccess());
        AspectIndexHolder.set(index);
        if (!server.getPlayerList().getPlayers().isEmpty()) {
            PacketDistributor.sendToAllPlayers(new ClientboundAspectIndexPayload(index));
        }
        pendingServer = null;
    }
}

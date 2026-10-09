package com.leclowndu93150.thaumaturge.client.toast;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ResearchToastEvents {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final String TITLE_KEY = "gui.thaumaturge.research.complete";

    private static int ticksSinceScan;

    private ResearchToastEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }
        ticksSinceScan++;
        if (ticksSinceScan < SCAN_INTERVAL_TICKS) {
            return;
        }
        ticksSinceScan = 0;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        List<Identifier> flagged = new ArrayList<>();
        for (Identifier research : knowledge.researchList()) {
            if (knowledge.hasResearchFlag(research, ResearchFlag.POPUP)) {
                flagged.add(research);
            }
        }
        for (Identifier research : flagged) {
            raiseToast(minecraft, player, research);
            knowledge.clearResearchFlag(research, ResearchFlag.POPUP);
        }
        for (Identifier research : flagged) {
            ClientPacketDistributor.sendToServer(new ServerboundClearResearchFlagsPayload(research, List.of(ResearchFlag.POPUP)));
        }
    }

    private static void raiseToast(Minecraft minecraft, LocalPlayer player, Identifier research) {
        ResourceKey<IResearchEntry> key = ResourceKey.create(IResearchEntry.REGISTRY_KEY, research);
        Holder<IResearchEntry> holder = player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).flatMap(registry -> registry.get(key)).orElse(null);
        if (holder == null) {
            return;
        }
        IResearchEntry entry = holder.value();
        Object icon = EntryIconRenderer.resolveIcon(entry, player.tickCount);
        minecraft.getToastManager().addToast(new ResearchToast(research, Component.translatable(TITLE_KEY), Component.translatable(entry.nameKey()), icon));
    }
}

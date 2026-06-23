package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.network.AspectIndexClientHandler;
import com.leclowndu93150.thaumcraft.client.network.AuraSnapshotClientHandler;
import com.leclowndu93150.thaumcraft.client.network.BeamClientHandler;
import com.leclowndu93150.thaumcraft.client.network.BoreVoidStreamClientHandler;
import com.leclowndu93150.thaumcraft.client.network.EssentiaStreamClientHandler;
import com.leclowndu93150.thaumcraft.client.network.OpenThaumonomiconHandler;
import com.leclowndu93150.thaumcraft.client.network.RecipeDisplayClientHandler;
import com.leclowndu93150.thaumcraft.client.network.SpawnParticleClientHandler;
import com.leclowndu93150.thaumcraft.client.network.TubeEventClientHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCPayloads {
    private static final String VERSION = "1";

    private TCPayloads() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                ClientboundAspectIndexPayload.TYPE,
                ClientboundAspectIndexPayload.STREAM_CODEC,
                AspectIndexClientHandler::handle
        );
        registrar.playToClient(
                ClientboundOpenThaumonomiconPayload.TYPE,
                ClientboundOpenThaumonomiconPayload.STREAM_CODEC,
                OpenThaumonomiconHandler::handle
        );
        registrar.playToServer(
                ServerboundAdvanceStagePayload.TYPE,
                ServerboundAdvanceStagePayload.STREAM_CODEC,
                ServerboundAdvanceStageHandler::handle
        );
        registrar.playToServer(
                ServerboundPlayCardPayload.TYPE,
                ServerboundPlayCardPayload.STREAM_CODEC,
                ServerboundTheorycraftHandlers::handlePlayCard
        );
        registrar.playToServer(
                ServerboundEndSessionPayload.TYPE,
                ServerboundEndSessionPayload.STREAM_CODEC,
                ServerboundTheorycraftHandlers::handleEndSession
        );
        registrar.playToServer(
                ServerboundTableActionPayload.TYPE,
                ServerboundTableActionPayload.STREAM_CODEC,
                ServerboundTableActionHandler::handle
        );
        registrar.playToServer(
                ServerboundStartTheoryPayload.TYPE,
                ServerboundStartTheoryPayload.STREAM_CODEC,
                ServerboundTableActionHandler::handleStart
        );
        registrar.playToServer(
                ServerboundCardAnimationCompletePayload.TYPE,
                ServerboundCardAnimationCompletePayload.STREAM_CODEC,
                ServerboundTheorycraftHandlers::handleCardAnimationComplete
        );
        registrar.playToServer(
                ServerboundDrawCardsPayload.TYPE,
                ServerboundDrawCardsPayload.STREAM_CODEC,
                ServerboundTheorycraftHandlers::handleDrawCards
        );
        registrar.playToClient(
                ClientboundRecipeDisplayPayload.TYPE,
                ClientboundRecipeDisplayPayload.STREAM_CODEC,
                RecipeDisplayClientHandler::handle
        );
        registrar.playToServer(
                ServerboundRequestRecipeDisplayPayload.TYPE,
                ServerboundRequestRecipeDisplayPayload.STREAM_CODEC,
                ServerboundRecipeDisplayHandler::handle
        );
        registrar.playToServer(
                ServerboundClearResearchFlagsPayload.TYPE,
                ServerboundClearResearchFlagsPayload.STREAM_CODEC,
                ServerboundClearResearchFlagsHandler::handle
        );
        registrar.playToServer(
                ServerboundUnlockResearchPayload.TYPE,
                ServerboundUnlockResearchPayload.STREAM_CODEC,
                ServerboundUnlockResearchHandler::handle
        );
        registrar.playToClient(
                ClientboundSpawnParticlePayload.TYPE,
                ClientboundSpawnParticlePayload.STREAM_CODEC,
                SpawnParticleClientHandler::handle
        );
        registrar.playToClient(
                ClientboundTubeVentPayload.TYPE,
                ClientboundTubeVentPayload.STREAM_CODEC,
                TubeEventClientHandler::handleVent
        );
        registrar.playToClient(
                ClientboundTubeCreakPayload.TYPE,
                ClientboundTubeCreakPayload.STREAM_CODEC,
                TubeEventClientHandler::handleCreak
        );
        registrar.playToServer(
                ServerboundRequestAuraChunkPayload.TYPE,
                ServerboundRequestAuraChunkPayload.STREAM_CODEC,
                ServerboundRequestAuraChunkHandler::handle
        );
        registrar.playToClient(
                ClientboundAuraSnapshotPayload.TYPE,
                ClientboundAuraSnapshotPayload.STREAM_CODEC,
                AuraSnapshotClientHandler::handle
        );
        registrar.playToClient(
                ClientboundEssentiaStreamPayload.TYPE,
                ClientboundEssentiaStreamPayload.STREAM_CODEC,
                EssentiaStreamClientHandler::handle
        );
        registrar.playToClient(
                ClientboundArcPayload.TYPE,
                ClientboundArcPayload.STREAM_CODEC,
                BeamClientHandler::handleArc
        );
        registrar.playToClient(
                ClientboundBoltPayload.TYPE,
                ClientboundBoltPayload.STREAM_CODEC,
                BeamClientHandler::handleBolt
        );
        registrar.playToClient(
                ClientboundBeamPayload.TYPE,
                ClientboundBeamPayload.STREAM_CODEC,
                BeamClientHandler::handleBeam
        );
        registrar.playToClient(
                ClientboundBoreStreamPayload.TYPE,
                ClientboundBoreStreamPayload.STREAM_CODEC,
                BoreVoidStreamClientHandler::handleBore
        );
        registrar.playToClient(
                ClientboundVoidStreamPayload.TYPE,
                ClientboundVoidStreamPayload.STREAM_CODEC,
                BoreVoidStreamClientHandler::handleVoid
        );
    }
}

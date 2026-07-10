package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.client.screen.ThaumatoriumClientHandler;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.network.AspectIndexClientHandler;
import com.leclowndu93150.thaumcraft.client.network.AuraSnapshotClientHandler;
import com.leclowndu93150.thaumcraft.client.network.KnowledgeGainClientHandler;
import com.leclowndu93150.thaumcraft.client.network.WispZapClientHandler;
import com.leclowndu93150.thaumcraft.client.fx.network.FXStreamClientHandler;
import com.leclowndu93150.thaumcraft.client.fx.network.FocusImpactClientHandler;
import com.leclowndu93150.thaumcraft.client.fx.network.InfusionSourceClientHandler;
import com.leclowndu93150.thaumcraft.client.fx.network.SpawnParticleClientHandler;
import com.leclowndu93150.thaumcraft.client.golem.GolemPressClientHandler;
import com.leclowndu93150.thaumcraft.client.golem.SealClientHandler;
import com.leclowndu93150.thaumcraft.client.network.OpenThaumonomiconHandler;
import com.leclowndu93150.thaumcraft.client.network.RecipeDisplayClientHandler;
import com.leclowndu93150.thaumcraft.client.network.TubeEventClientHandler;
import com.leclowndu93150.thaumcraft.client.warp.WarpFXClientHandler;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFXStreamPayload;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFocusImpactPayload;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundInfusionSourcePayload;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundSpawnParticlePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCPayloads {
    private static final String VERSION = "2";

    private TCPayloads() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                ClientboundAspectIndexPayload.TYPE,
                ClientboundAspectIndexPayload.STREAM_CODEC,
                (payload, context) -> AspectIndexClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundOpenThaumonomiconPayload.TYPE,
                ClientboundOpenThaumonomiconPayload.STREAM_CODEC,
                (payload, context) -> OpenThaumonomiconHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundSealPayload.TYPE,
                ClientboundSealPayload.STREAM_CODEC,
                (payload, context) -> SealClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundGolemPressStuffPayload.TYPE,
                ClientboundGolemPressStuffPayload.STREAM_CODEC,
                (payload, context) -> GolemPressClientHandler.handle(payload, context)
        );
                registrar.playToClient(
                ClientboundThaumatoriumRecipesPayload.TYPE,
                ClientboundThaumatoriumRecipesPayload.STREAM_CODEC,
                ThaumatoriumClientHandler::handle);
        registrar.playToServer(
                ServerboundThaumatoriumTogglePayload.TYPE,
                ServerboundThaumatoriumTogglePayload.STREAM_CODEC,
                ServerboundThaumatoriumToggleHandler::handle);
        registrar.playToServer(
                ServerboundGolemPressPayload.TYPE,
                ServerboundGolemPressPayload.STREAM_CODEC,
                ServerboundGolemPressHandler::handle
        );
        registrar.playToServer(
                ServerboundCloudJumpPayload.TYPE,
                ServerboundCloudJumpPayload.STREAM_CODEC,
                ServerboundCloudJumpPayload::handle
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
                (payload, context) -> RecipeDisplayClientHandler.handle(payload, context)
        );
        registrar.playToServer(
                ServerboundRequestItemRecipePayload.TYPE,
                ServerboundRequestItemRecipePayload.STREAM_CODEC,
                ServerboundRecipeDisplayHandler::handleItemRequest
        );
        registrar.playToClient(
                ClientboundItemRecipePayload.TYPE,
                ClientboundItemRecipePayload.STREAM_CODEC,
                (payload, context) -> RecipeDisplayClientHandler.handleItemRecipe(payload, context)
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
                (payload, context) -> SpawnParticleClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundTubeVentPayload.TYPE,
                ClientboundTubeVentPayload.STREAM_CODEC,
                (payload, context) -> TubeEventClientHandler.handleVent(payload, context)
        );
        registrar.playToClient(
                ClientboundTubeCreakPayload.TYPE,
                ClientboundTubeCreakPayload.STREAM_CODEC,
                (payload, context) -> TubeEventClientHandler.handleCreak(payload, context)
        );
        registrar.playToServer(
                ServerboundRequestAuraChunkPayload.TYPE,
                ServerboundRequestAuraChunkPayload.STREAM_CODEC,
                ServerboundRequestAuraChunkHandler::handle
        );
        registrar.playToClient(
                ClientboundWarpFXPayload.TYPE,
                ClientboundWarpFXPayload.STREAM_CODEC,
                (payload, context) -> WarpFXClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundAuraSnapshotPayload.TYPE,
                ClientboundAuraSnapshotPayload.STREAM_CODEC,
                (payload, context) -> AuraSnapshotClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundFXStreamPayload.TYPE,
                ClientboundFXStreamPayload.STREAM_CODEC,
                (payload, context) -> FXStreamClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundInfusionSourcePayload.TYPE,
                ClientboundInfusionSourcePayload.STREAM_CODEC,
                (payload, context) -> InfusionSourceClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundKnowledgeGainPayload.TYPE,
                ClientboundKnowledgeGainPayload.STREAM_CODEC,
                (payload, context) -> KnowledgeGainClientHandler.handle(payload, context)
        );
        registrar.playToClient(
                ClientboundWispZapPayload.TYPE,
                ClientboundWispZapPayload.STREAM_CODEC,
                (payload, context) -> WispZapClientHandler.handle(payload, context)
        );
        registrar.playToServer(
                ServerboundFocusDataPayload.TYPE,
                ServerboundFocusDataPayload.STREAM_CODEC,
                ServerboundFocusDataPayload::handle
        );
        registrar.playToClient(
                ClientboundFocusImpactPayload.TYPE,
                ClientboundFocusImpactPayload.STREAM_CODEC,
                (payload, context) -> FocusImpactClientHandler.handle(payload, context)
        );
        registrar.playToServer(
                ServerboundFocusChangePayload.TYPE,
                ServerboundFocusChangePayload.STREAM_CODEC,
                ServerboundFocusChangePayload::handle
        );
        registrar.playToServer(
                ServerboundCasterKeyPayload.TYPE,
                ServerboundCasterKeyPayload.STREAM_CODEC,
                ServerboundCasterKeyPayload::handle
        );
    }
}

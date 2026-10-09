package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public final class WarpLedger {
    private static final Identifier FIRST_STEPS_RESEARCH = TTIds.rl("first_steps");
    private static final Identifier WARP_RESEARCH = TTIds.rl("warp");
    private static final String GAIN_PREFIX = "warp.thaumaturge.gain.";
    private static final String LOSS_PREFIX = "warp.thaumaturge.lose.";
    private static final String RESEARCH_NOTICE = "research.thaumaturge.warp.warn";
    private static final float WHISPER_VOLUME = 0.5F;
    private static final float WHISPER_PITCH = 1.0F;

    private WarpLedger() {}

    public static PlayerWarpState state(ServerPlayer player) {
        return player.getData(TTAttachments.WARP);
    }

    public static void change(ServerPlayer player, int amount, WarpType type) {
        if (amount == 0) {
            return;
        }
        PlayerWarpState state = state(player);
        if (amount < 0) {
            if (state.get(type) <= 0) {
                return;
            }
            state.add(type, amount);
            WarpNotices.send(player, LOSS_PREFIX + poolName(type));
            player.syncData(TTAttachments.WARP);
            return;
        }
        state.add(type, amount);
        state.setCounter(state.total());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.WHISPERS.get(), SoundSource.AMBIENT, WHISPER_VOLUME, WHISPER_PITCH);
        WarpNotices.send(player, unlocksWarpResearch(player, type) ? RESEARCH_NOTICE : GAIN_PREFIX + poolName(type));
        player.syncData(TTAttachments.WARP);
    }

    private static boolean unlocksWarpResearch(ServerPlayer player, WarpType type) {
        if (type == WarpType.TEMPORARY) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (!knowledge.isResearchComplete(FIRST_STEPS_RESEARCH) || knowledge.isResearchComplete(WARP_RESEARCH)) {
            return false;
        }
        ResearchManager.complete(player, WARP_RESEARCH);
        return true;
    }

    private static String poolName(WarpType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }
}

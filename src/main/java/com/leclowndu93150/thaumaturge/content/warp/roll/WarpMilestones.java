package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class WarpMilestones {
    private static final Identifier BATH_SALTS_RESEARCH = TTIds.rl("bath_salts");
    private static final Identifier BATH_SALTS_HINT = TTIds.rl("bathsalts");
    private static final Identifier MINOR_ELDRITCH = TTIds.rl("eldritchminor");
    private static final Identifier MAJOR_ELDRITCH = TTIds.rl("eldritchmajor");
    private static final int HINT_THRESHOLD = 10;
    private static final int MINOR_THRESHOLD = 25;
    private static final int MAJOR_THRESHOLD = 50;
    private static final String HINT_NOTICE = "warp.thaumaturge.text.8";

    private WarpMilestones() {}

    public static void reach(ServerPlayer player, int actualWarp) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (actualWarp > HINT_THRESHOLD && !knowledge.isResearchKnown(BATH_SALTS_RESEARCH) && !knowledge.isResearchKnown(BATH_SALTS_HINT)) {
            WarpNotices.send(player, HINT_NOTICE);
            ResearchManager.complete(player, BATH_SALTS_HINT);
        }
        if (actualWarp > MINOR_THRESHOLD && !knowledge.isResearchKnown(MINOR_ELDRITCH)) {
            ResearchManager.complete(player, MINOR_ELDRITCH);
        }
        if (actualWarp > MAJOR_THRESHOLD && !knowledge.isResearchKnown(MAJOR_ELDRITCH)) {
            ResearchManager.complete(player, MAJOR_ELDRITCH);
        }
    }
}

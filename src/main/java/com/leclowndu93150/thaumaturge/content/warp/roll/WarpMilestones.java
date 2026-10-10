package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class WarpMilestones {
    private static final int HEADACHE_THRESHOLD = 10;
    private static final Identifier BATH_SALTS = TTIds.rl("bath_salts");
    private static final Identifier BATH_SALTS_HINT = TTIds.rl("bathsalts");
    private static final String HEADACHE_NOTICE = "warp.thaumaturge.text.8";
    private static final int MINOR_ELDRITCH_THRESHOLD = 25;
    private static final int MAJOR_ELDRITCH_THRESHOLD = 50;
    private static final Identifier MINOR_ELDRITCH = TTIds.rl("eldritchminor");
    private static final Identifier MAJOR_ELDRITCH = TTIds.rl("eldritchmajor");

    private static final List<Threshold> ELDRITCH_THRESHOLDS = List.of(new Threshold(MINOR_ELDRITCH_THRESHOLD, MINOR_ELDRITCH), new Threshold(MAJOR_ELDRITCH_THRESHOLD, MAJOR_ELDRITCH));

    private WarpMilestones() {}

    public static void review(ServerPlayer player, int actualWarp) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (actualWarp > HEADACHE_THRESHOLD && !knowledge.isResearchKnown(BATH_SALTS) && !knowledge.isResearchKnown(BATH_SALTS_HINT)) {
            WarpNotices.send(player, HEADACHE_NOTICE);
            ResearchManager.complete(player, BATH_SALTS_HINT);
        }
        for (Threshold threshold : ELDRITCH_THRESHOLDS) {
            if (actualWarp > threshold.above() && !knowledge.isResearchKnown(threshold.research())) {
                ResearchManager.complete(player, threshold.research());
            }
        }
    }

    private record Threshold(int above, Identifier research) {
    }
}

package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class NodeWandTap {
    private static final Identifier RESEARCH_TAPPER_ONE = TTIds.rl("node_tapper_1");
    private static final Identifier RESEARCH_TAPPER_TWO = TTIds.rl("node_tapper_2");
    private static final Identifier RESEARCH_PRESERVE = TTIds.rl("node_preserve");
    private static final int TAP_INTERVAL = 5;
    private static final int BASE_STRENGTH = 1;
    private static final int PRESERVE_MINIMUM_STOCK = 2;

    private NodeWandTap() {}

    static boolean tap(BlockEntityNode node, ServerLevel level, Player player, ItemStack wand, int remainingUse) {
        if (remainingUse % TAP_INTERVAL != 0) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        int strength = BASE_STRENGTH + (knowledge.isResearchComplete(RESEARCH_TAPPER_ONE) ? 1 : 0) + (knowledge.isResearchComplete(RESEARCH_TAPPER_TWO) ? 1 : 0);
        WandParts parts = WandVisHelper.partsOf(wand);
        boolean starter = parts.cap() == TTWandParts.CAP_IRON.get() && parts.rod() == TTWandParts.ROD_WOOD.get();
        boolean preserve = !player.isShiftKeyDown() && !starter && knowledge.isResearchComplete(RESEARCH_PRESERVE);
        int minimumStock = preserve ? PRESERVE_MINIMUM_STOCK : 1;
        int wandMax = WandVisHelper.capacityOf(wand);
        List<Holder<IAspect>> tappable = new ArrayList<>();
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Holder<IAspect> primal = Aspects.resolve(level, key);
            if (primal != null && node.held.amountOf(primal) >= minimumStock && WandVisHelper.storedIn(wand, key) < wandMax) {
                tappable.add(primal);
            }
        }
        if (tappable.isEmpty()) {
            return false;
        }
        Holder<IAspect> chosen = tappable.get(level.getRandom().nextInt(tappable.size()));
        int stock = node.held.amountOf(chosen);
        int offered = Math.min(strength, preserve ? stock - 1 : stock);
        int accepted = offered - WandVisHelper.topUp(wand, chosen.unwrapKey().orElseThrow(), offered, true);
        if (accepted <= 0) {
            return false;
        }
        node.held = node.held.reduce(chosen, accepted);
        node.recordDrain(player.getUUID(), chosen.value().color());
        return true;
    }
}

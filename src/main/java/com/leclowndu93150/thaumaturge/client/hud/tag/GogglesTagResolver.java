package com.leclowndu93150.thaumaturge.client.hud.tag;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class GogglesTagResolver {
    private static final double TOP_LIFT = 0.4;

    private GogglesTagResolver() {}

    public static Optional<GogglesTagTarget> resolve(Level level, LocalPlayer player, HitResult hitResult) {
        if (!GogglesAccess.wearsRevealingGear(player) || holdsScanner(player)) {
            return Optional.empty();
        }
        if (!(hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        BlockPos pos = hit.getBlockPos();
        if (level.getBlockEntity(pos) instanceof BlockEntityNode && !KnowledgeAccess.of(player).isResearchKnown(ScanNode.researchKey(level, pos))) {
            return Optional.empty();
        }
        boolean showAmounts = true;
        AspectList aspects = AspectList.EMPTY;
        IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, pos, hit.getDirection());
        if (container != null) {
            aspects = container.getAspects();
        } else {
            IAspectQuery query = level.getCapability(EssentiaCapabilities.ASPECT_QUERY, pos, hit.getDirection());
            if (query != null) {
                aspects = query.queryAspects();
                showAmounts = false;
            }
        }
        if (aspects.isEmpty()) {
            return Optional.empty();
        }
        boolean topFree = level.getBlockState(pos.above()).isAir();
        Direction face = topFree ? Direction.UP : player.getDirection().getOpposite();
        double originY = topFree ? pos.getY() + TOP_LIFT : pos.getY();
        return Optional.of(new GogglesTagTarget(pos, aspects, showAmounts, face, new Vec3(pos.getX(), originY, pos.getZ())));
    }

    private static boolean holdsScanner(LocalPlayer player) {
        return player.getMainHandItem().is(TTItems.THAUMOMETER) || player.getOffhandItem().is(TTItems.THAUMOMETER);
    }
}

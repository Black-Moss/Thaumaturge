package com.leclowndu93150.thaumcraft.content.aura.node;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.nodes.NodeModifier;
import com.leclowndu93150.thaumcraft.content.wands.WandEconomy;
import com.leclowndu93150.thaumcraft.content.wands.WandVisHelper;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class NodeJarRitual {
    public static final Identifier RESEARCH_NODE_JAR = TCIds.rl("node_jar");

    private static final int JAR_VIS_COST_PER_PRIMAL = 70;
    private static final float MODIFIER_DEGRADE_CHANCE = 0.75F;

    private NodeJarRitual() {}

    public static boolean tryJarNode(ServerLevel level, BlockPos nodePos, Player player) {
        if (!KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_JAR)) {
            return false;
        }
        if (!(level.getBlockEntity(nodePos) instanceof BlockEntityNode node)
                || node instanceof BlockEntityJarNode) {
            return false;
        }
        if (!fitsStructure(level, nodePos)) {
            return false;
        }
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            cost.put(primal, JAR_VIS_COST_PER_PRIMAL * WandEconomy.CENTIVIS_PER_VIS);
        }
        if (!WandVisHelper.consumeSpecificFromHotbar(player, cost, true)) {
            return false;
        }
        RandomSource random = level.getRandom();
        NodeModifier modifier = node.getNodeModifier();
        if (random.nextFloat() < MODIFIER_DEGRADE_CHANCE) {
            if (modifier == null) {
                modifier = NodeModifier.PALE;
            } else if (modifier == NodeModifier.BRIGHT) {
                modifier = null;
            } else if (modifier == NodeModifier.PALE) {
                modifier = NodeModifier.FADING;
            }
        }
        NodeData data = new NodeData(node.getNodeType(), Optional.ofNullable(modifier),
                node.getAspects(), node.getAspectsBase());
        level.removeBlockEntity(nodePos);
        level.setBlock(nodePos, TCBlocks.JAR_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(nodePos) instanceof BlockEntityJarNode jar) {
            jar.applyNodeData(data);
            jar.setChanged();
            level.sendBlockUpdated(nodePos, jar.getBlockState(), jar.getBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(null, nodePos, TCSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean fitsStructure(ServerLevel level, BlockPos nodePos) {
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                if (!level.getBlockState(nodePos.offset(xx, 2, zz)).is(BlockTags.WOODEN_SLABS)) {
                    return false;
                }
                for (int yy = -1; yy <= 1; yy++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    BlockState state = level.getBlockState(nodePos.offset(xx, yy, zz));
                    if (!state.is(Tags.Blocks.GLASS_BLOCKS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

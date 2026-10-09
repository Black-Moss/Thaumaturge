package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public final class NodeJarRitual {
    public static final Identifier RESEARCH_NODE_JAR = TTIds.rl("node_jar");

    private static final String MESSAGE_NO_RESEARCH = "message.thaumaturge.node_jar.no_research";
    private static final String MESSAGE_STRUCTURE = "message.thaumaturge.node_jar.structure";
    private static final String MESSAGE_VIS = "message.thaumaturge.node_jar.vis";

    private static final int VIS_PER_PRIMAL = 70;
    private static final float MODIFIER_DEGRADE_CHANCE = 0.75F;
    private static final int SHELL_BREAK_MARGIN_TICKS = 20;
    private static final int JAR_SETTLE_TICKS = 15;
    private static final int SHELL_RADIUS = 1;
    private static final int GLASS_LAYER_BELOW = -1;
    private static final int ROOF_LAYER = 2;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final List<Vec3i> SHELL_OFFSETS = buildShellOffsets();

    public static boolean tryJarNode(ServerLevel level, BlockPos pos, Player player) {
        Optional<BlockEntityNode> target = jarrableNodeAt(level, pos);
        target.filter(node -> !node.isJarring()).ifPresent(node -> attemptJarring(level, pos, player, node));
        return target.isPresent();
    }

    private static Optional<BlockEntityNode> jarrableNodeAt(ServerLevel level, BlockPos pos) {
        return Optional.ofNullable(level.getBlockEntity(pos)).filter(found -> found instanceof BlockEntityNode && !(found instanceof BlockEntityJarNode)).map(BlockEntityNode.class::cast);
    }

    private static void attemptJarring(ServerLevel level, BlockPos pos, Player player, BlockEntityNode node) {
        JarGate blocker = firstFailedGate(level, pos, player);
        if (blocker == null) {
            playRitualSound(level, pos);
            node.beginJarring(JAR_SETTLE_TICKS + breakShell(level, pos, player));
        } else {
            TTActionBar.sendPurple(player, blocker.messageKey, blocker.messageArgs);
        }
    }

    private static void playRitualSound(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.WAND.get(), SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
    }

    private static @Nullable JarGate firstFailedGate(ServerLevel level, BlockPos pos, Player player) {
        return Arrays.stream(JarGate.values()).filter(gate -> !gate.check.passes(level, pos, player)).findFirst().orElse(null);
    }

    public static void completeJar(ServerLevel level, BlockPos pos, BlockEntityNode node) {
        swapForJar(level, pos, carriedData(level, node));
        Effects.bamf(level, pos).withSound().fancy().send();
    }

    private static NodeData carriedData(ServerLevel level, BlockEntityNode node) {
        boolean degrades = level.getRandom().nextFloat() < MODIFIER_DEGRADE_CHANCE;
        NodeModifier trait = degrades ? degrade(node.trait()) : node.trait();
        return new NodeData(node.kind(), Optional.ofNullable(trait), node.getAspects(), node.capacity());
    }

    private static void swapForJar(ServerLevel level, BlockPos pos, NodeData data) {
        level.setBlock(pos, TTBlocks.JAR_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof BlockEntityJarNode jar) {
            jar.applyNodeData(data);
            jar.setChanged();
        }
        syncBlock(level, pos);
    }

    private static void syncBlock(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        level.sendBlockUpdated(pos, current, current, Block.UPDATE_ALL);
    }

    public static boolean fitsStructure(Level level, BlockPos pos) {
        return structurePositions(pos).stream().allMatch(cell -> matchesShellRole(level.getBlockState(cell), cell.getY() - pos.getY()));
    }

    private static boolean matchesShellRole(BlockState state, int layer) {
        return layer == ROOF_LAYER ? state.is(BlockTags.WOODEN_SLABS) : state.is(Tags.Blocks.GLASS_BLOCKS);
    }

    public static List<BlockPos> structurePositions(BlockPos pos) {
        return SHELL_OFFSETS.stream().map(pos::offset).collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Vec3i> buildShellOffsets() {
        List<Vec3i> offsets = new ArrayList<>();
        int side = 2 * SHELL_RADIUS + 1;
        int layers = ROOF_LAYER - GLASS_LAYER_BELOW + 1;
        for (int flat = 0; flat < layers * side * side; flat++) {
            int dy = GLASS_LAYER_BELOW + flat / (side * side);
            int dx = flat / side % side - SHELL_RADIUS;
            int dz = flat % side - SHELL_RADIUS;
            if (dx != 0 || dy != 0 || dz != 0) {
                offsets.add(new Vec3i(dx, dy, dz));
            }
        }
        return List.copyOf(offsets);
    }

    private static Map<ResourceKey<IAspect>, Integer> primalCost() {
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            cost.put(primal, VIS_PER_PRIMAL * WandEconomy.CENTIVIS_PER_VIS);
        }
        return cost;
    }

    private static int breakShell(ServerLevel level, BlockPos nodePos, Player player) {
        ServerPlayer caster = player instanceof ServerPlayer sp ? sp : null;
        Vec3 source = caster == null ? Vec3.atCenterOf(nodePos) : DustTriggerFx.posToHand(caster, InteractionHand.MAIN_HAND);
        return structurePositions(nodePos).stream().mapToInt(cell -> breakCell(level, cell, source, caster != null)).max().orElse(0);
    }

    private static int breakCell(ServerLevel level, BlockPos cell, Vec3 source, boolean sparkle) {
        if (sparkle) {
            DustTriggerFx.emitBlockSparkles(level, cell, source);
        }
        int delay = SHELL_BREAK_MARGIN_TICKS + (int) (source.distanceTo(Vec3.atCenterOf(cell)) * DustTriggerFx.SPARKLE_TICKS_PER_BLOCK);
        DustTriggerSwapQueue.enqueueClear(level, cell, level.getBlockState(cell), delay);
        return delay;
    }

    private interface GateCheck {
        boolean passes(ServerLevel level, BlockPos pos, Player player);
    }

    private enum JarGate {
        RESEARCH(MESSAGE_NO_RESEARCH, (level, pos, player) -> KnowledgeAccess.of(player).isResearchComplete(RESEARCH_NODE_JAR)), STRUCTURE(MESSAGE_STRUCTURE,
                (level, pos, player) -> fitsStructure(level, pos)), VIS(MESSAGE_VIS, (level, pos, player) -> WandVisHelper.consumeSpecificFromHotbar(player, primalCost(), true), VIS_PER_PRIMAL);

        private final String messageKey;
        private final GateCheck check;
        private final Object[] messageArgs;

        JarGate(String messageKey, GateCheck check, Object... messageArgs) {
            this.messageKey = messageKey;
            this.check = check;
            this.messageArgs = messageArgs;
        }
    }

    private static @Nullable NodeModifier degrade(@Nullable NodeModifier modifier) {
        if (modifier == null) {
            return NodeModifier.PALE;
        }
        return switch (modifier) {
            case BRIGHT -> null;
            case PALE -> NodeModifier.FADING;
            default -> modifier;
        };
    }

    private NodeJarRitual() {}
}

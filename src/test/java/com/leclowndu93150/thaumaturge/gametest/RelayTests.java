package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockVisRelay;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class RelayTests {
    private static final BlockPos NODE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos RELAY_POS = new BlockPos(1, 2, 4);
    private static final BlockPos RELAY_CHAIN_POS = new BlockPos(4, 2, 4);
    private static final BlockPos CONSUMER_POS = new BlockPos(4, 2, 6);
    private static final int IGNIS_AMOUNT = 30;
    private static final int ACCRUE_WAIT_TICKS = 80;

    private RelayTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("relay/all_faces_and_support_removal", 40, helper -> {
            BlockPos support = new BlockPos(2, 2, 3);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.setBlock(support, Blocks.STONE);
            for (Direction facing : Direction.values()) {
                placeOnFace(helper, player, support, facing);
                BlockPos pos = support.relative(facing);
                AABB shape = helper.getBlockState(pos).getShape(helper.getLevel(), helper.absolutePos(pos)).bounds();
                AABB expected = switch (facing) {
                    case UP -> new AABB(5, 0, 5, 11, 11, 11);
                    case DOWN -> new AABB(5, 5, 5, 11, 16, 11);
                    case NORTH -> new AABB(5, 5, 5, 11, 11, 16);
                    case SOUTH -> new AABB(5, 5, 0, 11, 11, 11);
                    case WEST -> new AABB(5, 5, 5, 16, 11, 11);
                    case EAST -> new AABB(0, 5, 5, 11, 11, 11);
                };
                if (!shape.equals(new AABB(expected.minX / 16, expected.minY / 16, expected.minZ / 16, expected.maxX / 16, expected.maxY / 16, expected.maxZ / 16))) {
                    helper.fail("Relay hitbox does not face " + facing + ": " + shape);
                }
            }
            helper.setBlock(support, Blocks.AIR);
            for (Direction facing : Direction.values()) {
                if (!helper.getBlockState(support.relative(facing)).isAir()) {
                    helper.fail("Relay remained attached to removed support on " + facing);
                }
            }
            helper.assertItemEntityCountIs(TTBlocks.VIS_RELAY.asItem(), support, 2.5, 6);
            helper.succeed();
        });

        r.add("relay/partial_block_support", 40, helper -> {
            BlockPos support = new BlockPos(2, 2, 3);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (Block block : new Block[]{Blocks.OAK_FENCE, Blocks.STONE_SLAB, Blocks.IRON_BARS}) {
                helper.setBlock(support, block);
                for (Direction facing : Direction.values()) {
                    placeOnFace(helper, player, support, facing);
                    helper.setBlock(support.relative(facing), Blocks.AIR);
                }
            }
            helper.succeed();
        });

        r.add("relay/requires_support", 40, helper -> {
            BlockPos support = new BlockPos(2, 2, 3);
            for (Block block : new Block[]{Blocks.AIR, Blocks.WATER}) {
                helper.setBlock(support, block);
                for (Direction facing : Direction.values()) {
                    BlockState state = TTBlocks.VIS_RELAY.get().defaultBlockState().setValue(BlockVisRelay.FACING, facing);
                    if (state.canSurvive(helper.getLevel(), helper.absolutePos(support.relative(facing)))) {
                        helper.fail("Relay accepts " + block + " as support on " + facing);
                    }
                }
            }
            helper.succeed();
        });

        r.add("relay/links_and_drains", 300, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay relay = placeRelay(helper, RELAY_POS);
            if (node == null || relay == null) {
                return;
            }
            relay.refreshLink(helper.getLevel());
            if (!relay.isLinked() || relay.depth() != 1) {
                helper.fail("Relay did not link to the energized node (depth " + relay.depth() + ")");
                return;
            }
            if (!helper.absolutePos(NODE_POS).equals(relay.parentPos())) {
                helper.fail("Relay linked to " + relay.parentPos() + " instead of the node");
                return;
            }
            helper.runAfterDelay(ACCRUE_WAIT_TICKS, () -> {
                int before = node.getAspects().totalAmount();
                int drained = VisRelayHelper.drainCentivis(helper.getLevel(), helper.absolutePos(CONSUMER_POS), TTAspects.IGNIS, 100, false);
                if (drained <= 0) {
                    helper.fail("Nothing drained through the relay after " + ACCRUE_WAIT_TICKS + " ticks of accrual");
                    return;
                }
                if (node.getAspects().totalAmount() >= before) {
                    helper.fail("Drain did not consume the source node's stored aspects");
                    return;
                }
                helper.succeed();
            });
        });

        r.add("relay/chains_through_parent_relay", 60, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay first = placeRelay(helper, RELAY_POS);
            BlockEntityVisRelay second = placeRelay(helper, RELAY_CHAIN_POS);
            if (node == null || first == null || second == null) {
                return;
            }
            first.refreshLink(helper.getLevel());
            if (!first.isLinked()) {
                helper.fail("First relay failed to link to the node");
                return;
            }
            helper.getLevel().removeBlock(helper.absolutePos(NODE_POS), false);
            second.refreshLink(helper.getLevel());
            if (second.depth() != 2 || !helper.absolutePos(RELAY_POS).equals(second.parentPos())) {
                helper.fail("Second relay did not chain through the first (depth " + second.depth() + ", parent " + second.parentPos() + ")");
                return;
            }
            helper.succeed();
        });

        r.add("relay/unlinks_when_deenergized", 60, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay relay = placeRelay(helper, RELAY_POS);
            if (node == null || relay == null) {
                return;
            }
            relay.refreshLink(helper.getLevel());
            if (!relay.isLinked()) {
                helper.fail("Relay failed to link before the de-energize check");
                return;
            }
            node.setEnergized(false);
            relay.refreshLink(helper.getLevel());
            if (relay.isLinked()) {
                helper.fail("Relay stayed linked to a de-energized node");
                return;
            }
            helper.succeed();
        });
    }

    private static Holder<IAspect> ignis(GameTestHelper helper) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), TTAspects.IGNIS);
        if (holder == null) {
            throw new IllegalStateException("Ignis missing from the aspect registry");
        }
        return holder;
    }

    private static BlockEntityNode placeEnergizedNode(GameTestHelper helper) {
        AspectList aspects = AspectList.EMPTY.add(ignis(helper), IGNIS_AMOUNT);
        BlockPos pos = helper.absolutePos(NODE_POS);
        if (!NodeGenerator.createNodeAt(helper.getLevel(), pos, NodeType.NORMAL, null, aspects)) {
            helper.fail("NodeGenerator.createNodeAt failed");
            return null;
        }
        if (!(helper.getLevel().getBlockEntity(pos) instanceof BlockEntityNode node)) {
            helper.fail("No node block entity after createNodeAt");
            return null;
        }
        node.setEnergized(true);
        return node;
    }

    private static BlockEntityVisRelay placeRelay(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, TTBlocks.VIS_RELAY.get().defaultBlockState());
        BlockEntityVisRelay relay = helper.getBlockEntity(pos, BlockEntityVisRelay.class);
        if (relay == null) {
            helper.fail("Vis relay block entity missing after placement");
        }
        return relay;
    }

    private static void placeOnFace(GameTestHelper helper, Player player, BlockPos support, Direction facing) {
        BlockPos absoluteSupport = helper.absolutePos(support);
        ItemStack stack = new ItemStack(TTBlocks.VIS_RELAY.asItem());
        Vec3 hit = Vec3.atCenterOf(absoluteSupport).add(facing.getStepX() * 0.5, facing.getStepY() * 0.5, facing.getStepZ() * 0.5);
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(hit, facing, absoluteSupport, false));
        if (!((BlockItem) stack.getItem()).place(context).consumesAction()) {
            helper.fail("Could not place relay on " + facing + " face of " + helper.getBlockState(support));
        }
        BlockPos pos = support.relative(facing);
        BlockState state = helper.getBlockState(pos);
        if (!state.is(TTBlocks.VIS_RELAY) || state.getValue(BlockVisRelay.FACING) != facing || !state.canSurvive(helper.getLevel(), helper.absolutePos(pos))) {
            helper.fail("Relay placement did not retain its supported " + facing + " orientation");
        }
    }
}

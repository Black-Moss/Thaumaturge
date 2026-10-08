package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class HungryNodeWardTests {
    private HungryNodeWardTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("parity/hungry_node_respects_wards", 20, helper -> {
            BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 3));
            BlockPos target = origin.east();
            ServerLevel level = helper.getLevel();
            NodeGenerator.createNodeAt(level, origin, NodeType.HUNGRY, null, AspectList.EMPTY);
            BlockEntityNode node = (BlockEntityNode) level.getBlockEntity(origin);
            level.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
            try {
                Method choose = BlockEntityNode.class.getDeclaredMethod("hungryTarget", Level.class, BlockPos.class, RandomSource.class);
                Method eat = BlockEntityNode.class.getDeclaredMethod("eatBlock", ServerLevel.class, BlockPos.class, RandomSource.class);
                choose.setAccessible(true);
                eat.setAccessible(true);
                long seed = 0;
                while (seed < 4096 && !target.equals(choose.invoke(node, level, origin, RandomSource.create(seed))))
                    seed++;
                helper.assertTrue(seed < 4096, "Unwarded stone must be a valid hungry-node target");
                UUID owner = UUID.randomUUID();
                helper.assertTrue(WardHandler.ward(level, target, owner), "Test stone must accept a ward");
                helper.assertTrue(choose.invoke(node, level, origin, RandomSource.create(seed)) == null, "Warded stone must be rejected as a target");
                eat.invoke(node, level, origin, RandomSource.create(seed));
                helper.assertTrue(level.getBlockState(target).is(Blocks.STONE), "Hungry node destroyed a warded block");
                WardHandler.unward(level, target, owner);
                eat.invoke(node, level, origin, RandomSource.create(seed));
                helper.assertTrue(level.getBlockState(target).isAir(), "Removing the ward must allow consumption again");
                helper.succeed();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}

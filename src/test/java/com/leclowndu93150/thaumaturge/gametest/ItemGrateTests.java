package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.content.device.grate.BlockEntityItemGrate;
import com.leclowndu93150.thaumaturge.content.device.grate.BlockItemGrate;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class ItemGrateTests {
    private static final BlockPos POS = new BlockPos(2, 3, 3);

    private ItemGrateTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("parity/grate_transaction_and_drop", 20, helper -> {
            BlockEntityItemGrate grate = place(helper, true);
            ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, grate.getBlockPos(), Direction.UP);
            helper.assertTrue(handler != null, "Grate must expose an item handler");
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(handler.insert(0, stone(), 64, transaction) == 64, "Open grate must accept a stack");
                helper.assertTrue(drops(helper).isEmpty(), "Insertion must not eject before commit");
            }
            helper.assertTrue(handler.getAmountAsInt(0) == 0 && drops(helper).isEmpty(), "Rollback must not create items");
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(handler.insert(0, stone(), 64, transaction) == 64, "Committed insertion must accept the stack");
                transaction.commit();
            }
            List<ItemEntity> drops = drops(helper);
            helper.assertTrue(drops.size() == 1, "Grate must eject one entity for the whole stack");
            ItemEntity item = drops.getFirst();
            BlockPos pos = grate.getBlockPos();
            helper.assertTrue(item.getItem().getCount() == 64 && handler.getAmountAsInt(0) == 0, "Ejection must preserve stack size");
            helper.assertTrue(item.getX() == pos.getX() + 0.5 && item.getY() == pos.getY() + 0.625 && item.getZ() == pos.getZ() + 0.5, "Ejection must begin directly beneath the grate");
            helper.assertTrue(item.getDeltaMovement().x == 0 && item.getDeltaMovement().z == 0 && item.getDeltaMovement().y < 0, "Ejection must point downward without sideways motion");
            helper.succeed();
        });

        r.add("parity/grate_cached_handler_rejects_closed", 20, helper -> {
            BlockEntityItemGrate grate = place(helper, true);
            ResourceHandler<ItemResource> handler = grate.inventory();
            helper.getLevel().setBlock(grate.getBlockPos(), grate.getBlockState().setValue(BlockItemGrate.OPEN, false), Block.UPDATE_ALL);
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(handler.insert(0, stone(), 64, transaction) == 0, "Cached handler must reject insertion after closing");
                transaction.commit();
            }
            helper.assertTrue(drops(helper).isEmpty() && handler.getAmountAsInt(0) == 0, "Closed grate must retain no new items");
            helper.succeed();
        });

        r.add("parity/grate_exit_rules", 20, helper -> {
            BlockEntityItemGrate grate = place(helper, true);
            helper.setBlock(POS.below(), Blocks.STONE);
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(grate.inventory().insert(0, stone(), 64, transaction) == 0, "Solid exit must reject insertion");
                transaction.commit();
            }
            for (Block block : List.of(Blocks.AIR, Blocks.WATER, Blocks.GLASS, TTBlocks.INFERNAL_FURNACE.get())) {
                helper.setBlock(POS.below(), block);
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(grate.inventory().insert(0, stone(), 64, transaction) == 64, "Non-solid exits and infernal furnaces must accept insertion: " + block);
                }
            }
            helper.assertTrue(drops(helper).isEmpty(), "Simulated transfers must not eject items");
            helper.succeed();
        });

        r.add("parity/grate_retries_when_exit_clears", 20, helper -> {
            BlockEntityItemGrate grate = place(helper, true);
            helper.setBlock(POS.below(), Blocks.STONE);
            grate.inventory().set(0, stone(), 12);
            helper.assertTrue(drops(helper).isEmpty(), "Stored items must wait while the exit is blocked");
            helper.setBlock(POS.below(), Blocks.AIR);
            helper.assertTrue(grate.inventory().getAmountAsInt(0) == 0 && drops(helper).size() == 1, "Neighbor update must release stored items when the exit clears");
            helper.succeed();
        });
    }

    private static ItemResource stone() {
        return ItemResource.of(Items.STONE);
    }

    private static BlockEntityItemGrate place(GameTestHelper helper, boolean open) {
        helper.setBlock(POS, TTBlocks.ITEM_GRATE.get().defaultBlockState().setValue(BlockItemGrate.OPEN, open));
        return (BlockEntityItemGrate) helper.getLevel().getBlockEntity(helper.absolutePos(POS));
    }

    private static List<ItemEntity> drops(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(POS)).inflate(0.5));
    }
}

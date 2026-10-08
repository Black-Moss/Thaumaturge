package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jspecify.annotations.Nullable;

public final class InventoryScanTests {
    private static final BlockPos POS = new BlockPos(2, 2, 3);
    private static final String PREFIX = "parity_scan_";

    private InventoryScanTests() {}

    public static void register(TTTestRegistrar r) {
        ScanningManager.register(new IScannable() {
            @Override
            public boolean matches(Player player, ScanTarget target) {
                Component name = target.carriedStack().get(DataComponents.CUSTOM_NAME);
                return name != null && name.getString().startsWith(PREFIX);
            }

            @Override
            public Identifier research(Player player, ScanTarget target) {
                return key(target.carriedStack().get(DataComponents.CUSTOM_NAME).getString());
            }

            @Override
            public @Nullable Component refusal(Player player, ScanTarget target) {
                String name = target.carriedStack().get(DataComponents.CUSTOM_NAME).getString();
                return name.equals(PREFIX + "dependent") && !KnowledgeAccess.of(player).isResearchKnown(key(PREFIX + "base")) ? Component.literal("Missing prerequisite") : null;
            }
        });

        r.add("parity/inventory_scan_all_slots", 20, helper -> {
            ServerPlayer player = player(helper, "scan_all");
            helper.setBlock(POS, Blocks.CHEST);
            BlockPos pos = helper.absolutePos(POS);
            LargeChest chest = new LargeChest(pos, helper.getLevel().getBlockState(pos));
            helper.getLevel().setBlockEntity(chest);
            for (int slot = 0; slot < 105; slot++)
                chest.setItem(slot, named(Integer.toString(slot)));
            chest.setItem(105, named("0"));
            chest.setItem(127, named("last"));
            ScanTarget target = ScanTarget.block(pos);
            List<ItemStack> snapshot = ScanInventories.contents(player, target);
            helper.assertTrue(snapshot.size() == 106, "Scan must include every distinct stack beyond 100 slots and deduplicate repeated contents");
            ScanningManager.scan(player, target);
            for (int slot = 0; slot < 105; slot++)
                assertKnown(helper, player, Integer.toString(slot));
            assertKnown(helper, player, "last");
            helper.assertTrue(chest.getItem(127).getCount() == 7, "Scanning must not consume inventory contents");
            chest.setItem(127, named("new"));
            helper.assertTrue(ScanningManager.isStillScannable(player, target), "A known container must remain scannable after its contents change");
            ScanningManager.scan(player, target);
            assertKnown(helper, player, "new");
            helper.succeed();
        });

        r.add("parity/inventory_scan_prerequisites", 20, helper -> {
            ServerPlayer player = player(helper, "scan_prereq");
            helper.setBlock(POS, Blocks.BARREL);
            Container container = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(POS));
            container.setItem(0, named("dependent"));
            ScanTarget target = ScanTarget.block(helper.absolutePos(POS));
            ScanningManager.scan(player, target);
            helper.assertTrue(!KnowledgeAccess.of(player).isResearchKnown(key(PREFIX + "dependent")), "Inventory scans must respect discovery prerequisites");
            container.setItem(1, named("base"));
            ScanningManager.scan(player, target);
            assertKnown(helper, player, "base");
            assertKnown(helper, player, "dependent");
            helper.assertTrue(container.getItem(0).getCount() == 7 && container.getItem(1).getCount() == 7, "Scanning must leave both stacks untouched");
            helper.succeed();
        });

        r.add("parity/inventory_scan_furnace_slots", 20, helper -> {
            ServerPlayer player = player(helper, "scan_furnace");
            helper.setBlock(POS, Blocks.FURNACE);
            Container container = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(POS));
            for (int slot = 0; slot < 3; slot++)
                container.setItem(slot, named("furnace_" + slot));
            ScanningManager.scan(player, ScanTarget.block(helper.absolutePos(POS)));
            for (int slot = 0; slot < 3; slot++)
                assertKnown(helper, player, "furnace_" + slot);
            helper.succeed();
        });

        r.add("parity/inventory_scan_container_entity", 20, helper -> {
            ServerPlayer player = player(helper, "scan_cart");
            MinecartChest cart = EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            cart.setItem(26, named("cart"));
            ScanningManager.scan(player, ScanTarget.entity(cart));
            assertKnown(helper, player, "cart");
            helper.assertTrue(cart.getItem(26).getCount() == 7, "Scanning must not consume minecart contents");
            helper.succeed();
        });
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
    }

    private static ItemStack named(String suffix) {
        ItemStack stack = new ItemStack(Items.STONE, 7);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(PREFIX + suffix));
        return stack;
    }

    private static Identifier key(String name) {
        return TTIds.rl("gametest/" + name);
    }

    private static void assertKnown(GameTestHelper helper, ServerPlayer player, String suffix) {
        helper.assertTrue(KnowledgeAccess.of(player).isResearchKnown(key(PREFIX + suffix)), "Inventory scan missed " + suffix);
    }

    private static final class LargeChest extends ChestBlockEntity {
        LargeChest(BlockPos pos, BlockState state) {
            super(pos, state);
            setItems(NonNullList.withSize(128, ItemStack.EMPTY));
        }

        @Override
        public int getContainerSize() {
            return 128;
        }
    }
}

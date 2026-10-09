package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public final class ElementalShovelItem extends Item implements IArchitect {
    private static final int ORIENTATION_COUNT = 3;
    private static final int ORIENTATION_PARALLEL = 0;
    private static final int ORIENTATION_VERTICAL = 1;
    private static final int PLANE_RADIUS = 1;
    private static final int PLANE_SPAN = PLANE_RADIUS * 2 + 1;
    private static final int PLANE_CELLS = PLANE_SPAN * PLANE_SPAN;
    private static final int NO_SLOT = -1;
    private static final int PLACE_WEAR = 1;
    private static final double NON_PLAYER_REACH = 4.5;
    private static final float FULL_PARTIAL_TICK = 1.0F;
    private static final float PLACE_VOLUME = 0.6F;
    private static final float PLACE_PITCH_BASE = 0.9F;
    private static final float PLACE_PITCH_SPREAD = 0.2F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final float PUFF_RED = 128.0F / CHANNEL_MAX;
    private static final float PUFF_GREEN = 51.0F / CHANNEL_MAX;
    private static final float PUFF_BLUE = 0.0F;

    public ElementalShovelItem(Properties properties) {
        super(properties);
    }

    public static int getOrientation(ItemStack stack) {
        int stored = stack.getOrDefault(TTDataComponents.TOOL_ORIENTATION.get(), ORIENTATION_PARALLEL);
        return Math.floorMod(stored, ORIENTATION_COUNT);
    }

    public static void cycleOrientation(ItemStack stack) {
        int following = Math.floorMod(getOrientation(stack) + 1, ORIENTATION_COUNT);
        stack.set(TTDataComponents.TOOL_ORIENTATION.get(), following);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockState source = context.getLevel().getBlockState(context.getClickedPos());
        if (player == null || source.hasBlockEntity()) {
            return InteractionResult.FAIL;
        }
        ItemStack tool = context.getItemInHand();
        Direction face = context.getClickedFace();
        List<BlockPos> targets = cells(context.getClickedPos(), face, player.getDirection(), getOrientation(tool));
        if (context.getLevel() instanceof ServerLevel server) {
            return placeAll(server, player, context.getHand(), targets, face, source) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        boolean anyFits = !fitting(context.getLevel(), targets, source).isEmpty();
        return anyFits ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private static boolean placeAll(ServerLevel level, Player player, InteractionHand hand, List<BlockPos> targets, Direction face, BlockState source) {
        ItemStack tool = player.getItemInHand(hand);
        int index = 0;
        int successes = 0;
        while (index < targets.size() && !tool.isEmpty()) {
            successes += place(level, player, hand, targets.get(index++), face, source) ? 1 : 0;
        }
        return successes > 0;
    }

    @Override
    public List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        List<BlockPos> targets = cells(pos, side, player.getDirection(), getOrientation(stack));
        return fitting(level, targets, level.getBlockState(pos));
    }

    @Override
    public @Nullable HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        double reach = caster instanceof Player player ? player.blockInteractionRange() : NON_PLAYER_REACH;
        return caster.pick(reach, FULL_PARTIAL_TICK, false);
    }

    @Override
    public boolean replacesBlockHighlight(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return false;
    }

    private static boolean place(ServerLevel level, Player player, InteractionHand hand, BlockPos cell, Direction face, BlockState source) {
        ItemStack tool = player.getItemInHand(hand);
        if (!fits(level, cell, source) || !isPermitted(level, player, tool, cell, face)) {
            return false;
        }
        Payment payment = pay(player, source);
        if (payment == null || !level.setBlock(cell, payment.state(), Block.UPDATE_ALL)) {
            return false;
        }
        if (payment.slot() != NO_SLOT) {
            player.getInventory().getNonEquipmentItems().get(payment.slot()).shrink(1);
        }
        level.gameEvent(GameEvent.BLOCK_PLACE, cell, GameEvent.Context.of(player, payment.state()));
        Vec3 centre = Vec3.atCenterOf(cell);
        float pitch = PLACE_PITCH_BASE + level.getRandom().nextFloat() * PLACE_PITCH_SPREAD;
        level.playSound(null, centre.x, centre.y, centre.z, source.getSoundType().getBreakSound(), SoundSource.BLOCKS, PLACE_VOLUME, pitch);
        Effects.bamf(level, centre).color(PUFF_RED, PUFF_GREEN, PUFF_BLUE).side(face).send();
        player.swing(hand, true);
        tool.hurtAndBreak(PLACE_WEAR, player, hand.asEquipmentSlot());
        return true;
    }

    private static boolean isPermitted(ServerLevel level, Player player, ItemStack tool, BlockPos cell, Direction face) {
        if (player.mayUseItemAt(cell, face, tool) && player.mayInteract(level, cell)) {
            return !EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, cell), face);
        }
        return false;
    }

    private static @Nullable Payment pay(Player player, BlockState source) {
        if (player.hasInfiniteMaterials()) {
            return new Payment(NO_SLOT, source);
        }
        Inventory inventory = player.getInventory();
        Payment exact = withdraw(inventory, source.getBlock().asItem(), source);
        if (exact != null) {
            return exact;
        }
        return source.is(Blocks.GRASS_BLOCK) ? withdraw(inventory, Items.DIRT, Blocks.DIRT.defaultBlockState()) : null;
    }

    private static @Nullable Payment withdraw(Inventory inventory, Item item, BlockState placed) {
        int slot = findSlot(inventory, item);
        return slot == NO_SLOT ? null : new Payment(slot, placed);
    }

    private static boolean fits(Level level, BlockPos cell, BlockState source) {
        if (!level.getBlockState(cell).canBeReplaced()) {
            return false;
        }
        return source.canSurvive(level, cell);
    }

    private static List<BlockPos> fitting(Level level, List<BlockPos> targets, BlockState source) {
        return targets.stream().filter(target -> fits(level, target, source)).toList();
    }

    private static int findSlot(Inventory inventory, Item item) {
        if (item == Items.AIR) {
            return NO_SLOT;
        }
        List<ItemStack> stacks = inventory.getNonEquipmentItems();
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (stacks.get(slot).is(item)) {
                return slot;
            }
        }
        return NO_SLOT;
    }

    private static List<BlockPos> cells(BlockPos clicked, Direction face, Direction facing, int orientation) {
        BlockPos centre = clicked.relative(face);
        PlaneAxes axes = planeAxes(face, facing, orientation);
        List<BlockPos> cells = new ArrayList<>(PLANE_CELLS);
        for (int along = -PLANE_RADIUS; along <= PLANE_RADIUS; along++) {
            BlockPos row = centre.relative(axes.first(), along);
            for (int across = -PLANE_RADIUS; across <= PLANE_RADIUS; across++) {
                cells.add(row.relative(axes.second(), across));
            }
        }
        return cells;
    }

    private static PlaneAxes planeAxes(Direction face, Direction facing, int orientation) {
        Direction.Axis faceAxis = face.getAxis();
        if (orientation == ORIENTATION_PARALLEL) {
            return parallelAxes(faceAxis);
        }
        if (faceAxis.isVertical()) {
            return new PlaneAxes(facing.getClockWise().getAxis(), Direction.Axis.Y);
        }
        boolean upright = orientation == ORIENTATION_VERTICAL;
        return upright ? new PlaneAxes(faceAxis, Direction.Axis.Y) : parallelAxes(Direction.Axis.Y);
    }

    private static PlaneAxes parallelAxes(Direction.Axis normal) {
        Direction.Axis[] others = Arrays.stream(Direction.Axis.values()).filter(axis -> axis != normal).toArray(Direction.Axis[]::new);
        return new PlaneAxes(others[0], others[1]);
    }

    private record PlaneAxes(Direction.Axis first, Direction.Axis second) {
    }

    private record Payment(int slot, BlockState state) {
    }
}

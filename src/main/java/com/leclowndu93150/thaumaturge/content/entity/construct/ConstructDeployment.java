package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record ConstructDeployment<T extends EntityOwnedConstruct>(Supplier<EntityType<T>> type, BiConsumer<T, Player> orient) {
    private static final double FOOTPRINT_WIDTH = 1.0;
    private static final double FOOTPRINT_HEIGHT = 2.0;
    private static final double CENTER_OFFSET = 0.5;
    private static final float PLACE_VOLUME = 0.75F;
    private static final float PLACE_PITCH = 0.8F;

    public static <T extends EntityOwnedConstruct> ConstructDeployment<T> upright(Supplier<EntityType<T>> type) {
        return new ConstructDeployment<>(type, ConstructDeployment::keepDefaultFacing);
    }

    private static <E extends EntityOwnedConstruct> void keepDefaultFacing(E construct, Player placer) {}

    public InteractionResult deploy(UseOnContext context) {
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();
        if (player == null || face == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos footing = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(face);
        if (!canStandAt(level, footing, player, face, context)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        T construct = type.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (construct == null) {
            return InteractionResult.FAIL;
        }
        server.removeBlock(footing, false);
        server.removeBlock(footing.above(), false);
        construct.snapTo(footing.getX() + CENTER_OFFSET, footing.getY(), footing.getZ() + CENTER_OFFSET, 0.0F, 0.0F);
        construct.setValidSpawn();
        construct.setOwner(player);
        orient.accept(construct, player);
        server.addFreshEntity(construct);
        server.playSound(null, construct.getX(), construct.getY(), construct.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, PLACE_VOLUME, PLACE_PITCH);
        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean canStandAt(Level level, BlockPos footing, Player player, Direction face, UseOnContext context) {
        if (!player.mayUseItemAt(footing, face, context.getItemInHand())) {
            return false;
        }
        if (!level.getBlockState(footing).canBeReplaced() || !level.getBlockState(footing.above()).canBeReplaced()) {
            return false;
        }
        AABB box = new AABB(footing.getX(), footing.getY(), footing.getZ(), footing.getX() + FOOTPRINT_WIDTH, footing.getY() + FOOTPRINT_HEIGHT, footing.getZ() + FOOTPRINT_WIDTH);
        return level.getEntities(null, box).isEmpty();
    }
}

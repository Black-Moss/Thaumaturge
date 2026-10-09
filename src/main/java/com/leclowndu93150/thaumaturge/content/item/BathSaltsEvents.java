package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BathSaltsEvents {
    private static final int LIFESPAN_TICKS = 200;
    private static final float VERTICAL_RESPONSE = 0.35F;
    private static final double MAX_VERTICAL_SPEED = 0.08;
    private static final int BUBBLE_TICK_INTERVAL = 2;
    private static final int BUBBLE_MIN_COUNT = 2;
    private static final int BUBBLE_COUNT_RANGE = 3;
    private static final double BUBBLE_HORIZONTAL_SPREAD = 0.15;
    private static final double BUBBLE_VERTICAL_SPREAD = 0.1;
    private static final double BUBBLE_SPEED = 0.0;
    private static final float BUBBLE_MIN_SCALE = 1.5F;
    private static final float BUBBLE_SCALE_RANGE = 0.5F;
    private static final int BUBBLE_MIN_AGE = 18;
    private static final int BUBBLE_AGE_RANGE = 10;
    private static final int BUBBLE_COLOR = 0xFFE8E8FF;
    private static final float BUBBLE_ALPHA = 1.0F;
    private static final float BUBBLE_BUOYANCY = -0.025F;

    private BathSaltsEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        ItemEntity salts = saltsOrNull(event.getEntity());
        if (salts != null) {
            salts.lifespan = LIFESPAN_TICKS;
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        ItemEntity salts = saltsOrNull(event.getEntity());
        if (salts != null && salts.isInWater() && salts.level() instanceof ServerLevel level) {
            hoverAndFizz(level, salts);
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        ItemEntity salts = saltsOrNull(event.getEntity());
        if (salts != null && !salts.level().isClientSide()) {
            purifyAt(salts.level(), salts.blockPosition());
        }
    }

    private static void hoverAndFizz(ServerLevel level, ItemEntity item) {
        BlockPos waterPos = findWaterPos(level, item);
        if (waterPos == null) {
            return;
        }
        double surfaceGap = level.getFluidState(waterPos).getHeight(level, waterPos) - item.getBbHeight();
        double goalY = waterPos.getY() + Math.max(0.0, surfaceGap / 2.0);
        double pull = (goalY - item.getY()) * VERTICAL_RESPONSE;
        double speedY = Math.max(-MAX_VERTICAL_SPEED, Math.min(MAX_VERTICAL_SPEED, pull));
        item.setDeltaMovement(item.getDeltaMovement().with(Direction.Axis.Y, speedY));
        if (item.tickCount % BUBBLE_TICK_INTERVAL == 0) {
            emitBubbles(level, item);
        }
    }

    private static void purifyAt(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(Blocks.WATER) || !current.getFluidState().isSource()) {
            return;
        }
        BlockState purified = TTBlocks.PURIFYING_FLUID.get().defaultBlockState();
        level.setBlock(pos, purified, Block.UPDATE_ALL);
    }

    private static ItemEntity saltsOrNull(Entity entity) {
        if (entity instanceof ItemEntity item && item.getItem().getItem() == TTItems.BATH_SALTS.get()) {
            return item;
        }
        return null;
    }

    private static BlockPos findWaterPos(Level level, ItemEntity item) {
        BlockPos here = item.blockPosition();
        if (level.getFluidState(here).is(FluidTags.WATER)) {
            return here;
        }
        BlockPos beneath = here.below();
        return level.getFluidState(beneath).is(FluidTags.WATER) ? beneath : null;
    }

    private static void emitBubbles(ServerLevel level, ItemEntity item) {
        RandomSource rng = level.getRandom();
        int amount = BUBBLE_MIN_COUNT + rng.nextInt(BUBBLE_COUNT_RANGE);
        float size = BUBBLE_MIN_SCALE + rng.nextFloat() * BUBBLE_SCALE_RANGE;
        int lifetime = BUBBLE_MIN_AGE + rng.nextInt(BUBBLE_AGE_RANGE);
        BubbleParticleOptions bubble = new BubbleParticleOptions(BUBBLE_COLOR, BUBBLE_ALPHA, size, lifetime, BUBBLE_BUOYANCY, false);
        double centerY = item.getY() + item.getBbHeight() / 2.0;
        level.sendParticles(bubble, item.getX(), centerY, item.getZ(), amount, BUBBLE_HORIZONTAL_SPREAD, BUBBLE_VERTICAL_SPREAD, BUBBLE_HORIZONTAL_SPREAD, BUBBLE_SPEED);
    }
}

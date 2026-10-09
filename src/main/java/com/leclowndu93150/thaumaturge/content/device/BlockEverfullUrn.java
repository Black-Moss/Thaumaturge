package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

public final class BlockEverfullUrn extends BaseEntityBlock {
    private static final int BOTTLE_COST = 333;
    private static final float SPLASH_VOLUME = 0.33F;
    private static final float SPLASH_BASE_PITCH = 1.0F;
    private static final double SPLASH_PITCH_SPREAD = 0.2;
    private static final VoxelShape SHAPE = Shapes.or(box(3.0, 1.0, 3.0, 13.0, 9.0, 13.0), box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0), box(4.0, 9.0, 4.0, 12.0, 10.0, 12.0),
            box(5.0, 10.0, 5.0, 11.0, 15.0, 11.0), box(4.0, 13.0, 4.0, 5.0, 16.0, 12.0), box(5.0, 13.0, 4.0, 12.0, 16.0, 5.0), box(5.0, 13.0, 11.0, 12.0, 16.0, 12.0),
            box(11.0, 13.0, 5.0, 12.0, 16.0, 11.0));

    public static final MapCodec<BlockEverfullUrn> CODEC = simpleCodec(BlockEverfullUrn::new);

    public BlockEverfullUrn(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEverfullUrn> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEverfullUrn(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityEverfullUrn urn)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return stack.is(Items.GLASS_BOTTLE) || holdsFluidCapability(stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            urn.setChanged();
            splash(level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (stack.is(Items.GLASS_BOTTLE) && urn.waterAmount() >= BOTTLE_COST && level instanceof ServerLevel serverLevel) {
            bottle(serverLevel, pos, player, stack, urn);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TTBlockEntities.EVERFULL_URN.get(), BlockEntityEverfullUrn::serverTick);
    }

    private static boolean holdsFluidCapability(ItemStack stack) {
        return !stack.isEmpty() && ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM) != null;
    }

    private static void bottle(ServerLevel level, BlockPos pos, Player player, ItemStack held, BlockEntityEverfullUrn urn) {
        if (!player.hasInfiniteMaterials()) {
            held.shrink(1);
        }
        ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        if (!player.getInventory().add(water)) {
            player.spawnAtLocation(level, water);
        }
        urn.drainWater(BOTTLE_COST);
        splash(level, pos);
    }

    private static void splash(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, SPLASH_VOLUME, SPLASH_BASE_PITCH + (float) random.triangle(0.0, SPLASH_PITCH_SPREAD));
    }
}

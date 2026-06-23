package com.leclowndu93150.thaumcraft.content.essentia.jar;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.blocks.ILabelable;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockJar extends BaseEntityBlock implements ILabelable {
    public static final MapCodec<BlockJar> CODEC = simpleCodec(BlockJar::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create("fill_level", 0, 4);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public BlockJar(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILL_LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILL_LEVEL);
    }

    public static int amountToFillLevel(int amount) {
        if (amount <= 0) return 0;
        if (amount < 63) return 1;
        if (amount < 125) return 2;
        if (amount < 188) return 3;
        return 4;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJar(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityJar jar && jar.isBraced()) {
            popResource(level, pos, new ItemStack(TCItems.JAR_BRACE.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return 0;
        int amt = jar.amount();
        if (amt <= 0) return 0;
        float ratio = amt / (float) BlockEntityJar.CAPACITY;
        return Math.min(15, (int) Math.floor(ratio * 14.0F) + 1);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return InteractionResult.PASS;
        if (stack.is(TCItems.JAR_BRACE.get())) {
            if (jar.aspectFilterKey() != null) return InteractionResult.PASS;
            ResourceKey<IAspect> current = jar.aspectKey();
            if (current == null) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                jar.setAspectFilter(current);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return InteractionResult.PASS;
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        Direction labelFace = state.getValue(FACING);
        if (jar.aspectFilterKey() != null && hitResult.getDirection() == labelFace) {
            if (!level.isClientSide()) {
                jar.setAspectFilter(null);
                ItemStack labelDrop = new ItemStack(TCItems.LABEL.get());
                double dx = pos.getX() + 0.5 + labelFace.getStepX() / 3.0;
                double dy = pos.getY() + 0.5;
                double dz = pos.getZ() + 0.5 + labelFace.getStepZ() / 3.0;
                ItemEntity entity = new ItemEntity(level, dx, dy, dz, labelDrop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
                level.playSound(null, pos, TCSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            if (!level.isClientSide()) {
                jar.emptyJar();
                level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
                float pitch = 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F;
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5F, pitch);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack) {
        Level level = player.level();
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return false;
        if (jar.aspectFilterKey() != null) return false;
        IEssentiaContainerItem container = stack.getCapability(EssentiaCapabilities.CONTAINER);
        AspectList stackAspects = container == null ? AspectList.EMPTY : container.getAspects(stack);
        boolean stackHasAspects = stackAspects != null && !stackAspects.isEmpty();
        ResourceKey<IAspect> current = jar.aspectKey();
        if (jar.amount() == 0 && !stackHasAspects) return false;
        if (current == null && stackHasAspects) {
            AspectInstance first = stackAspects.entries().get(0);
            ResourceKey<IAspect> stackKey = first.aspect().unwrapKey().orElse(null);
            if (stackKey == null) return false;
            jar.setAspectFromLabel(stackKey);
            jar.setAspectFilter(stackKey);
        } else if (current != null) {
            jar.setAspectFilter(current);
        } else {
            return false;
        }
        if (!level.isClientSide()) {
            level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
        }
        return true;
    }
}

package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockPedestal extends BaseEntityBlock {
    private static final int MAX_CHARGE = 15;
    private static final String CHARGE_NAME = "charge";
    private static final String VARIANT_FIELD = "variant";
    private static final float SOUND_VOLUME = 0.2F;
    private static final float PITCH_CENTER = 1.0F;
    private static final float PITCH_DEVIATION = 0.7F;
    private static final float PITCH_INSERT_SCALE = 1.6F;
    private static final float PITCH_TAKE_SCALE = 1.5F;
    private static final double FULL = 16.0;
    private static final double FOOT_TOP = 4.0;
    private static final double INSET_LOW = 2.0;
    private static final double INSET_HIGH = 14.0;
    private static final double NARROW_LOW = 4.0;
    private static final double NARROW_HIGH = 12.0;
    private static final double STEP_TOP = 8.0;

    public static final MapCodec<BlockPedestal> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Variant.CODEC.fieldOf(VARIANT_FIELD).forGetter(block -> block.variant), propertiesCodec()).apply(instance, BlockPedestal::new));
    public static final IntegerProperty CHARGE = IntegerProperty.create(CHARGE_NAME, 0, MAX_CHARGE);

    private final Variant variant;

    public BlockPedestal(Variant variant, Properties properties) {
        super(properties);
        this.variant = variant;
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockInlay.updateNetwork(level, pos);
        }
    }

    @Override
    protected MapCodec<BlockPedestal> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPedestal(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return variant.shape();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return exchange(level, pos, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, InteractionResult.PASS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return exchange(level, pos, player, hand, stack, InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    private InteractionResult exchange(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held, InteractionResult noEntityResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal)) {
            return noEntityResult;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack current = pedestal.getItem();
        boolean loaded = !current.isEmpty();
        if (!loaded && held.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (loaded) {
            giveToPlayer(player, current);
            pedestal.setItem(ItemStack.EMPTY);
        }
        if (!held.isEmpty()) {
            ItemStack source = player.getItemInHand(hand);
            pedestal.setItem(source.copyWithCount(1));
            source.consume(1, player);
        }
        RandomSource random = level.getRandom();
        float pitch = (float) random.triangle(PITCH_CENTER, PITCH_DEVIATION) * (loaded ? PITCH_TAKE_SCALE : PITCH_INSERT_SCALE);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, SOUND_VOLUME, pitch);
        return InteractionResult.SUCCESS;
    }

    private static void giveToPlayer(Player player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    public enum Variant implements StringRepresentable {
        ARCANE("arcane",
                Shapes.or(Block.box(0.0, 0.0, 0.0, FULL, FOOT_TOP, FULL), Block.box(NARROW_LOW, FOOT_TOP, NARROW_LOW, NARROW_HIGH, NARROW_HIGH, NARROW_HIGH),
                        Block.box(INSET_LOW, NARROW_HIGH, INSET_LOW, INSET_HIGH, FULL, INSET_HIGH))), ELDRITCH("eldritch",
                                Shapes.or(Block.box(0.0, 0.0, 0.0, FULL, FOOT_TOP, FULL), Block.box(INSET_LOW, FOOT_TOP, INSET_LOW, INSET_HIGH, STEP_TOP, INSET_HIGH),
                                        Block.box(NARROW_LOW, STEP_TOP, NARROW_LOW, NARROW_HIGH, NARROW_HIGH, NARROW_HIGH)));

        public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);

        private final String name;
        private final VoxelShape shape;

        Variant(String name, VoxelShape shape) {
            this.name = name;
            this.shape = shape;
        }

        public VoxelShape shape() {
            return shape;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}

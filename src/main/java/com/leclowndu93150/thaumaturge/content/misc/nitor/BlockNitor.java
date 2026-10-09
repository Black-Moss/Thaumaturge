package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockNitor extends BaseEntityBlock {
    private static final String DYE_FIELD = "dye";

    public static final MapCodec<BlockNitor> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(DyeColor.CODEC.fieldOf(DYE_FIELD).forGetter(BlockNitor::dye), propertiesCodec()).apply(instance, BlockNitor::new));

    private static final double SHAPE_MIN = 5.28;
    private static final double SHAPE_MAX = 10.56;
    private static final VoxelShape SHAPE = box(SHAPE_MIN, SHAPE_MIN, SHAPE_MIN, SHAPE_MAX, SHAPE_MAX, SHAPE_MAX);

    private final DyeColor dye;

    public BlockNitor(DyeColor dye, Properties properties) {
        super(properties);
        this.dye = dye;
    }

    public DyeColor dye() {
        return dye;
    }

    public int dyeColor() {
        return dye.getMapColor().col;
    }

    @Override
    protected MapCodec<BlockNitor> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNitor(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.NITOR.get(), BlockEntityNitor::clientTick);
    }
}

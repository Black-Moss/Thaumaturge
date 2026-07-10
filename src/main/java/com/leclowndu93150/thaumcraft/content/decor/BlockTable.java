package com.leclowndu93150.thaumcraft.content.decor;

import com.leclowndu93150.thaumcraft.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumcraft.api.items.IScribeTools;
import com.leclowndu93150.thaumcraft.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumcraft.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class BlockTable extends Block {
    public static final MapCodec<BlockTable> CODEC = simpleCodec(BlockTable::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(1.0, 0.0, 1.0, 5.0, 12.0, 5.0),
            Block.box(11.0, 0.0, 1.0, 15.0, 12.0, 5.0),
            Block.box(1.0, 0.0, 11.0, 5.0, 12.0, 15.0),
            Block.box(11.0, 0.0, 11.0, 15.0, 12.0, 15.0));

    public BlockTable(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockTable> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (this != TCBlocks.TABLE_WOOD.get() || !(stack.getItem() instanceof IScribeTools)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState table = TCBlocks.RESEARCH_TABLE.get().defaultBlockState()
                .setValue(BlockResearchTable.FACING, player.getDirection());
        level.setBlock(pos, table, 3);
        if (level.getBlockEntity(pos) instanceof BlockEntityResearchTable researchTable) {
            ItemStack tools = stack.copy();
            researchTable.items().set(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, ItemResource.of(tools), tools.getCount());
            researchTable.setChanged();
        }
        player.setItemInHand(hand, ItemStack.EMPTY);
        if (player instanceof ServerPlayer serverPlayer) {
            ResearchProgressionEvents.recordCrafted(serverPlayer, new ItemStack(TCBlocks.RESEARCH_TABLE.get()));
        }
        return InteractionResult.SUCCESS;
    }
}

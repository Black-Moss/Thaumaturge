package com.leclowndu93150.thaumcraft.content.world.tree;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockSaplingTC extends SaplingBlock {
    public static final MapCodec<BlockSaplingTC> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(TreeGrower.CODEC.fieldOf("tree").forGetter(b -> b.treeGrower), propertiesCodec())
                    .apply(i, BlockSaplingTC::new)
    );

    public BlockSaplingTC(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public MapCodec<? extends BlockSaplingTC> codec() {
        return CODEC;
    }
}

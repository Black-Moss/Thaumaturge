package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class TemplateBlocks {
    private TemplateBlocks() {}

    public record Entry(BlockPos pos, BlockState state, Optional<CompoundTag> nbt) {
    }

    public static List<Entry> read(StructureTemplate template, HolderGetter<Block> blocks) {
        CompoundTag tag = template.save(new CompoundTag());
        Vec3i size = template.getSize();
        ListTag paletteTag = tag.getList("palette").orElseGet(() -> tag.getListOrEmpty("palettes").getListOrEmpty(0));
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blocks, paletteTag.getCompoundOrEmpty(i)));
        }
        ListTag blockTags = tag.getListOrEmpty("blocks");
        List<Entry> entries = new ArrayList<>(blockTags.size());
        for (int i = 0; i < blockTags.size(); i++) {
            CompoundTag block = blockTags.getCompoundOrEmpty(i);
            ListTag posTag = block.getListOrEmpty("pos");
            BlockPos pos = new BlockPos(posTag.getIntOr(0, 0), posTag.getIntOr(1, 0), posTag.getIntOr(2, 0));
            int stateId = block.getIntOr("state", 0);
            if (stateId < 0 || stateId >= palette.size() || pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() >= size.getX() || pos.getY() >= size.getY() || pos.getZ() >= size.getZ()) {
                continue;
            }
            entries.add(new Entry(pos, palette.get(stateId), block.getCompound("nbt")));
        }
        return entries;
    }
}

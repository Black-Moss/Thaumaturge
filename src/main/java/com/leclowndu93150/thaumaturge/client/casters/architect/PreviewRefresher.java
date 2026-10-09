package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PreviewRefresher {
    public static final int REFRESH_BUCKET_TICKS = 5;

    private final LongSet members = new LongOpenHashSet();
    private @Nullable BlockPos lastAnchor;
    private @Nullable Direction lastFace;
    private int lastBucket;
    private PreviewSnapshot current = PreviewSnapshot.EMPTY;

    public PreviewSnapshot update(IArchitect architect, ItemStack stack, ClientLevel level, BlockPos anchor, Direction face, Player player, int bucket) {
        if (!anchor.equals(lastAnchor) || face != lastFace || bucket != lastBucket) {
            lastAnchor = anchor.immutable();
            lastFace = face;
            lastBucket = bucket;
            current = build(architect.previewBlocks(stack, level, anchor, face, player));
        }
        return current;
    }

    private PreviewSnapshot build(List<BlockPos> positions) {
        members.clear();
        for (BlockPos pos : positions) {
            members.add(pos.asLong());
        }
        int[] masks = new int[positions.size()];
        for (int index = 0; index < masks.length; index++) {
            masks[index] = NeighbourMask.of(members, positions.get(index));
        }
        return new PreviewSnapshot(positions, masks);
    }
}

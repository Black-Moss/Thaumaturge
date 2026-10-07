package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum ReliquaryView {
    CLAIMABLE, CLAIMED, INELIGIBLE, WARDED;

    public static final StreamCodec<ByteBuf, ReliquaryView> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], ReliquaryView::ordinal);

    public boolean showsReward() {
        return this == CLAIMABLE || this == WARDED;
    }
}

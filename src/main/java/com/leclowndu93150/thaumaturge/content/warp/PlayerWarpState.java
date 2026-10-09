package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public final class PlayerWarpState implements IPlayerWarp {
    public static final int POOL_MIN = 0;
    public static final int POOL_MAX = 500;

    public static final MapCodec<PlayerWarpState> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                    .group(Codec.INT.fieldOf("permanent").forGetter(PlayerWarpState::permanent), Codec.INT.fieldOf("normal").forGetter(PlayerWarpState::normal),
                            Codec.INT.fieldOf("temporary").forGetter(PlayerWarpState::temporary), Codec.INT.fieldOf("counter").forGetter(PlayerWarpState::getCounter))
                    .apply(instance, PlayerWarpState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerWarpState> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PlayerWarpState::permanent, ByteBufCodecs.VAR_INT,
            PlayerWarpState::normal, ByteBufCodecs.VAR_INT, PlayerWarpState::temporary, ByteBufCodecs.VAR_INT, PlayerWarpState::getCounter, PlayerWarpState::new);

    private final int[] pools = new int[WarpType.values().length];
    private int counter;

    public PlayerWarpState() {}

    private PlayerWarpState(int permanent, int normal, int temporary, int counter) {
        set(WarpType.PERMANENT, permanent);
        set(WarpType.NORMAL, normal);
        set(WarpType.TEMPORARY, temporary);
        this.counter = counter;
    }

    private int permanent() {
        return get(WarpType.PERMANENT);
    }

    private int normal() {
        return get(WarpType.NORMAL);
    }

    private int temporary() {
        return get(WarpType.TEMPORARY);
    }

    public int actual() {
        return permanent() + normal();
    }

    public int total() {
        return actual() + temporary();
    }

    @Override
    public int get(WarpType type) {
        return pools[type.ordinal()];
    }

    @Override
    public void set(WarpType type, int amount) {
        pools[type.ordinal()] = Mth.clamp(amount, POOL_MIN, POOL_MAX);
    }

    @Override
    public int add(WarpType type, int amount) {
        long next = (long) get(type) + amount;
        pools[type.ordinal()] = (int) Mth.clamp(next, POOL_MIN, POOL_MAX);
        return get(type);
    }

    @Override
    public int reduce(WarpType type, int amount) {
        long next = (long) get(type) - amount;
        pools[type.ordinal()] = (int) Mth.clamp(next, POOL_MIN, POOL_MAX);
        return get(type);
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public void clear() {
        for (WarpType type : WarpType.values()) {
            pools[type.ordinal()] = POOL_MIN;
        }
        counter = 0;
    }
}

package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

public final class BlockJarCustom extends BlockJar {
    public static final MapCodec<BlockJarCustom> CODEC = simpleCodec(props -> new BlockJarCustom(
            props,
            IEssentiaJar.DEFAULT_CAPACITY,
            IEssentiaJar.DEFAULT_SIDE_TEXTURE,
            IEssentiaJar.DEFAULT_TOP_TEXTURE,
            IEssentiaJar.DEFAULT_BOTTOM_TEXTURE));

    private final int capacity;
    private final Identifier sideTexture;
    private final Identifier topTexture;
    private final Identifier bottomTexture;

    public BlockJarCustom(
            Properties properties,
            int capacity,
            Identifier sideTexture,
            Identifier topTexture,
            Identifier bottomTexture) {
        super(properties);
        this.capacity = capacity;
        this.sideTexture = sideTexture;
        this.topTexture = topTexture;
        this.bottomTexture = bottomTexture;
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    @Override
    public int jarCapacity() {
        return capacity;
    }

    @Override
    public Identifier jarSideTexture() {
        return sideTexture;
    }

    @Override
    public Identifier jarTopTexture() {
        return topTexture;
    }

    @Override
    public Identifier jarBottomTexture() {
        return bottomTexture;
    }
}

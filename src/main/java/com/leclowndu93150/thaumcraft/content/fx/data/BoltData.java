package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BoltData(double targetX, double targetY, double targetZ,
                       float r, float g, float b, float width) implements ParticleOptions {
    public static final MapCodec<BoltData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("target_x").forGetter(BoltData::targetX),
                    Codec.DOUBLE.fieldOf("target_y").forGetter(BoltData::targetY),
                    Codec.DOUBLE.fieldOf("target_z").forGetter(BoltData::targetZ),
                    Codec.FLOAT.fieldOf("r").forGetter(BoltData::r),
                    Codec.FLOAT.fieldOf("g").forGetter(BoltData::g),
                    Codec.FLOAT.fieldOf("b").forGetter(BoltData::b),
                    Codec.FLOAT.fieldOf("width").forGetter(BoltData::width)
            ).apply(instance, BoltData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BoltData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, BoltData::targetX,
            ByteBufCodecs.DOUBLE, BoltData::targetY,
            ByteBufCodecs.DOUBLE, BoltData::targetZ,
            ByteBufCodecs.FLOAT, BoltData::r,
            ByteBufCodecs.FLOAT, BoltData::g,
            ByteBufCodecs.FLOAT, BoltData::b,
            ByteBufCodecs.FLOAT, BoltData::width,
            BoltData::new
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BOLT.get();
    }
}

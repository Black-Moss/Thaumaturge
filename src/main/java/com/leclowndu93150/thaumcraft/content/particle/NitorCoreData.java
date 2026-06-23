package com.leclowndu93150.thaumcraft.content.particle;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NitorCoreData(float r, float g, float b) implements ParticleOptions {
    public static final MapCodec<NitorCoreData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(NitorCoreData::r),
            Codec.FLOAT.fieldOf("g").forGetter(NitorCoreData::g),
            Codec.FLOAT.fieldOf("b").forGetter(NitorCoreData::b)
    ).apply(inst, NitorCoreData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NitorCoreData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, NitorCoreData::r,
            ByteBufCodecs.FLOAT, NitorCoreData::g,
            ByteBufCodecs.FLOAT, NitorCoreData::b,
            NitorCoreData::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.NITOR_CORE.get();
    }
}

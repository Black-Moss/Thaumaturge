package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record VisSparkleData(double tx, double ty, double tz) implements ParticleOptions {
    public static final MapCodec<VisSparkleData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("tx").forGetter(VisSparkleData::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(VisSparkleData::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(VisSparkleData::tz)
            ).apply(instance, VisSparkleData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VisSparkleData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
            },
            buf -> new VisSparkleData(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.VIS_SPARKLE.get();
    }
}

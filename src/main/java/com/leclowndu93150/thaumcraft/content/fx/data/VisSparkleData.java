package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record VisSparkleData(double tx, double ty, double tz, int color) implements ParticleOptions {
    public static final int DEFAULT_COLOR = -1;

    public VisSparkleData(double tx, double ty, double tz) {
        this(tx, ty, tz, DEFAULT_COLOR);
    }

    public static final MapCodec<VisSparkleData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("tx").forGetter(VisSparkleData::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(VisSparkleData::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(VisSparkleData::tz),
                    Codec.INT.optionalFieldOf("color", DEFAULT_COLOR).forGetter(VisSparkleData::color)
            ).apply(instance, VisSparkleData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VisSparkleData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
            },
            buf -> new VisSparkleData(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt())
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.VIS_SPARKLE.get();
    }
}

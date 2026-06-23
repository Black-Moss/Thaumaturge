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

public record WispData(int entityId) implements ParticleOptions {
    public static final int NO_ENTITY = -1;

    public static final MapCodec<WispData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("entity_id").forGetter(WispData::entityId)
            ).apply(instance, WispData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, WispData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WispData::entityId,
            WispData::new
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WISP.get();
    }
}

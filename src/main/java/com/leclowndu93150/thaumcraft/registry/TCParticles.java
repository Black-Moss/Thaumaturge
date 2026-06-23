package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.particle.EssentiaDropParticleData;
import com.leclowndu93150.thaumcraft.content.particle.BlockRunesData;
import com.leclowndu93150.thaumcraft.content.particle.BoreParticlesData;
import com.leclowndu93150.thaumcraft.content.particle.BoreSparkleData;
import com.leclowndu93150.thaumcraft.content.particle.FXGenericData;
import com.leclowndu93150.thaumcraft.content.particle.FireMoteData;
import com.leclowndu93150.thaumcraft.content.particle.NitorCoreData;
import com.leclowndu93150.thaumcraft.content.particle.SmokeSpiralData;
import com.leclowndu93150.thaumcraft.content.particle.VentData;
import com.leclowndu93150.thaumcraft.content.particle.VisSparkleData;
import com.leclowndu93150.thaumcraft.content.particle.WispData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, TCIds.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FXGenericData>> FX_GENERIC =
            PARTICLES.register("fx_generic", () -> new ParticleType<FXGenericData>(false) {
                @Override
                public MapCodec<FXGenericData> codec() {
                    return FXGenericData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, FXGenericData> streamCodec() {
                    return FXGenericData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<FireMoteData>> FIRE_MOTE =
            PARTICLES.register("fire_mote", () -> new ParticleType<FireMoteData>(false) {
                @Override
                public MapCodec<FireMoteData> codec() {
                    return FireMoteData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, FireMoteData> streamCodec() {
                    return FireMoteData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<NitorCoreData>> NITOR_CORE =
            PARTICLES.register("nitor_core", () -> new ParticleType<NitorCoreData>(false) {
                @Override
                public MapCodec<NitorCoreData> codec() {
                    return NitorCoreData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, NitorCoreData> streamCodec() {
                    return NitorCoreData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<VentData>> VENT =
            PARTICLES.register("vent", () -> new ParticleType<VentData>(false) {
                @Override
                public MapCodec<VentData> codec() {
                    return VentData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, VentData> streamCodec() {
                    return VentData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockRunesData>> BLOCK_RUNES =
            PARTICLES.register("block_runes", () -> new ParticleType<BlockRunesData>(false) {
                @Override
                public MapCodec<BlockRunesData> codec() {
                    return BlockRunesData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, BlockRunesData> streamCodec() {
                    return BlockRunesData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<SmokeSpiralData>> SMOKE_SPIRAL =
            PARTICLES.register("smoke_spiral", () -> new ParticleType<SmokeSpiralData>(false) {
                @Override
                public MapCodec<SmokeSpiralData> codec() {
                    return SmokeSpiralData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, SmokeSpiralData> streamCodec() {
                    return SmokeSpiralData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<VisSparkleData>> VIS_SPARKLE =
            PARTICLES.register("vis_sparkle", () -> new ParticleType<VisSparkleData>(false) {
                @Override
                public MapCodec<VisSparkleData> codec() {
                    return VisSparkleData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, VisSparkleData> streamCodec() {
                    return VisSparkleData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<WispData>> WISP =
            PARTICLES.register("wisp", () -> new ParticleType<WispData>(false) {
                @Override
                public MapCodec<WispData> codec() {
                    return WispData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, WispData> streamCodec() {
                    return WispData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<BoreParticlesData>> BORE_PARTICLES =
            PARTICLES.register("bore_particles", () -> new ParticleType<BoreParticlesData>(false) {
                @Override
                public MapCodec<BoreParticlesData> codec() {
                    return BoreParticlesData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, BoreParticlesData> streamCodec() {
                    return BoreParticlesData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<BoreSparkleData>> BORE_SPARKLE =
            PARTICLES.register("bore_sparkle", () -> new ParticleType<BoreSparkleData>(false) {
                @Override
                public MapCodec<BoreSparkleData> codec() {
                    return BoreSparkleData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, BoreSparkleData> streamCodec() {
                    return BoreSparkleData.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<EssentiaDropParticleData>> ESSENTIA_DROP =
            PARTICLES.register("essentia_drop", () -> new ParticleType<EssentiaDropParticleData>(false) {
                @Override
                public MapCodec<EssentiaDropParticleData> codec() {
                    return EssentiaDropParticleData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, EssentiaDropParticleData> streamCodec() {
                    return EssentiaDropParticleData.STREAM_CODEC;
                }
            });

    private TCParticles() {}

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}

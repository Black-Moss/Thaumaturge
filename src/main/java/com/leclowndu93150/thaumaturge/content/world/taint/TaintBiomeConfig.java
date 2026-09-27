package com.leclowndu93150.thaumaturge.content.world.taint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TaintBiomeConfig(Block crust, int maxCrustBlobs, IntProvider crustRadius, int grassFibreAttempts, int generalFibreAttempts, int groundSearchDepth, boolean landmarks,
        int landmarkRadiusChunks, int landmarkAttempts) implements FeatureConfiguration {
    public static final Codec<TaintBiomeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("crust").forGetter(TaintBiomeConfig::crust),
            Codec.intRange(0, 16).fieldOf("max_crust_blobs").forGetter(TaintBiomeConfig::maxCrustBlobs), IntProviders.codec(0, 8).fieldOf("crust_radius").forGetter(TaintBiomeConfig::crustRadius),
            Codec.intRange(0, 64).fieldOf("grass_fibre_attempts").forGetter(TaintBiomeConfig::grassFibreAttempts),
            Codec.intRange(0, 64).fieldOf("general_fibre_attempts").forGetter(TaintBiomeConfig::generalFibreAttempts),
            Codec.intRange(1, 128).fieldOf("ground_search_depth").forGetter(TaintBiomeConfig::groundSearchDepth), Codec.BOOL.fieldOf("landmarks").forGetter(TaintBiomeConfig::landmarks),
            Codec.intRange(1, 16).fieldOf("landmark_radius_chunks").forGetter(TaintBiomeConfig::landmarkRadiusChunks),
            Codec.intRange(1, 256).fieldOf("landmark_attempts").forGetter(TaintBiomeConfig::landmarkAttempts)).apply(instance, TaintBiomeConfig::new));
}

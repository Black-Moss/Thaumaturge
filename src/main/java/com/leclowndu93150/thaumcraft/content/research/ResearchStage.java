package com.leclowndu93150.thaumcraft.content.research;

import com.leclowndu93150.thaumcraft.api.research.IResearchStage;
import com.leclowndu93150.thaumcraft.api.research.KnowledgeReward;
import com.leclowndu93150.thaumcraft.api.research.ResearchRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;

public record ResearchStage(
        String textKey,
        List<Identifier> recipes,
        List<ResearchRequirement> obtain,
        List<ResearchRequirement> craft,
        List<KnowledgeReward> knowledge,
        List<Identifier> requiredResearch,
        int warp
) implements IResearchStage {
    public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(ResearchStage::textKey),
            Identifier.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchStage::recipes),
            ResearchRequirement.CODEC.listOf().optionalFieldOf("obtain", List.of()).forGetter(ResearchStage::obtain),
            ResearchRequirement.CODEC.listOf().optionalFieldOf("craft", List.of()).forGetter(ResearchStage::craft),
            KnowledgeReward.CODEC.listOf().optionalFieldOf("knowledge", List.of()).forGetter(ResearchStage::knowledge),
            Identifier.CODEC.listOf().optionalFieldOf("required_research", List.of()).forGetter(ResearchStage::requiredResearch),
            Codec.INT.optionalFieldOf("warp", 0).forGetter(ResearchStage::warp)
    ).apply(instance, ResearchStage::new));
}

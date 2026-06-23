package com.leclowndu93150.thaumcraft.content.research;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record ResearchCategory(
        Optional<Identifier> requiredResearch,
        AspectList formula,
        Identifier icon,
        Identifier background,
        Optional<Identifier> overlayBackground
) implements IResearchCategory {
    public static final Codec<ResearchCategory> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("required_research").forGetter(ResearchCategory::requiredResearch),
            AspectList.CODEC.fieldOf("formula").forGetter(ResearchCategory::formula),
            Identifier.CODEC.fieldOf("icon").forGetter(ResearchCategory::icon),
            Identifier.CODEC.fieldOf("background").forGetter(ResearchCategory::background),
            Identifier.CODEC.optionalFieldOf("overlay_background").forGetter(ResearchCategory::overlayBackground)
    ).apply(instance, ResearchCategory::new));

    public static final Codec<IResearchCategory> CODEC = DIRECT_CODEC.xmap(c -> (IResearchCategory) c, ResearchCategory::ofInterface);

    private static ResearchCategory ofInterface(IResearchCategory category) {
        if (category instanceof ResearchCategory concrete) {
            return concrete;
        }
        return new ResearchCategory(
                category.requiredResearch(),
                category.formula(),
                category.icon(),
                category.background(),
                category.overlayBackground()
        );
    }
}

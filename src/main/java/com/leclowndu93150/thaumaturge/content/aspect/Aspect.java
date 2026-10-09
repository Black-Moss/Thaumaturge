package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;

public record Aspect(String tag, int color, List<Holder<IAspect>> components, Optional<String> chatColor, Identifier texture, int blend) implements IAspect {
    public static final int DEFAULT_BLEND = 1;
    public static final int CONTRAST_BLEND = 771;

    private static final int PRIMAL_COMPONENTS = 0;
    private static final int COMPOUND_COMPONENTS = 2;
    private static final String TEXTURE_PREFIX = "textures/aspects/";
    private static final String TEXTURE_SUFFIX = ".png";

    public static final Codec<Aspect> DIRECT_CODEC = RecordCodecBuilder.<Aspect>create(builder -> builder.group(Codec.STRING.fieldOf("tag").forGetter(Aspect::tag),
            Codec.INT.fieldOf("color").forGetter(Aspect::color), RegistryFixedCodec.create(IAspect.REGISTRY_KEY).listOf().optionalFieldOf("components", List.of()).forGetter(Aspect::components),
            Codec.STRING.optionalFieldOf("chat_color").forGetter(Aspect::chatColor), Identifier.CODEC.optionalFieldOf("texture").forGetter(Aspect::optionalTexture),
            Codec.INT.optionalFieldOf("blend", DEFAULT_BLEND).forGetter(Aspect::blend)).apply(builder, Aspect::create)).validate(Aspect::validate);

    public static final Codec<IAspect> CODEC = DIRECT_CODEC.xmap(aspect -> aspect, Aspect::ofIAspect);

    private static Aspect create(String tag, int color, List<Holder<IAspect>> components, Optional<String> chatColor, Optional<Identifier> texture, int blend) {
        return new Aspect(tag, color, components, chatColor, texture.orElseGet(() -> defaultTexture(tag)), blend);
    }

    private static DataResult<Aspect> validate(Aspect aspect) {
        int count = aspect.components.size();
        if (count == PRIMAL_COMPONENTS || count == COMPOUND_COMPONENTS) {
            return DataResult.success(aspect);
        }
        return DataResult.error(() -> "Aspect '" + aspect.tag + "' must have 0 or 2 components, has " + count);
    }

    private static Identifier defaultTexture(String tag) {
        return TTIds.rl(TEXTURE_PREFIX + tag + TEXTURE_SUFFIX);
    }

    private static Aspect ofIAspect(IAspect aspect) {
        if (aspect instanceof Aspect direct) {
            return direct;
        }
        return new Aspect(aspect.tag(), aspect.color(), aspect.components(), aspect.chatColor(), aspect.texture(), aspect.blend());
    }

    private Optional<Identifier> optionalTexture() {
        return texture.equals(defaultTexture(tag)) ? Optional.empty() : Optional.of(texture);
    }
}

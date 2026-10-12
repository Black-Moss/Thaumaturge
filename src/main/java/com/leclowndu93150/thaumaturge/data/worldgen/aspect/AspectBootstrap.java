package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class AspectBootstrap {
    private AspectBootstrap() {}

    public static void bootstrap(BootstrapContext<IAspect> ctx) {
        primal(ctx, TTAspects.AER, 0xF2E66B, "e", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.TERRA, 0x5DB83A, "a", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.IGNIS, 0xF2712B, "c", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.AQUA, 0x2EB4F5, "b", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.ORDO, 0xB8B4EA, "7", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.PERDITIO, 0x5A5A5F, "8", Aspect.CONTRAST_BLEND);

        compound(ctx, TTAspects.VACUOS, 0xA6A6A6, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.PERDITIO);
        compound(ctx, TTAspects.LUX, 0xFFF4C2, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.IGNIS);
        compound(ctx, TTAspects.MOTUS, 0x8E9BE6, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.ORDO);
        compound(ctx, TTAspects.GELUM, 0xC8EEFF, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.VITREUS, 0x9FE3E6, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AER);
        compound(ctx, TTAspects.METALLUM, 0x8E9AAB, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.ORDO);
        compound(ctx, TTAspects.VICTUS, 0xC41E2A, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AQUA);
        compound(ctx, TTAspects.PERMUTATIO, 0x7E9A73, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.ORDO);
        compound(ctx, TTAspects.POTENTIA, 0x9CF5EC, Aspect.DEFAULT_BLEND, TTAspects.ORDO, TTAspects.IGNIS);
        compound(ctx, TTAspects.VOLATUS, 0xDCD3BA, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.MOTUS);
        compound(ctx, TTAspects.VINCULUM, 0x6A1A1E, Aspect.CONTRAST_BLEND, TTAspects.MOTUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.TENEBRAE, 0x33293A, Aspect.CONTRAST_BLEND, TTAspects.VACUOS, TTAspects.LUX);
        compound(ctx, TTAspects.MORTUUS, 0x5E1F27, Aspect.CONTRAST_BLEND, TTAspects.VICTUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.HERBA, 0x2F7A2A, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.TERRA);
        compound(ctx, TTAspects.PRAECANTATIO, 0xC53FD6, Aspect.DEFAULT_BLEND, TTAspects.POTENTIA, TTAspects.AER);
        compound(ctx, TTAspects.INSTRUMENTUM, 0x2A2E8C, Aspect.DEFAULT_BLEND, TTAspects.METALLUM, TTAspects.POTENTIA);
        compound(ctx, TTAspects.ALIENIS, 0x4A3426, Aspect.CONTRAST_BLEND, TTAspects.VACUOS, TTAspects.TENEBRAE);
        compound(ctx, TTAspects.SPIRITUS, 0xD6CDEB, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.MORTUUS);
        compound(ctx, TTAspects.AURAM, 0xF2B8D4, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AER);
        compound(ctx, TTAspects.ALKIMIA, 0x1E9E62, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AQUA);
        compound(ctx, TTAspects.VITIUM, 0x7A1FB0, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.PRAECANTATIO);
        compound(ctx, TTAspects.FABRICO, 0x9AA58E, Aspect.DEFAULT_BLEND, TTAspects.PERMUTATIO, TTAspects.INSTRUMENTUM);
        compound(ctx, TTAspects.MACHINA, 0x66778C, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.INSTRUMENTUM);
        compound(ctx, TTAspects.EXANIMIS, 0x5A5A26, Aspect.CONTRAST_BLEND, TTAspects.MOTUS, TTAspects.MORTUUS);
        compound(ctx, TTAspects.BESTIA, 0xA8762E, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.VICTUS);
        compound(ctx, TTAspects.COGNITIO, 0xF2A98C, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.SPIRITUS);
        compound(ctx, TTAspects.SENSUS, 0xB6E02E, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.SPIRITUS);
        compound(ctx, TTAspects.AVERSIO, 0xA8473A, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.PRAEMUNIO, 0x1F6E6B, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.TERRA);
        compound(ctx, TTAspects.DESIDERIUM, 0xCFA35A, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VACUOS);
        compound(ctx, TTAspects.HUMANUS, 0xE8C2A0, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VICTUS);
    }

    private static void primal(BootstrapContext<IAspect> ctx, ResourceKey<IAspect> key, int color, String chatColor, int blend) {
        ctx.register(key, new Aspect(key.identifier().getPath(), color, List.of(), Optional.of(chatColor),
                Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "textures/aspects/" + key.identifier().getPath() + ".png"), blend));
    }

    private static void compound(BootstrapContext<IAspect> ctx, ResourceKey<IAspect> key, int color, int blend, ResourceKey<IAspect> componentA, ResourceKey<IAspect> componentB) {
        Holder<IAspect> a = ctx.lookup(IAspect.REGISTRY_KEY).getOrThrow(componentA);
        Holder<IAspect> b = ctx.lookup(IAspect.REGISTRY_KEY).getOrThrow(componentB);
        ctx.register(key, new Aspect(key.identifier().getPath(), color, List.of(a, b), Optional.empty(),
                Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "textures/aspects/" + key.identifier().getPath() + ".png"), blend));
    }
}

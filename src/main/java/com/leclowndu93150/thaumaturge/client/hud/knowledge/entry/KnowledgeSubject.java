package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.hud.HudTextures;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record KnowledgeSubject(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) implements GainSubject {
    private static final int ICON_SIZE = 16;
    private static final int ICON_ORIGIN = -ICON_SIZE / 2;
    private static final int CATEGORY_ICON_TEX = 32;
    private static final float CATEGORY_SCALE = 0.75F;

    @Override
    public void drawIcon(GuiGraphicsExtractor graphics, ClientLevel level) {
        Identifier typeIcon = type == KnowledgeType.THEORY ? HudTextures.GAIN_THEORY : HudTextures.GAIN_OBSERVATION;
        graphics.blit(RenderPipelines.GUI_TEXTURED, typeIcon, ICON_ORIGIN, ICON_ORIGIN, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        if (category == null) {
            return;
        }
        Optional<Holder.Reference<IResearchCategory>> resolved = level.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).get(category);
        if (resolved.isEmpty()) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().scale(CATEGORY_SCALE, CATEGORY_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, resolved.get().value().icon(), ICON_ORIGIN, ICON_ORIGIN, 0, 0, ICON_SIZE, ICON_SIZE, CATEGORY_ICON_TEX, CATEGORY_ICON_TEX, CATEGORY_ICON_TEX,
                CATEGORY_ICON_TEX);
        graphics.pose().popMatrix();
    }
}

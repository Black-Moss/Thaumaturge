package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.blockentity.DeconstructionTableRenderer;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class DeconTableItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final int BOOK_SEED = 0;

    private final DeconTableModel model;

    public DeconTableItemSpecialRenderer(DeconTableModel model) {
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        DeconstructionTableRenderer.submitTable(model, poseStack, collector, lightCoords);
        submitBook(poseStack, collector, lightCoords);
    }

    private static void submitBook(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStackRenderState bookState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(bookState, new ItemStack(TTItems.THAUMOMETER.get()), ItemDisplayContext.FIXED, minecraft.level, null, BOOK_SEED);
        DeconstructionTableRenderer.submitThaumometer(bookState, poseStack, collector, lightCoords);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new DeconTableItemSpecialRenderer(new DeconTableModel(context.entityModelSet().bakeLayer(TTModelLayers.DECONSTRUCTION_TABLE)));
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

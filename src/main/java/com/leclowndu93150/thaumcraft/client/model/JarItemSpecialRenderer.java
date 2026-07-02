package com.leclowndu93150.thaumcraft.client.model;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.client.render.blockentity.JarRenderer;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;


public class JarItemSpecialRenderer implements SpecialModelRenderer<AspectInstance> {

    private final BakingContext context;

    public JarItemSpecialRenderer(BakingContext context) {
        this.context = context;
    }

    @Override
    public void submit(@Nullable AspectInstance aspectInstance, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int i1, boolean b, int i2) {
        if (aspectInstance == null) return;
        // Render the essentia inside the jar using the sprite and aspectInstance
        JarRenderer.submitFluid(aspectInstance.amount(), aspectInstance.aspect().value().color(), lightCoords,context.sprites().get(JarRenderer.ANIMATED_GLOW_SPRITE),poseStack,submitNodeCollector);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        AbstractEndPortalRenderer.getExtents(consumer);
    }

    @Override
    public @Nullable AspectInstance extractArgument(ItemStack itemStack) {
        var essentiaList = itemStack.get(TCDataComponents.ESSENTIA_CONTENTS.get());
        if (essentiaList == null || essentiaList.isEmpty()) return null;
        return essentiaList.contents().entries().getFirst();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<AspectInstance> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<AspectInstance> bake(BakingContext bakingContext) {
            return new JarItemSpecialRenderer(bakingContext);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<AspectInstance>> type() {
            return MAP_CODEC;
        }
    }
}

package com.leclowndu93150.thaumaturge.mixin.client.gui;

import com.leclowndu93150.thaumaturge.client.item.ItemTintGraphics;
import com.leclowndu93150.thaumaturge.client.item.TintedGuiItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements ItemTintGraphics {
    @Unique
    private int thaumaturge$itemTint = TintedGuiItems.NO_TINT;

    @Override
    public int thaumaturge$itemTint() {
        return thaumaturge$itemTint;
    }

    @Override
    public void thaumaturge$setItemTint(int tint) {
        this.thaumaturge$itemTint = tint;
    }

    @WrapOperation(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void thaumaturge$tintItem(ItemModelResolver resolver, ItemStackRenderState state, ItemStack stack, ItemDisplayContext context, Level level, ItemOwner owner, int seed, Operation<Void> original) {
        original.call(resolver, state, stack, context, level, owner, seed);
        if (thaumaturge$itemTint != TintedGuiItems.NO_TINT) {
            TintedGuiItems.apply(state, thaumaturge$itemTint);
        }
    }
}

package com.leclowndu93150.thaumcraft.client.color;

import com.leclowndu93150.thaumcraft.content.casters.ItemFocus;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record FocusColorTint() implements ItemTintSource {
    public static final MapCodec<FocusColorTint> MAP_CODEC = MapCodec.unit(new FocusColorTint());
    private static final int OPAQUE = 0xFF000000;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return OPAQUE | ItemFocus.getFocusColor(stack);
    }

    @Override
    public MapCodec<FocusColorTint> type() {
        return MAP_CODEC;
    }
}

package com.leclowndu93150.thaumcraft.client.color;

import com.leclowndu93150.thaumcraft.content.golem.GolemProperties;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record GolemMaterialTint() implements ItemTintSource {
    public static final MapCodec<GolemMaterialTint> MAP_CODEC = MapCodec.unit(new GolemMaterialTint());
    private static final int OPAQUE = 0xFF000000;
    private static final int DEFAULT = 0xFFFFFFFF;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        GolemProperties props = stack.get(TCDataComponents.GOLEM_PROPERTIES.get());
        if (props == null) {
            return DEFAULT;
        }
        return OPAQUE | props.getMaterial().itemColor();
    }

    @Override
    public MapCodec<GolemMaterialTint> type() {
        return MAP_CODEC;
    }
}

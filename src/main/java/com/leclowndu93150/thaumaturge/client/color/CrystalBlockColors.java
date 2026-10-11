package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CrystalBlockColors {
    private static final int FALLBACK = 0xFFFFFF;

    private CrystalBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        register(event, TTBlocks.CRYSTAL_AER.get());
        register(event, TTBlocks.CRYSTAL_IGNIS.get());
        register(event, TTBlocks.CRYSTAL_AQUA.get());
        register(event, TTBlocks.CRYSTAL_TERRA.get());
        register(event, TTBlocks.CRYSTAL_ORDO.get());
        register(event, TTBlocks.CRYSTAL_PERDITIO.get());
        register(event, TTBlocks.CRYSTAL_VITIUM.get());
    }

    private static void register(RegisterColorHandlersEvent.BlockTintSources event, BlockCrystal block) {
        ResourceKey<IAspect> aspect = block.aspect();
        BlockTintSource source = state -> aspectColor(aspect);
        event.register(List.of(source), block);
    }

    private static int aspectColor(@Nullable ResourceKey<IAspect> aspect) {
        Holder<IAspect> holder = aspect == null ? null : Aspects.resolve(Minecraft.getInstance().level, aspect);
        return ARGB.opaque(holder == null ? FALLBACK : holder.value().color());
    }
}

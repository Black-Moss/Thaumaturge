package com.leclowndu93150.thaumcraft.client.color;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksBCrystals;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CrystalBlockColors {
    private static final int AER = 0xFFFF7E;
    private static final int IGNIS = 0xFF5A01;
    private static final int AQUA = 0x3CD4FC;
    private static final int TERRA = 0x56C000;
    private static final int ORDO = 0xD5D4EC;
    private static final int PERDITIO = 0x404040;
    private static final int VITIUM = 0x800080;

    private CrystalBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        register(event, TCBlocksBCrystals.CRYSTAL_AER.get(), AER);
        register(event, TCBlocksBCrystals.CRYSTAL_IGNIS.get(), IGNIS);
        register(event, TCBlocksBCrystals.CRYSTAL_AQUA.get(), AQUA);
        register(event, TCBlocksBCrystals.CRYSTAL_TERRA.get(), TERRA);
        register(event, TCBlocksBCrystals.CRYSTAL_ORDO.get(), ORDO);
        register(event, TCBlocksBCrystals.CRYSTAL_PERDITIO.get(), PERDITIO);
        register(event, TCBlocksBCrystals.CRYSTAL_VITIUM.get(), VITIUM);
    }

    private static void register(RegisterColorHandlersEvent.BlockTintSources event, Block block, int rgb) {
        int argb = ARGB.opaque(rgb);
        BlockTintSource source = state -> argb;
        event.register(List.of(source), block);
    }
}

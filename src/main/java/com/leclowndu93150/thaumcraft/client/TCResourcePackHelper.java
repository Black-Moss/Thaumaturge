package com.leclowndu93150.thaumcraft.client;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCResourcePackHelper {

    @SubscribeEvent
    public static void onPackFinderRegister(AddPackFindersEvent event){
        event.addPackFinders(
                TCIds.rl("programmer_art"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("resourcePack.thaumcraft.programmer_art.name"),
                PackSource.FEATURE,
                false,
                Pack.Position.BOTTOM
        );
    }

}

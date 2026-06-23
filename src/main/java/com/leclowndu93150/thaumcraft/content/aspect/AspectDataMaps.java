package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class AspectDataMaps {
    public static final DataMapType<Item, AspectList> BASE_ASPECTS = DataMapType.builder(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "base_aspects"),
            Registries.ITEM,
            AspectList.CODEC
    ).synced(AspectList.CODEC, false).build();

    private AspectDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(BASE_ASPECTS);
    }
}

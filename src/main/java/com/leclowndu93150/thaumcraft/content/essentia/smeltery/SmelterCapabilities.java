package com.leclowndu93150.thaumcraft.content.essentia.smeltery;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

@EventBusSubscriber(modid = TCIds.MODID)
public class SmelterCapabilities {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.SMELTER.get(),
                (be, side) -> {
                    if (side == null) return be.getInventory();
                    else if (side == Direction.UP || be.getBlockState().getValue(BlockSmelter.FACING).getAxis().equals(side.getAxis())) return RangedResourceHandler.ofSingleIndex(be.getInventory(),0);
                    return RangedResourceHandler.ofSingleIndex(be.getInventory(),1);
                }
        );

        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.ALEMBIC.get(),
                (be, side) -> be
        );

        event.registerBlockEntity(
                AspectCapabilities.CONTAINER,
                TCBlockEntities.ALEMBIC.get(),
                (be, side) -> be
        );

    }
}

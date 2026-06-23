package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.item.equipment.GogglesItem;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsMAuraHud {
    public static final int GOGGLES_DURABILITY = 350;

    public static final ResourceKey<EquipmentAsset> GOGGLES_REVEALING_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, TCIds.rl("goggles_revealing"));

    public static final DeferredItem<GogglesItem> GOGGLES_REVEALING = TCItems.ITEMS.registerItem(
            "goggles_revealing",
            GogglesItem::new,
            props -> props
                    .stacksTo(1)
                    .durability(GOGGLES_DURABILITY)
                    .rarity(Rarity.RARE)
                    .component(
                            DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.HEAD)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                                    .setAsset(GOGGLES_REVEALING_ASSET)
                                    .build()
                    )
    );

    private TCItemsMAuraHud() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}

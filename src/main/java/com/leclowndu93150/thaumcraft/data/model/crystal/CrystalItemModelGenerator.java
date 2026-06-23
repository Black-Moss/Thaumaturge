package com.leclowndu93150.thaumcraft.data.model.crystal;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsBCrystals;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class CrystalItemModelGenerator {
    private static final Identifier PLANTER_TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/crystal_planter");

    private CrystalItemModelGenerator() {}

    public static void register(ItemModelGenerators itemModels) {
        emit(itemModels, TCItemsBCrystals.CRYSTAL_AER.get(), 0xFFFF7E);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_IGNIS.get(), 0xFF5A01);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_AQUA.get(), 0x3CD4FC);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_TERRA.get(), 0x56C000);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_ORDO.get(), 0xD5D4EC);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_PERDITIO.get(), 0x404040);
        emit(itemModels, TCItemsBCrystals.CRYSTAL_VITIUM.get(), 0x800080);
    }

    private static void emit(ItemModelGenerators itemModels, Item item, int color) {
        Identifier model = ModelLocationUtils.getModelLocation(item);
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(PLANTER_TEXTURE)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new Dye(color | 0xFF000000)));
    }
}

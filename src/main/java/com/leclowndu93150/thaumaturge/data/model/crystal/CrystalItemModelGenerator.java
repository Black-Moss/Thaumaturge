package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
    private static final Identifier PLANTER_TEXTURE = TTIds.rl("item/crystal_planter");
    private static final int OPAQUE = 0xFF000000;
    private static final int TINT_AER = 0xFFFF7E;
    private static final int TINT_IGNIS = 0xFF5A01;
    private static final int TINT_AQUA = 0x3CD4FC;
    private static final int TINT_TERRA = 0x56C000;
    private static final int TINT_ORDO = 0xD5D4EC;
    private static final int TINT_PERDITIO = 0x404040;
    private static final int TINT_VITIUM = 0x800080;

    private CrystalItemModelGenerator() {}

    public static void register(ItemModelGenerators itemModels) {
        emit(itemModels, TTBlocks.CRYSTAL_AER.asItem(), TINT_AER);
        emit(itemModels, TTBlocks.CRYSTAL_IGNIS.asItem(), TINT_IGNIS);
        emit(itemModels, TTBlocks.CRYSTAL_AQUA.asItem(), TINT_AQUA);
        emit(itemModels, TTBlocks.CRYSTAL_TERRA.asItem(), TINT_TERRA);
        emit(itemModels, TTBlocks.CRYSTAL_ORDO.asItem(), TINT_ORDO);
        emit(itemModels, TTBlocks.CRYSTAL_PERDITIO.asItem(), TINT_PERDITIO);
        emit(itemModels, TTBlocks.CRYSTAL_VITIUM.asItem(), TINT_VITIUM);
    }

    private static void emit(ItemModelGenerators itemModels, Item item, int color) {
        Identifier model = ModelLocationUtils.getModelLocation(item);
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(PLANTER_TEXTURE)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new Dye(color | OPAQUE)));
    }
}

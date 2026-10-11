package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class CrystalItemModelGenerator {
    private static final Identifier PLANTER_TEXTURE = TTIds.rl("item/crystal_planter");
    private static final int OPAQUE = 0xFF000000;
    private static final List<DeferredBlock<BlockCrystal>> PLANTED_CRYSTALS = List.of(TTBlocks.CRYSTAL_AER, TTBlocks.CRYSTAL_TERRA, TTBlocks.CRYSTAL_IGNIS, TTBlocks.CRYSTAL_AQUA,
            TTBlocks.CRYSTAL_ORDO, TTBlocks.CRYSTAL_PERDITIO, TTBlocks.CRYSTAL_VITIUM);

    private CrystalItemModelGenerator() {}

    public static void register(ItemModelGenerators itemModels, HolderGetter<IAspect> aspects) {
        for (DeferredBlock<BlockCrystal> crystal : PLANTED_CRYSTALS) {
            ResourceKey<IAspect> aspect = crystal.get().aspect();
            if (aspect == null) {
                throw new IllegalStateException("Crystal " + crystal.getId() + " has no aspect to take its tint from");
            }
            emit(itemModels, crystal.asItem(), aspects.getOrThrow(aspect).value().color());
        }
    }

    private static void emit(ItemModelGenerators itemModels, Item item, int color) {
        Identifier model = ModelLocationUtils.getModelLocation(item);
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(PLANTER_TEXTURE)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new Dye(color | OPAQUE)));
    }
}

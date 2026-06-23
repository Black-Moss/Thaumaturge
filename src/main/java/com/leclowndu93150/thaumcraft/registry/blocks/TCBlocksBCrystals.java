package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCSoundTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksBCrystals {
    public static final DeferredBlock<BlockCrystal> CRYSTAL_AER = register("crystal_aer", TCAspects.AER, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_IGNIS = register("crystal_ignis", TCAspects.IGNIS, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_AQUA = register("crystal_aqua", TCAspects.AQUA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_TERRA = register("crystal_terra", TCAspects.TERRA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_ORDO = register("crystal_ordo", TCAspects.ORDO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_PERDITIO = register("crystal_perditio", TCAspects.PERDITIO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_VITIUM = register("crystal_vitium", TCAspects.VITIUM, true);

    private TCBlocksBCrystals() {}

    private static DeferredBlock<BlockCrystal> register(String name, ResourceKey<IAspect> aspect, boolean flux) {
        return TCBlocks.BLOCKS.registerBlock(
                name,
                props -> new BlockCrystal(props, aspect, flux),
                props -> props
                        .mapColor(MapColor.NONE)
                        .strength(0.25F)
                        .sound(TCSoundTypes.CRYSTAL.get())
                        .lightLevel(state -> 1)
                        .noOcclusion()
                        .randomTicks()
                        .pushReaction(PushReaction.DESTROY)
        );
    }

    public static void touch() {}
}

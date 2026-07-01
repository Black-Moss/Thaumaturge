package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangTaint {
    private TCLangTaint() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.flux_goo", "Flux Goo");
        provider.add("fluid_type.thaumcraft.flux_goo", "Flux Goo");

        provider.add("block.thaumcraft.taint_rock", "Tainted Rock");
        provider.add("block.thaumcraft.taint_soil", "Tainted Soil");
        provider.add("block.thaumcraft.taint_crust", "Tainted Crust");
        provider.add("block.thaumcraft.taint_geyser", "Taint Geyser");
        provider.add("block.thaumcraft.taint_log", "Tainted Log");
        provider.add("block.thaumcraft.taint_feature", "Taint Feature");
        provider.add("block.thaumcraft.taint_fibre", "Taint Fibre");

        provider.add("effect.thaumcraft.vis_exhaust", "Vis Exhaust");
        provider.add("effect.thaumcraft.infectious_vis_exhaust", "Infectious Vis Exhaust");
        provider.add("effect.thaumcraft.flux_taint", "Flux Taint");

        provider.add("death.attack.thaumcraft.taint", "%1$s was tainted");
        provider.add("death.attack.thaumcraft.tentacle", "%1$s was strangled by tentacles");
        provider.add("death.attack.thaumcraft.swarm", "%1$s was swarmed");
        provider.add("death.attack.thaumcraft.dissolve", "%1$s dissolved");

        provider.add("item.thaumcraft.essentia_crystal", "Crystal of %s");
        provider.add("item.thaumcraft.essentia_crystal.unknown", "Crystal of Unknown");

        provider.add("entity.thaumcraft.thaumic_slime", "Thaumic Slime");
        provider.add("entity.thaumcraft.taint_seed", "Taint Seed");
        provider.add("entity.thaumcraft.taint_seed_prime", "Greater Taint Seed");
        provider.add("entity.thaumcraft.taint_crawler", "Taint Crawler");
        provider.add("entity.thaumcraft.taint_swarm", "Taint Swarm");
        provider.add("entity.thaumcraft.taintacle", "Taintacle");
        provider.add("entity.thaumcraft.taintacle_small", "Lesser Taintacle");
        provider.add("entity.thaumcraft.falling_taint", "Falling Taint");
        provider.add("entity.thaumcraft.bottle_taint", "Bottle of Tainted Goo");
    }
}

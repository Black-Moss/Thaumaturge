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
        provider.add("effect.thaumcraft.infectious_vis_exhaust", "Flux Phage");
        provider.add("effect.thaumcraft.flux_taint", "Flux Taint");

        provider.add("death.attack.thaumcraft.taint", "%1$s was tainted");
        provider.add("death.attack.thaumcraft.tentacle", "%1$s was strangled by tentacles");
        provider.add("death.attack.thaumcraft.swarm", "%1$s was swarmed");
        provider.add("death.attack.thaumcraft.dissolve", "%1$s dissolved");

        provider.add("item.thaumcraft.essentia_crystal", "%s Vis Crystal");
        provider.add("item.thaumcraft.essentia_crystal.unknown", "Unknown Vis Crystal");

        provider.add("entity.thaumcraft.thaumic_slime", "Thaumic Slime");
        provider.add("entity.thaumcraft.taint_seed", "Taint Seed");
        provider.add("entity.thaumcraft.taint_seed_prime", "Greater Taint Seed");
        provider.add("entity.thaumcraft.taint_crawler", "Taint Crawler");
        provider.add("entity.thaumcraft.taint_swarm", "Taint Swarm");
        provider.add("entity.thaumcraft.taintacle", "Taintacle");
        provider.add("entity.thaumcraft.taintacle_small", "Lesser Taintacle");
        provider.add("entity.thaumcraft.falling_taint", "Falling Taint");
        provider.add("entity.thaumcraft.bottle_taint", "Bottle of Tainted Goo");
        provider.add("entity.thaumcraft.wisp", "Wisp");
        provider.add("entity.thaumcraft.brainy_zombie", "Angry Zombie");
        provider.add("entity.thaumcraft.giant_brainy_zombie", "Furious Zombie");
        provider.add("entity.thaumcraft.firebat", "Firebat");
        provider.add("entity.thaumcraft.mind_spider", "Mind Spider");
        provider.add("item.thaumcraft.brain", "Zombie Brain");

        provider.add("item.thaumcraft.brainy_zombie_spawn_egg", "Angry Zombie Spawn Egg");
        provider.add("item.thaumcraft.giant_brainy_zombie_spawn_egg", "Furious Zombie Spawn Egg");
        provider.add("item.thaumcraft.firebat_spawn_egg", "Firebat Spawn Egg");
        provider.add("item.thaumcraft.mind_spider_spawn_egg", "Mind Spider Spawn Egg");
        provider.add("item.thaumcraft.wisp_spawn_egg", "Wisp Spawn Egg");
        provider.add("item.thaumcraft.thaumic_slime_spawn_egg", "Thaumic Slime Spawn Egg");
        provider.add("item.thaumcraft.taint_crawler_spawn_egg", "Taint Crawler Spawn Egg");
        provider.add("item.thaumcraft.taintacle_spawn_egg", "Taintacle Spawn Egg");
        provider.add("item.thaumcraft.taint_swarm_spawn_egg", "Taint Swarm Spawn Egg");
        provider.add("item.thaumcraft.taint_seed_spawn_egg", "Taint Seed Spawn Egg");
        provider.add("item.thaumcraft.taint_seed_prime_spawn_egg", "Greater Taint Seed Spawn Egg");
    }
}

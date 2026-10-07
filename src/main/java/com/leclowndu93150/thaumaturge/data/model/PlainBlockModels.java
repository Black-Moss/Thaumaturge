package com.leclowndu93150.thaumaturge.data.model;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

final class PlainBlockModels {
    private PlainBlockModels() {}

    static void generate(BiConsumer<Identifier, ModelInstance> output) {
        model(output, "ancient_inner_stairs", Optional.of(Identifier.withDefaultNamespace("block/inner_stairs")),
                Map.of("bottom", TTIds.rl("block/ancient_stone_1"), "top", TTIds.rl("block/ancient_stone_0"), "side", TTIds.rl("block/ancient_stone_3")));
        model(output, "ancient_outer_stairs", Optional.of(Identifier.withDefaultNamespace("block/outer_stairs")),
                Map.of("bottom", TTIds.rl("block/ancient_stone_1"), "top", TTIds.rl("block/ancient_stone_0"), "side", TTIds.rl("block/ancient_stone_3")));
        model(output, "ancient_stairs", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/ancient_stone_1"), "top", TTIds.rl("block/ancient_stone_0"), "side", TTIds.rl("block/ancient_stone_3")));
        model(output, "arcane_brick_inner_stairs", Optional.of(Identifier.withDefaultNamespace("block/inner_stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_brick_stone"), "top", TTIds.rl("block/arcane_brick_stone"), "side", TTIds.rl("block/arcane_brick_stone")));
        model(output, "arcane_brick_outer_stairs", Optional.of(Identifier.withDefaultNamespace("block/outer_stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_brick_stone"), "top", TTIds.rl("block/arcane_brick_stone"), "side", TTIds.rl("block/arcane_brick_stone")));
        model(output, "arcane_brick_stairs", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_brick_stone"), "top", TTIds.rl("block/arcane_brick_stone"), "side", TTIds.rl("block/arcane_brick_stone")));
        model(output, "arcane_inner_stairs", Optional.of(Identifier.withDefaultNamespace("block/inner_stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_stone_1"), "top", TTIds.rl("block/arcane_stone_1"), "side", TTIds.rl("block/arcane_stone_3")));
        model(output, "arcane_outer_stairs", Optional.of(Identifier.withDefaultNamespace("block/outer_stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_stone_1"), "top", TTIds.rl("block/arcane_stone_1"), "side", TTIds.rl("block/arcane_stone_3")));
        model(output, "arcane_stairs", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_stone_1"), "top", TTIds.rl("block/arcane_stone_1"), "side", TTIds.rl("block/arcane_stone_3")));
        model(output, "flux_goo", Optional.empty(), Map.of("particle", TTIds.rl("block/flux_goo")));
        model(output, "leaves_greatwood", Optional.of(Identifier.withDefaultNamespace("block/leaves")), Map.of("all", TTIds.rl("block/leaves_greatwood")));
        model(output, "leaves_silverwood", Optional.of(Identifier.withDefaultNamespace("block/leaves")), Map.of("all", TTIds.rl("block/leaves_silverwood")));
        model(output, "levitator_off", Optional.of(TTIds.rl("block/levitator_on")), Map.of("particle", TTIds.rl("block/levitator_top_off"), "top", TTIds.rl("block/levitator_top_off"), "side",
                TTIds.rl("block/levitator_side_off"), "bottom", TTIds.rl("block/levitator_bottom")));
        model(output, "liquid_death", Optional.empty(), Map.of("particle", TTIds.rl("block/animatedglow")));
        model(output, "loot_crate_rare", Optional.of(TTIds.rl("block/loot_crate_common")), Map.of("side", TTIds.rl("block/loot_crate_side_2"), "particle", TTIds.rl("block/loot_crate_side_2")));
        model(output, "loot_crate_uncommon", Optional.of(TTIds.rl("block/loot_crate_common")), Map.of("side", TTIds.rl("block/loot_crate_side_1"), "particle", TTIds.rl("block/loot_crate_side_1")));
        model(output, "loot_urn_rare", Optional.of(TTIds.rl("block/loot_urn_common")), Map.of("side", TTIds.rl("block/loot_urn_side_2"), "particle", TTIds.rl("block/loot_urn_side_2")));
        model(output, "loot_urn_uncommon", Optional.of(TTIds.rl("block/loot_urn_common")), Map.of("side", TTIds.rl("block/loot_urn_side_1"), "particle", TTIds.rl("block/loot_urn_side_1")));
        model(output, "pedestal_ancient", Optional.of(TTIds.rl("block/pedestal_eldritch")),
                Map.of("side", TTIds.rl("block/pedestal_ancient_side"), "top", TTIds.rl("block/pedestal_ancient_top"), "particle", TTIds.rl("block/pedestal_ancient_side")));
        model(output, "plank_greatwood", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/plank_greatwood")));
        model(output, "plank_silverwood", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/plank_silverwood")));
        model(output, "purifying_fluid", Optional.empty(), Map.of("particle", Identifier.withDefaultNamespace("block/water_still")));
        model(output, "sapling_greatwood", Optional.of(Identifier.withDefaultNamespace("block/cross")), Map.of("cross", TTIds.rl("block/sapling_greatwood")));
        model(output, "sapling_silverwood", Optional.of(Identifier.withDefaultNamespace("block/cross")), Map.of("cross", TTIds.rl("block/sapling_silverwood")));
        model(output, "smelter_basic_off", Optional.of(Identifier.withDefaultNamespace("block/cube")),
                Map.of("north", TTIds.rl("block/smelter_basic_front_off"), "south", TTIds.rl("block/smelter_basic_side"), "east", TTIds.rl("block/smelter_basic_side"), "west",
                        TTIds.rl("block/smelter_basic_side"), "particle", TTIds.rl("block/smelter_basic_side"), "up", TTIds.rl("block/smelter_top"), "down",
                        Identifier.withDefaultNamespace("block/furnace_top")));
        model(output, "smelter_basic_on", Optional.of(TTIds.rl("block/smelter_basic_off")), Map.of("north", TTIds.rl("block/smelter_basic_front_on")));
        model(output, "smelter_thaumium_off", Optional.of(TTIds.rl("block/smelter_basic_off")),
                Map.of("north", TTIds.rl("block/smelter_thaumium_front_off"), "south", TTIds.rl("block/smelter_thaumium_side"), "east", TTIds.rl("block/smelter_thaumium_side"), "west",
                        TTIds.rl("block/smelter_thaumium_side"), "particle", TTIds.rl("block/smelter_thaumium_side")));
        model(output, "smelter_thaumium_on", Optional.of(TTIds.rl("block/smelter_thaumium_off")), Map.of("north", TTIds.rl("block/smelter_thaumium_front_on")));
        model(output, "smelter_void_off", Optional.of(TTIds.rl("block/smelter_basic_off")), Map.of("north", TTIds.rl("block/smelter_void_front_off"), "south", TTIds.rl("block/smelter_void_side"),
                "east", TTIds.rl("block/smelter_void_side"), "west", TTIds.rl("block/smelter_void_side"), "particle", TTIds.rl("block/smelter_void_side")));
        model(output, "smelter_void_on", Optional.of(TTIds.rl("block/smelter_void_off")), Map.of("north", TTIds.rl("block/smelter_void_front_on")));
        model(output, "stairs_ancient", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/ancient_stone_1"), "top", TTIds.rl("block/ancient_stone_0"), "side", TTIds.rl("block/ancient_stone_3")));
        model(output, "stairs_arcane", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_stone_1"), "top", TTIds.rl("block/arcane_stone_1"), "side", TTIds.rl("block/arcane_stone_3")));
        model(output, "stairs_arcane_brick", Optional.of(Identifier.withDefaultNamespace("block/stairs")),
                Map.of("bottom", TTIds.rl("block/arcane_brick_stone"), "top", TTIds.rl("block/arcane_brick_stone"), "side", TTIds.rl("block/arcane_brick_stone")));
        model(output, "stone_ancient", Optional.of(Identifier.withDefaultNamespace("block/cube")),
                Map.of("particle", TTIds.rl("block/ancient_stone_0"), "up", TTIds.rl("block/ancient_stone_0"), "down", TTIds.rl("block/ancient_stone_1"), "east", TTIds.rl("block/ancient_stone_2"),
                        "west", TTIds.rl("block/ancient_stone_3"), "north", TTIds.rl("block/ancient_stone_4"), "south", TTIds.rl("block/ancient_stone_5")));
        model(output, "stone_ancient_doorway", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/eldritch_door")));
        model(output, "stone_ancient_glyphed", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/ancient_glyph")));
        model(output, "stone_ancient_rock", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/ancient_rock_stone_2")));
        model(output, "stone_ancient_tile", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/ancient_tile")));
        model(output, "stone_arcane", Optional.of(Identifier.withDefaultNamespace("block/cube")),
                Map.of("particle", TTIds.rl("block/arcane_stone_1"), "up", TTIds.rl("block/arcane_stone_1"), "down", TTIds.rl("block/arcane_stone_1"), "east", TTIds.rl("block/arcane_stone_2"), "west",
                        TTIds.rl("block/arcane_stone_2"), "north", TTIds.rl("block/arcane_stone_3"), "south", TTIds.rl("block/arcane_stone_3")));
        model(output, "stone_arcane_brick", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/arcane_brick_stone")));
        model(output, "stone_eldritch_tile", Optional.of(Identifier.withDefaultNamespace("block/cube")),
                Map.of("particle", TTIds.rl("block/eldritch_stone_1"), "up", TTIds.rl("block/eldritch_stone_1"), "down", TTIds.rl("block/eldritch_stone_1"), "east", TTIds.rl("block/eldritch_stone_2"),
                        "west", TTIds.rl("block/eldritch_stone_2"), "north", TTIds.rl("block/eldritch_stone_3"), "south", TTIds.rl("block/eldritch_stone_3")));
        model(output, "stone_porous", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/porous_stone")));
        model(output, "table_stone", Optional.of(TTIds.rl("block/table_wood")),
                Map.of("particle", TTIds.rl("block/table_stone_top"), "top", TTIds.rl("block/table_stone_top"), "side", TTIds.rl("block/table_stone_side")));
        model(output, "taint_crust_0", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_crust_0")));
        model(output, "taint_crust_1", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_crust_1")));
        model(output, "taint_crust_2", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_crust_2")));
        model(output, "taint_geyser", Optional.of(Identifier.withDefaultNamespace("block/cube_bottom_top")),
                Map.of("side", TTIds.rl("block/taint_geyser_side"), "top", TTIds.rl("block/taint_geyser_top"), "bottom", TTIds.rl("block/taint_log_top")));
        model(output, "taint_growth_1", Optional.of(Identifier.withDefaultNamespace("block/cross")), Map.of("cross", TTIds.rl("block/taint_growth_1")));
        model(output, "taint_growth_2", Optional.of(Identifier.withDefaultNamespace("block/cross")), Map.of("cross", TTIds.rl("block/taint_growth_2")));
        model(output, "taint_growth_4", Optional.of(Identifier.withDefaultNamespace("block/cross")), Map.of("cross", TTIds.rl("block/taint_growth_4")));
        model(output, "taint_log", Optional.of(Identifier.withDefaultNamespace("block/cube")),
                Map.of("particle", TTIds.rl("block/taint_log_top"), "up", TTIds.rl("block/taint_log_top"), "down", TTIds.rl("block/taint_log_top"), "north", TTIds.rl("block/taint_log_0"), "south",
                        TTIds.rl("block/taint_log_0"), "east", TTIds.rl("block/taint_log_0"), "west", TTIds.rl("block/taint_log_0")));
        model(output, "taint_log_east1", Optional.of(TTIds.rl("block/taint_log")), Map.of("east", TTIds.rl("block/taint_log_1")));
        model(output, "taint_log_east2", Optional.of(TTIds.rl("block/taint_log")), Map.of("east", TTIds.rl("block/taint_log_2")));
        model(output, "taint_log_north1", Optional.of(TTIds.rl("block/taint_log")), Map.of("north", TTIds.rl("block/taint_log_1")));
        model(output, "taint_log_north2", Optional.of(TTIds.rl("block/taint_log")), Map.of("north", TTIds.rl("block/taint_log_2")));
        model(output, "taint_log_south1", Optional.of(TTIds.rl("block/taint_log")), Map.of("south", TTIds.rl("block/taint_log_1")));
        model(output, "taint_log_south2", Optional.of(TTIds.rl("block/taint_log")), Map.of("south", TTIds.rl("block/taint_log_2")));
        model(output, "taint_log_west1", Optional.of(TTIds.rl("block/taint_log")), Map.of("west", TTIds.rl("block/taint_log_1")));
        model(output, "taint_log_west2", Optional.of(TTIds.rl("block/taint_log")), Map.of("west", TTIds.rl("block/taint_log_2")));
        model(output, "taint_rock", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_rock")));
        model(output, "taint_soil_0", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_soil_0")));
        model(output, "taint_soil_1", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_soil_1")));
        model(output, "taint_soil_2", Optional.of(Identifier.withDefaultNamespace("block/cube_all")), Map.of("all", TTIds.rl("block/taint_soil_2")));
        model(output, "tc_banner", Optional.empty(), Map.of("particle", Identifier.withDefaultNamespace("block/oak_planks")));
    }

    private static void model(BiConsumer<Identifier, ModelInstance> output, String name, Optional<Identifier> parent, Map<String, Identifier> textures) {
        TextureMapping mapping = new TextureMapping();
        TextureSlot[] slots = new TextureSlot[textures.size()];
        int index = 0;
        for (Map.Entry<String, Identifier> texture : textures.entrySet()) {
            TextureSlot slot = TextureSlot.create(texture.getKey());
            mapping.put(slot, new Material(texture.getValue()));
            slots[index++] = slot;
        }
        new ModelTemplate(parent, Optional.empty(), slots).create(TTIds.rl("block/" + name), mapping, output);
    }
}

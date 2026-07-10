package com.leclowndu93150.thaumcraft.data.lang;

import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.StringUtils;
import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCEnglishProvider extends LanguageProvider {
    public TCEnglishProvider(PackOutput output) {
        super(output, TCIds.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.thaumcraft", "Thaumcraft");

        aspect("aer", "Aer", "Air");
        aspect("terra", "Terra", "Earth");
        aspect("ignis", "Ignis", "Fire");
        aspect("aqua", "Aqua", "Water");
        aspect("ordo", "Ordo", "Order");
        aspect("perditio", "Perditio", "Entropy");

        aspect("vacuos", "Vacuos", "Void");
        aspect("lux", "Lux", "Light");
        aspect("motus", "Motus", "Motion");
        aspect("gelum", "Gelum", "Cold");
        aspect("vitreus", "Vitreus", "Crystal");
        aspect("metallum", "Metallum", "Metal");
        aspect("victus", "Victus", "Life");
        aspect("mortuus", "Mortuus", "Death");
        aspect("potentia", "Potentia", "Energy");
        aspect("permutatio", "Permutatio", "Exchange");
        aspect("praecantatio", "Praecantatio", "Magic");
        aspect("auram", "Auram", "Aura");
        aspect("alkimia", "Alkimia", "Alchemy");
        aspect("vitium", "Vitium", "Flux");
        aspect("tenebrae", "Tenebrae", "Darkness");
        aspect("alienis", "Alienis", "Eldritch");
        aspect("volatus", "Volatus", "Flight");
        aspect("herba", "Herba", "Plant");
        aspect("instrumentum", "Instrumentum", "Tool");
        aspect("fabrico", "Fabrico", "Craft");
        aspect("machina", "Machina", "Mechanism");
        aspect("vinculum", "Vinculum", "Trap");
        aspect("spiritus", "Spiritus", "Soul");
        aspect("cognitio", "Cognitio", "Mind");
        aspect("sensus", "Sensus", "Senses");
        aspect("aversio", "Aversio", "Aversion");
        aspect("praemunio", "Praemunio", "Protect");
        aspect("desiderium", "Desiderium", "Desire");
        aspect("exanimis", "Exanimis", "Undead");
        aspect("bestia", "Bestia", "Beast");
        aspect("humanus", "Humanus", "Man");

        researchCategory("basics", "Thaumaturgy");
        researchCategory("auromancy", "Auromancy");
        researchCategory("alchemy", "Alchemy");
        researchCategory("artifice", "Artifice");
        researchCategory("infusion", "Infusion");
        researchCategory("golemancy", "Golemancy");
        researchCategory("eldritch", "Eldritch");

        card("study", "Study: %s",
                "You have spent some time studying %s and gained a small but real insight into the subject.");
        card("analyze", "Analyze: %s",
                "By spending an observation you have made about %s you have gained additional insight into the subject as well as a small bonus to %s.");
        card("balance", "Balance",
                "By rethinking your conclusions you redistribute your findings evenly amongst all known categories. Your progress in any one category will never exceed the average. A small bonus is added to Thaumaturgy.");
        card("ponder", "Ponder",
                "After some careful pondering you make small but steady progress in all available categories. A small bonus is added to Thaumaturgy and you may draw one bonus card next round.");
        card("inspired", "Inspired",
                "Inspiration strikes! You gain %1$s additional progress in %2$s.");
        card("notation", "Notation",
                "By spending the smaller of two related insights you can make significant progress in another. Moves the points from %1$s into %2$s.");
        card("rethink", "Rethink",
                "You take a moment to rethink your approach. You remove some progress from your current categories but gain a bonus draw and a small Thaumaturgy bonus.");
        card("reject", "Reject: %s",
                "You decide that %s is not worth pursuing this session. The category is blocked and a small Thaumaturgy bonus is granted.");
        card("experimentation", "Experimentation",
                "Sometimes the best way forward is to simply try things. You gain progress in a random category and a small Thaumaturgy bonus.");

        add("knowledge_type.thaumcraft.theory", "Theory");
        add("knowledge_type.thaumcraft.observation", "Observation");
        add("tc.knowledge.tooltip", "%1$s: %2$s");
        add("tc.research_category.percent", "%1$s (%2$s%%)");
        add("tc.need.obtain", "Items that will be consumed");
        add("tc.need.craft", "Items that need to be crafted");
        add("tc.need.research", "Things that need to be discovered");
        add("tc.need.know", "Knowledge that must be used");
        add("tc.research.stage", "Current stage:");
        add("tc.research.stage.short", "%1$s / %2$s");
        add("tc.research.begin", "You have not yet begun this research");
        add("tc.researchmissing", "Missing required research:");
        add("tc.research.newresearch", "§6Newly discovered research§0");
        add("tc.research.newpage", "§aNew page added§0");
        add("tc.search", "Search");
        add("tc.search.more", "...too many results found. Refine your search");
        add("tile.researchtable.noink.0", "You have run out of ink!");
        add("tile.researchtable.noink.1", "Refill your scribing tools.");
        add("tile.researchtable.nopaper.0", "You have run out of paper!");
        add("tc.card.unknown", "Unknown item");
        add("tc.warp.warn", " Warping level: %n");
        add("tc.forbidden", "§l§5Forbidden knowledge (%n)§0");
        add("tc.forbidden.level.1", "Mostly Harmless");
        add("tc.forbidden.level.2", "Minor");
        add("tc.forbidden.level.3", "Moderate");
        add("tc.forbidden.level.4", "Dangerous");
        add("tc.forbidden.level.5", "Taboo");
        add("tc.inst", "Instability: ");
        add("tc.inst.0", "§1Negligible§0");
        add("tc.inst.1", "§9Minor§0");
        add("tc.inst.2", "§5Moderate§0");
        add("tc.inst.3", "§eHigh§0");
        add("tc.inst.4", "§6Very High§0");
        add("tc.inst.5", "§4Dangerous§0");
        add("tc.type.theory", "Theory");
        add("tc.type.observation", "Observation");
        add("recipe.return", "Back");
        add("recipe.clickthrough", "Click for recipe");
        add("recipe.unknown", "You cannot craft this yet");
        add("recipe.type.workbench", "Workbench");
        add("recipe.type.workbenchshapeless", "Workbench (Shapeless)");
        add("recipe.type.smelting", "Smelting");
        add("recipe.type.arcane", "Arcane Workbench");
        add("recipe.type.arcane.shapeless", "Arcane Workbench (Shapeless)");
        add("recipe.type.crucible", "Crucible");
        add("recipe.type.infusion", "Arcane Infusion");
        add("recipe.type.infusion_enchantment", "Infusion Enchantment");
        add("tooltip.thaumcraft.charge", "Vis: %s / %s");
        add("tooltip.thaumcraft.infusion_stabiliser", "Infusion Stabilizer");
        add("recipe.type.construct", "Mystical Construct");
        add("wandtable.text1", "Vis Cost");
        add("gui.thaumcraft.research_table.title", "Research Table");
        add("gui.thaumcraft.research_table.inspiration", "Inspiration: %s");
        add("gui.thaumcraft.research_table.draw", "Draw");
        add("gui.thaumcraft.research_table.play", "Play");
        add("gui.thaumcraft.research_table.complete", "Complete Research");
        add("gui.thaumcraft.arcane_workbench.vis_available", "%s available");
        add("gui.thaumcraft.arcane_workbench.required_vis", "%s vis");
        add("gui.thaumcraft.arcane_workbench.required_vis_discount", "%s vis (%s%% discount)");
        add("button.thaumcraft.create_theory", "Create Theory");
        add("button.thaumcraft.complete_theory", "Complete Theory");
        add("button.thaumcraft.scrap_theory", "Scrap Theory");
        add("block.thaumcraft.research_table", "Research Table");
        add("block.thaumcraft.arcane_workbench", "Arcane Workbench");
        add("block.thaumcraft.arcane_workbench_charger", "Workbench Charger");
        add("block.thaumcraft.crucible", "Crucible");
        add("block.thaumcraft.infernal_furnace", "Infernal Furnace");
        add("block.thaumcraft.alembic", "Arcane Alembic");
        add("block.thaumcraft.bellows", "Arcane Bellows");
        add("block.thaumcraft.smelter_basic", "Essentia Smeltery");
        add("block.thaumcraft.smelter_thaumium", "Thaumium Essentia Smeltery");
        add("block.thaumcraft.smelter_void", "Void Metal Essentia Smeltery");
        add("block.thaumcraft.smelter_aux", "Auxiliary Slurry Pump");
        add("block.thaumcraft.smelter_vent", "Auxiliary Venting Port");
        add("block.thaumcraft.jar_normal", "Warded Jar");
        add("block.thaumcraft.jar_void", "Void Jar");
        add("item.thaumcraft.thaumonomicon", "Thaumonomicon");
        add("item.thaumcraft.jar_brace", "Brass Lid Brace");
        add("item.thaumcraft.label", "Label");
        add("item.thaumcraft.salis_mundus", "Salis Mundus");
        add("tooltip.thaumcraft.salis_mundus.desc", "Magical dust that triggers transmutations when applied to certain blocks.");
        add("item.thaumcraft.vis_resonator", "Vis Resonator");
        add("item.thaumcraft.fabric", "Enchanted Fabric");
        add("item.thaumcraft.mirrored_glass", "Mirrored Glass");
        add("item.thaumcraft.filter", "Essentia Filter");
        add("item.thaumcraft.mechanism_simple", "Simple Arcane Mechanism");
        add("item.thaumcraft.mechanism_complex", "Complex Arcane Mechanism");
        add("item.thaumcraft.morphic_resonator", "Morphic Resonator");
        add("item.thaumcraft.bath_salts", "Bath Salts");
        add("item.thaumcraft.sanity_soap", "Sanity Soap");
        add("block.thaumcraft.spa", "Arcane Spa");
        add("block.thaumcraft.purifying_fluid", "Purifying Fluid");
        add("fluid_type.thaumcraft.purifying", "Purifying Fluid");
        add("gui.thaumcraft.spa.mix.true", "Mix with ingredient");
        add("gui.thaumcraft.spa.mix.false", "Use just the fluid");
        add("item.thaumcraft.chunk_beef", "Beef Nugget");
        add("item.thaumcraft.chunk_chicken", "Chicken Nugget");
        add("item.thaumcraft.chunk_pork", "Pork Nugget");
        add("item.thaumcraft.chunk_fish", "Fish Nugget");
        add("item.thaumcraft.chunk_rabbit", "Rabbit Nugget");
        add("item.thaumcraft.chunk_mutton", "Mutton Nugget");
        add("item.thaumcraft.triple_meat_treat", "Triple Meat Treat");
        add("attributes.thaumcraft.vis_discount", "Vis Discount");
        add("subtitles.thaumcraft.jar", "Jar clinks");
        add("subtitles.thaumcraft.creak", "Tube creaks");
        add("subtitles.thaumcraft.key", "Filter clicks");
        add("subtitles.thaumcraft.pump", "Essentia pumps");
        add("subtitles.thaumcraft.squeek", "Valve squeaks");
        add("subtitles.thaumcraft.tool", "Tube clicks");
        add("block.thaumcraft.tube", "Essentia Tube");
        add("block.thaumcraft.tube_valve", "Essentia Valve");
        add("block.thaumcraft.tube_restrict", "Restricted Essentia Tube");
        add("block.thaumcraft.tube_filter", "Essentia Filter Tube");
        add("block.thaumcraft.tube_oneway", "One-Way Essentia Tube");
        add("block.thaumcraft.tube_buffer", "Essentia Buffer");

        add("item.thaumcraft.alumentum", "Alumentum");
        add("block.thaumcraft.nitor_white", "White Nitor");
        add("block.thaumcraft.nitor_orange", "Orange Nitor");
        add("block.thaumcraft.nitor_magenta", "Magenta Nitor");
        add("block.thaumcraft.nitor_light_blue", "Light Blue Nitor");
        add("block.thaumcraft.nitor_yellow", "Yellow Nitor");
        add("block.thaumcraft.nitor_lime", "Lime Nitor");
        add("block.thaumcraft.nitor_pink", "Pink Nitor");
        add("block.thaumcraft.nitor_gray", "Gray Nitor");
        add("block.thaumcraft.nitor_light_gray", "Light Gray Nitor");
        add("block.thaumcraft.nitor_cyan", "Cyan Nitor");
        add("block.thaumcraft.nitor_purple", "Purple Nitor");
        add("block.thaumcraft.nitor_blue", "Blue Nitor");
        add("block.thaumcraft.nitor_brown", "Brown Nitor");
        add("block.thaumcraft.nitor_green", "Green Nitor");
        add("block.thaumcraft.nitor_red", "Red Nitor");
        add("block.thaumcraft.nitor_black", "Black Nitor");

        researchEntry("basics_root", "Thaumaturgy",
                "Your first studies of the arcane begin here. Gather scribing paper and start recording your observations.");
        researchEntry("unlock_auromancy", "The Aura",
                "The world is suffused with vis, the raw energy of magic. Tap into it through glowing motes.");
        researchEntry("unlock_alchemy", "Discovering Alchemy",
                "Strange ingredients yield stranger results when combined under the right conditions.");
        researchEntry("unlock_artifice", "Mechanical Aid",
                "Redstone and clever construction can carry your thaumic experiments far beyond mortal hands.");
        researchEntry("unlock_infusion", "Infusion Crafting",
                "Combine alchemy and auromancy to imbue items with new properties through ritual.");
        researchEntry("unlock_golemancy", "Sentient Servants",
                "Shape clay into automatons that perform simple tasks at your bidding.");
        researchEntry("unlock_eldritch", "Eldritch Whispers",
                "Some doors should not be opened. You open them anyway.");

        researchEntry("aura_basics", "Aura Sensing",
                "Use focused crystals to perceive the flow of vis around you.");
        researchEntryStage("vis_taps", "Vis Taps", 0,
                "A simple drain can siphon vis from an aura node into a portable vessel.");
        researchEntryStage("vis_taps", null, 1,
                "Refining the design lets vis pass through a lantern, lighting it with arcane fire.");

        researchEntry("crucible", "Thaumic Crucible",
                "A cauldron heated under the right conditions becomes a vessel for liquid essentia.");
        researchEntry("alembic", "Alembic Distillation",
                "Brewing-stand mechanics can be repurposed to separate essentia into its primal components.");

        researchEntry("arcane_workbench", "Arcane Workbench",
                "Crafting tables modified with thaumic catalysts can produce items mundane benches cannot.");
        researchEntry("wand_crafting", "Wandcraft",
                "A stick capped with metal becomes a conduit for shaped magic.");

        researchEntry("runic_matrix", "Runic Matrix",
                "Carved obsidian acts as the central node in an infusion ritual. Beware the unstable energies.");

        researchEntry("golem_intro", "First Golem",
                "A handful of clay, shaped and bound, becomes a creature that follows simple commands.");

        researchEntry("eldritch_warning", "Eldritch Whispers",
                "The voices you hear are not your own. They are not new, either.");
        add("key.thaumcraft.thaumonomicon", "Open Thaumonomicon");
        add("gui.thaumcraft.entry.advance", "Mark As Read");
        add("tc.stage.complete", "Complete");
        add("tc.stage.hold", "Completing...");
        add("tc.aspect.name", "Aspects of Essentia");
        add("tc.knowledge.name", "Knowledge Totals");
        add("tc.aspect.primal", "Primal Aspect");
        add("tc.aspect.unknown", "Unknown Aspect");
        add("tc.addendumtext", "§oAddendum %1$s§r");

        add("jei.thaumcraft.category.arcane_workbench", "Arcane Workbench");
        add("jei.thaumcraft.arcane_workbench.vis_cost", "Vis Cost");
        add("jei.thaumcraft.category.crucible", "Crucible");
        add("jei.thaumcraft.category.dust_trigger", "Salis Mundus Trigger");
        add("jei.thaumcraft.category.multiblock_dust_trigger", "Multiblock Trigger");
        add("jei.thaumcraft.category.aspect_composition", "Aspect Composition");
        add("jei.thaumcraft.category.aspect_from_stacks", "Aspect from ItemStack");
        add("jei.thaumcraft.dust_trigger.usage", "Right-click the target block with Salis Mundus to trigger this transmutation.");
        add("jei.thaumcraft.dust_trigger.target.tag", "Any block in tag %1$s");
        add("jei.thaumcraft.dust_trigger.target.multiblock", "Right-click any block of the multiblock to trigger this transmutation.");
        add("jei.thaumcraft.research.missing_research", "Missing research: ");
        add("tooltip.thaumcraft.aspects.header", "Aspects:");

        // Resources
        add("block.thaumcraft.ore_amber", "Amber Bearing Stone");
        add("block.thaumcraft.ore_cinnabar", "Cinnabar Ore");
        add("block.thaumcraft.ore_quartz", "Quartz Ore");

        add("block.thaumcraft.alchemical_construct", "Alchemical Construct");
        add("block.thaumcraft.advanced_alchemical_construct", "Advanced Alchemical Construct");

        add("block.thaumcraft.metal_thaumium", "Thaumium Block");
        add("block.thaumcraft.metal_brass", "Alchemical Brass Block");
        add("block.thaumcraft.metal_void", "Void Metal Block");
        add("block.thaumcraft.amber_block", "Amber Block");

        add("item.thaumcraft.rare_earth", "Rare Earth");

        add("item.thaumcraft.quicksilver", "Quicksilver");
        add("item.thaumcraft.amber", "Amber");
        add("item.thaumcraft.ingot_thaumium", "Thaumium Ingot");
        add("item.thaumcraft.ingot_brass", "Alchemical Brass Ingot");
        add("item.thaumcraft.ingot_void", "Void Metal Ingot");

        add("item.thaumcraft.nugget_quartz", "Quartz Sliver");
        add("item.thaumcraft.nugget_quicksilver", "Quicksilver Drop");
        add("item.thaumcraft.nugget_thaumium", "Thaumium Nugget");
        add("item.thaumcraft.nugget_brass", "Alchemical Brass Nugget");
        add("item.thaumcraft.nugget_void", "Void Metal Nugget");

        add("item.thaumcraft.plate_iron", "Iron Plate");
        add("item.thaumcraft.plate_brass", "Alchemical Brass Plate");
        add("item.thaumcraft.plate_thaumium", "Thaumium Plate");
        add("item.thaumcraft.plate_void", "Void Metal Plate");

        add("item.thaumcraft.cluster_iron", "Native Iron Cluster");
        add("item.thaumcraft.cluster_gold", "Native Gold Cluster");
        add("item.thaumcraft.cluster_copper", "Native Copper Cluster");
        add("item.thaumcraft.cluster_silver", "Native Silver Cluster");
        add("item.thaumcraft.cluster_lead", "Native Lead Cluster");
        add("item.thaumcraft.cluster_tin", "Native Tin Cluster");
        add("item.thaumcraft.cluster_cinnabar", "Native Cinnabar Cluster");
        add("item.thaumcraft.cluster_quartz", "Native Quartz Cluster");


        langBCrystals();
        langBaubles();
        langCStone();
        langDTrees();
        langEPlants();
        langMAuraHud();
        langHContainers();
        langTaint();
        langGTools();
        langNScanning();
        langDecor();
        langCasters();
        langEnchantments();
        langGolemancy();
        langEldritch();
        langBosses();
        langConstructs();
        langDecorSweep();
        langOuterLands();
    }

    private void aspect(String tag, String name, String description) {
        add("aspect.thaumcraft." + tag, name);
        add("aspect.thaumcraft." + tag + ".desc", description);
    }

    private void researchCategory(String path, String name) {
        add("research_category.thaumcraft." + path, name);
    }

    private void card(String path, String name, String text) {
        add("card.thaumcraft." + path + ".name", name);
        add("card.thaumcraft." + path + ".text", text);
    }

    private void researchEntry(String path, String title, String stageText) {
        add("research.thaumcraft." + path + ".title", title);
        add("research.thaumcraft." + path + ".stage_0", stageText);
    }

    private void researchEntryStage(String path, String title, int stage, String stageText) {
        if (title != null) {
            add("research.thaumcraft." + path + ".title", title);
        }
        add("research.thaumcraft." + path + ".stage_" + stage, stageText);
    }

    private void langBCrystals() {

        add("block.thaumcraft.crystal_aer", "Air Crystal");
        add("block.thaumcraft.crystal_ignis", "Fire Crystal");
        add("block.thaumcraft.crystal_aqua", "Water Crystal");
        add("block.thaumcraft.crystal_terra", "Earth Crystal");
        add("block.thaumcraft.crystal_ordo", "Order Crystal");
        add("block.thaumcraft.crystal_perditio", "Entropy Crystal");
        add("block.thaumcraft.crystal_vitium", "Flux Crystal");
    
    }

    private void langBaubles() {

        add("item.thaumcraft.amulet_mundane", "Mundane Amulet");
        add("item.thaumcraft.ring_mundane", "Mundane Ring");
        add("item.thaumcraft.girdle_mundane", "Mundane Belt");
        add("item.thaumcraft.ring_apprentice", "Apprentice's Ring");
        add("item.thaumcraft.amulet_fancy", "Fancy Amulet");
        add("item.thaumcraft.ring_fancy", "Fancy Ring");
        add("item.thaumcraft.girdle_fancy", "Fancy Belt");
        add("item.thaumcraft.amulet_vis", "Vis Stone");
        add("item.thaumcraft.amulet_vis_crafted", "Amulet of Vis");
        add("item.thaumcraft.amulet_vis.text", "Recharges armor, baubles and hotbar items.");
        add("item.thaumcraft.charm_undying", "Charm of Undying");
        add("item.thaumcraft.cloud_ring", "Cloudstepper Ring");
        add("item.thaumcraft.curiosity_band", "Headband of Curiosity");
        add("item.thaumcraft.verdant_charm", "Verdant Heart Charm");
        add("item.thaumcraft.verdant_charm.life.text", "Lifegiver");
        add("item.thaumcraft.verdant_charm.sustain.text", "Sustainer");
        add("item.thaumcraft.voidseer_charm", "Voidseer's Pearl");
        add("item.thaumcraft.voidseer_charm.text",
                "You peer into the inky blackness... you think you see something staring back...");
        add("item.thaumcraft.focus_pouch", "Focus Pouch");
        add("item.thaumcraft.sanity_checker", "Sanity Checker");
        add("item.thaumcraft.curio_arcane", "Arcane Curiosity");
        add("item.thaumcraft.curio_preserved", "Preserved Curiosity");
        add("item.thaumcraft.curio_ancient", "Ancient Curiosity");
        add("item.thaumcraft.curio_eldritch", "Eldritch Curiosity");
        add("item.thaumcraft.curio_knowledge", "Illuminating Curiosity");
        add("item.thaumcraft.curio_twisted", "Twisted Curiosity");
        add("item.thaumcraft.curio_rites", "Crimson Rites");
        add("tc.knowledge.gained", "You have gained some knowledge");
        add("block.thaumcraft.mirror", "Magic Mirror");
        add("block.thaumcraft.mirror_essentia", "Essentia Mirror");
        add("item.thaumcraft.hand_mirror", "Magic Hand Mirror");
        add("tc.handmirrorlinked", "Link established.");
        add("tc.handmirrorerror", "Destination mirror is missing or misplaced. Link broken.");
        add("tc.handmirrorlinkedto.full", "Linked to %s,%s,%s in %s");
        add("tc.mirrorlinkedalready", "That mirror is already linked to a valid destination.");
        add("fail.crimsonrites", "This book contains nothing but crazed ravings.");
        add("item.thaumcraft.creative_flux_sponge", "Creative Flux Sponge");
        add("tooltip.thaumcraft.flux_sponge.drain.0", "Right-click to drain all");
        add("tooltip.thaumcraft.flux_sponge.drain.1", "flux from 9x9 chunk area");
        add("tooltip.thaumcraft.flux_sponge.rifts.0", "Also removes flux rifts");
        add("tooltip.thaumcraft.flux_sponge.rifts.1", "if used while sneaking.");
        add("tooltip.thaumcraft.flux_sponge.creative", "Creative only");
        add("tc.flux_sponge.drained", "%s flux drained from 81 chunks.");
        add("tc.flux_sponge.rifts", "%s flux rifts removed.");
        add("item.thaumcraft.resonator", "Essentia Resonator");
        add("item.thaumcraft.fortress_helm", "Thaumium Fortress Helm");
        add("item.thaumcraft.fortress_chest", "Thaumium Fortress Cuirass");
        add("item.thaumcraft.fortress_legs", "Thaumium Fortress Thigh Guards");
        add("item.thaumcraft.fortress_helm.mask.0", "Grinning Devil");
        add("item.thaumcraft.fortress_helm.mask.1", "Angry Ghost");
        add("item.thaumcraft.fortress_helm.mask.2", "Sipping Fiend");
        add("item.thaumcraft.void_robe_helm", "Void Thaumaturge Hood");
        add("item.thaumcraft.void_robe_chest", "Void Thaumaturge Robe");
        add("item.thaumcraft.void_robe_legs", "Void Thaumaturge Leggings");
        add("tc.resonator1", "Contains %1$s %2$s essentia");
        add("tc.resonator2", "Suction %1$s %2$s");
        add("tc.resonator3", "Untyped");
        add("tc.condenser1", "Cost: %1$s essentia");
        add("tc.condenser2", "Time: %1$s ticks (%2$s seconds)");
    
    }

    private void langCStone() {

        add("block.thaumcraft.stone_arcane", "Arcane Stone");
        add("block.thaumcraft.stone_arcane_brick", "Arcane Stone Brick");
        add("block.thaumcraft.stone_ancient", "Ancient Stone");
        add("block.thaumcraft.stone_ancient_tile", "Ancient Stone Tile");
        add("block.thaumcraft.stone_ancient_rock", "Ancient Rock");
        add("block.thaumcraft.stone_ancient_glyphed", "Glyphed Stone");
        add("block.thaumcraft.stone_ancient_doorway", "Ancient Barrier");
        add("block.thaumcraft.stone_eldritch_tile", "Eldritch Stone");
        add("block.thaumcraft.stone_porous", "Porous Stone");
        add("block.thaumcraft.stairs_arcane", "Arcane Stone Stairs");
        add("block.thaumcraft.stairs_arcane_brick", "Arcane Brick Stairs");
        add("block.thaumcraft.stairs_ancient", "Ancient Stone Stairs");
    
    }

    private void langDTrees() {

        add("block.thaumcraft.sapling_greatwood", "Greatwood Sapling");
        add("block.thaumcraft.sapling_silverwood", "Silverwood Sapling");
        add("block.thaumcraft.log_greatwood", "Greatwood");
        add("block.thaumcraft.log_silverwood", "Silverwood");
        add("block.thaumcraft.leaves_greatwood", "Greatwood Leaves");
        add("block.thaumcraft.leaves_silverwood", "Silverwood Leaves");
        add("block.thaumcraft.plank_greatwood", "Greatwood Planks");
        add("block.thaumcraft.plank_silverwood", "Silverwood Planks");
    
    }

    private void langEPlants() {

        add("block.thaumcraft.shimmerleaf", "Shimmerleaf");
        add("block.thaumcraft.cinderpearl", "Cinderpearl");
        add("block.thaumcraft.vishroom", "Vishroom");
        add("block.thaumcraft.grass_ambient", "Ambient Grass Block");
        add("biome.thaumcraft.magical_forest", "Magical Forest");
        add("biome.thaumcraft.eerie", "Eerie");
        add("biome.thaumcraft.eldritch", "Eldritch");
    
    }

    private void langMAuraHud() {

        add("item.thaumcraft.goggles_revealing", "Goggles of Revealing");
        add("hud.thaumcraft.aura.title", "Aura");
        add("hud.thaumcraft.aura.vis", "Vis: %1$s");
        add("hud.thaumcraft.aura.flux", "Flux: %1$s");
        add("hud.thaumcraft.aura.base", "Base: %1$s");
    
    }

    private void langHContainers() {

        add("item.thaumcraft.phial.empty", "Glass Phial");
        add("item.thaumcraft.phial.filled", "Phial of %1$s Essential");
        add("item.thaumcraft.primordial_pearl.pearl", "Primordial Pearl");
        add("item.thaumcraft.primordial_pearl.nodule", "Primordial Nodule");
        add("item.thaumcraft.primordial_pearl.mote", "Primordial Mote");
    
    }

    private void langTaint() {

        add("block.thaumcraft.flux_goo", "Flux Goo");
        add("fluid_type.thaumcraft.flux_goo", "Flux Goo");

        add("block.thaumcraft.taint_rock", "Tainted Rock");
        add("block.thaumcraft.taint_soil", "Tainted Soil");
        add("block.thaumcraft.taint_crust", "Tainted Crust");
        add("block.thaumcraft.taint_geyser", "Taint Geyser");
        add("block.thaumcraft.taint_log", "Tainted Log");
        add("block.thaumcraft.taint_feature", "Taint Feature");
        add("block.thaumcraft.taint_fibre", "Taint Fibre");

        add("effect.thaumcraft.vis_exhaust", "Vis Exhaust");
        add("effect.thaumcraft.infectious_vis_exhaust", "Flux Phage");
        add("effect.thaumcraft.flux_taint", "Flux Taint");

        add("death.attack.thaumcraft.taint", "%1$s was tainted");
        add("death.attack.thaumcraft.tentacle", "%1$s was strangled by tentacles");
        add("death.attack.thaumcraft.swarm", "%1$s was swarmed");
        add("death.attack.thaumcraft.dissolve", "%1$s dissolved");

        add("item.thaumcraft.essentia_crystal", "%s Vis Crystal");
        add("item.thaumcraft.essentia_crystal.unknown", "Unknown Vis Crystal");

        add("entity.thaumcraft.thaumic_slime", "Thaumic Slime");
        add("entity.thaumcraft.taint_seed", "Taint Seed");
        add("entity.thaumcraft.taint_seed_prime", "Greater Taint Seed");
        add("entity.thaumcraft.taint_crawler", "Taint Crawler");
        add("entity.thaumcraft.taint_swarm", "Taint Swarm");
        add("entity.thaumcraft.taintacle", "Taintacle");
        add("entity.thaumcraft.taintacle_small", "Lesser Taintacle");
        add("entity.thaumcraft.falling_taint", "Falling Taint");
        add("entity.thaumcraft.bottle_taint", "Bottle of Tainted Goo");
        add("item.thaumcraft.bottle_taint", "Bottle of Taint");
        add("entity.thaumcraft.wisp", "Wisp");
        add("entity.thaumcraft.brainy_zombie", "Angry Zombie");
        add("entity.thaumcraft.giant_brainy_zombie", "Furious Zombie");
        add("entity.thaumcraft.firebat", "Firebat");
        add("entity.thaumcraft.mind_spider", "Mind Spider");
        add("item.thaumcraft.brain", "Zombie Brain");

        add("item.thaumcraft.brainy_zombie_spawn_egg", "Angry Zombie Spawn Egg");
        add("item.thaumcraft.giant_brainy_zombie_spawn_egg", "Furious Zombie Spawn Egg");
        add("item.thaumcraft.firebat_spawn_egg", "Firebat Spawn Egg");
        add("item.thaumcraft.mind_spider_spawn_egg", "Mind Spider Spawn Egg");
        add("item.thaumcraft.wisp_spawn_egg", "Wisp Spawn Egg");
        add("item.thaumcraft.thaumic_slime_spawn_egg", "Thaumic Slime Spawn Egg");
        add("item.thaumcraft.taint_crawler_spawn_egg", "Taint Crawler Spawn Egg");
        add("item.thaumcraft.taintacle_spawn_egg", "Taintacle Spawn Egg");
        add("item.thaumcraft.taint_swarm_spawn_egg", "Taint Swarm Spawn Egg");
        add("item.thaumcraft.taint_seed_spawn_egg", "Taint Seed Spawn Egg");
        add("item.thaumcraft.taint_seed_prime_spawn_egg", "Greater Taint Seed Spawn Egg");
    
    }

    private void langGTools() {

        add("item.thaumcraft.thaumometer", "Thaumometer");
        add("item.thaumcraft.scribing_tools", "Scribing Tools");
    
    }

    private void langNScanning() {

        add("tc.unknownobject", "Nothing new can be learned from this.");
        add("tc.knownobject", "You have learned something new.");
        add("tc.invtoolarge", "Inventory too large. Only scanning first 100 items.");
        add("tc.celestial.fail.1", "You have already studied that today.");
        add("tc.celestial.fail.2", "You are unable to take notes of your studies.");

        add("item.thaumcraft.celestial_notes", "Celestial Notes");
        add("item.thaumcraft.celestial_notes.sun.text", "Solar");
        add("item.thaumcraft.celestial_notes.stars_1.text", "Stellar, Northern Quadrant");
        add("item.thaumcraft.celestial_notes.stars_2.text", "Stellar, Southern Quadrant");
        add("item.thaumcraft.celestial_notes.stars_3.text", "Stellar, Western Quadrant");
        add("item.thaumcraft.celestial_notes.stars_4.text", "Stellar, Eastern Quadrant");
        add("item.thaumcraft.celestial_notes.moon_1.text", "Lunar, Full");
        add("item.thaumcraft.celestial_notes.moon_2.text", "Lunar, Waning Gibbous");
        add("item.thaumcraft.celestial_notes.moon_3.text", "Lunar, Third Quarter");
        add("item.thaumcraft.celestial_notes.moon_4.text", "Lunar, Waning Crescent");
        add("item.thaumcraft.celestial_notes.moon_5.text", "Lunar, New");
        add("item.thaumcraft.celestial_notes.moon_6.text", "Lunar, Waxing Crescent");
        add("item.thaumcraft.celestial_notes.moon_7.text", "Lunar, First Quarter");
        add("item.thaumcraft.celestial_notes.moon_8.text", "Lunar, Waxing Gibbous");

        add("card.thaumcraft.celestial.name", "Celestial Studies");
        add("card.thaumcraft.celestial.text",
                "You take some of the celestial notes you have made and compare them for possible correlations with your primary research category. You gain 25 to 50 inspiration toward %1$s. You may gain other things as well...");

        add("research.thaumcraft.celestial_scanning.title", "Celestial Observation");
        add("research.thaumcraft.celestial_scanning.stage_0",
                "I have noticed a link between the phases of the moon and the speed at which the aura replenishes itself. I should investigate this phenomenon further and even expand my investigation to see if other heavenly bodies influence magic.<BR>A few minor tweaks to my thaumometer should enable it to take measurements from objects in the sky. I should remember to carry some paper and scribing tools with me so I can take notes.<BR>Measuring the same body or quadrant of the sky more than once a day will not give me new insights - I need to study them over time to measure changes in their aural influence.<BR>The notes I gather should come in handy for theorycrafting.");
        add("research.thaumcraft.flux.title", "Flux");
        add("research.thaumcraft.flux.stage_0",
                "I have been monitoring a strange buildup of magical energy in the aura. It seems to be a form of 'dirty' vis that I have named Flux. It shows as an ominous dark bar on my thaumometer.<BR>I now know what those purple puffs are I occasionally see when I am being careless - it is essentia spilling into the atmosphere and becoming Flux. Pure essentia, it seems, should not be directly exposed to the aura.<BR>I'm not exactly sure what negative effects this will cause, but I have noticed that Flux seems to clog the local aura preventing Vis from replenishing naturally.<BR>Picture it as a thick layer of scum on a natural pool of clean water.<BR>The local aura has a point at which it is 'filled' and Flux seems to count towards this capacity.<BR>More alarmingly, it seems to take precedence over vis and I suspect it can probably exceed this natural limit.<BR>I am not sure what will happen when Flux overflows, but it it bound to be interesting.");
        add("research.thaumcraft.flux.warn", "Something seems to be building up in the aura. Something bad.");

        scanEntry("thaumcraft/thaumic_slime", "Thaumic Slime",
                "These purple slimes are a strange offshoot of their green cousins. Normal slimes form from the organic gunk that occasionally collects in caves, but Thaumic Slimes are formed from the purple proto-matter residue left over from magical mishaps.<BR>Apart from their strange origins they are rather similar to their green cousins for the most part. They do appear to have the ability to split at will, often spitting a lesser version of themselves at targets that venture too close. If there are no targets nearby, split slimes will endeavour to reform to once again form a larger organism.");
        scanEntry("thaumcraft/taint_seed", "Taint Seed",
                "Occasionally tight knots of Tainted tendrils will form. From out of this 'seed' foul tainted air will spew which acts as the basis for new Taint growth.<BR>These seeds can lash out to defend themselves, but the risk must be taken if you wish to rid an area of taint.<BR>Without the foul air these seeds create, tainted growth soon withers and dies.");
        scanEntry("thaumcraft/taint_crawler", "Tainted Crawler",
                "These disgusting grubs secrete corrupting Taint wherever they go. Most of the time the Taint is short-lived if they wander too far from the core of the Taint growth, but other times this can cause the Taint to spread much quicker than it normally would.<BR>It is best to destroy them on sight.");
        scanEntry("thaumcraft/taintacle", "Taintacle",
                "These dangerous growths look like giant tentacles. They will lash out at anything that comes nearby.<BR>Though they are immobile, keeping your distance might not keep you safe as miniature versions of them will spawn all around you should you draw their attention.");
        scanEntry("thaumcraft/taint_swarm", "Taint Swarm",
                "Sometimes purple hive-like growths will form on the remains of trees within Taint. Swarms of tiny biting creatures will spawn from these growths and rove around in aggressive swarms.<BR>They move fast, are hard to avoid and even harder to hit. Fortunately the individual creatures are not particularly robust so a properly prepared traveller should be able to dispatch them easily.");
        scanEntry("thaumcraft/wisp", "Wisp",
                "These strange creatures appear to be floating points of light and are often found near high concentrations of vis. Honestly I am not sure if they are creatures at all - they move randomly and do not appear to exhibit the normal characteristics of a living organism. They do however react to being attacked and occasionally become aggressive for no apparent reason.<BR>My initial thoughts are they might be a form of proto-life made up of vis instead of normal matter, but that cannot be the whole picture as I am detecting strange dimensional energies at their core.");
        scanEntry("thaumcraft/pech", "The Pech",
                "These strange humanoids can be found wherever the veil between worlds is at its thinnest. They seem drawn to the magical energies that abound in such places.<IMG>thaumcraft:textures/research/research5.png:128:136:108:117:.75</IMG>Don't let their small stature fool you. They have incredible strength and can carry many times their own bodyweight.<BR>Pech are normally not aggressive, but when riled up they will band together and hunt down their attacker. Under normal conditions they avoid people at all costs.<BR>Pechs are notoriously avaricious, and will loot anything not nailed down, though they prefer precious things. It is said that if you feed this desire for material wealth it could be possible to befriend one.<BR>Once befriended they often carry wondrous objects that they might be willing to part with.<BR>It should be noted that such 'friendships' seldom last long.");
        scanEntry("thaumcraft/firebat", "Firebat",
                "Fire bats, or Hellbats, are usually found in the Nether. Engulfed in flame and unreasonably aggressive they are perfectly suited to the environment they live in. They also have the alarming tendency to explode when attacking.<BR>One wonders what possible evolutionary advantage exploding gives them as a species, but the same can be asked of creepers.");
        scanEntry("thaumcraft/cultist", "Crimson Cultist",
                "These red-robed cultists have been popping up of late. Their goals are unknown to me and they seem unwilling to talk. Beyond that there is something... wrong about them. Their grip on sanity seems tenuous at best, but the wrongness goes much deeper than that. It is hard to describe.<BR>My presence seems to enrage them and I believe it might have something to do with my capacity for thaumaturgy.<BR>Their clerics are also capable of wielding magic and I can sense that they are drawing it from the aura, but the magic itself does not seem to be thaumaturgy. Whatever it is, it makes my skin crawl and I can feel the aura buckling and tearing every time they draw upon it.<BR>Some of them seem to carry a book around. I should try and get my hand on one of those, maybe it will tell me more about this strange cult. I should be careful though, some knowledge can be dangerous.");
        scanEntry("thaumcraft/brainy_zombie", "Angry Zombie",
                "I have observed that some zombies seem much more aware than their lumbering brethren. Their eyes burn with a malevolent intelligence leading me to believe that they retained much more intelligence after death than is normal. They are definitely more aggressive and dangerous than a normal zombie.<BR>They should make good sources of brain matter for my experiments.");
        scanEntry("thaumcraft/eldritch_guardian", "Eldritch Guardian",
                "These ghost-like figures are a dangerous menace. They can be found wherever the walls of reality are at their thinnest. I believe they act as guardians of such places, protecting those weak spots from trespassers.<BR>Their form is held together by dimensional energies from some other place making them much weaker in our world. I would hate having to confront them in whatever place they call home.<BR>Their touch is deadly and they are able to hurl entropic dimensional energy at those too far to reach.");
        scanEntry("thaumcraft/eldritch_crab", "Eldritch Crab",
                "Eldritch Crabs, or Helmed Crabs, are a most unusual and unnatural creature. I am convinced these creatures are the result of magical tinkering.<BR>A pair of powerful claws make them dangerous to face in combat, but it is how they use those claws that make them horrific. They leap at their foes heads in an attempt to decapitate them. If successful they are somehow able to burrow into the victims body and take control of what remains. Their abdomen looks much like a helmed head allowing them to disguise the true nature of the husk they now control.<BR>These husks are not much more powerful than a normal zombie so the reason why they do this eludes me - they are much more of a threat without their 'mount'. Possibly they were created as weapons of terror by some demented inventor?");
    
    }

    private void langDecor() {

        add("block.thaumcraft.dioptra", "Thaumic Dioptra");
        add("block.thaumcraft.vis_battery", "Vis Battery");
        add("block.thaumcraft.matrix_speed", "Infusion Speed Stone");
        add("block.thaumcraft.matrix_cost", "Infusion Cost Stone");
        add("block.thaumcraft.jar_brain", "Brain in a Jar");
        add("block.thaumcraft.arcane_ear", "Arcane Ear");
        add("block.thaumcraft.arcane_ear_toggle", "Arcane Ear (Toggle)");
        add("block.thaumcraft.lamp_arcane", "Arcane Lamp");
        add("block.thaumcraft.lamp_growth", "Lamp of Growth");
        add("block.thaumcraft.lamp_fertility", "Lamp of Fertility");
        add("block.thaumcraft.centrifuge", "Essentia Centrifuge");
        add("block.thaumcraft.hungry_chest", "Hungry Chest");
        add("block.thaumcraft.everfull_urn", "Everfull Urn");
        add("block.thaumcraft.vis_generator", "Vis Generator");
        add("block.thaumcraft.essentia_input", "Filling Essentia Transfuser");
        add("block.thaumcraft.essentia_output", "Emptying Essentia Transfuser");
        add("block.thaumcraft.condenser", "Flux Condenser");
        add("block.thaumcraft.condenser_lattice", "Flux Condenser Lattice");
        add("block.thaumcraft.condenser_lattice_dirty", "Clogged Flux Condenser Lattice");
        add("block.thaumcraft.stabilizer", "Stabilizer");
        add("block.thaumcraft.redstone_relay", "Redstone Relay");
        add("block.thaumcraft.void_siphon", "Void Siphon");
        add("block.thaumcraft.thaumatorium", "Thaumatorium");
        add("block.thaumcraft.thaumatorium_top", "Thaumatorium");
        add("block.thaumcraft.brain_box", "Brain Box");
        add("item.thaumcraft.tallow", "Magic Tallow");
        add("block.thaumcraft.placeholder_obsidian", "Infernal Furnace");
        add("block.thaumcraft.placeholder_nether_bricks", "Infernal Furnace");
        add("item.thaumcraft.warping", "Warping");
        add("effect.thaumcraft.thaumarhia", "Thaumorrhea");
        add("effect.thaumcraft.unnatural_hunger", "Unnatural Hunger");
        add("effect.thaumcraft.sun_scorned", "Sun Scorned");
        add("effect.thaumcraft.death_gaze", "Deadly Gaze");
        add("effect.thaumcraft.blurred_vision", "Blurred Vision");
        add("effect.thaumcraft.warp_ward", "Warp Ward");
        add("warp.thaumcraft.gain.permanent", "You have gained permanent Warp!");
        add("warp.thaumcraft.gain.normal", "You have gained Warp!");
        add("warp.thaumcraft.gain.temporary", "You have gained temporary Warp!");
        add("warp.thaumcraft.lose.permanent", "You have lost permanent Warp!");
        add("warp.thaumcraft.lose.normal", "You have lost Warp!");
        add("warp.thaumcraft.lose.temporary", "You have lost temporary Warp!");
        add("warp.thaumcraft.text.1", "You feel oddly drained.");
        add("warp.thaumcraft.text.2", "A sudden and unnatural hunger consumes you.");
        add("warp.thaumcraft.text.4", "Your vision becomes strange and grim.");
        add("warp.thaumcraft.text.5", "The light suddenly becomes overwhelmingly bright and burns your skin.");
        add("warp.thaumcraft.text.6", "A thick fog appears from nowhere. Something stirs in its depths.");
        add("warp.thaumcraft.text.7", "They're everywhere! Run!");
        add("warp.thaumcraft.text.9", "You suddenly feel reluctant to break things.");
        add("warp.thaumcraft.text.10", "Your perception suddenly expands.");
        add("warp.thaumcraft.text.11", "What was that noise? Something is behind you.");
        add("warp.thaumcraft.text.12", "Something is following you.");
        add("warp.thaumcraft.text.13", "Something is watching you. Maybe it is time to stop.");
        add("warp.thaumcraft.text.14", "You have a moment of clarity.");
        add("warp.thaumcraft.text.15", "Your stomach suddenly gurgles very strangely.");
        add("warp.thaumcraft.text.16", "The faint sound of chanting can be heard nearby.");
        add("warp.thaumcraft.text.hunger.1", "Your hunger cannot be satisfied with normal food.");
        add("warp.thaumcraft.text.hunger.2", "Your hunger begins to fade.");
        add("warp.thaumcraft.fluxevent.2", "You feel something invading your mind and sapping your will.");
        add("warp.thaumcraft.fluxevent.3", "You feel a sudden release of magical tension nearby.");
        add("entity.thaumcraft.flux_rift", "Flux Rift");
        add("item.thaumcraft.void_seed", "Void Seed");
        add("item.thaumcraft.causality_collapser", "Causality Collapser");
        add("block.thaumcraft.infusion_matrix", "Runic Matrix");
        add("block.thaumcraft.pedestal_arcane", "Arcane Pedestal");
        add("block.thaumcraft.recharge_pedestal", "Recharge Pedestal");
        add("block.thaumcraft.pedestal_ancient", "Ancient Pedestal");
        add("block.thaumcraft.pedestal_eldritch", "Eldritch Pedestal");
        add("block.thaumcraft.pillar_arcane", "Infusion Pillar");
        add("block.thaumcraft.pillar_ancient", "Ancient Infusion Pillar");
        add("block.thaumcraft.pillar_eldritch", "Eldritch Infusion Pillar");
        add("gui.thaumcraft.infusion.instability", "Instability: %s");
        add("gui.thaumcraft.infusion.instability.0", "Negligible");
        add("gui.thaumcraft.infusion.instability.1", "Minor");
        add("gui.thaumcraft.infusion.instability.2", "Moderate");
        add("gui.thaumcraft.infusion.instability.3", "High");
        add("gui.thaumcraft.infusion.instability.4", "Very High");
        add("gui.thaumcraft.infusion.instability.5", "Dangerous");
        add("gui.thaumcraft.infusion.stability.very_stable", "Very Stable");
        add("gui.thaumcraft.infusion.stability.stable", "Stable");
        add("gui.thaumcraft.infusion.stability.unstable", "Unstable");
        add("gui.thaumcraft.infusion.stability.very_unstable", "Dangerously Unstable");
        add("gui.thaumcraft.infusion.stability.gain", "gain / cycle");
        add("gui.thaumcraft.infusion.stability.range", "0 to ");
        add("gui.thaumcraft.infusion.stability.loss", "loss / cycle");
        add("entity.thaumcraft.causality_collapser", "Causality Collapser");
        add("item.thaumcraft.thaumium_sword", "Thaumium Sword");
        add("item.thaumcraft.thaumium_pickaxe", "Thaumium Pickaxe");
        add("item.thaumcraft.thaumium_axe", "Thaumium Axe");
        add("item.thaumcraft.thaumium_shovel", "Thaumium Shovel");
        add("item.thaumcraft.thaumium_hoe", "Thaumium Hoe");
        add("item.thaumcraft.thaumium_helm", "Thaumium Helm");
        add("item.thaumcraft.thaumium_chest", "Thaumium Chestplate");
        add("item.thaumcraft.thaumium_legs", "Thaumium Greaves");
        add("item.thaumcraft.thaumium_boots", "Thaumium Boots");
        add("item.thaumcraft.void_sword", "Void Sword");
        add("item.thaumcraft.void_pickaxe", "Void Pickaxe");
        add("item.thaumcraft.void_axe", "Void Axe");
        add("item.thaumcraft.void_shovel", "Void Shovel");
        add("item.thaumcraft.void_hoe", "Void Hoe");
        add("item.thaumcraft.void_helm", "Void Helm");
        add("item.thaumcraft.void_chest", "Void Chestplate");
        add("item.thaumcraft.void_legs", "Void Greaves");
        add("item.thaumcraft.void_boots", "Void Boots");
        add("item.thaumcraft.elemental_sword", "Sword of the Zephyr");
        add("item.thaumcraft.elemental_pickaxe", "Pickaxe of the Core");
        add("item.thaumcraft.elemental_axe", "Axe of the Stream");
        add("item.thaumcraft.elemental_shovel", "Shovel of the Earthmover");
        add("item.thaumcraft.elemental_hoe", "Hoe of Growth");
        add("item.thaumcraft.primal_crusher", "Primal Crusher");
        add("item.thaumcraft.traveller_boots", "Boots of the Traveller");
        add("item.thaumcraft.cloth_chest", "Apprentice's Robes");
        add("item.thaumcraft.cloth_legs", "Apprentice's Leggings");
        add("item.thaumcraft.cloth_boots", "Apprentice's Boots");

        for (DyeColor dye : DyeColor.values()) {
            add("block.thaumcraft.candle_" + dye.getName(), dyeName(dye) + " Tallow Candle");
            add("block.thaumcraft.banner_" + dye.getName(), dyeName(dye) + " Banner");
            add("block.thaumcraft.wall_banner_" + dye.getName(), dyeName(dye) + " Banner");
        }
        add("block.thaumcraft.banner_crimson_cult", "Crimson Cult Banner");
        add("block.thaumcraft.wall_banner_crimson_cult", "Crimson Cult Banner");
    
    }

    private void langCasters() {

        add("key.category.thaumcraft.main", "Thaumcraft");
        add("key.thaumcraft.change_focus", "Change Caster Focus");
        add("key.thaumcraft.misc_toggle", "Misc Caster Toggle");
        add("item.thaumcraft.caster_basic", "Caster's Gauntlet");
        add("item.thaumcraft.focus_1", "Blank Lesser Focus");
        add("item.thaumcraft.focus_2", "Blank Advanced Focus");
        add("item.thaumcraft.focus_3", "Blank Greater Focus");
        add("tooltip.thaumcraft.caster.vis_cost", "Vis cost: %s Vis per cast");
        add("tooltip.thaumcraft.focus.vis_cost", "%s Vis per cast");
        add("entity.thaumcraft.focus_projectile", "Focus Projectile");
        add("entity.thaumcraft.focus_cloud", "Focus Cloud");
        add("entity.thaumcraft.focus_mine", "Focus Mine");
        add("entity.thaumcraft.spell_bat", "Spellbat");

        add("block.thaumcraft.hole", "Dimensional Tear");
        add("block.thaumcraft.effect_sap", "Sapping Field");

        add("focus.thaumcraft.root.name", "Caster");
        add("focus.thaumcraft.root.text", "The caster and point of origin of the spell.");
        add("focus.thaumcraft.touch.name", "Touch");
        add("focus.thaumcraft.touch.text", "Allows you to affect things you can touch, within roughly 4 blocks of the point of origin.");
        add("focus.thaumcraft.projectile.name", "Projectile");
        add("focus.thaumcraft.projectile.text", "Gathers the energy of the focus into a magical orb that you can throw like a projectile. This projectile travels slowly and is affected by gravity.");
        add("focus.thaumcraft.bolt.name", "Bolt");
        add("focus.thaumcraft.bolt.text", "Hurl magic directly at your target as a concentrated bolt of energy. The effect is instantaneous, but the range is limited to 16 blocks.");
        add("focus.thaumcraft.plan.name", "Plan");
        add("focus.thaumcraft.plan.text", "Allows you to plan exactly which blocks will be affected.");
        add("focus.thaumcraft.cloud.name", "Cloud");
        add("focus.thaumcraft.cloud.text", "Creates a lingering cloud of magical energy that effects anything inside.");
        add("focus.thaumcraft.mine.name", "Arcane Mine");
        add("focus.thaumcraft.mine.text", "Creates a mystical construct that detonates when a hostile entity passes nearby, releasing the effects upon it.");
        add("focus.thaumcraft.spellbat.name", "Summon Spellbat");
        add("focus.thaumcraft.spellbat.text", "Conjures a mystical bat that will hunt down enemies and inflict them with the focus's effects.");

        add("focus.thaumcraft.air.name", "Air");
        add("focus.thaumcraft.air.text", "Creates a blast of air that knocks things back, but causes only minor damage.");
        add("focus.thaumcraft.earth.name", "Earth");
        add("focus.thaumcraft.earth.text", "Hurls a blast of earthen shrapnel that causes significant damage and may break weaker blocks.");
        add("focus.thaumcraft.fire.name", "Fire");
        add("focus.thaumcraft.fire.text", "Hurls flame at your target and sets it alight.");
        add("focus.thaumcraft.frost.name", "Frost");
        add("focus.thaumcraft.frost.text", "Throws chilling cold at your target, causing damage and freezing it. Freezes water and will slow down creatures.");
        add("focus.thaumcraft.break.name", "Break");
        add("focus.thaumcraft.break.text", "Summons disruptive energy that breaks down most materials.");
        add("focus.thaumcraft.curse.name", "Curse");
        add("focus.thaumcraft.curse.text", "Summons the powers of entropy to harm and disrupt the targeted creature.");
        add("focus.thaumcraft.flux.name", "Flux");
        add("focus.thaumcraft.flux.text", "This effect conjures raw, unfocused vis that disrupts living (and dead) creatures. This energy cannot interact with inanimate objects, but it does bypass mundane armor.");
        add("focus.thaumcraft.rift.name", "Rift");
        add("focus.thaumcraft.rift.text", "Shifts matter into an alternate reality, creating temporary 'holes' through which you can travel.");
        add("focus.thaumcraft.exchange.name", "Exchange");
        add("focus.thaumcraft.exchange.text", "Swap one type of block in the world for another.");
        add("focus.thaumcraft.heal.name", "Heal");
        add("focus.thaumcraft.heal.text", "This effect heals living creatures and harms undead.");

        add("focus.thaumcraft.scatter.name", "Scatter");
        add("focus.thaumcraft.scatter.text", "Split a single trajectory into multiple random trajectories.");
        add("focus.thaumcraft.split_target.name", "Split Target");
        add("focus.thaumcraft.split_target.text", "Split a single target into two weaker ones.");
        add("focus.thaumcraft.split_trajectory.name", "Split Trajectory");
        add("focus.thaumcraft.split_trajectory.text", "Split a single trajectory into two weaker ones.");

        add("focus.common.power", "Power");
        add("focus.common.duration", "Duration (seconds)");
        add("focus.common.radius", "Radius (blocks)");
        add("focus.common.silk", "Silk Touch");
        add("focus.common.no", "No");
        add("focus.common.yes", "Yes");
        add("focus.common.fortune", "Fortune");
        add("focus.common.target", "Target Type");
        add("focus.common.friend", "Friendly");
        add("focus.common.enemy", "Non-friendly");
        add("focus.common.none", "None");
        add("focus.common.options", "Options");
        add("focus.fire.burn", "Burn duration (seconds)");
        add("focus.scatter.cone", "Spread angle (degrees)");
        add("focus.scatter.forks", "Trajectory forks");
        add("focus.projectile.speed", "Projectile speed");
        add("focus.projectile.bouncy", "Bouncy");
        add("focus.projectile.seeking.friendly", "Seek friendly");
        add("focus.projectile.seeking.hostile", "Seek hostile");
        add("focus.break.power", "Breaking strength");
        add("focus.plan.method", "Planning method");
        add("focus.plan.full", "Full");
        add("focus.plan.surface", "Surface");
        add("focus.rift.depth", "Depth");
        add("focus.heal.power", "Healing");

        add("death.attack.thaumcraft.focus_fire", "%1$s was burned to a crisp by magic");
        add("death.attack.thaumcraft.focus_fire.player", "%1$s was burned to a crisp by %2$s's magic");
        add("block.thaumcraft.focal_manipulator", "Focal Manipulator");
        add("gui.thaumcraft.wandtable.craft", "Start Crafting");
        add("gui.thaumcraft.wandtable.complexity", "Total Complexity");
        add("gui.thaumcraft.wandtable.xp_cost", "Experience Cost");
        add("gui.thaumcraft.wandtable.vis_cost", "Vis Cost");
        add("gui.thaumcraft.wandtable.cast_cost", "Vis per cast: %s");
        add("gui.thaumcraft.wandtable.components", "Crystals Required");
        add("gui.thaumcraft.wandtable.part_complexity", "Complexity:");
        add("gui.thaumcraft.wandtable.part_efficiency", "Effect Multiplier:");
        add("gui.thaumcraft.wandtable.heal_power", "Healing");
        add("gui.thaumcraft.wandtable.problem.in_progress", "Crafting in progress...");
        add("gui.thaumcraft.wandtable.problem.complexity", "Too complex: %s/%s");
        add("gui.thaumcraft.wandtable.problem.empty_nodes", "The spell has unfilled nodes");
        add("gui.thaumcraft.wandtable.problem.crystal", "Missing %sx %s");
        add("gui.thaumcraft.wandtable.problem.no_effects", "The spell needs at least one effect");
        add("gui.thaumcraft.wandtable.problem.xp", "Requires %s experience levels");
        add("gui.thaumcraft.wandtable.problem.ready", "Ready to craft!");
    
    }

    private void langEnchantments() {

        add("enchantment.thaumcraft.collector", "Collector");
        add("enchantment.thaumcraft.destructive", "Destructive");
        add("enchantment.thaumcraft.burrowing", "Burrowing");
        add("enchantment.thaumcraft.sounding", "Sounding");
        add("enchantment.thaumcraft.refining", "Refining");
        add("enchantment.thaumcraft.arcing", "Arcing");
        add("enchantment.thaumcraft.essence", "Essence Harvester");
        add("enchantment.thaumcraft.lamplight", "Lamplighter");
        add("block.thaumcraft.effect_glimmer", "Glimmer");
    
    }

    private void langGolemancy() {

        add("item.thaumcraft.mind_clockwork", "Clockwork Mind");
        add("item.thaumcraft.mind_biothaumic", "Biothaumic Mind");
        add("item.thaumcraft.module_vision", "Vision Module");
        add("item.thaumcraft.module_aggression", "Aggression Module");
        add("item.thaumcraft.golem_bell", "Golemancer's Bell");
        add("item.thaumcraft.golem", "Golem");
        add("item.thaumcraft.seal_blank", "Blank Seal");
        add("item.thaumcraft.seal_pickup", "Control Seal: Collect");
        add("item.thaumcraft.seal_pickup_advanced", "Advanced Control Seal: Collect");
        add("item.thaumcraft.seal_fill", "Control Seal: Store");
        add("item.thaumcraft.seal_fill_advanced", "Advanced Control Seal: Store");
        add("item.thaumcraft.seal_empty", "Control Seal: Empty");
        add("item.thaumcraft.seal_empty_advanced", "Advanced Control Seal: Empty");
        add("item.thaumcraft.seal_harvest", "Control Seal: Harvest");
        add("item.thaumcraft.seal_butcher", "Control Seal: Butcher");
        add("item.thaumcraft.seal_guard", "Control Seal: Guard");
        add("item.thaumcraft.seal_guard_advanced", "Advanced Control Seal: Guard");
        add("item.thaumcraft.seal_lumber", "Control Seal: Lumberjack");
        add("item.thaumcraft.seal_breaker", "Control Seal: Block Breaker");
        add("item.thaumcraft.seal_breaker_advanced", "Control Seal: Advanced Block Breaker");
        add("item.thaumcraft.seal_use", "Control Seal: Use");
        add("item.thaumcraft.seal_provider", "Control Seal: Provide");
        add("item.thaumcraft.seal_stock", "Control Seal: Stock");
        add("block.thaumcraft.levitator", "Arcane Levitator");
        add("block.thaumcraft.golem_builder", "Golem Press");
        add("block.thaumcraft.placeholder_iron_bars", "Iron Bars");
        add("block.thaumcraft.placeholder_cauldron", "Cauldron");
        add("block.thaumcraft.placeholder_anvil", "Anvil");
        add("block.thaumcraft.placeholder_table", "Stone Table");
        add("entity.thaumcraft.golem", "Thaumcraft Golem");
        add("entity.thaumcraft.golem_dart", "Golem Dart");
        add("golem.follow", "I'm coming, Master!");
        add("golem.stay", "I will stay here, Master.");
        add("golem.rank", "Rank");
        add("tc.notowned", "This does not belong to you.");
        add("gui.thaumcraft.levitator", "Range set to %s blocks. (%s vis/minute)");
        trait("smart", "Smart", "This golem is nearly sentient, with superior reasoning and decision-making capabilities.");
        trait("deft", "Deft", "This golem has a great deal of manual dexterity and can perform tasks requiring precision and a delicate touch. Counters and countered by Clumsy.");
        trait("clumsy", "Clumsy", "This golem has almost no manual dexterity and can only perform simple tasks requiring basic interactions. Counters and countered by Deft.");
        trait("fighter", "Fighter", "The golem is capable of taking hostile action against other entities.");
        trait("wheeled", "Wheeled", "The golem propels itself using wheels. This gives it greater speed, but it is unable to jump or navigate steep slopes.");
        trait("flyer", "Flyer", "The golem is capable of flight, giving it greater mobility at the cost of speed.");
        trait("climber", "Climber", "Sheer cliffs do not deter this golem. It can easily scale them to get to its destination.");
        trait("heavy", "Heavy Frame", "The golem is heavier than average, which reduces its speed and agility. Counters and countered by Light.");
        trait("light", "Light Frame", "The golem is lighter than average, which gives it increased speed and agility. Counters and countered by Heavy.");
        trait("fragile", "Fragile", "The golem is built with delicate components or weak materials that reduce its life total and armour rating. Counters and countered by Armored.");
        trait("repair", "Improved Self Repair", "The golem repairs any damage suffered at more than double the normal rate.");
        trait("scout", "Scout", "The golem has a greater visual range and can operate in a much wider area: 48 blocks away from its home location, instead of 32.");
        trait("armored", "Armored", "The golem is reinforced with additional material which increases its armor rating. Counters and countered by Fragile.");
        trait("brutal", "Brutal", "The golem inflicts greater melee damage in combat.");
        trait("fireproof", "Fireproof", "The golem is immune to fire damage.");
        trait("breaker", "Breaker", "The golem is capable of destroying most blocks with ease.");
        trait("hauler", "Hauler", "Allows the golem to carry two stack of items instead of one.");
        trait("ranged", "Ranged", "The golem can attack targets at range. ");
        trait("blastproof", "Blast-proof", "The golem is highly resistant to explosion damage.");
        material("wood", "Greatwood", "The golem is crafted from greatwood. It is light and agile, but not particularly sturdy.");
        material("iron", "Iron", "The golem is crafted from iron. It is sturdy and fireproof, but heavy.");
        material("clay", "Clay", "The golem is crafted from baked clay. It is not a particularly sturdy material, but the resulting golem is fireproof and relatively light.");
        material("brass", "Brass", "The golem is crafted from brass. It is not as sturdy as iron nor as resistant to fire. Normally brass is heavier than iron, but it allows for superior construction methods which results in a much lighter frame.");
        material("thaumium", "Thaumium", "The golem is crafted from thaumium. It shares many characteristics with iron, though it is sturdier and more resistant to damage.");
        material("void", "Void Metal", "The golem is crafted from void metal. Slightly softer than iron, this metal makes up for it by being lighter and able to repair itself.");
        head("basic", "Clockwork Head", "The default golem head. No particular strengths or weaknesses.");
        head("smart", "Smart Head", "This head contains an advanced biothaumic brain, giving the golem greater capabilities and the ability to learn. ");
        head("smart_armored", "Armored Smart Head", "The smart head enhanced with additional armor and padding.");
        head("scout", "Perceptive Head", "The basic clockwork head enhanced with improved biothaumic eyes. ");
        head("smart_scout", "Biothaumic Head", "Using both biothaumic eyes and brain, this head is the pinnacle of the biothaumic art. ");
        arm("basic", "Basic Arms", "The default golem arms and hands. No particular strengths or weaknesses.");
        arm("fine", "Fine Manipulators", "These arms end in delicate and dexterous hands. ");
        arm("claws", "Claw Arms", "These arms end in a terrifying pair of razor sharp metal pincers. Comes with a built-in aggression module.");
        arm("breakers", "Breaker Arms", "These arms end in a pair of pneumatic, diamond tipped grinders. ");
        arm("darts", "Dart Launchers", "These arms end in a pair of pneumatic dart launchers. The darts are magically created as they are needed. Comes with a built-in aggression module.");
        leg("walker", "Basic Legs", "A pair of simple legs. No particular strengths or weaknesses.");
        leg("roller", "Uni-wheel ", "A single wheel. Quite fast, but incapable of jumping or going up steep hills.");
        leg("climber", "Climbing Legs", "A pair of simple legs enhanced with crampons and other devices allowing for vertical ascent.");
        leg("flyer", "Levitation Module", "A modified arcane levitator granting the golem the power of flight. Reduces speed by a third.");
        addon("none", "None", "No addon installed.");
        addon("armored", "Armor Plating", "Grants the golem increased durability.");
        addon("fighter", "Aggression Module", "Allows the golem to engage in combat.");
        addon("hauler", "Carry Frame", "Allows the golem to carry two stack of items instead of one.");
        add("golem.prop.replant", "Replant crops");
        add("golem.prop.cycle", "Cycle whitelist");
        add("golem.prop.meta", "Use metadata");
        add("golem.prop.nbt", "Use NBT data");
        add("golem.prop.ore", "Use Ore Dictionary");
        add("golem.prop.mod", "Use from same mod");
        add("golem.prop.mob", "Target Mobs ");
        add("golem.prop.animal", "Target Animals");
        add("golem.prop.player", "Target Players");
        add("golem.prop.left", "Left click");
        add("golem.prop.empty", "Click empty air");
        add("golem.prop.emptyhand", "Can use empty hand");
        add("golem.prop.sneak", "Simulate sneaky click");
        add("golem.prop.single", "Single item only");
        add("golem.prop.provision", "Request provisioning");
        add("golem.prop.provision.wl", "Request provisioning from whitelist");
        add("golem.prop.priority", "Task Priority");
        add("golem.prop.color", "Set for %s golems");
        add("golem.prop.colorall", "All golems");
        add("golem.prop.owner", "You own this seal");
        add("golem.prop.lock", "Only golems owned by seal owner can perform these tasks");
        add("golem.prop.unlock", "All golems can perform these tasks");
        add("golem.prop.redon", "Redstone sensitive");
        add("golem.prop.redoff", "Ignores Redstone signals");
        add("golem.prop.exist", "Container must already contain item");
        add("golem.prop.leave", "Always leave at least 1 item");
        add("golem.prop.silk", "Use Silk Touch");
        add("gui.thaumcraft.seal", "Seal");
        add("golem.prop.blacklist", "Blacklist");
        add("golem.prop.whitelist", "Whitelist");
        add("button.category.0", "Priority/Locking");
        add("button.category.1", "Filter");
        add("button.category.2", "Area");
        add("button.category.3", "Options");
        add("button.category.4", "Requirements");
        add("button.caption.x", "East / West");
        add("button.caption.y", "Up / Down");
        add("button.caption.z", "North / South");
        add("button.caption.required", "Required");
        add("button.caption.forbidden", "Forbidden");
    
    }

    private void langBosses() {
        add("entity.thaumcraft.cultist_leader", "Cultist Praetor");
        add("entity.thaumcraft.cultist_leader.name.custom", "Praetor %1$s the %2$s");
        add("entity.thaumcraft.cultist_portal_greater", "Greater Crimson Portal");
        add("entity.thaumcraft.eldritch_golem", "Eldritch Golem");
        add("entity.thaumcraft.eldritch_golem.name.custom", "%1$s Eldritch Construct");
        add("entity.thaumcraft.eldritch_warden", "Eldritch Warden");
        add("entity.thaumcraft.eldritch_warden.name.custom", "%1$s the %2$s");
        add("entity.thaumcraft.taintacle_giant", "Giant Taintacle");
        add("tc.boss.enrage", "is enraged by your powerful blows.");
        add("item.thaumcraft.crimson_praetor_helm", "Crimson Praetor Helm");
        add("item.thaumcraft.crimson_praetor_chest", "Crimson Praetor Cuirass");
        add("item.thaumcraft.crimson_praetor_legs", "Crimson Praetor Greaves");
    }

    private void langConstructs() {
        add("block.thaumcraft.activator_rail", "Arcane Activator Rail");
        add("item.thaumcraft.turret_basic", "Automated Crossbow");
        add("item.thaumcraft.turret_advanced", "Advanced Automated Crossbow");
        add("item.thaumcraft.turret_bore", "Arcane Bore");
        add("item.thaumcraft.grapple_gun", "Arcane Grappler");
        add("item.thaumcraft.grapple_gun_tip", "Grappler Head");
        add("item.thaumcraft.grapple_gun_spool", "Grappler Spool");
        add("entity.thaumcraft.turret_crossbow", "Automated Crossbow");
        add("entity.thaumcraft.turret_crossbow_advanced", "Advanced Automated Crossbow");
        add("entity.thaumcraft.arcane_bore", "Arcane Bore");
        add("entity.thaumcraft.grapple", "Grappler");
        add("button.turretfocus.1", "Target Animals");
        add("button.turretfocus.2", "Target Mobs");
        add("button.turretfocus.3", "Target Players");
        add("button.turretfocus.4", "Target Friendly");
        add("gui.thaumcraft.bore.width", "Width: %s");
        add("gui.thaumcraft.bore.depth", "Depth: %s");
        add("gui.thaumcraft.bore.speed", "Speed: +%s");
        add("gui.thaumcraft.bore.properties", "Other properties:");
        add("gui.thaumcraft.bore.refining", "Refining %s");
        add("gui.thaumcraft.bore.fortune", "Fortune %s");
        add("gui.thaumcraft.bore.silktouch", "Silk Touch");
    }

    private void langDecorSweep() {
        add("block.thaumcraft.slab_greatwood", "Greatwood Slab");
        add("block.thaumcraft.slab_silverwood", "Silverwood Slab");
        add("block.thaumcraft.slab_arcane_stone", "Arcane Stone Slab");
        add("block.thaumcraft.slab_arcane_brick", "Arcane Brick Slab");
        add("block.thaumcraft.slab_ancient", "Ancient Stone Slab");
        add("block.thaumcraft.slab_eldritch", "Eldritch Stone Slab");
        add("block.thaumcraft.stairs_greatwood", "Greatwood Stairs");
        add("block.thaumcraft.stairs_silverwood", "Silverwood Stairs");
        add("block.thaumcraft.table_wood", "Wood Table");
        add("block.thaumcraft.table_stone", "Stone Table");
        add("block.thaumcraft.paving_stone_travel", "Paving Stone of Travel");
        add("block.thaumcraft.paving_stone_barrier", "Barrier Stone");
        add("block.thaumcraft.barrier", "Barrier");
        add("block.thaumcraft.amber_brick", "Amber Bricks");
        add("block.thaumcraft.flesh_block", "Block of Flesh");
        add("block.thaumcraft.effect_shock", "Static Field");
    }

    private void langOuterLands() {
        add("block.thaumcraft.obsidian_tile", "Obsidian Tile");
        add("block.thaumcraft.eldritch_stone", "Eldritch Stone");
        add("block.thaumcraft.eldritch_stone_inert", "Inert Eldritch Stone");
        add("block.thaumcraft.eldritch_rock", "Eldritch Rock");
        add("block.thaumcraft.eldritch_crust", "Eldritch Crust");
        add("block.thaumcraft.eldritch_crust_glowing", "Glowing Eldritch Crust");
        add("block.thaumcraft.stairs_eldritch", "Eldritch Stone Stairs");
        add("block.thaumcraft.eldritch_door", "Glowing Eldritch Stone");
        add("block.thaumcraft.eldritch_pedestal", "Eldritch Pedestal");
        add("block.thaumcraft.eldritch_stone_crystal", "Crystallized Eldritch Stone");
        add("block.thaumcraft.eldritch_nothing", "Nothingness");
        add("block.thaumcraft.eldritch_lock", "Eldritch Lock");
        add("block.thaumcraft.eldritch_crab_spawner", "Crusted Opening");
        add("block.thaumcraft.eldritch_trap", "Eldritch Stone");
        add("block.thaumcraft.eldritch_altar", "Eldritch Altar");
        add("block.thaumcraft.eldritch_obelisk", "Eldritch Obelisk");
        add("block.thaumcraft.eldritch_pillar", "Eldritch Pillar");
        add("block.thaumcraft.eldritch_capstone", "Eldritch Capstone");
        add("block.thaumcraft.eldritch_portal", "Eldritch Portal");
        add("item.thaumcraft.eldritch_eye", "Eldritch Eye");
        add("item.thaumcraft.runed_tablet", "Runed Tablet");
        add("tc.boss.warden", "A voice thunders through the halls: WHO DARES DISTURB MY SLUMBER?");
        add("tc.boss.golem", "You hear the grinding of ancient gears as something massive stirs...");
        add("tc.boss.crimson", "Chanting in an unknown tongue echoes from beyond the door...");
        add("tc.boss.taint", "A sickening squelching sound comes from the chamber beyond...");
        add("gui.thaumcraft.altar.not_enough_vis", "The local aura is too weak to tear open the veil. It needs at least 100 vis.");
    }

    private void langEldritch() {

        add("entity.thaumcraft.pech", "Pech Forager");
        add("entity.thaumcraft.pech.mage", "Pech Mage");
        add("entity.thaumcraft.pech.stalker", "Pech Stalker");
        add("entity.thaumcraft.eldritch_crab", "Eldritch Crab");
        add("entity.thaumcraft.inhabited_zombie", "Shambling Husk");
        add("entity.thaumcraft.eldritch_guardian", "Eldritch Guardian");
        add("entity.thaumcraft.cultist_knight", "Crimson Knight");
        add("entity.thaumcraft.cultist_cleric", "Crimson Cleric");
        add("entity.thaumcraft.cultist_portal_lesser", "Lesser Crimson Portal");
        add("entity.thaumcraft.eldritch_orb", "Eldritch Orb");
        add("entity.thaumcraft.golem_orb", "Arcane Orb");
        add("item.thaumcraft.pech_spawn_egg", "Pech Spawn Egg");
        add("item.thaumcraft.eldritch_crab_spawn_egg", "Eldritch Crab Spawn Egg");
        add("item.thaumcraft.inhabited_zombie_spawn_egg", "Shambling Husk Spawn Egg");
        add("item.thaumcraft.eldritch_guardian_spawn_egg", "Eldritch Guardian Spawn Egg");
        add("item.thaumcraft.cultist_knight_spawn_egg", "Crimson Knight Spawn Egg");
        add("item.thaumcraft.cultist_cleric_spawn_egg", "Crimson Cleric Spawn Egg");
        add("item.thaumcraft.cultist_portal_lesser_spawn_egg", "Lesser Crimson Portal Spawn Egg");
        add("item.thaumcraft.pech_wand", "Pech Wand");
        add("item.thaumcraft.crimson_blade", "Crimson Blade");
        add("item.thaumcraft.crimson_boots", "Crimson Cult Boots");
        add("item.thaumcraft.crimson_robe_helm", "Crimson Cult Hood");
        add("item.thaumcraft.crimson_robe_chest", "Crimson Cult Robe");
        add("item.thaumcraft.crimson_robe_legs", "Crimson Cult Leggings");
        add("item.thaumcraft.crimson_plate_helm", "Crimson Cult Helm");
        add("item.thaumcraft.crimson_plate_chest", "Crimson Cult Chestplate");
        add("item.thaumcraft.crimson_plate_legs", "Crimson Cult Greaves");
        add("enchantment.special.sapgreat", "Greater Sapping");
        add("item.curio.text", "What knowledge does this hold?");
        add("not.pechwand", "You cannot fathom the workings of this yet.");
        add("got.pechwand", "Hmmm, curious. I should investigate this further.");
        add("item.thaumcraft.loot_bag_common", "Common Treasure");
        add("item.thaumcraft.loot_bag_uncommon", "Uncommon Treasure");
        add("item.thaumcraft.loot_bag_rare", "Rare Treasure");
        add("tc.lootbag", "Click to open, or keep to trade.");
        add("champion.thaumcraft.name", "%1$s %2$s");
        add("champion.mod.bold", "Bold");
        add("champion.mod.spine", "Spined");
        add("champion.mod.armor", "Armored");
        add("champion.mod.mighty", "Mighty");
        add("champion.mod.grim", "Grim");
        add("champion.mod.warded", "Warded");
        add("champion.mod.warp", "Warped");
        add("champion.mod.undying", "Undying");
        add("champion.mod.fiery", "Fiery");
        add("champion.mod.sickly", "Sickly");
        add("champion.mod.venomous", "Venomous");
        add("champion.mod.vampiric", "Vampiric");
        add("champion.mod.infested", "Infested");
        add("champion.mod.tainted", "Tainted");
        add("attributes.thaumcraft.champion_mod", "Champion Modifier");
        add("attributes.thaumcraft.tainted_mod", "Tainted Modifier");
        add("block.thaumcraft.loot_crate_common", "Common Crate");
        add("block.thaumcraft.loot_crate_uncommon", "Uncommon Crate");
        add("block.thaumcraft.loot_crate_rare", "Rare Crate");
        add("block.thaumcraft.loot_urn_common", "Common Urn");
        add("block.thaumcraft.loot_urn_uncommon", "Uncommon Urn");
        add("block.thaumcraft.loot_urn_rare", "Rare Urn");
    
    }

    private void scanEntry(String entityPath, String title, String text) {
        add("research.thaumcraft.scanned/entity/" + entityPath + ".title", title);
        add("research.thaumcraft.scanned/entity/" + entityPath + ".stage_0", text);
    }

    private String dyeName(DyeColor dye) {
        String[] parts = dye.getName().split("_");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(StringUtils.capitalize(part));
        }
        return name.toString();
    }

    private void trait(String key, String name, String text) {
        add("golem.trait." + key, name);
        add("golem.trait.text." + key, text);
    }

    private void material(String key, String name, String text) {
        add("golem.material." + key, name);
        add("golem.material.text." + key, text);
    }

    private void head(String key, String name, String text) {
        add("golem.head." + key, name);
        add("golem.head.text." + key, text);
    }

    private void arm(String key, String name, String text) {
        add("golem.arm." + key, name);
        add("golem.arm.text." + key, text);
    }

    private void leg(String key, String name, String text) {
        add("golem.leg." + key, name);
        add("golem.leg.text." + key, text);
    }

    private void addon(String key, String name, String text) {
        add("golem.addon." + key, name);
        add("golem.addon.text." + key, text);
    }
}

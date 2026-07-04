package com.leclowndu93150.thaumcraft.data.lang;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangBCrystals;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangCStone;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangDTrees;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangDecor;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangFMetals;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangHContainers;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangGTools;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangMAuraHud;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangNScanning;
import com.leclowndu93150.thaumcraft.data.lang.fragments.TCLangTaint;
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
        add("recipe.type.construct", "Mystical Construct");
        add("wandtable.text1", "Vis Cost");
        add("gui.thaumcraft.research_table.title", "Research Table");
        add("gui.thaumcraft.research_table.inspiration", "Inspiration: %s");
        add("gui.thaumcraft.research_table.draw", "Draw");
        add("gui.thaumcraft.research_table.play", "Play");
        add("gui.thaumcraft.research_table.complete", "Complete Research");
        add("gui.thaumcraft.arcane_workbench.vis_available", "%s available");
        add("gui.thaumcraft.arcane_workbench.required_vis", "%s vis");
        add("gui.thaumcraft.arcane_workbench.required_vis_discount", "%s vis (%s% discount)");
        add("button.thaumcraft.create_theory", "Create Theory");
        add("button.thaumcraft.complete_theory", "Complete Theory");
        add("button.thaumcraft.scrap_theory", "Scrap Theory");
        add("block.thaumcraft.research_table", "Research Table");
        add("block.thaumcraft.arcane_workbench", "Arcane Workbench");
        add("block.thaumcraft.arcane_workbench_charger", "Workbench Charger");
        add("block.thaumcraft.crucible", "Crucible");
        add("block.thaumcraft.jar_normal", "Warded Jar");
        add("block.thaumcraft.jar_void", "Void Jar");
        add("item.thaumcraft.thaumonomicon", "Thaumonomicon");
        add("item.thaumcraft.jar_brace", "Jar Brace");
        add("item.thaumcraft.label", "Label");
        add("item.thaumcraft.salis_mundus", "Salis Mundus");
        add("tooltip.thaumcraft.salis_mundus.desc", "Magical dust that triggers transmutations when applied to certain blocks.");
        add("item.thaumcraft.nugget_quartz", "Quartz Sliver");
        add("item.thaumcraft.vis_resonator", "Vis Resonator");
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

        add("block.thaumcraft.ore_amber", "Amber Bearing Stone");
        add("block.thaumcraft.ore_cinnabar", "Cinnabar Ore");
        add("block.thaumcraft.ore_quartz", "Quartz Ore");
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
        add("item.thaumcraft.amber", "Amber");
        add("item.thaumcraft.cinnabar", "Native Cinnabar Cluster");

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
        add("key.categories.thaumcraft", "Thaumcraft");
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
        add("jei.thaumcraft.category.aspect_composition", "Aspect Composition");
        add("jei.thaumcraft.category.aspect_from_stacks", "Aspect from ItemStack");
        add("jei.thaumcraft.dust_trigger.usage", "Right-click the target block with Salis Mundus to trigger this transmutation.");
        add("jei.thaumcraft.dust_trigger.target.tag", "Any block in tag %1$s");
        add("jei.thaumcraft.dust_trigger.target.multiblock", "Multiblock blueprint: %1$s");
        add("jei.thaumcraft.research.missing_research", "Missing research: ");
        add("tooltip.thaumcraft.aspects.header", "Aspects:");

        TCLangBCrystals.register(this);
        TCLangCStone.register(this);
        TCLangFMetals.register(this);
        TCLangDTrees.register(this);
        TCLangMAuraHud.register(this);
        TCLangHContainers.register(this);
        TCLangTaint.register(this);
        TCLangGTools.register(this);
        TCLangNScanning.register(this);
        TCLangDecor.register(this);
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
}

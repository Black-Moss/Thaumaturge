package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangCasters {
    private TCLangCasters() {}

    public static void register(LanguageProvider provider) {
        provider.add("key.category.thaumcraft.main", "Thaumcraft");
        provider.add("key.thaumcraft.change_focus", "Change Caster Focus");
        provider.add("key.thaumcraft.misc_toggle", "Misc Caster Toggle");
        provider.add("item.thaumcraft.caster_basic", "Caster's Gauntlet");
        provider.add("item.thaumcraft.focus_1", "Blank Lesser Focus");
        provider.add("item.thaumcraft.focus_2", "Blank Advanced Focus");
        provider.add("item.thaumcraft.focus_3", "Blank Greater Focus");
        provider.add("tooltip.thaumcraft.caster.vis_cost", "Vis cost: %s Vis per cast");
        provider.add("tooltip.thaumcraft.focus.vis_cost", "%s Vis per cast");
        provider.add("entity.thaumcraft.focus_projectile", "Focus Projectile");
        provider.add("entity.thaumcraft.focus_cloud", "Focus Cloud");
        provider.add("entity.thaumcraft.focus_mine", "Focus Mine");
        provider.add("entity.thaumcraft.spell_bat", "Spellbat");

        provider.add("block.thaumcraft.hole", "Dimensional Tear");
        provider.add("block.thaumcraft.effect_sap", "Sapping Field");

        provider.add("focus.thaumcraft.root.name", "Caster");
        provider.add("focus.thaumcraft.root.text", "The caster and point of origin of the spell.");
        provider.add("focus.thaumcraft.touch.name", "Touch");
        provider.add("focus.thaumcraft.touch.text", "Allows you to affect things you can touch, within roughly 4 blocks of the point of origin.");
        provider.add("focus.thaumcraft.projectile.name", "Projectile");
        provider.add("focus.thaumcraft.projectile.text", "Gathers the energy of the focus into a magical orb that you can throw like a projectile. This projectile travels slowly and is affected by gravity.");
        provider.add("focus.thaumcraft.bolt.name", "Bolt");
        provider.add("focus.thaumcraft.bolt.text", "Hurl magic directly at your target as a concentrated bolt of energy. The effect is instantaneous, but the range is limited to 16 blocks.");
        provider.add("focus.thaumcraft.plan.name", "Plan");
        provider.add("focus.thaumcraft.plan.text", "Allows you to plan exactly which blocks will be affected.");
        provider.add("focus.thaumcraft.cloud.name", "Cloud");
        provider.add("focus.thaumcraft.cloud.text", "Creates a lingering cloud of magical energy that effects anything inside.");
        provider.add("focus.thaumcraft.mine.name", "Arcane Mine");
        provider.add("focus.thaumcraft.mine.text", "Creates a mystical construct that detonates when a hostile entity passes nearby, releasing the effects upon it.");
        provider.add("focus.thaumcraft.spellbat.name", "Summon Spellbat");
        provider.add("focus.thaumcraft.spellbat.text", "Conjures a mystical bat that will hunt down enemies and inflict them with the focus's effects.");

        provider.add("focus.thaumcraft.air.name", "Air");
        provider.add("focus.thaumcraft.air.text", "Creates a blast of air that knocks things back, but causes only minor damage.");
        provider.add("focus.thaumcraft.earth.name", "Earth");
        provider.add("focus.thaumcraft.earth.text", "Hurls a blast of earthen shrapnel that causes significant damage and may break weaker blocks.");
        provider.add("focus.thaumcraft.fire.name", "Fire");
        provider.add("focus.thaumcraft.fire.text", "Hurls flame at your target and sets it alight.");
        provider.add("focus.thaumcraft.frost.name", "Frost");
        provider.add("focus.thaumcraft.frost.text", "Throws chilling cold at your target, causing damage and freezing it. Freezes water and will slow down creatures.");
        provider.add("focus.thaumcraft.break.name", "Break");
        provider.add("focus.thaumcraft.break.text", "Summons disruptive energy that breaks down most materials.");
        provider.add("focus.thaumcraft.curse.name", "Curse");
        provider.add("focus.thaumcraft.curse.text", "Summons the powers of entropy to harm and disrupt the targeted creature.");
        provider.add("focus.thaumcraft.flux.name", "Flux");
        provider.add("focus.thaumcraft.flux.text", "This effect conjures raw, unfocused vis that disrupts living (and dead) creatures. This energy cannot interact with inanimate objects, but it does bypass mundane armor.");
        provider.add("focus.thaumcraft.rift.name", "Rift");
        provider.add("focus.thaumcraft.rift.text", "Shifts matter into an alternate reality, creating temporary 'holes' through which you can travel.");
        provider.add("focus.thaumcraft.exchange.name", "Exchange");
        provider.add("focus.thaumcraft.exchange.text", "Swap one type of block in the world for another.");
        provider.add("focus.thaumcraft.heal.name", "Heal");
        provider.add("focus.thaumcraft.heal.text", "This effect heals living creatures and harms undead.");

        provider.add("focus.thaumcraft.scatter.name", "Scatter");
        provider.add("focus.thaumcraft.scatter.text", "Split a single trajectory into multiple random trajectories.");
        provider.add("focus.thaumcraft.split_target.name", "Split Target");
        provider.add("focus.thaumcraft.split_target.text", "Split a single target into two weaker ones.");
        provider.add("focus.thaumcraft.split_trajectory.name", "Split Trajectory");
        provider.add("focus.thaumcraft.split_trajectory.text", "Split a single trajectory into two weaker ones.");

        provider.add("focus.common.power", "Power");
        provider.add("focus.common.duration", "Duration (seconds)");
        provider.add("focus.common.radius", "Radius (blocks)");
        provider.add("focus.common.silk", "Silk Touch");
        provider.add("focus.common.no", "No");
        provider.add("focus.common.yes", "Yes");
        provider.add("focus.common.fortune", "Fortune");
        provider.add("focus.common.target", "Target Type");
        provider.add("focus.common.friend", "Friendly");
        provider.add("focus.common.enemy", "Non-friendly");
        provider.add("focus.common.none", "None");
        provider.add("focus.common.options", "Options");
        provider.add("focus.fire.burn", "Burn duration (seconds)");
        provider.add("focus.scatter.cone", "Spread angle (degrees)");
        provider.add("focus.scatter.forks", "Trajectory forks");
        provider.add("focus.projectile.speed", "Projectile speed");
        provider.add("focus.projectile.bouncy", "Bouncy");
        provider.add("focus.projectile.seeking.friendly", "Seek friendly");
        provider.add("focus.projectile.seeking.hostile", "Seek hostile");
        provider.add("focus.break.power", "Breaking strength");
        provider.add("focus.plan.method", "Planning method");
        provider.add("focus.plan.full", "Full");
        provider.add("focus.plan.surface", "Surface");
        provider.add("focus.rift.depth", "Depth");
        provider.add("focus.heal.power", "Healing");

        provider.add("death.attack.thaumcraft.focus_fire", "%1$s was burned to a crisp by magic");
        provider.add("death.attack.thaumcraft.focus_fire.player", "%1$s was burned to a crisp by %2$s's magic");
        provider.add("block.thaumcraft.focal_manipulator", "Focal Manipulator");
        provider.add("gui.thaumcraft.wandtable.craft", "Start Crafting");
        provider.add("gui.thaumcraft.wandtable.complexity", "Total Complexity");
        provider.add("gui.thaumcraft.wandtable.xp_cost", "Experience Cost");
        provider.add("gui.thaumcraft.wandtable.vis_cost", "Vis Cost");
        provider.add("gui.thaumcraft.wandtable.cast_cost", "Vis per cast: %s");
        provider.add("gui.thaumcraft.wandtable.components", "Crystals Required");
        provider.add("gui.thaumcraft.wandtable.part_complexity", "Complexity:");
        provider.add("gui.thaumcraft.wandtable.part_efficiency", "Effect Multiplier:");
        provider.add("gui.thaumcraft.wandtable.heal_power", "Healing");
        provider.add("gui.thaumcraft.wandtable.problem.in_progress", "Crafting in progress...");
        provider.add("gui.thaumcraft.wandtable.problem.complexity", "Too complex: %s/%s");
        provider.add("gui.thaumcraft.wandtable.problem.empty_nodes", "The spell has unfilled nodes");
        provider.add("gui.thaumcraft.wandtable.problem.crystal", "Missing %sx %s");
        provider.add("gui.thaumcraft.wandtable.problem.no_effects", "The spell needs at least one effect");
        provider.add("gui.thaumcraft.wandtable.problem.xp", "Requires %s experience levels");
        provider.add("gui.thaumcraft.wandtable.problem.ready", "Ready to craft!");
    }
}

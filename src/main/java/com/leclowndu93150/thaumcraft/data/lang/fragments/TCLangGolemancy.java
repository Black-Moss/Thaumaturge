package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangGolemancy {
    private TCLangGolemancy() {}

    public static void register(LanguageProvider provider) {
        provider.add("item.thaumcraft.mind_clockwork", "Clockwork Mind");
        provider.add("item.thaumcraft.mind_biothaumic", "Biothaumic Mind");
        provider.add("item.thaumcraft.module_vision", "Vision Module");
        provider.add("item.thaumcraft.module_aggression", "Aggression Module");
        provider.add("item.thaumcraft.golem_bell", "Golemancer's Bell");
        provider.add("item.thaumcraft.golem", "Golem");
        provider.add("item.thaumcraft.seal_blank", "Blank Seal");
        provider.add("item.thaumcraft.seal_pickup", "Control Seal: Collect");
        provider.add("item.thaumcraft.seal_pickup_advanced", "Advanced Control Seal: Collect");
        provider.add("item.thaumcraft.seal_fill", "Control Seal: Store");
        provider.add("item.thaumcraft.seal_fill_advanced", "Advanced Control Seal: Store");
        provider.add("item.thaumcraft.seal_empty", "Control Seal: Empty");
        provider.add("item.thaumcraft.seal_empty_advanced", "Advanced Control Seal: Empty");
        provider.add("item.thaumcraft.seal_harvest", "Control Seal: Harvest");
        provider.add("item.thaumcraft.seal_butcher", "Control Seal: Butcher");
        provider.add("item.thaumcraft.seal_guard", "Control Seal: Guard");
        provider.add("item.thaumcraft.seal_guard_advanced", "Advanced Control Seal: Guard");
        provider.add("item.thaumcraft.seal_lumber", "Control Seal: Lumberjack");
        provider.add("item.thaumcraft.seal_breaker", "Control Seal: Block Breaker");
        provider.add("item.thaumcraft.seal_breaker_advanced", "Control Seal: Advanced Block Breaker");
        provider.add("item.thaumcraft.seal_use", "Control Seal: Use");
        provider.add("item.thaumcraft.seal_provider", "Control Seal: Provide");
        provider.add("item.thaumcraft.seal_stock", "Control Seal: Stock");
        provider.add("block.thaumcraft.levitator", "Arcane Levitator");
        provider.add("block.thaumcraft.golem_builder", "Golem Press");
        provider.add("block.thaumcraft.placeholder_iron_bars", "Iron Bars");
        provider.add("block.thaumcraft.placeholder_cauldron", "Cauldron");
        provider.add("block.thaumcraft.placeholder_anvil", "Anvil");
        provider.add("block.thaumcraft.placeholder_smithing_table", "Smithing Table");
        provider.add("entity.thaumcraft.golem", "Thaumcraft Golem");
        provider.add("entity.thaumcraft.golem_dart", "Golem Dart");
        provider.add("golem.follow", "I'm coming, Master!");
        provider.add("golem.stay", "I will stay here, Master.");
        provider.add("golem.rank", "Rank");
        provider.add("tc.notowned", "This does not belong to you.");
        provider.add("gui.thaumcraft.levitator", "Range set to %s blocks. (%s vis/minute)");
        trait(provider, "smart", "Smart", "This golem is nearly sentient, with superior reasoning and decision-making capabilities.");
        trait(provider, "deft", "Deft", "This golem has a great deal of manual dexterity and can perform tasks requiring precision and a delicate touch. Counters and countered by Clumsy.");
        trait(provider, "clumsy", "Clumsy", "This golem has almost no manual dexterity and can only perform simple tasks requiring basic interactions. Counters and countered by Deft.");
        trait(provider, "fighter", "Fighter", "The golem is capable of taking hostile action against other entities.");
        trait(provider, "wheeled", "Wheeled", "The golem propels itself using wheels. This gives it greater speed, but it is unable to jump or navigate steep slopes.");
        trait(provider, "flyer", "Flyer", "The golem is capable of flight, giving it greater mobility at the cost of speed.");
        trait(provider, "climber", "Climber", "Sheer cliffs do not deter this golem. It can easily scale them to get to its destination.");
        trait(provider, "heavy", "Heavy Frame", "The golem is heavier than average, which reduces its speed and agility. Counters and countered by Light.");
        trait(provider, "light", "Light Frame", "The golem is lighter than average, which gives it increased speed and agility. Counters and countered by Heavy.");
        trait(provider, "fragile", "Fragile", "The golem is built with delicate components or weak materials that reduce its life total and armour rating. Counters and countered by Armored.");
        trait(provider, "repair", "Improved Self Repair", "The golem repairs any damage suffered at more than double the normal rate.");
        trait(provider, "scout", "Scout", "The golem has a greater visual range and can operate in a much wider area: 48 blocks away from its home location, instead of 32.");
        trait(provider, "armored", "Armored", "The golem is reinforced with additional material which increases its armor rating. Counters and countered by Fragile.");
        trait(provider, "brutal", "Brutal", "The golem inflicts greater melee damage in combat.");
        trait(provider, "fireproof", "Fireproof", "The golem is immune to fire damage.");
        trait(provider, "breaker", "Breaker", "The golem is capable of destroying most blocks with ease.");
        trait(provider, "hauler", "Hauler", "Allows the golem to carry two stack of items instead of one.");
        trait(provider, "ranged", "Ranged", "The golem can attack targets at range. ");
        trait(provider, "blastproof", "Blast-proof", "The golem is highly resistant to explosion damage.");
        material(provider, "wood", "Greatwood", "The golem is crafted from greatwood. It is light and agile, but not particularly sturdy.");
        material(provider, "iron", "Iron", "The golem is crafted from iron. It is sturdy and fireproof, but heavy.");
        material(provider, "clay", "Clay", "The golem is crafted from baked clay. It is not a particularly sturdy material, but the resulting golem is fireproof and relatively light.");
        material(provider, "brass", "Brass", "The golem is crafted from brass. It is not as sturdy as iron nor as resistant to fire. Normally brass is heavier than iron, but it allows for superior construction methods which results in a much lighter frame.");
        material(provider, "thaumium", "Thaumium", "The golem is crafted from thaumium. It shares many characteristics with iron, though it is sturdier and more resistant to damage.");
        material(provider, "void", "Void Metal", "The golem is crafted from void metal. Slightly softer than iron, this metal makes up for it by being lighter and able to repair itself.");
        head(provider, "basic", "Clockwork Head", "The default golem head. No particular strengths or weaknesses.");
        head(provider, "smart", "Smart Head", "This head contains an advanced biothaumic brain, giving the golem greater capabilities and the ability to learn. ");
        head(provider, "smart_armored", "Armored Smart Head", "The smart head enhanced with additional armor and padding.");
        head(provider, "scout", "Perceptive Head", "The basic clockwork head enhanced with improved biothaumic eyes. ");
        head(provider, "smart_scout", "Biothaumic Head", "Using both biothaumic eyes and brain, this head is the pinnacle of the biothaumic art. ");
        arm(provider, "basic", "Basic Arms", "The default golem arms and hands. No particular strengths or weaknesses.");
        arm(provider, "fine", "Fine Manipulators", "These arms end in delicate and dexterous hands. ");
        arm(provider, "claws", "Claw Arms", "These arms end in a terrifying pair of razor sharp metal pincers. Comes with a built-in aggression module.");
        arm(provider, "breakers", "Breaker Arms", "These arms end in a pair of pneumatic, diamond tipped grinders. ");
        arm(provider, "darts", "Dart Launchers", "These arms end in a pair of pneumatic dart launchers. The darts are magically created as they are needed. Comes with a built-in aggression module.");
        leg(provider, "walker", "Basic Legs", "A pair of simple legs. No particular strengths or weaknesses.");
        leg(provider, "roller", "Uni-wheel ", "A single wheel. Quite fast, but incapable of jumping or going up steep hills.");
        leg(provider, "climber", "Climbing Legs", "A pair of simple legs enhanced with crampons and other devices allowing for vertical ascent.");
        leg(provider, "flyer", "Levitation Module", "A modified arcane levitator granting the golem the power of flight. Reduces speed by a third.");
        addon(provider, "none", "None", "No addon installed.");
        addon(provider, "armored", "Armor Plating", "Grants the golem increased durability.");
        addon(provider, "fighter", "Aggression Module", "Allows the golem to engage in combat.");
        addon(provider, "hauler", "Carry Frame", "Allows the golem to carry two stack of items instead of one.");
        provider.add("golem.prop.replant", "Replant crops");
        provider.add("golem.prop.cycle", "Cycle whitelist");
        provider.add("golem.prop.meta", "Use metadata");
        provider.add("golem.prop.nbt", "Use NBT data");
        provider.add("golem.prop.ore", "Use Ore Dictionary");
        provider.add("golem.prop.mod", "Use from same mod");
        provider.add("golem.prop.mob", "Target Mobs ");
        provider.add("golem.prop.animal", "Target Animals");
        provider.add("golem.prop.player", "Target Players");
        provider.add("golem.prop.left", "Left click");
        provider.add("golem.prop.empty", "Click empty air");
        provider.add("golem.prop.emptyhand", "Can use empty hand");
        provider.add("golem.prop.sneak", "Simulate sneaky click");
        provider.add("golem.prop.single", "Single item only");
        provider.add("golem.prop.provision", "Request provisioning");
        provider.add("golem.prop.provision.wl", "Request provisioning from whitelist");
        provider.add("golem.prop.priority", "Task Priority");
        provider.add("golem.prop.color", "Set for %s golems");
        provider.add("golem.prop.colorall", "All golems");
        provider.add("golem.prop.owner", "You own this seal");
        provider.add("golem.prop.lock", "Only golems owned by seal owner can perform these tasks");
        provider.add("golem.prop.unlock", "All golems can perform these tasks");
        provider.add("golem.prop.redon", "Redstone sensitive");
        provider.add("golem.prop.redoff", "Ignores Redstone signals");
        provider.add("golem.prop.exist", "Container must already contain item");
        provider.add("golem.prop.leave", "Always leave at least 1 item");
        provider.add("golem.prop.silk", "Use Silk Touch");
        provider.add("gui.thaumcraft.seal", "Seal");
        provider.add("golem.prop.blacklist", "Blacklist");
        provider.add("golem.prop.whitelist", "Whitelist");
        provider.add("button.category.0", "Priority/Locking");
        provider.add("button.category.1", "Filter");
        provider.add("button.category.2", "Area");
        provider.add("button.category.3", "Options");
        provider.add("button.category.4", "Requirements");
        provider.add("button.caption.x", "East / West");
        provider.add("button.caption.y", "Up / Down");
        provider.add("button.caption.z", "North / South");
        provider.add("button.caption.required", "Required");
        provider.add("button.caption.forbidden", "Forbidden");
    }

    private static void trait(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.trait." + key, name);
        provider.add("golem.trait.text." + key, text);
    }

    private static void material(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.material." + key, name);
        provider.add("golem.material.text." + key, text);
    }

    private static void head(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.head." + key, name);
        provider.add("golem.head.text." + key, text);
    }

    private static void arm(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.arm." + key, name);
        provider.add("golem.arm.text." + key, text);
    }

    private static void leg(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.leg." + key, name);
        provider.add("golem.leg.text." + key, text);
    }

    private static void addon(LanguageProvider provider, String key, String name, String text) {
        provider.add("golem.addon." + key, name);
        provider.add("golem.addon.text." + key, text);
    }
}

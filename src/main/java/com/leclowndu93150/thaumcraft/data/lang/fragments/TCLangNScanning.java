package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangNScanning {
    private TCLangNScanning() {}

    public static void register(LanguageProvider provider) {
        provider.add("tc.unknownobject", "Nothing new can be learned from this.");
        provider.add("tc.knownobject", "You have learned something new.");
        provider.add("tc.invtoolarge", "Inventory too large. Only scanning first 100 items.");
        provider.add("tc.celestial.fail.1", "You have already studied that today.");
        provider.add("tc.celestial.fail.2", "You are unable to take notes of your studies.");

        provider.add("item.thaumcraft.celestial_notes", "Celestial Notes");
        provider.add("item.thaumcraft.celestial_notes.sun.text", "Solar");
        provider.add("item.thaumcraft.celestial_notes.stars_1.text", "Stellar, Northern Quadrant");
        provider.add("item.thaumcraft.celestial_notes.stars_2.text", "Stellar, Southern Quadrant");
        provider.add("item.thaumcraft.celestial_notes.stars_3.text", "Stellar, Western Quadrant");
        provider.add("item.thaumcraft.celestial_notes.stars_4.text", "Stellar, Eastern Quadrant");
        provider.add("item.thaumcraft.celestial_notes.moon_1.text", "Lunar, Full");
        provider.add("item.thaumcraft.celestial_notes.moon_2.text", "Lunar, Waning Gibbous");
        provider.add("item.thaumcraft.celestial_notes.moon_3.text", "Lunar, Third Quarter");
        provider.add("item.thaumcraft.celestial_notes.moon_4.text", "Lunar, Waning Crescent");
        provider.add("item.thaumcraft.celestial_notes.moon_5.text", "Lunar, New");
        provider.add("item.thaumcraft.celestial_notes.moon_6.text", "Lunar, Waxing Crescent");
        provider.add("item.thaumcraft.celestial_notes.moon_7.text", "Lunar, First Quarter");
        provider.add("item.thaumcraft.celestial_notes.moon_8.text", "Lunar, Waxing Gibbous");

        provider.add("card.thaumcraft.celestial.name", "Celestial Studies");
        provider.add("card.thaumcraft.celestial.text",
                "You take some of the celestial notes you have made and compare them for possible correlations with your primary research category. You gain 25 to 50 inspiration toward %1$s. You may gain other things as well...");

        provider.add("research.thaumcraft.celestial_scanning.title", "Celestial Observation");
        provider.add("research.thaumcraft.celestial_scanning.stage_0",
                "I have noticed a link between the phases of the moon and the speed at which the aura replenishes itself. I should investigate this phenomenon further and even expand my investigation to see if other heavenly bodies influence magic.<BR>A few minor tweaks to my thaumometer should enable it to take measurements from objects in the sky. I should remember to carry some paper and scribing tools with me so I can take notes.<BR>Measuring the same body or quadrant of the sky more than once a day will not give me new insights - I need to study them over time to measure changes in their aural influence.<BR>The notes I gather should come in handy for theorycrafting.");
        provider.add("research.thaumcraft.flux.title", "Flux");
        provider.add("research.thaumcraft.flux.stage_0",
                "I have been monitoring a strange buildup of magical energy in the aura. It seems to be a form of 'dirty' vis that I have named Flux. It shows as an ominous dark bar on my thaumometer.<BR>I now know what those purple puffs are I occasionally see when I am being careless - it is essentia spilling into the atmosphere and becoming Flux. Pure essentia, it seems, should not be directly exposed to the aura.<BR>I'm not exactly sure what negative effects this will cause, but I have noticed that Flux seems to clog the local aura preventing Vis from replenishing naturally.<BR>Picture it as a thick layer of scum on a natural pool of clean water.<BR>The local aura has a point at which it is 'filled' and Flux seems to count towards this capacity.<BR>More alarmingly, it seems to take precedence over vis and I suspect it can probably exceed this natural limit.<BR>I am not sure what will happen when Flux overflows, but it it bound to be interesting.");
        provider.add("research.thaumcraft.flux.warn", "Something seems to be building up in the aura. Something bad.");

        scanEntry(provider, "thaumcraft/thaumic_slime", "Thaumic Slime",
                "These purple slimes are a strange offshoot of their green cousins. Normal slimes form from the organic gunk that occasionally collects in caves, but Thaumic Slimes are formed from the purple proto-matter residue left over from magical mishaps.<BR>Apart from their strange origins they are rather similar to their green cousins for the most part. They do appear to have the ability to split at will, often spitting a lesser version of themselves at targets that venture too close. If there are no targets nearby, split slimes will endeavour to reform to once again form a larger organism.");
        scanEntry(provider, "thaumcraft/taint_seed", "Taint Seed",
                "Occasionally tight knots of Tainted tendrils will form. From out of this 'seed' foul tainted air will spew which acts as the basis for new Taint growth.<BR>These seeds can lash out to defend themselves, but the risk must be taken if you wish to rid an area of taint.<BR>Without the foul air these seeds create, tainted growth soon withers and dies.");
        scanEntry(provider, "thaumcraft/taint_crawler", "Tainted Crawler",
                "These disgusting grubs secrete corrupting Taint wherever they go. Most of the time the Taint is short-lived if they wander too far from the core of the Taint growth, but other times this can cause the Taint to spread much quicker than it normally would.<BR>It is best to destroy them on sight.");
        scanEntry(provider, "thaumcraft/taintacle", "Taintacle",
                "These dangerous growths look like giant tentacles. They will lash out at anything that comes nearby.<BR>Though they are immobile, keeping your distance might not keep you safe as miniature versions of them will spawn all around you should you draw their attention.");
        scanEntry(provider, "thaumcraft/taint_swarm", "Taint Swarm",
                "Sometimes purple hive-like growths will form on the remains of trees within Taint. Swarms of tiny biting creatures will spawn from these growths and rove around in aggressive swarms.<BR>They move fast, are hard to avoid and even harder to hit. Fortunately the individual creatures are not particularly robust so a properly prepared traveller should be able to dispatch them easily.");
        scanEntry(provider, "thaumcraft/wisp", "Wisp",
                "These strange creatures appear to be floating points of light and are often found near high concentrations of vis. Honestly I am not sure if they are creatures at all - they move randomly and do not appear to exhibit the normal characteristics of a living organism. They do however react to being attacked and occasionally become aggressive for no apparent reason.<BR>My initial thoughts are they might be a form of proto-life made up of vis instead of normal matter, but that cannot be the whole picture as I am detecting strange dimensional energies at their core.");
        scanEntry(provider, "thaumcraft/pech", "The Pech",
                "These strange humanoids can be found wherever the veil between worlds is at its thinnest. They seem drawn to the magical energies that abound in such places.<IMG>thaumcraft:textures/research/research5.png:128:136:108:117:.75</IMG>Don't let their small stature fool you. They have incredible strength and can carry many times their own bodyweight.<BR>Pech are normally not aggressive, but when riled up they will band together and hunt down their attacker. Under normal conditions they avoid people at all costs.<BR>Pechs are notoriously avaricious, and will loot anything not nailed down, though they prefer precious things. It is said that if you feed this desire for material wealth it could be possible to befriend one.<BR>Once befriended they often carry wondrous objects that they might be willing to part with.<BR>It should be noted that such 'friendships' seldom last long.");
        scanEntry(provider, "thaumcraft/firebat", "Firebat",
                "Fire bats, or Hellbats, are usually found in the Nether. Engulfed in flame and unreasonably aggressive they are perfectly suited to the environment they live in. They also have the alarming tendency to explode when attacking.<BR>One wonders what possible evolutionary advantage exploding gives them as a species, but the same can be asked of creepers.");
        scanEntry(provider, "thaumcraft/cultist", "Crimson Cultist",
                "These red-robed cultists have been popping up of late. Their goals are unknown to me and they seem unwilling to talk. Beyond that there is something... wrong about them. Their grip on sanity seems tenuous at best, but the wrongness goes much deeper than that. It is hard to describe.<BR>My presence seems to enrage them and I believe it might have something to do with my capacity for thaumaturgy.<BR>Their clerics are also capable of wielding magic and I can sense that they are drawing it from the aura, but the magic itself does not seem to be thaumaturgy. Whatever it is, it makes my skin crawl and I can feel the aura buckling and tearing every time they draw upon it.<BR>Some of them seem to carry a book around. I should try and get my hand on one of those, maybe it will tell me more about this strange cult. I should be careful though, some knowledge can be dangerous.");
        scanEntry(provider, "thaumcraft/brainy_zombie", "Angry Zombie",
                "I have observed that some zombies seem much more aware than their lumbering brethren. Their eyes burn with a malevolent intelligence leading me to believe that they retained much more intelligence after death than is normal. They are definitely more aggressive and dangerous than a normal zombie.<BR>They should make good sources of brain matter for my experiments.");
        scanEntry(provider, "thaumcraft/eldritch_guardian", "Eldritch Guardian",
                "These ghost-like figures are a dangerous menace. They can be found wherever the walls of reality are at their thinnest. I believe they act as guardians of such places, protecting those weak spots from trespassers.<BR>Their form is held together by dimensional energies from some other place making them much weaker in our world. I would hate having to confront them in whatever place they call home.<BR>Their touch is deadly and they are able to hurl entropic dimensional energy at those too far to reach.");
        scanEntry(provider, "thaumcraft/eldritch_crab", "Eldritch Crab",
                "Eldritch Crabs, or Helmed Crabs, are a most unusual and unnatural creature. I am convinced these creatures are the result of magical tinkering.<BR>A pair of powerful claws make them dangerous to face in combat, but it is how they use those claws that make them horrific. They leap at their foes heads in an attempt to decapitate them. If successful they are somehow able to burrow into the victims body and take control of what remains. Their abdomen looks much like a helmed head allowing them to disguise the true nature of the husk they now control.<BR>These husks are not much more powerful than a normal zombie so the reason why they do this eludes me - they are much more of a threat without their 'mount'. Possibly they were created as weapons of terror by some demented inventor?");
    }

    private static void scanEntry(LanguageProvider provider, String entityPath, String title, String text) {
        provider.add("research.thaumcraft.scanned/entity/" + entityPath + ".title", title);
        provider.add("research.thaumcraft.scanned/entity/" + entityPath + ".stage_0", text);
    }
}

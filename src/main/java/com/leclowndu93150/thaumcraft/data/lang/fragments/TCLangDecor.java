package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

public final class TCLangDecor {
    private TCLangDecor() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.dioptra", "Thaumic Dioptra");
        provider.add("block.thaumcraft.vis_battery", "Vis Battery");
        provider.add("block.thaumcraft.matrix_speed", "Infusion Speed Stone");
        provider.add("block.thaumcraft.matrix_cost", "Infusion Cost Stone");
        provider.add("block.thaumcraft.jar_brain", "Brain in a Jar");
        provider.add("block.thaumcraft.arcane_ear", "Arcane Ear");
        provider.add("block.thaumcraft.arcane_ear_toggle", "Arcane Ear (Toggle)");
        provider.add("block.thaumcraft.lamp_arcane", "Arcane Lamp");
        provider.add("block.thaumcraft.lamp_growth", "Lamp of Growth");
        provider.add("block.thaumcraft.lamp_fertility", "Lamp of Fertility");
        provider.add("block.thaumcraft.centrifuge", "Essentia Centrifuge");
        provider.add("block.thaumcraft.hungry_chest", "Hungry Chest");
        provider.add("item.thaumcraft.tallow", "Magic Tallow");
        provider.add("item.thaumcraft.warping", "Warping");
        provider.add("effect.thaumcraft.thaumarhia", "Thaumorrhea");
        provider.add("effect.thaumcraft.unnatural_hunger", "Unnatural Hunger");
        provider.add("effect.thaumcraft.sun_scorned", "Sun Scorned");
        provider.add("effect.thaumcraft.death_gaze", "Deadly Gaze");
        provider.add("effect.thaumcraft.blurred_vision", "Blurred Vision");
        provider.add("effect.thaumcraft.warp_ward", "Warp Ward");
        provider.add("warp.thaumcraft.gain.permanent", "You have gained permanent Warp!");
        provider.add("warp.thaumcraft.gain.normal", "You have gained Warp!");
        provider.add("warp.thaumcraft.gain.temporary", "You have gained temporary Warp!");
        provider.add("warp.thaumcraft.lose.permanent", "You have lost permanent Warp!");
        provider.add("warp.thaumcraft.lose.normal", "You have lost Warp!");
        provider.add("warp.thaumcraft.lose.temporary", "You have lost temporary Warp!");
        provider.add("warp.thaumcraft.text.1", "You feel oddly drained.");
        provider.add("warp.thaumcraft.text.2", "A sudden and unnatural hunger consumes you.");
        provider.add("warp.thaumcraft.text.4", "Your vision becomes strange and grim.");
        provider.add("warp.thaumcraft.text.5", "The light suddenly becomes overwhelmingly bright and burns your skin.");
        provider.add("warp.thaumcraft.text.6", "A thick fog appears from nowhere. Something stirs in its depths.");
        provider.add("warp.thaumcraft.text.7", "They're everywhere! Run!");
        provider.add("warp.thaumcraft.text.9", "You suddenly feel reluctant to break things.");
        provider.add("warp.thaumcraft.text.10", "Your perception suddenly expands.");
        provider.add("warp.thaumcraft.text.11", "What was that noise? Something is behind you.");
        provider.add("warp.thaumcraft.text.12", "Something is following you.");
        provider.add("warp.thaumcraft.text.13", "Something is watching you. Maybe it is time to stop.");
        provider.add("warp.thaumcraft.text.14", "You have a moment of clarity.");
        provider.add("warp.thaumcraft.text.15", "Your stomach suddenly gurgles very strangely.");
        provider.add("warp.thaumcraft.text.16", "The faint sound of chanting can be heard nearby.");
        provider.add("warp.thaumcraft.text.hunger.1", "Your hunger cannot be satisfied with normal food.");
        provider.add("warp.thaumcraft.text.hunger.2", "Your hunger begins to fade.");
        provider.add("warp.thaumcraft.fluxevent.2", "You feel something invading your mind and sapping your will.");
        provider.add("warp.thaumcraft.fluxevent.3", "You feel a sudden release of magical tension nearby.");
        provider.add("entity.thaumcraft.flux_rift", "Flux Rift");
        provider.add("item.thaumcraft.void_seed", "Void Seed");
        provider.add("item.thaumcraft.causality_collapser", "Causality Collapser");
        provider.add("block.thaumcraft.infusion_matrix", "Runic Matrix");
        provider.add("block.thaumcraft.pedestal_arcane", "Arcane Pedestal");
        provider.add("block.thaumcraft.recharge_pedestal", "Recharge Pedestal");
        provider.add("block.thaumcraft.pedestal_ancient", "Ancient Pedestal");
        provider.add("block.thaumcraft.pedestal_eldritch", "Eldritch Pedestal");
        provider.add("block.thaumcraft.pillar_arcane", "Infusion Pillar");
        provider.add("block.thaumcraft.pillar_ancient", "Ancient Infusion Pillar");
        provider.add("block.thaumcraft.pillar_eldritch", "Eldritch Infusion Pillar");
        provider.add("gui.thaumcraft.infusion.instability", "Instability: %s");
        provider.add("gui.thaumcraft.infusion.instability.0", "Negligible");
        provider.add("gui.thaumcraft.infusion.instability.1", "Minor");
        provider.add("gui.thaumcraft.infusion.instability.2", "Moderate");
        provider.add("gui.thaumcraft.infusion.instability.3", "High");
        provider.add("gui.thaumcraft.infusion.instability.4", "Very High");
        provider.add("gui.thaumcraft.infusion.instability.5", "Dangerous");
        provider.add("gui.thaumcraft.infusion.stability.very_stable", "Very Stable");
        provider.add("gui.thaumcraft.infusion.stability.stable", "Stable");
        provider.add("gui.thaumcraft.infusion.stability.unstable", "Unstable");
        provider.add("gui.thaumcraft.infusion.stability.very_unstable", "Dangerously Unstable");
        provider.add("gui.thaumcraft.infusion.stability.gain", "gain / cycle");
        provider.add("gui.thaumcraft.infusion.stability.range", "0 to ");
        provider.add("gui.thaumcraft.infusion.stability.loss", "loss / cycle");
        provider.add("entity.thaumcraft.causality_collapser", "Causality Collapser");
        provider.add("item.thaumcraft.thaumium_sword", "Thaumium Sword");
        provider.add("item.thaumcraft.thaumium_pickaxe", "Thaumium Pickaxe");
        provider.add("item.thaumcraft.thaumium_axe", "Thaumium Axe");
        provider.add("item.thaumcraft.thaumium_shovel", "Thaumium Shovel");
        provider.add("item.thaumcraft.thaumium_hoe", "Thaumium Hoe");
        provider.add("item.thaumcraft.thaumium_helm", "Thaumium Helm");
        provider.add("item.thaumcraft.thaumium_chest", "Thaumium Chestplate");
        provider.add("item.thaumcraft.thaumium_legs", "Thaumium Greaves");
        provider.add("item.thaumcraft.thaumium_boots", "Thaumium Boots");
        provider.add("item.thaumcraft.void_sword", "Void Sword");
        provider.add("item.thaumcraft.void_pickaxe", "Void Pickaxe");
        provider.add("item.thaumcraft.void_axe", "Void Axe");
        provider.add("item.thaumcraft.void_shovel", "Void Shovel");
        provider.add("item.thaumcraft.void_hoe", "Void Hoe");
        provider.add("item.thaumcraft.void_helm", "Void Helm");
        provider.add("item.thaumcraft.void_chest", "Void Chestplate");
        provider.add("item.thaumcraft.void_legs", "Void Greaves");
        provider.add("item.thaumcraft.void_boots", "Void Boots");
        provider.add("item.thaumcraft.elemental_sword", "Sword of the Zephyr");
        provider.add("item.thaumcraft.elemental_pickaxe", "Pickaxe of the Core");
        provider.add("item.thaumcraft.elemental_axe", "Axe of the Stream");
        provider.add("item.thaumcraft.elemental_shovel", "Shovel of the Earthmover");
        provider.add("item.thaumcraft.elemental_hoe", "Hoe of Growth");
        provider.add("item.thaumcraft.primal_crusher", "Primal Crusher");
        provider.add("item.thaumcraft.traveller_boots", "Boots of the Traveller");
        provider.add("item.thaumcraft.cloth_chest", "Apprentice's Robes");
        provider.add("item.thaumcraft.cloth_legs", "Apprentice's Leggings");
        provider.add("item.thaumcraft.cloth_boots", "Apprentice's Boots");

        for (DyeColor dye : DyeColor.values()) {
            provider.add("block.thaumcraft.candle_" + dye.getName(), dyeName(dye) + " Tallow Candle");
            provider.add("block.thaumcraft.banner_" + dye.getName(), dyeName(dye) + " Banner");
            provider.add("block.thaumcraft.wall_banner_" + dye.getName(), dyeName(dye) + " Banner");
        }
        provider.add("block.thaumcraft.banner_crimson_cult", "Crimson Cult Banner");
        provider.add("block.thaumcraft.wall_banner_crimson_cult", "Crimson Cult Banner");
    }

    private static String dyeName(DyeColor dye) {
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
}

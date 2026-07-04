package com.leclowndu93150.thaumcraft.compat.jei.utils;

import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.compat.jei.ThaumcraftJEIPlugin;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ResearchUtils {

    public static List<Component> generateMissingResearchList(ResearchGate... research) {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("jei.thaumcraft.research.missing_research").withStyle(ChatFormatting.GOLD));
        for (ResearchGate gate : research) {
            if (!ResearchManager.doesPassGate(Minecraft.getInstance().player, gate)) {
                RegistryAccess access = ThaumcraftJEIPlugin.clientRegistryAccess();
                if (access == null) {
                    list.add(Component.literal("- ").append(gate.entry().toString()).withStyle(ChatFormatting.RED));
                } else {
                    IResearchEntry entry = access.lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(gate.entry()).map(Holder::value).orElse(null);
                    if (entry != null) {
                        list.add(Component.literal("- ").append(Component.translatable(entry.nameKey())).withStyle(ChatFormatting.RED));
                    } else {
                        list.add(Component.literal("- ").append(gate.entry().toString()).withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
        return list;
    }
}

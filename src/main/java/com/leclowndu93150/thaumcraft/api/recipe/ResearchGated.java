package com.leclowndu93150.thaumcraft.api.recipe;

import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface ResearchGated {

    Optional<ResearchGate> researchGate();

    default boolean doesPassGate(Player player){
        return ResearchManager.doesPassGate(player,researchGate().orElse(null));
    }
}

package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface ResearchGated {

    Optional<ResearchGate> researchGate();

    default boolean doesPassGate(Player player){
        return ResearchManager.doesPassGate(player,researchGate().orElse(null));
    }
}

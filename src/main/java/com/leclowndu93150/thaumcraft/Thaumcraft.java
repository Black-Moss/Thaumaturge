package com.leclowndu93150.thaumcraft;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftAccess;
import com.leclowndu93150.thaumcraft.content.aura.AuraHelperBindings;
import com.leclowndu93150.thaumcraft.config.ThaumcraftClientConfig;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.config.ThaumcraftServerConfig;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCCreativeTabs;
import com.leclowndu93150.thaumcraft.registry.TCDamageTypes;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCMenus;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.leclowndu93150.thaumcraft.registry.TCRecipeSerializers;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksAOres;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksBCrystals;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksCStone;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksDTrees;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksEPlants;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksFMetals;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsAOres;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsBCrystals;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsCStone;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsDTrees;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsEPlants;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsFMetals;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsGTools;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsHContainers;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsMAuraHud;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TCIds.MODID)
public final class Thaumcraft {
    public static final Logger LOGGER = LoggerFactory.getLogger(TCIds.MODID);

    public Thaumcraft(IEventBus modBus, ModContainer container) {
        TCBlocksAOres.touch();
        TCBlocksBCrystals.touch();
        TCBlocksCStone.touch();
        TCBlocksFMetals.touch();
        TCBlocksEPlants.touch();
        TCBlocksDTrees.touch();
        TCItemsAOres.touch();
        TCItemsBCrystals.touch();
        TCItemsCStone.touch();
        TCItemsEPlants.touch();
        TCItemsDTrees.touch();
        TCItemsFMetals.touch();
        TCItemsHContainers.touch();
        TCItemsMAuraHud.touch();
        TCBlocks.register(modBus);
        TCItems.register(modBus);
        TCItemsGTools.register(modBus);
        TCBlockEntities.register(modBus);
        TCEntities.register(modBus);
        TCMenus.register(modBus);
        TCRecipeTypes.register(modBus);
        TCRecipeSerializers.register(modBus);
        TCDataComponents.register(modBus);
        TCCreativeTabs.register(modBus);
        TCParticles.register(modBus);
        TCSounds.register(modBus);
        TCAttachments.register(modBus);
        TCDamageTypes.register(modBus);
        TCMobEffects.register(modBus);
        TCTheorycraft.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, ThaumcraftCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ThaumcraftClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ThaumcraftServerConfig.SPEC);

        KnowledgeAccess.bind(player -> player.getData(TCAttachments.KNOWLEDGE));
        TheorycraftAccess.bind(player -> player.getData(TCAttachments.RESEARCH_TABLE));
        AuraHelper.bind(new AuraHelperBindings());
    }
}

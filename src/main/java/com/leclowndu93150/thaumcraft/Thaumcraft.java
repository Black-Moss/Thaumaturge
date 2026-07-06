package com.leclowndu93150.thaumcraft;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftAccess;
import com.leclowndu93150.thaumcraft.api.taint.TaintApi;
import com.leclowndu93150.thaumcraft.content.aura.AuraHelperBindings;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.research.scan.ScanBindings;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.content.taint.TaintApiBindings;
import com.leclowndu93150.thaumcraft.content.warp.WarpManager;
import com.leclowndu93150.thaumcraft.config.ThaumcraftClientConfig;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.config.ThaumcraftServerConfig;
import com.leclowndu93150.thaumcraft.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TCIds.MODID)
public final class Thaumcraft {
    public static final Logger LOGGER = LoggerFactory.getLogger(TCIds.MODID);

    public Thaumcraft(IEventBus modBus, ModContainer container) {
        TCFluidTypes.register(modBus);
        TCFluids.register(modBus);
        TCBlocks.register(modBus);
        TCItems.register(modBus);
        TCFeatures.register(modBus);
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
        TCAttributes.register(modBus);
        TCFocusElements.register(modBus);

        NeoForge.EVENT_BUS.addListener(TCRecipeTypes::registerSynchronizedRecipes);

        container.registerConfig(ModConfig.Type.COMMON, ThaumcraftCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ThaumcraftClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ThaumcraftServerConfig.SPEC);

        KnowledgeAccess.bind(player -> player.getData(TCAttachments.KNOWLEDGE));
        TheorycraftAccess.bind(player -> player.getData(TCAttachments.RESEARCH_TABLE));
        AuraHelper.bind(new AuraHelperBindings());
        TaintApi.bind(new TaintApiBindings());
        WarpHelper.bind(new WarpManager.Bindings());
        ScanningManager.bind(new ScanBindings());
        FocusEngine.bindRegistry(TCFocusElements.registry());
    }
}

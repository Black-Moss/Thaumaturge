package com.leclowndu93150.thaumcraft.content.research.scan;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanBlock;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanBlockTag;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanEntity;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanItem;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanItemTag;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.entity.EntityCultist;
import com.leclowndu93150.thaumcraft.content.entity.EntityInhabitedZombie;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ScanBootstrap {
    private static final Set<Identifier> DYNAMIC_ASPECTS = new HashSet<>();
    private static final Set<Identifier> DYNAMIC_ENCHANTMENTS = new HashSet<>();
    private static boolean registered;

    private ScanBootstrap() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!registered) {
            registered = true;
            ScanningManager.addScannableThing(new ScanGeneric());
            ScanningManager.addScannableThing(new ScanSky());
            for (Holder.Reference<MobEffect> effect : BuiltInRegistries.MOB_EFFECT.listElements().toList()) {
                ScanningManager.addScannableThing(new ScanPotion(effect));
            }
            registerContentScans();
        }
        event.getServer().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            if (DYNAMIC_ASPECTS.add(aspect.key().identifier())) {
                ScanningManager.addScannableThing(new ScanAspectDiscovery(aspect.key()));
            }
        });
        event.getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(enchantment -> {
            if (DYNAMIC_ENCHANTMENTS.add(enchantment.key().identifier())) {
                ScanningManager.addScannableThing(new ScanEnchantment(enchantment.key().identifier()));
            }
        });
    }

    private static void registerContentScans() {
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("ore"),
                TCBlocks.ORE_AMBER.get(), TCBlocks.ORE_CINNABAR.get(),
                TCBlocks.CRYSTAL_AER.get(), TCBlocks.CRYSTAL_IGNIS.get(), TCBlocks.CRYSTAL_AQUA.get(),
                TCBlocks.CRYSTAL_TERRA.get(), TCBlocks.CRYSTAL_ORDO.get(), TCBlocks.CRYSTAL_PERDITIO.get(),
                TCBlocks.CRYSTAL_VITIUM.get()));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("scanned/orecrystal"),
                TCBlocks.CRYSTAL_AER.get(), TCBlocks.CRYSTAL_IGNIS.get(), TCBlocks.CRYSTAL_AQUA.get(),
                TCBlocks.CRYSTAL_TERRA.get(), TCBlocks.CRYSTAL_ORDO.get(), TCBlocks.CRYSTAL_PERDITIO.get(),
                TCBlocks.CRYSTAL_VITIUM.get()));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("plants"),
                TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(),
                TCBlocks.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get(),
                TCBlocks.PLANT_CINDERPEARL.get(), TCBlocks.PLANT_SHIMMERLEAF.get(), TCBlocks.PLANT_VISHROOM.get()));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("scanned/plantwood"),
                TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(),
                TCBlocks.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get()));

        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("f_teleport"),
                Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME));
        ScanningManager.addScannableThing(new ScanItem(TCIds.rl("f_teleport"), new ItemStack(Items.ENDER_PEARL)));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_teleport"), EntityType.ENDERMAN));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_spider"), Spider.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_bat"), Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_fly"), Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_fly"), Parrot.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_fly"), Ghast.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_fly"), Blaze.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("f_fly"), TCEntities.TAINT_SWARM.get()));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("f_dispenser"), Blocks.DISPENSER));
        ScanningManager.addScannableThing(new ScanItem(TCIds.rl("f_matclay"), new ItemStack(Items.CLAY_BALL)));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("f_matclay"), Blocks.CLAY));
        ScanningManager.addScannableThing(new ScanBlockTag(TCIds.rl("f_matclay"), BlockTags.TERRACOTTA));
        ScanningManager.addScannableThing(new ScanItemTag(TCIds.rl("f_matiron"), Tags.Items.ORES_IRON));
        ScanningManager.addScannableThing(new ScanItemTag(TCIds.rl("f_matiron"), Tags.Items.INGOTS_IRON));
        ScanningManager.addScannableThing(new ScanItemTag(TCIds.rl("f_matiron"), Tags.Items.STORAGE_BLOCKS_IRON));
        ScanningManager.addScannableThing(new ScanItem(TCIds.rl("f_matbrass"), new ItemStack(TCItems.INGOT_BRASS.get())));
        ScanningManager.addScannableThing(new ScanBlock(TCIds.rl("f_matbrass"), TCBlocks.METAL_BRASS_BLOCK.get()));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("scanned/entity/thaumcraft/cultist"),
                EntityCultist.class, true));
        ScanningManager.addScannableThing(new ScanEntity(TCIds.rl("scanned/entity/thaumcraft/eldritch_crab"),
                EntityInhabitedZombie.class, true));
    }
}

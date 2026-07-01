package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.jar.JarBraceItem;
import com.leclowndu93150.thaumcraft.content.item.LabelItem;
import com.leclowndu93150.thaumcraft.content.item.SalisMundusItem;
import com.leclowndu93150.thaumcraft.content.research.book.ThaumonomiconItem;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemBottleTaint;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemEssentiaCrystal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TCIds.MODID);

    public static final DeferredItem<BlockItem> RESEARCH_TABLE = ITEMS.registerSimpleBlockItem(TCBlocks.RESEARCH_TABLE);

    public static final DeferredItem<BlockItem> JAR_NORMAL = ITEMS.registerSimpleBlockItem(TCBlocks.JAR_NORMAL);

    public static final DeferredItem<BlockItem> JAR_VOID = ITEMS.registerSimpleBlockItem(TCBlocks.JAR_VOID);

    public static final DeferredItem<BlockItem> TUBE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE);

    public static final DeferredItem<BlockItem> TUBE_VALVE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_VALVE);

    public static final DeferredItem<BlockItem> TUBE_RESTRICT = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_RESTRICT);

    public static final DeferredItem<BlockItem> TUBE_FILTER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_FILTER);

    public static final DeferredItem<BlockItem> TUBE_ONEWAY = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_ONEWAY);

    public static final DeferredItem<BlockItem> TUBE_BUFFER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_BUFFER);

    public static final DeferredItem<BlockItem> TAINT_ROCK = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_ROCK);
    public static final DeferredItem<BlockItem> TAINT_SOIL = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_SOIL);
    public static final DeferredItem<BlockItem> TAINT_CRUST = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_CRUST);
    public static final DeferredItem<BlockItem> TAINT_GEYSER = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_GEYSER);
    public static final DeferredItem<BlockItem> TAINT_LOG = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_LOG);
    public static final DeferredItem<BlockItem> TAINT_FEATURE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FEATURE);
    public static final DeferredItem<BlockItem> TAINT_FIBRE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FIBRE);

    public static final DeferredItem<JarBraceItem> JAR_BRACE = ITEMS.registerItem(
            "jar_brace",
            JarBraceItem::new
    );

    public static final DeferredItem<LabelItem> LABEL = ITEMS.registerItem(
            "label",
            LabelItem::new
    );

    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = ITEMS.registerItem(
            "thaumonomicon",
            ThaumonomiconItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<SalisMundusItem> SALIS_MUNDUS = ITEMS.registerItem(
            "salis_mundus",
            SalisMundusItem::new,
            props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemEssentiaCrystal> ESSENTIA_CRYSTAL = ITEMS.registerItem(
            "essentia_crystal",
            ItemEssentiaCrystal::new);

    public static final DeferredItem<ItemBottleTaint> BOTTLE_TAINT = ITEMS.registerItem(
            "bottle_taint",
            ItemBottleTaint::new,
            props -> props.stacksTo(16).rarity(Rarity.UNCOMMON));

    private TCItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}

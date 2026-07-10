package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.AidBlock;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.AidEntity;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardAwareness;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardBeacon;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardCalibrate;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardChannel;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardConcentrate;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardCurio;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardDarkWhispers;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardDragonEgg;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardEnchantment;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardFocus;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardGlyphs;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardInfuse;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardMeasure;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardMindOverMatter;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardPortal;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardReactions;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardRealization;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardRevelation;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardScripting;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardSculpting;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardSpellbinding;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardSynergy;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardSynthesis;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardTinker;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardTruth;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardAnalyze;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardBalance;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardExperimentation;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardInspired;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardNotation;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardPonder;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardReject;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardRethink;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardCelestial;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardStudy;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCTheorycraft {
    public static final ResourceKey<Registry<CardFactory>> CARDS_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TCIds.MODID, "theorycraft_card"));

    public static final ResourceKey<Registry<ITheorycraftAid>> AIDS_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TCIds.MODID, "theorycraft_aid"));

    public static final DeferredRegister<CardFactory> CARDS = DeferredRegister.create(CARDS_REGISTRY_KEY, TCIds.MODID);
    public static final DeferredRegister<ITheorycraftAid> AIDS = DeferredRegister.create(AIDS_REGISTRY_KEY, TCIds.MODID);

    public static final DeferredHolder<CardFactory, CardFactory> CARD_STUDY = CARDS.register("study", () -> CardStudy::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_ANALYZE = CARDS.register("analyze", () -> CardAnalyze::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_BALANCE = CARDS.register("balance", () -> CardBalance::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_PONDER = CARDS.register("ponder", () -> CardPonder::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_INSPIRED = CARDS.register("inspired", () -> CardInspired::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_NOTATION = CARDS.register("notation", () -> CardNotation::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_RETHINK = CARDS.register("rethink", () -> CardRethink::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_REJECT = CARDS.register("reject", () -> CardReject::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_EXPERIMENTATION = CARDS.register("experimentation", () -> CardExperimentation::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_CELESTIAL = CARDS.register("celestial", () -> CardCelestial::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_CURIO = CARDS.register("curio", () -> CardCurio::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_ENCHANTMENT = CARDS.register("enchantment", () -> CardEnchantment::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_BEACON = CARDS.register("beacon", () -> CardBeacon::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_DRAGON_EGG = CARDS.register("dragon_egg", () -> CardDragonEgg::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_CONCENTRATE = CARDS.register("concentrate", () -> CardConcentrate::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_REACTIONS = CARDS.register("reactions", () -> CardReactions::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_SYNTHESIS = CARDS.register("synthesis", () -> CardSynthesis::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_CALIBRATE = CARDS.register("calibrate", () -> CardCalibrate::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_MIND_OVER_MATTER = CARDS.register("mind_over_matter", () -> CardMindOverMatter::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_TINKER = CARDS.register("tinker", () -> CardTinker::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_MEASURE = CARDS.register("measure", () -> CardMeasure::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_CHANNEL = CARDS.register("channel", () -> CardChannel::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_INFUSE = CARDS.register("infuse", () -> CardInfuse::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_FOCUS = CARDS.register("focus", () -> CardFocus::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_AWARENESS = CARDS.register("awareness", () -> CardAwareness::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_SPELLBINDING = CARDS.register("spellbinding", () -> CardSpellbinding::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_SCULPTING = CARDS.register("sculpting", () -> CardSculpting::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_SCRIPTING = CARDS.register("scripting", () -> CardScripting::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_SYNERGY = CARDS.register("synergy", () -> CardSynergy::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_DARK_WHISPERS = CARDS.register("dark_whispers", () -> CardDarkWhispers::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_GLYPHS = CARDS.register("glyphs", () -> CardGlyphs::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_PORTAL = CARDS.register("portal", () -> CardPortal::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_REVELATION = CARDS.register("revelation", () -> CardRevelation::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_REALIZATION = CARDS.register("realization", () -> CardRealization::new);
    public static final DeferredHolder<CardFactory, CardFactory> CARD_TRUTH = CARDS.register("truth", () -> CardTruth::new);

    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BOOKSHELF = AIDS.register("bookshelf",
            () -> new AidBlock(() -> List.of(Blocks.BOOKSHELF), () -> new ItemStack(Items.BOOKSHELF),
                    List.of(CARD_BALANCE.getKey(), CARD_NOTATION.getKey(), CARD_NOTATION.getKey(),
                            CARD_STUDY.getKey(), CARD_STUDY.getKey(), CARD_STUDY.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BRAIN_IN_A_JAR = AIDS.register("brain_in_a_jar",
            () -> new AidBlock(() -> List.of(TCBlocks.JAR_BRAIN.get()), () -> new ItemStack(TCBlocks.JAR_BRAIN.get()),
                    List.of(CARD_DARK_WHISPERS.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_GLYPHED_STONE = AIDS.register("glyphed_stone",
            () -> new AidBlock(() -> List.of(TCBlocks.STONE_ANCIENT_GLYPHED.get()), () -> new ItemStack(TCBlocks.STONE_ANCIENT_GLYPHED.get()),
                    List.of(CARD_GLYPHS.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_PORTAL_NETHER = AIDS.register("portal_nether",
            () -> new AidBlock(() -> List.of(Blocks.NETHER_PORTAL), () -> new ItemStack(Items.OBSIDIAN),
                    List.of(CARD_PORTAL.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_PORTAL_END = AIDS.register("portal_end",
            () -> new AidBlock(() -> List.of(Blocks.END_PORTAL), () -> new ItemStack(Items.END_PORTAL_FRAME),
                    List.of(CARD_PORTAL.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_PORTAL_CRIMSON = AIDS.register("portal_crimson",
            () -> new AidEntity(() -> TCEntities.CULTIST_PORTAL_LESSER.get(),
                    () -> new ItemStack(TCItems.BANNER_CRIMSON_CULT.get()),
                    "entity.thaumcraft.cultist_portal_lesser",
                    List.of(CARD_PORTAL.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_ALCHEMY = AIDS.register("basic_alchemy",
            () -> new AidBlock(() -> List.of(TCBlocks.CRUCIBLE.get()), () -> new ItemStack(TCBlocks.CRUCIBLE.get()),
                    List.of(CARD_CONCENTRATE.getKey(), CARD_REACTIONS.getKey(), CARD_SYNTHESIS.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_ARTIFICE = AIDS.register("basic_artifice",
            () -> new AidBlock(() -> List.of(TCBlocks.ARCANE_WORKBENCH.get()), () -> new ItemStack(TCBlocks.ARCANE_WORKBENCH.get()),
                    List.of(CARD_CALIBRATE.getKey(), CARD_TINKER.getKey(), CARD_MIND_OVER_MATTER.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_INFUSION = AIDS.register("basic_infusion",
            () -> new AidBlock(() -> List.of(TCBlocks.INFUSION_MATRIX.get()), () -> new ItemStack(TCBlocks.INFUSION_MATRIX.get()),
                    List.of(CARD_MEASURE.getKey(), CARD_CHANNEL.getKey(), CARD_INFUSE.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_AUROMANCY = AIDS.register("basic_auromancy",
            () -> new AidBlock(() -> List.of(TCBlocks.FOCAL_MANIPULATOR.get()), () -> new ItemStack(TCBlocks.FOCAL_MANIPULATOR.get()),
                    List.of(CARD_FOCUS.getKey(), CARD_AWARENESS.getKey(), CARD_SPELLBINDING.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_GOLEMANCY = AIDS.register("basic_golemancy",
            () -> new AidBlock(() -> List.of(TCBlocks.GOLEM_BUILDER.get()), () -> new ItemStack(TCBlocks.GOLEM_BUILDER.get()),
                    List.of(CARD_SCULPTING.getKey(), CARD_SCRIPTING.getKey(), CARD_SYNERGY.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BASIC_ELDRITCH = AIDS.register("basic_eldritch",
            () -> new AidBlock(() -> List.of(TCBlocks.ELDRITCH_STONE.get()), () -> new ItemStack(TCBlocks.ELDRITCH_STONE.get()),
                    List.of(CARD_REALIZATION.getKey(), CARD_REVELATION.getKey(), CARD_TRUTH.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_ENCHANTMENT_TABLE = AIDS.register("enchantment_table",
            () -> new AidBlock(() -> List.of(Blocks.ENCHANTING_TABLE), () -> new ItemStack(Items.ENCHANTING_TABLE),
                    List.of(CARD_ENCHANTMENT.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_BEACON = AIDS.register("beacon",
            () -> new AidBlock(() -> List.of(Blocks.BEACON), () -> new ItemStack(Items.BEACON),
                    List.of(CARD_BEACON.getKey())));
    public static final DeferredHolder<ITheorycraftAid, ITheorycraftAid> AID_DRAGON_EGG = AIDS.register("dragon_egg",
            () -> new AidBlock(() -> List.of(Blocks.DRAGON_EGG), () -> new ItemStack(Items.DRAGON_EGG),
                    List.of(CARD_DRAGON_EGG.getKey())));

    static {
        CARDS.makeRegistry(builder -> builder.sync(false));
        AIDS.makeRegistry(builder -> builder.sync(false));
    }

    private TCTheorycraft() {}

    public static void register(IEventBus modBus) {
        CARDS.register(modBus);
        AIDS.register(modBus);
    }
}

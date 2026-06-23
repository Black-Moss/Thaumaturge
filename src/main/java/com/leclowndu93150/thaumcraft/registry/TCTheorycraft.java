package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardAnalyze;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardBalance;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardExperimentation;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardInspired;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardNotation;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardPonder;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardReject;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardRethink;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.cards.CardStudy;
import net.minecraft.core.Registry;
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

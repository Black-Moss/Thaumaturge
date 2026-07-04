package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.aura.AuraData;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumcraft.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.ResearchTableData;
import com.leclowndu93150.thaumcraft.content.warp.WarpData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TCIds.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerKnowledge>> KNOWLEDGE =
            register("knowledge", () -> AttachmentType.builder(PlayerKnowledge::new)
                    .serialize(PlayerKnowledge.CODEC)
                    .sync(PlayerKnowledge.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ResearchTableData>> RESEARCH_TABLE =
            register("research_table", () -> AttachmentType.builder(ResearchTableData::new)
                    .serialize(ResearchTableData.CODEC)
                    .sync(ResearchTableData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AuraData>> AURA =
            register("aura", () -> AttachmentType.builder(AuraData::new)
                    .serialize(AuraData.CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WarpData>> WARP =
            register("warp", () -> AttachmentType.builder(WarpData::new)
                    .serialize(WarpData.CODEC)
                    .sync((holder, to) -> holder == to, WarpData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DustTriggerSwapQueue>> DUST_TRIGGER_QUEUE =
            register("dust_trigger_queue", () -> AttachmentType.builder(DustTriggerSwapQueue::new)
                    .serialize(DustTriggerSwapQueue.CODEC)
                    .build());

    private TCAttachments() {}

    private static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> supplier) {
        return ATTACHMENTS.register(name, supplier);
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}

package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.TheorycraftManager;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundTableActionHandler {
    private ServerboundTableActionHandler() {}

    public static void handle(ServerboundTableActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof MenuResearchTable menu)) return;
            ServerLevel level = player.level() instanceof ServerLevel sl ? sl : null;
            if (level == null) return;
            BlockEntity blockEntity = level.getBlockEntity(menu.pos());
            if (!(blockEntity instanceof BlockEntityResearchTable table)) return;

            switch (payload.action()) {
                case COMPLETE -> {
                    if (!player.getData(TCAttachments.RESEARCH_TABLE).isComplete()) return;
                    TheorycraftManager.endSession(player);
                }
                case SCRAP -> TheorycraftManager.endSession(player);
                case START -> {}
            }
        });
    }

    public static void handleStart(ServerboundStartTheoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof MenuResearchTable menu)) return;
            ServerLevel level = player.level() instanceof ServerLevel sl ? sl : null;
            if (level == null) return;
            BlockEntity blockEntity = level.getBlockEntity(menu.pos());
            if (!(blockEntity instanceof BlockEntityResearchTable table)) return;
            if (!table.hasInkReady() || !table.hasPaperReady()) return;
            table.consumePaper();
            TheorycraftManager.beginSession(player, level, table.getBlockPos(), Set.copyOf(payload.selectedAids()));
        });
    }
}

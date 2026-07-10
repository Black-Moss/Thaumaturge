package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardScripting extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.GOLEMANCY;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.scripting.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.scripting.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        BlockEntityResearchTable table = tableFor(player);
        if (table == null || !table.hasInkReady() || !table.hasPaperReady()) {
            return false;
        }
        table.consumeInk();
        table.consumePaper();
        data.addCategoryTotal(TCResearchCategories.GOLEMANCY, 25);
        return true;
    }

    private static BlockEntityResearchTable tableFor(ServerPlayer player) {
        if (player.containerMenu instanceof MenuResearchTable menu
                && player.level().getBlockEntity(menu.tablePos()) instanceof BlockEntityResearchTable table) {
            return table;
        }
        return null;
    }
}

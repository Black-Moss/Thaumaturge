package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CardDarkWhispers extends TheorycraftCard {
    private static final int LEVEL_DRAIN_BASE = 10;

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.ELDRITCH;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.darkwhisper.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.darkwhisper.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        int levels = player.experienceLevel;
        player.giveExperienceLevels(-(LEVEL_DRAIN_BASE + levels));
        if (levels > 0) {
            for (ResourceKey<IResearchCategory> key : CardHelper.allCategories(player)) {
                if (!player.getRandom().nextBoolean()) {
                    data.addCategoryTotal(key,
                            Mth.nextInt(player.getRandom(), 0, Math.max(1, (int) Math.sqrt(levels))));
                }
            }
        }
        data.addCategoryTotal(TCResearchCategories.ELDRITCH,
                Mth.nextInt(player.getRandom(), Math.max(1, levels / 5), Math.max(5, levels / 2)));
        WarpHelper.addWarp(player, Math.max(1, (int) Math.sqrt(levels)), WarpType.NORMAL);
        if (player.getRandom().nextBoolean()) {
            data.addBonusDraws(1);
        }
        return true;
    }
}

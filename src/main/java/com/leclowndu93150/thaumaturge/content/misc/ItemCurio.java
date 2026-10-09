package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.TTResearchCategories;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.research.KnowledgeGrant;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class ItemCurio extends Item {
    private static final Identifier RITES_RESEARCH = TTIds.rl("crimson_rites");
    private static final KnowledgeGrant OBSERVATION_GRANT = new KnowledgeGrant(KnowledgeType.OBSERVATION, 2, 1);
    private static final KnowledgeGrant THEORY_GRANT = new KnowledgeGrant(KnowledgeType.THEORY, 3, 2);
    private static final int RITES_WARP_THRESHOLD = 20;
    private static final int NORMAL_WARP = 1;
    private static final int TEMPORARY_WARP = 5;
    private static final int PERMANENT_WARP = 1;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH_NUMERATOR = 0.4F;
    private static final float SOUND_DIVISOR_MIN = 0.8F;
    private static final float SOUND_DIVISOR_MAX = 1.2F;

    private final Variant variant;

    public ItemCurio(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("tooltip.thaumaturge.curio.read"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer user)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        RandomSource random = user.getRandom();
        float pitch = SOUND_PITCH_NUMERATOR / Mth.randomBetween(random, SOUND_DIVISOR_MIN, SOUND_DIVISOR_MAX);
        level.playSound(null, user.getX(), user.getY(), user.getZ(), TTSounds.LEARN.get(), SoundSource.NEUTRAL, SOUND_VOLUME, pitch);
        if (variant.rites && WarpHelper.getActualWarp(user) <= RITES_WARP_THRESHOLD) {
            user.sendSystemMessage(Component.translatable("message.thaumaturge.curio.crimson_rites").withStyle(ChatFormatting.DARK_PURPLE));
            return InteractionResult.SUCCESS;
        }
        if (variant.rites && !KnowledgeAccess.of(user).isResearchKnown(RITES_RESEARCH)) {
            ResearchManager.complete(user, RITES_RESEARCH);
        }
        OBSERVATION_GRANT.award(user);
        THEORY_GRANT.award(user);
        if (variant.warp) {
            WarpHelper.addWarp(user, NORMAL_WARP, WarpType.NORMAL);
            WarpHelper.addWarp(user, TEMPORARY_WARP, WarpType.TEMPORARY);
            if (variant.rites && random.nextBoolean()) {
                WarpHelper.addWarp(user, PERMANENT_WARP, WarpType.PERMANENT);
            }
        }
        user.getItemInHand(hand).consume(1, user);
        user.sendSystemMessage(Component.translatable("message.thaumaturge.curio.knowledge_gained").withStyle(ChatFormatting.DARK_PURPLE));
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    public enum Variant {
        ARCANE(TTResearchCategories.AUROMANCY, false, false), PRESERVED(TTResearchCategories.ALCHEMY, false, false), ANCIENT(TTResearchCategories.GOLEMANCY, false, false), ELDRITCH(
                TTResearchCategories.ELDRITCH, true,
                false), KNOWLEDGE(TTResearchCategories.INFUSION, false, false), TWISTED(TTResearchCategories.ARTIFICE, false, false), RITES(TTResearchCategories.ELDRITCH, true, true);

        private final ResourceKey<IResearchCategory> category;
        private final boolean warp;
        private final boolean rites;

        Variant(ResourceKey<IResearchCategory> category, boolean warp, boolean rites) {
            this.category = category;
            this.warp = warp;
            this.rites = rites;
        }
    }
}

package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
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
    private static final float PERMANENT_WARP_CHANCE = 0.5F;
    private static final int GRANT_ROUNDS = 2;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 0.45F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;
    private static final String RITES_UNREADABLE_KEY = "message.thaumaturge.curio.crimson_rites";
    private static final String KNOWLEDGE_GAINED_KEY = "message.thaumaturge.curio.knowledge_gained";

    private final Variant variant;

    public ItemCurio(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer reader)) {
            return InteractionResult.SUCCESS;
        }
        RandomSource random = reader.getRandom();
        float pitch = SOUND_PITCH + (random.nextFloat() - random.nextFloat()) * SOUND_PITCH_SPREAD;
        level.playSound(null, reader.getX(), reader.getY(), reader.getZ(), TTSounds.LEARN.get(), SoundSource.PLAYERS, SOUND_VOLUME, pitch);
        if (variant.demandsWarp() && !unlockRites(reader)) {
            reader.sendSystemMessage(Component.translatable(RITES_UNREADABLE_KEY).withStyle(ChatFormatting.DARK_PURPLE));
            return InteractionResult.FAIL;
        }
        for (int round = 0; round < GRANT_ROUNDS; round++) {
            OBSERVATION_GRANT.award(reader);
            THEORY_GRANT.award(reader);
        }
        if (variant.warping()) {
            WarpHelper.addWarp(reader, NORMAL_WARP, WarpType.NORMAL);
            WarpHelper.addWarp(reader, TEMPORARY_WARP, WarpType.TEMPORARY);
        }
        if (variant.demandsWarp() && random.nextFloat() < PERMANENT_WARP_CHANCE) {
            WarpHelper.addWarp(reader, PERMANENT_WARP, WarpType.PERMANENT);
        }
        reader.getItemInHand(hand).consume(1, reader);
        reader.sendSystemMessage(Component.translatable(KNOWLEDGE_GAINED_KEY).withStyle(ChatFormatting.DARK_PURPLE));
        reader.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean unlockRites(ServerPlayer reader) {
        if (totalWarp(reader) <= RITES_WARP_THRESHOLD) {
            return false;
        }
        if (!PlayerResearchAccess.of(reader).knows(RITES_RESEARCH)) {
            ResearchManager.complete(reader, RITES_RESEARCH);
        }
        return true;
    }

    private static int totalWarp(ServerPlayer reader) {
        int total = 0;
        for (WarpType type : WarpType.values()) {
            total += WarpHelper.getWarp(reader).get(type);
        }
        return total;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("tooltip.thaumaturge.curio.read"));
    }

    public enum Variant {
        ARCANE(TTResearchCategories.AUROMANCY, false, false), PRESERVED(TTResearchCategories.ALCHEMY, false, false), ANCIENT(TTResearchCategories.GOLEMANCY, false, false), ELDRITCH(
                TTResearchCategories.ELDRITCH, true,
                false), KNOWLEDGE(TTResearchCategories.INFUSION, false, false), TWISTED(TTResearchCategories.ARTIFICE, false, false), RITES(TTResearchCategories.ELDRITCH, true, true);

        private final ResourceKey<IResearchCategory> field;
        private final boolean warping;
        private final boolean demandsWarp;

        Variant(ResourceKey<IResearchCategory> field, boolean warping, boolean demandsWarp) {
            this.field = field;
            this.warping = warping;
            this.demandsWarp = demandsWarp;
        }

        public ResourceKey<IResearchCategory> field() {
            return field;
        }

        public boolean warping() {
            return warping;
        }

        public boolean demandsWarp() {
            return demandsWarp;
        }
    }

}

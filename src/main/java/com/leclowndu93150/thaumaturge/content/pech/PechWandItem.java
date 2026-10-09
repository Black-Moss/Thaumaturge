package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.content.research.KnowledgeGrant;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class PechWandItem extends Item {
    private static final Identifier PREREQUISITE = TTIds.rl("base_auromancy");
    private static final Identifier REVEALED = TTIds.rl("scanned/pechwand");
    private static final String TOOLTIP_KEY = "tooltip.thaumaturge.curio.read";
    private static final String UNFATHOMABLE_KEY = "message.thaumaturge.pech_wand.unfathomable";
    private static final String CURIOUS_KEY = "message.thaumaturge.discovery.pech_wand";
    private static final float LEARN_VOLUME = 0.5F;
    private static final float LEARN_PITCH = 0.42F;
    private static final float LEARN_PITCH_SPREAD = 0.08F;
    private static final int OBSERVATION_SMALLEST_DIVISOR = 3;
    private static final int OBSERVATION_LARGEST_DIVISOR = 2;
    private static final int THEORY_SMALLEST_DIVISOR = 5;
    private static final int THEORY_LARGEST_DIVISOR = 4;
    private static final KnowledgeGrant OBSERVATION_GRANT = new KnowledgeGrant(KnowledgeType.OBSERVATION, OBSERVATION_SMALLEST_DIVISOR, OBSERVATION_LARGEST_DIVISOR);
    private static final KnowledgeGrant THEORY_GRANT = new KnowledgeGrant(KnowledgeType.THEORY, THEORY_SMALLEST_DIVISOR, THEORY_LARGEST_DIVISOR);

    public PechWandItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(TOOLTIP_KEY));
        super.appendHoverText(stack, context, display, builder, flag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(PREREQUISITE)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable(UNFATHOMABLE_KEY).withStyle(ChatFormatting.RED));
            }
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        player.getItemInHand(hand).consume(1, player);
        RandomSource random = level.getRandom();
        float pitch = LEARN_PITCH + (random.nextFloat() - random.nextFloat()) * LEARN_PITCH_SPREAD;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.LEARN.get(), SoundSource.NEUTRAL, LEARN_VOLUME, pitch);
        serverPlayer.sendSystemMessage(Component.translatable(CURIOUS_KEY).withStyle(ChatFormatting.DARK_PURPLE));
        if (!knowledge.isResearchKnown(REVEALED)) {
            ResearchManager.complete(serverPlayer, REVEALED);
        }
        OBSERVATION_GRANT.award(serverPlayer);
        THEORY_GRANT.award(serverPlayer);
        return InteractionResult.SUCCESS_SERVER;
    }
}

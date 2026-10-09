package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityCondenser;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class ItemResonator extends Item {
    private static final String CONTENTS_KEY = "message.thaumaturge.resonator.contents";
    private static final String SUCTION_KEY = "message.thaumaturge.resonator.suction";
    private static final String UNTYPED_KEY = "message.thaumaturge.resonator.untyped";
    private static final String CONDENSER_COST_KEY = "message.thaumaturge.resonator.condenser_cost";
    private static final String CONDENSER_TIME_KEY = "message.thaumaturge.resonator.condenser_time";
    private static final int TICKS_PER_SECOND = 20;
    private static final double CENTER_OFFSET = 0.5;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_BASE_PITCH = 1.9F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;

    public ItemResonator(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        IEssentiaTransport transport = EssentiaAccess.transport(level, pos, face);
        if (transport == null) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        reportContents(player, level, pos, face, transport);
        reportSuction(player, transport, face);
        reportCondenser(player, level.getBlockEntity(pos));
        level.playSound(null, pos.getX() + CENTER_OFFSET, pos.getY() + CENTER_OFFSET, pos.getZ() + CENTER_OFFSET, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, SOUND_VOLUME,
                SOUND_BASE_PITCH + level.getRandom().nextFloat() * SOUND_PITCH_SPREAD);
        return InteractionResult.SUCCESS;
    }

    private static void reportContents(Player player, Level level, BlockPos pos, Direction face, IEssentiaTransport transport) {
        IAspectContainer container = level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer ? level.getCapability(AspectCapabilities.CONTAINER, pos, face) : null;
        if (container != null) {
            for (AspectInstance entry : container.getAspects().sortedByTag()) {
                player.sendSystemMessage(contents(entry.amount(), entry.aspect()));
            }
            return;
        }
        Holder<IAspect> type = transport.getEssentiaType(face);
        if (type != null) {
            player.sendSystemMessage(contents(transport.getEssentiaAmount(face), type));
        }
    }

    private static void reportSuction(Player player, IEssentiaTransport transport, Direction face) {
        Holder<IAspect> type = transport.getSuctionType(face);
        Component name = type == null ? Component.translatable(UNTYPED_KEY) : AspectComponents.trueName(type);
        player.sendSystemMessage(Component.translatable(SUCTION_KEY, Integer.toString(transport.getSuctionAmount(face)), name));
    }

    private static void reportCondenser(Player player, @Nullable BlockEntity entity) {
        if (entity instanceof BlockEntityCondenser condenser) {
            player.sendSystemMessage(Component.translatable(CONDENSER_COST_KEY, Integer.toString(condenser.cost())));
            player.sendSystemMessage(Component.translatable(CONDENSER_TIME_KEY, Integer.toString(condenser.interval()), Integer.toString(condenser.interval() / TICKS_PER_SECOND)));
        }
    }

    private static Component contents(int amount, Holder<IAspect> aspect) {
        return Component.translatable(CONTENTS_KEY, Integer.toString(amount), AspectComponents.trueName(aspect));
    }
}

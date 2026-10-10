package com.leclowndu93150.thaumaturge.content.warp.soap;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public final class SanitySoapItem extends Item {
    private static final int USE_TICKS = 100;
    private static final int SCRUB_COMPLETE_TICKS = 95;
    private static final int CONSUMED = 1;

    private static final int SCRUB_BUBBLES = 3;
    private static final double SCRUB_COLUMN_WIDTH = 1.0;
    private static final float SCRUB_SQUELCH_CHANCE = 0.2F;
    private static final float SCRUB_VOLUME = 0.1F;
    private static final float SCRUB_PITCH = 1.6F;
    private static final float SCRUB_PITCH_VARIATION = 0.3F;

    private static final int RINSE_BUBBLES = 40;
    private static final double RINSE_COLUMN_WIDTH = 1.4;
    private static final float RINSE_SOUND_CHANCE = 1.0F;
    private static final float RINSE_VOLUME = 0.6F;
    private static final float RINSE_PITCH = 1.0F;
    private static final float RINSE_PITCH_SPREAD = 0.0F;

    private static final SoapLather SCRUBBING = new SoapLather(SCRUB_BUBBLES, SCRUB_COLUMN_WIDTH, () -> SoundEvents.CHORUS_FLOWER_DEATH, SCRUB_SQUELCH_CHANCE, SCRUB_VOLUME, SCRUB_PITCH,
            SCRUB_PITCH_VARIATION);
    private static final SoapLather RINSING = new SoapLather(RINSE_BUBBLES, RINSE_COLUMN_WIDTH, TTSounds.CRAFTSTART, RINSE_SOUND_CHANCE, RINSE_VOLUME, RINSE_PITCH, RINSE_PITCH_SPREAD);

    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
        if (scrubbedLongEnough(ticksRemaining)) {
            user.releaseUsingItem();
            return;
        }
        SCRUBBING.play(level, user);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int ticksRemaining) {
        if (!scrubbedLongEnough(ticksRemaining)) {
            return false;
        }
        RINSING.play(level, user);
        if (user instanceof ServerPlayer player) {
            SoapCleanse.SANITY_SOAP.apply(player);
            stack.consume(CONSUMED, player);
        }
        return true;
    }

    private static boolean scrubbedLongEnough(int ticksRemaining) {
        return USE_TICKS - ticksRemaining > SCRUB_COMPLETE_TICKS;
    }
}

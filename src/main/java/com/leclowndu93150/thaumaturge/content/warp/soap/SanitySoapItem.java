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

    private static final int SCRUB_BUBBLES = 10;
    private static final double SCRUB_SPREAD = 1.0;
    private static final float SCRUB_SOUND_CHANCE = 0.2F;
    private static final float SCRUB_VOLUME = 0.1F;
    private static final float SCRUB_BASE_PITCH = 1.5F;
    private static final float SCRUB_PITCH_SPREAD = 0.2F;
    private static final int RINSE_BUBBLES = 40;
    private static final double RINSE_SPREAD = 1.5;
    private static final float RINSE_SOUND_CHANCE = 1.0F;
    private static final float RINSE_VOLUME = 0.25F;
    private static final float RINSE_PITCH = 1.0F;
    private static final float RINSE_PITCH_SPREAD = 0.0F;

    private static final SoapLather SCRUB_LATHER = new SoapLather(SCRUB_BUBBLES, SCRUB_SPREAD, () -> SoundEvents.CHORUS_FLOWER_DEATH, SCRUB_SOUND_CHANCE, SCRUB_VOLUME, SCRUB_BASE_PITCH,
            SCRUB_PITCH_SPREAD);
    private static final SoapLather RINSE_LATHER = new SoapLather(RINSE_BUBBLES, RINSE_SPREAD, TTSounds.CRAFTSTART, RINSE_SOUND_CHANCE, RINSE_VOLUME, RINSE_PITCH, RINSE_PITCH_SPREAD);

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
        }
        SCRUB_LATHER.play(level, user);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int ticksRemaining) {
        if (!scrubbedLongEnough(ticksRemaining) || !(user instanceof Player player)) {
            return false;
        }
        RINSE_LATHER.play(level, player);
        if (player instanceof ServerPlayer serverPlayer) {
            SoapCleanse.SANITY_SOAP.apply(serverPlayer);
        }
        stack.shrink(CONSUMED);
        return true;
    }

    private static boolean scrubbedLongEnough(int ticksRemaining) {
        return USE_TICKS - ticksRemaining > SCRUB_COMPLETE_TICKS;
    }
}

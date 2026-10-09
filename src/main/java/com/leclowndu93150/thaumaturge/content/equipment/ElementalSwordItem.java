package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.client.effect.WhirlwindSmokeEmitter;
import com.leclowndu93150.thaumaturge.content.equipment.whirlwind.HoverPhysics;
import com.leclowndu93150.thaumaturge.content.equipment.whirlwind.PulseScheduler;
import com.leclowndu93150.thaumaturge.content.equipment.whirlwind.RepelField;
import com.leclowndu93150.thaumaturge.content.equipment.whirlwind.WhirlwindStrategy;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public final class ElementalSwordItem extends Item implements IChanneledItem {
    public static final UseEffects USE_EFFECTS = new UseEffects(UseEffects.DEFAULT.canSprint(), false, UseEffects.DEFAULT.speedMultiplier());

    private static final int USE_DURATION_TICKS = 72000;
    private static final int WIND_INTERVAL_TICKS = 20;
    private static final int WEAR_PER_PULSE = 1;
    private static final float TOGGLE_VOLUME = 0.5F;
    private static final float TOGGLE_PITCH_OFF = 0.8F;
    private static final float TOGGLE_PITCH_ON = 1.2F;
    private static final float WIND_VOLUME = 0.5F;
    private static final float WIND_PITCH_BASE = 0.9F;
    private static final float WIND_PITCH_SPREAD = 0.2F;
    private static final List<WhirlwindStrategy> STRATEGIES = List.of(new HoverPhysics(), new RepelField());
    private static final PulseScheduler PULSES = new PulseScheduler(WIND_INTERVAL_TICKS);
    private static final WhirlwindSmokeEmitter SMOKE = new WhirlwindSmokeEmitter();

    public ElementalSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.elemental_sword.toggle").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            toggle(level, player, stack);
            return InteractionResult.SUCCESS;
        }
        if (stack.getOrDefault(TTDataComponents.WHIRLWIND_DISABLED.get(), false)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        for (WhirlwindStrategy strategy : STRATEGIES) {
            strategy.apply(level, user);
        }
        if (level.isClientSide()) {
            SMOKE.emit(level, user);
            return;
        }
        if (user instanceof ServerPlayer player) {
            ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
        }
        if (PULSES.isDue(USE_DURATION_TICKS - remainingUseDuration)) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), TTSounds.WIND.get(), SoundSource.PLAYERS, WIND_VOLUME, WIND_PITCH_BASE + level.getRandom().nextFloat() * WIND_PITCH_SPREAD);
            user.gameEvent(GameEvent.ELYTRA_GLIDE);
            stack.hurtAndBreak(WEAR_PER_PULSE, user, user.getUsedItemHand().asEquipmentSlot());
        }
    }

    private static void toggle(Level level, Player player, ItemStack stack) {
        boolean disabled = !stack.getOrDefault(TTDataComponents.WHIRLWIND_DISABLED.get(), false);
        stack.set(TTDataComponents.WHIRLWIND_DISABLED.get(), disabled);
        if (level.isClientSide()) {
            return;
        }
        String message = disabled ? "message.thaumaturge.elemental_sword.whirlwind_off" : "message.thaumaturge.elemental_sword.whirlwind_on";
        player.sendOverlayMessage(Component.translatable(message).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.KEY.get(), SoundSource.PLAYERS, TOGGLE_VOLUME, disabled ? TOGGLE_PITCH_OFF : TOGGLE_PITCH_ON);
    }
}

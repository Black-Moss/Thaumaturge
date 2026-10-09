package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ItemHandMirror extends Item {
    private static final Identifier RESEARCH = TTIds.rl("mirror_hand");
    private static final String LINKED_KEY = "message.thaumaturge.hand_mirror.linked";
    private static final String MISSING_KEY = "message.thaumaturge.hand_mirror.missing";
    private static final String LINKED_TO_KEY = "tooltip.thaumaturge.mirror.linked_to";
    private static final String MENU_TITLE_KEY = "item.thaumaturge.hand_mirror";
    private static final float LINK_VOLUME = 1.0F;
    private static final float LINK_PITCH = 2.0F;
    private static final float BREAK_VOLUME = 1.0F;
    private static final float BREAK_PITCH = 0.8F;
    private static final float TELEPORT_VOLUME = 0.1F;
    private static final float TELEPORT_PITCH = 1.0F;

    public ItemHandMirror(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || !isItemMirror(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            stack.set(TTDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, LINK_VOLUME, LINK_PITCH);
            player.sendSystemMessage(Component.translatable(LINKED_KEY).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isItemMirror(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BlockMirror mirror && !mirror.isEssentia();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        if (!DeviceGate.passes(player, RESEARCH)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        GlobalPos link = stack.get(TTDataComponents.MIRROR_LINK.get());
        if (link == null) {
            return InteractionResult.PASS;
        }
        if (destination(serverLevel.getServer(), link) == null) {
            breakLink(stack, serverPlayer);
            return InteractionResult.SUCCESS;
        }
        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) -> new MenuHandMirror(containerId, inventory), Component.translatable(MENU_TITLE_KEY));
        serverPlayer.openMenu(provider);
        return InteractionResult.SUCCESS;
    }

    public static boolean transport(ItemStack mirror, ItemStack payload, ServerPlayer sender) {
        GlobalPos link = mirror.get(TTDataComponents.MIRROR_LINK.get());
        if (link == null) {
            return false;
        }
        BlockEntityMirror target = destination(sender.level().getServer(), link);
        if (target == null) {
            breakLink(mirror, sender);
            return false;
        }
        target.transportDirect(payload.copy());
        sender.level().playSound(null, sender.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, TELEPORT_VOLUME, TELEPORT_PITCH);
        return true;
    }

    private static @Nullable BlockEntityMirror destination(@Nullable MinecraftServer server, GlobalPos link) {
        ServerLevel target = server == null ? null : server.getLevel(link.dimension());
        if (target == null) {
            return null;
        }
        return target.getBlockEntity(link.pos()) instanceof BlockEntityMirror mirror ? mirror : null;
    }

    private static void breakLink(ItemStack mirror, ServerPlayer player) {
        Component notice = Component.translatable(MISSING_KEY).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
        mirror.remove(TTDataComponents.MIRROR_LINK.get());
        BlockPos at = player.blockPosition();
        player.level().playSound(null, at, TTSounds.ZAP.get(), SoundSource.PLAYERS, BREAK_VOLUME, BREAK_PITCH);
        player.sendSystemMessage(notice);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(TTDataComponents.MIRROR_LINK.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        GlobalPos link = stack.get(TTDataComponents.MIRROR_LINK.get());
        if (link == null) {
            return;
        }
        BlockPos pos = link.pos();
        String dimension = link.dimension().identifier().toString();
        builder.accept(Component.translatable(LINKED_TO_KEY, pos.getX(), pos.getY(), pos.getZ(), dimension).withStyle(ChatFormatting.DARK_PURPLE));
    }
}

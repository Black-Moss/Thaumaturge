package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFocusChangePayload(String focusKey) implements CustomPacketPayload {
    private static final int MAX_KEY_LENGTH = 256;

    public static final Type<ServerboundFocusChangePayload> TYPE = new Type<>(TTIds.rl("focus_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFocusChangePayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(MAX_KEY_LENGTH),
            ServerboundFocusChangePayload::focusKey, ServerboundFocusChangePayload::new);

    public static void handle(ServerboundFocusChangePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack main = player.getMainHandItem();
            ItemStack target = main.getItem() instanceof ICaster ? main : player.getOffhandItem();
            if (target.getItem() instanceof ICaster) {
                CasterManager.applyFocusChoice(target, player.level(), player, payload.focusKey());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

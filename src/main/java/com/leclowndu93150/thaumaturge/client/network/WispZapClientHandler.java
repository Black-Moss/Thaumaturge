package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.network.ClientboundWispZapPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class WispZapClientHandler {
    private static final float BOLT_WIDTH = 0.6F;
    private static final float DEFAULT_CHANNEL = 1.0F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;

    private WispZapClientHandler() {}

    public static void handle(ClientboundWispZapPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> apply(payload));
    }

    private static void apply(ClientboundWispZapPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;
        Entity source = level.getEntity(payload.sourceId());
        Entity target = level.getEntity(payload.targetId());
        if (source == null || target == null)
            return;
        float red = DEFAULT_CHANNEL;
        float green = DEFAULT_CHANNEL;
        float blue = DEFAULT_CHANNEL;
        if (source instanceof WispEntity wisp) {
            Holder<IAspect> aspect = wisp.aspect();
            if (aspect != null && aspect.isBound()) {
                int color = aspect.value().color();
                red = ((color >> RED_SHIFT) & CHANNEL_MASK) / CHANNEL_MAX;
                green = ((color >> GREEN_SHIFT) & CHANNEL_MASK) / CHANNEL_MAX;
                blue = (color & CHANNEL_MASK) / CHANNEL_MAX;
            }
        }
        Vec3 from = source.position();
        Vec3 to = target.position();
        level.addParticle(new BoltParticleOptions(to.x, to.y, to.z, red, green, blue, BOLT_WIDTH), from.x, from.y, from.z, 0.0D, 0.0D, 0.0D);
    }
}

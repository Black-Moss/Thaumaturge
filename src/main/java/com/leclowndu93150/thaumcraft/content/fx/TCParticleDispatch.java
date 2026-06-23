package com.leclowndu93150.thaumcraft.content.fx;

import com.leclowndu93150.thaumcraft.content.fx.data.VisSparkleData;
import com.leclowndu93150.thaumcraft.content.fx.data.WispData;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFXStreamPayload;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundSpawnParticlePayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TCParticleDispatch {
    private static final double DEFAULT_RADIUS = 64.0;

    private TCParticleDispatch() {}

    public static void spawnEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag) {
        spawnEssentiaStream(level, from, to, color, typeTag, level.getRandom().nextInt(8), 0.15F, 20, 0.0);
    }

    public static void spawnEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my) {
        EssentiaSourceFXTracker.dispatch(level, from, to, color, typeTag, count, scale, extend, my);
    }

    static void broadcastEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my) {
        ClientboundFXStreamPayload payload = ClientboundFXStreamPayload.essentia(
                from.x, from.y, from.z, to.x, to.y, to.z, color, count, scale, extend, my);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnVisSparkle(ServerLevel level, Vec3 origin, Vec3 target) {
        VisSparkleData data = new VisSparkleData(target.x, target.y, target.z);
        broadcast(level, data, origin);
    }

    public static void spawnArc(ServerLevel level, Vec3 from, Vec3 to, int color, float gravityHint) {
        ClientboundFXStreamPayload payload = ClientboundFXStreamPayload.arc(
                from.x, from.y, from.z, to.x, to.y, to.z, color, gravityHint);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnBolt(ServerLevel level, Vec3 from, Vec3 to, int color, float width) {
        ClientboundFXStreamPayload payload = ClientboundFXStreamPayload.bolt(
                from.x, from.y, from.z, to.x, to.y, to.z, color, width);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnWisp(ServerLevel level, Vec3 origin) {
        spawnWisp(level, origin, WispData.NO_ENTITY);
    }

    public static void spawnWisp(ServerLevel level, Vec3 origin, int entityId) {
        WispData data = new WispData(entityId);
        broadcast(level, data, origin);
    }

    private static void broadcast(ServerLevel level, ParticleOptions options, Vec3 origin) {
        PacketDistributor.sendToPlayersNear(level, null, origin.x, origin.y, origin.z, DEFAULT_RADIUS,
                new ClientboundSpawnParticlePayload(options, origin.x, origin.y, origin.z));
    }
}

package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.CultistClericRenderer;
import com.leclowndu93150.thaumaturge.client.entity.CultistLeaderRenderer;
import com.leclowndu93150.thaumaturge.client.entity.CultistRenderer;
import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakTracker;
import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakWearer;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CapeSwayModifiers {
    private static final float FLAP_PER_BLOCK = 10.0F;
    private static final float FLAP_MIN = -6.0F;
    private static final float FLAP_MAX = 32.0F;
    private static final float LEAN_PER_BLOCK = 100.0F;
    private static final float LEAN_MAX = 150.0F;
    private static final float SIDE_LEAN_LIMIT = 20.0F;
    private static final float STEP_FREQUENCY = 6.0F;
    private static final float STEP_FLAP = 32.0F;

    private CapeSwayModifiers() {}

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarCapeModifier());
        event.registerEntityModifier(CultistRenderer.class, CapeSwayModifiers::fromCloak);
        event.registerEntityModifier(CultistClericRenderer.class, CapeSwayModifiers::fromCloak);
        event.registerEntityModifier(CultistLeaderRenderer.class, CapeSwayModifiers::fromCloak);
    }

    private static <E extends LivingEntity & CloakWearer> void fromCloak(E entity, EntityRenderState state) {
        float partialTick = state.partialTick;
        CloakTracker cloak = entity.cloak();
        double dx = cloak.x(partialTick) - Mth.lerp(partialTick, entity.xo, entity.getX());
        double dy = cloak.y(partialTick) - Mth.lerp(partialTick, entity.yo, entity.getY());
        double dz = cloak.z(partialTick) - Mth.lerp(partialTick, entity.zo, entity.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
        double backX = Mth.sin(bodyYaw);
        double backZ = -Mth.cos(bodyYaw);
        float step = Mth.sin(cloak.walkDistance(partialTick) * STEP_FREQUENCY) * STEP_FLAP * cloak.bob(partialTick);
        float flap = Mth.clamp((float) dy * FLAP_PER_BLOCK, FLAP_MIN, FLAP_MAX) + step;
        float lean = Mth.clamp((float) (dx * backX + dz * backZ) * LEAN_PER_BLOCK, 0.0F, LEAN_MAX);
        float sideLean = Mth.clamp((float) (dx * backZ - dz * backX) * LEAN_PER_BLOCK, -SIDE_LEAN_LIMIT, SIDE_LEAN_LIMIT);
        state.setRenderData(CapeSway.KEY, new CapeSway(flap, lean, sideLean));
    }

    private static final class AvatarCapeModifier extends AvatarRenderStateModifier {
        @Override
        public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
            state.setRenderData(CapeSway.KEY, new CapeSway(state.capeFlap, state.capeLean, state.capeLean2));
        }
    }
}

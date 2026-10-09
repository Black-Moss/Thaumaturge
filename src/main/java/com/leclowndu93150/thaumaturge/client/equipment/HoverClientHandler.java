package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.client.input.TTKeybinds;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.network.ServerboundToggleHoverPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class HoverClientHandler {
    private static final double HORIZONTAL_DRAG = 0.7;
    private static final int SKIP_ROLL_BOUND = 3;
    private static final int SKIP_ROLL_VALUE = 0;
    private static final double BACK_OFFSET = 0.5;
    private static final double SPAWN_HEIGHT = 1.17;
    private static final double CROUCH_DROP = 0.075;
    private static final double QUARTER_TURN = 90.0;
    private static final int YAW_FAN_BOUND = 180;
    private static final int PITCH_SPAN = 160;
    private static final int PITCH_MIN = -80;
    private static final double REACH = 6.0;
    private static final float BOLT_RED = 0.75F;
    private static final float BOLT_GREEN = 1.0F;
    private static final float BOLT_BLUE = 1.0F;
    private static final float BOLT_WIDTH = 0.45F;

    private HoverClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer self = minecraft.player;
        if (level == null || self == null) {
            return;
        }
        while (TTKeybinds.TOGGLE_HOVER.consumeClick()) {
            if (minecraft.screen == null && HoverManager.isWearingHoverGear(self)) {
                ClientPacketDistributor.sendToServer(ServerboundToggleHoverPayload.INSTANCE);
            }
        }
        if (minecraft.isPaused()) {
            return;
        }
        boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
        RandomSource random = level.getRandom();
        for (Player hoverer : level.players()) {
            boolean hiddenOwn = hoverer == minecraft.getCameraEntity() && firstPerson;
            if (hoverer.isInvisible() || hiddenOwn || !HoverManager.isHovering(hoverer) || !HoverManager.isWearingHoverGear(hoverer)) {
                continue;
            }
            if (random.nextInt(SKIP_ROLL_BOUND) != SKIP_ROLL_VALUE) {
                discharge(level, hoverer, random);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.isCreative() || player.isSpectator() || !HoverManager.isHovering(player)) {
            return;
        }
        ItemStack gear = HoverManager.wornHoverGear(player);
        if (!(gear.getItem() instanceof IHoverGear hoverGear) || hoverGear.getHoverFuel(gear) <= 0) {
            return;
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !player.onGround()) {
            abilities.flying = true;
        }
        if (abilities.flying) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * HORIZONTAL_DRAG, motion.y, motion.z * HORIZONTAL_DRAG);
        }
    }

    private static void discharge(ClientLevel level, Player hoverer, RandomSource random) {
        double bodyYaw = hoverer.yBodyRot;
        double backAngle = Math.toRadians(bodyYaw + QUARTER_TURN);
        double height = SPAWN_HEIGHT - (hoverer.isCrouching() ? CROUCH_DROP : 0.0);
        Vec3 origin = new Vec3(hoverer.getX() - BACK_OFFSET * Math.cos(backAngle), hoverer.getY() + height, hoverer.getZ() - BACK_OFFSET * Math.sin(backAngle));
        float yaw = (float) (bodyYaw - QUARTER_TURN - random.nextInt(YAW_FAN_BOUND));
        float pitch = random.nextInt(PITCH_SPAN) + PITCH_MIN;
        Vec3 end = origin.add(Vec3.directionFromRotation(pitch, yaw).scale(REACH));
        BlockHitResult hit = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, hoverer));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        Vec3 target = hit.getLocation();
        level.addParticle(new BoltParticleOptions(target.x, target.y, target.z, BOLT_RED, BOLT_GREEN, BOLT_BLUE, BOLT_WIDTH), origin.x, origin.y, origin.z, 0.0, 0.0, 0.0);
    }
}

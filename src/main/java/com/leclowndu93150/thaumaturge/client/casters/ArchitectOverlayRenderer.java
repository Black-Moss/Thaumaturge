package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.client.casters.architect.AxisArrowPass;
import com.leclowndu93150.thaumaturge.client.casters.architect.CornerFramePass;
import com.leclowndu93150.thaumaturge.client.casters.architect.PreviewRefresher;
import com.leclowndu93150.thaumaturge.client.casters.architect.PreviewSnapshot;
import com.leclowndu93150.thaumaturge.client.casters.architect.SideFramePass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ArchitectOverlayRenderer {
    private static final PreviewRefresher REFRESHER = new PreviewRefresher();
    private static final SideFramePass SIDE_PASS = new SideFramePass();
    private static final CornerFramePass CORNER_PASS = new CornerFramePass();
    private static final AxisArrowPass ARROW_PASS = new AxisArrowPass();

    private ArchitectOverlayRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft client = Minecraft.getInstance();
        AimedArchitect aimed = findAimed(client);
        if (aimed == null) {
            return;
        }
        PreviewSnapshot preview = REFRESHER.update(aimed.architect(), aimed.stack(), aimed.level(), aimed.anchor(), aimed.face(), aimed.player(),
                aimed.player().tickCount / PreviewRefresher.REFRESH_BUCKET_TICKS);
        if (preview.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        Matrix4f pose = event.getPoseStack().last().pose();
        Vec3 camera = client.gameRenderer.getMainCamera().position();
        SIDE_PASS.draw(buffers, pose, camera, preview);
        CORNER_PASS.draw(buffers, pose, camera, preview);
        boolean[] visibleAxes = visibleAxes(aimed);
        if (visibleAxes != null) {
            ARROW_PASS.draw(buffers, pose, camera, aimed.anchor(), aimed.player().tickCount, visibleAxes);
        }
    }

    @SubscribeEvent
    public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        Player viewer = Minecraft.getInstance().player;
        ItemStack held = viewer == null ? ItemStack.EMPTY : heldArchitect(viewer);
        if (held.getItem() instanceof IArchitect architect && architect.replacesBlockHighlight(held)
                && !architect.previewBlocks(held, event.getLevel(), event.getBlockPos(), event.getHitResult().getDirection(), viewer).isEmpty()) {
            event.setCanceled(true);
        }
    }

    private static @Nullable AimedArchitect findAimed(Minecraft client) {
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null || client.options.hideGui) {
            return null;
        }
        ItemStack held = heldArchitect(player);
        if (!(held.getItem() instanceof IArchitect architect)) {
            return null;
        }
        if (architect.aim(held, level, player) instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            return new AimedArchitect(level, player, held, architect, hit.getBlockPos(), hit.getDirection());
        }
        return null;
    }

    private static boolean @Nullable [] visibleAxes(AimedArchitect aimed) {
        Direction.Axis[] axes = Direction.Axis.values();
        boolean[] visible = new boolean[axes.length];
        boolean any = false;
        for (Direction.Axis axis : axes) {
            boolean shows = aimed.architect().showsAxis(aimed.stack(), aimed.level(), aimed.player(), aimed.face(), axis);
            visible[axis.ordinal()] = shows;
            any |= shows;
        }
        return any ? visible : null;
    }

    private static ItemStack heldArchitect(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack candidate = player.getItemInHand(hand);
            if (candidate.getItem() instanceof IArchitect) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private record AimedArchitect(ClientLevel level, LocalPlayer player, ItemStack stack, IArchitect architect, BlockPos anchor, Direction face) {
    }
}

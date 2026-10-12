package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusCaster;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.model.connected.FaceCorners;
import com.leclowndu93150.thaumaturge.content.spell.effect.WardEffect;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class WardOverlayRenderer {
    private static final double RENDER_RANGE = 48.0;
    private static final double SECTION_WIDTH = 16.0;
    private static final double SECTION_RANGE = RENDER_RANGE + SECTION_WIDTH;

    private static final float OUTSET = 0.003F;
    private static final float SPAN = 1.0F + OUTSET + OUTSET;
    private static final float HALF = 0.5F;

    private static final float OWNED_RED_BASE = 0.55F;
    private static final float OWNED_GREEN_BASE = 0.3F;
    private static final float OWNED_BLUE_BASE = 0.02F;
    private static final float OWNED_ALPHA = 0.85F;
    private static final float FOREIGN_RED_BASE = 0.55F;
    private static final float FOREIGN_GREEN_BASE = 0.1F;
    private static final float FOREIGN_BLUE_BASE = 0.06F;
    private static final float FOREIGN_ALPHA = 0.42F;
    private static final float PULSE = 0.07F;
    private static final float RED_PERIOD = 11.0F;
    private static final float GREEN_PERIOD = 17.0F;
    private static final float BLUE_PERIOD = 23.0F;
    private static final float PHASE_X = 0.9F;
    private static final float PHASE_Y = 1.7F;
    private static final float PHASE_Z = 1.3F;
    private static final Direction[] FACES = Direction.values();

    private static final int[][] FACE_BASE = {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {0, 1, 1}, {0, 1, 0}, {1, 1, 1}};
    private static final int[][] FACE_U = {{1, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
    private static final int[][] FACE_V = {{0, 0, 1}, {0, 0, 1}, {0, -1, 0}, {0, -1, 0}, {0, -1, 0}, {0, -1, 0}};

    private static final float[] CORNER_U = {0.0F, HALF, 0.0F, HALF};
    private static final float[] CORNER_V = {0.0F, 0.0F, HALF, HALF};

    private static final RenderType RUNES = TTFXRenderTypes.WARD_RUNES;

    private WardOverlayRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui) {
            return;
        }
        if (!holdsWardFocus(player, minecraft.level.registryAccess()) || ClientWardHolder.sections().isEmpty()) {
            return;
        }
        Vec3 cam = minecraft.gameRenderer.getMainCamera().position();
        float time = player.tickCount + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RUNES);
        Matrix4f pose = event.getPoseStack().last().pose();
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[FaceCorners.COUNT * FaceCorners.STATES];
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (SectionPos section : ClientWardHolder.sections()) {
            if (cam.distanceToSqr(section.center().getCenter()) > SECTION_RANGE * SECTION_RANGE) {
                continue;
            }
            for (BlockPos pos : ClientWardHolder.wardsIn(section)) {
                if (cam.distanceToSqr(Vec3.atCenterOf(pos)) > RENDER_RANGE * RENDER_RANGE) {
                    continue;
                }
                drawWard(buffer, pose, sprites, neighbour, cursor, pos, cam, time);
            }
        }
        buffers.endBatch(RUNES);
    }

    private static void drawWard(VertexConsumer buffer, Matrix4f pose, TextureAtlasSprite[] sprites, BlockPos.MutableBlockPos neighbour, BlockPos.MutableBlockPos cursor, BlockPos pos, Vec3 cam, float time) {
        boolean owned = ClientWardHolder.isOwned(pos);
        Predicate<BlockPos> sameOwner = other -> ClientWardHolder.isWarded(other) && ClientWardHolder.isOwned(other) == owned;
        float phase = pos.getX() * PHASE_X + pos.getY() * PHASE_Y + pos.getZ() * PHASE_Z;
        float red = shimmer(owned ? OWNED_RED_BASE : FOREIGN_RED_BASE, time, RED_PERIOD, phase);
        float green = shimmer(owned ? OWNED_GREEN_BASE : FOREIGN_GREEN_BASE, time, GREEN_PERIOD, phase);
        float blue = shimmer(owned ? OWNED_BLUE_BASE : FOREIGN_BLUE_BASE, time, BLUE_PERIOD, phase);
        float alpha = owned ? OWNED_ALPHA : FOREIGN_ALPHA;
        for (Direction face : FACES) {
            neighbour.setWithOffset(pos, face);
            if (ClientWardHolder.isWarded(neighbour)) {
                continue;
            }
            int mask = WardConnectedTexture.connectionMask(pos, face, cursor, sameOwner);
            for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
                drawCorner(buffer, pose, pos, face, corner, sprite(sprites, corner, FaceCorners.state(mask, corner)), cam, red, green, blue, alpha);
            }
        }
    }

    private static float shimmer(float base, float time, float period, float phase) {
        return Mth.clamp(base + PULSE * Mth.sin(time * Mth.TWO_PI / period + phase), 0.0F, 1.0F);
    }

    private static TextureAtlasSprite sprite(TextureAtlasSprite[] cache, int corner, int state) {
        int slot = FaceCorners.slot(corner, state);
        TextureAtlasSprite sprite = cache[slot];
        if (sprite == null) {
            sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(WardConnectedTexture.sprite(corner, state));
            cache[slot] = sprite;
        }
        return sprite;
    }

    private static void drawCorner(VertexConsumer buffer, Matrix4f pose, BlockPos pos, Direction face, int corner, TextureAtlasSprite sprite, Vec3 cam, float red, float green, float blue, float alpha) {
        int index = face.ordinal();
        int[] base = FACE_BASE[index];
        int[] uAxis = FACE_U[index];
        int[] vAxis = FACE_V[index];
        float originX = (float) (pos.getX() - cam.x) + edge(base[0]);
        float originY = (float) (pos.getY() - cam.y) + edge(base[1]);
        float originZ = (float) (pos.getZ() - cam.z) + edge(base[2]);
        float u0 = CORNER_U[corner];
        float v0 = CORNER_V[corner];
        float u1 = u0 + HALF;
        float v1 = v0 + HALF;
        vertex(buffer, pose, originX, originY, originZ, uAxis, vAxis, u0, v0, sprite.getU0(), sprite.getV0(), red, green, blue, alpha);
        vertex(buffer, pose, originX, originY, originZ, uAxis, vAxis, u1, v0, sprite.getU1(), sprite.getV0(), red, green, blue, alpha);
        vertex(buffer, pose, originX, originY, originZ, uAxis, vAxis, u1, v1, sprite.getU1(), sprite.getV1(), red, green, blue, alpha);
        vertex(buffer, pose, originX, originY, originZ, uAxis, vAxis, u0, v1, sprite.getU0(), sprite.getV1(), red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, float originX, float originY, float originZ, int[] uAxis, int[] vAxis, float u, float v, float spriteU, float spriteV, float red, float green, float blue, float alpha) {
        float x = originX + (uAxis[0] * u + vAxis[0] * v) * SPAN;
        float y = originY + (uAxis[1] * u + vAxis[1] * v) * SPAN;
        float z = originZ + (uAxis[2] * u + vAxis[2] * v) * SPAN;
        buffer.addVertex(pose, x, y, z).setUv(spriteU, spriteV).setColor(red, green, blue, alpha);
    }

    private static float edge(int flag) {
        return flag == 0 ? -OUTSET : 1.0F + OUTSET;
    }

    private static boolean holdsWardFocus(LocalPlayer player, RegistryAccess registries) {
        return hasWard(player.getMainHandItem(), registries) || hasWard(player.getOffhandItem(), registries);
    }

    private static boolean hasWard(ItemStack stack, RegistryAccess registries) {
        if (!(stack.getItem() instanceof FocusCaster caster)) {
            return false;
        }
        Spell spell = Spells.spellOf(caster.socketedFocus(stack));
        if (spell == null) {
            return false;
        }
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = Spells.part(registries, node.part());
            if (part.isPresent() && part.get().behavior() instanceof WardEffect) {
                return true;
            }
        }
        return false;
    }
}

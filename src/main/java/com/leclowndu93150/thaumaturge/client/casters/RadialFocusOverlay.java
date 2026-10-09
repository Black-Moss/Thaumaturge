package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat.CurioPouchRef;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class RadialFocusOverlay implements GuiLayer {
    private static final Identifier RING_ONE = TTIds.rl("textures/misc/radial.png");
    private static final Identifier RING_TWO = TTIds.rl("textures/misc/radial2.png");
    private static final int RING_TEXTURE_SIZE = 256;
    private static final int RING_TINT = 0x80808080;
    private static final float RING_ONE_FACTOR = 2.75F;
    private static final float RING_TWO_FACTOR = 2.55F;
    private static final float RING_ART_OFFSET_DEGREES = 90.0F;
    private static final int RING_SPIN_PERIOD_TICKS = 720;
    private static final float RING_SPIN_DIVISOR = 2.0F;

    private static final float BASE_RADIUS = 16.0F;
    private static final float RADIUS_PER_ENTRY = 2.5F;
    private static final int HOVER_HALF_SIZE = 10;
    private static final int ITEM_SIZE = 16;
    private static final int ITEM_HALF = 8;
    private static final float HOVER_SCALE_MAX = 1.3F;
    private static final float HOVER_GROW_MS = 150.0F;
    private static final float HOVER_DECAY_MS = 250.0F;
    private static final float PICKER_MS = 150.0F;
    private static final float START_DEGREES = 90.0F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final int TOOLTIP_OFFSET_X = -4;
    private static final int TOOLTIP_OFFSET_Y = 20;

    private static final int MAIN_SLOTS = 36;

    private static float pickerScale;
    private static boolean pendingClick;

    private final SortedMap<String, FocusEntry> entries = new TreeMap<>();
    private boolean centreHover;
    private long lastFrame;
    private boolean lastActive;
    private ItemStack tooltipStack = ItemStack.EMPTY;

    public RadialFocusOverlay() {}

    public static boolean isAnimating() {
        return pickerScale > 0.0F;
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (!CasterKeyHandler.radialOpen || Minecraft.getInstance().screen != null) {
            return;
        }
        event.setCanceled(true);
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            pendingClick = true;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor gui, DeltaTracker tracker) {
        long now = Util.getMillis();
        Minecraft mc = Minecraft.getInstance();
        boolean open = CasterKeyHandler.radialOpen;
        LocalPlayer player = mc.player;
        if (player == null || (!open && pickerScale <= 0.0F)) {
            idle(mc, now);
            return;
        }
        if (open && mc.screen != null) {
            abandon(now);
            return;
        }
        ItemStack caster = CasterKeyHandler.heldCaster(player);
        ItemStack socketed = CasterKeyHandler.socketedFocus(caster);
        if (open) {
            if (pickerScale <= 0.0F) {
                openPicker(mc, player, socketed);
            }
        } else if (lastActive && mc.screen == null) {
            CasterKeyHandler.regrabMouse(mc);
        }
        tooltipStack = ItemStack.EMPTY;
        boolean hasContent = !entries.isEmpty() || !socketed.isEmpty();
        if (hasContent && !caster.isEmpty()) {
            drawPicker(gui, mc, player, socketed, tracker.getGameTimeDeltaPartialTick(false));
        }
        sendReleaseRequest();
        animate(now);
        lastFrame = now;
        pendingClick = false;
    }

    private void abandon(long now) {
        lastFrame = now;
        pendingClick = false;
        CasterKeyHandler.inputLock = true;
        CasterKeyHandler.radialOpen = false;
    }

    private void openPicker(Minecraft mc, LocalPlayer player, ItemStack socketed) {
        collectFoci(player);
        boolean hasContent = !entries.isEmpty() || !socketed.isEmpty();
        if (hasContent && mc.mouseHandler.isMouseGrabbed()) {
            mc.mouseHandler.releaseMouse();
        }
    }

    private void idle(Minecraft mc, long now) {
        boolean closing = lastActive && !CasterKeyHandler.radialOpen;
        if (closing) {
            lastActive = false;
            if (mc.player != null && mc.screen == null) {
                CasterKeyHandler.regrabMouse(mc);
            }
        }
        lastFrame = now;
        pendingClick = false;
    }

    private void collectFoci(LocalPlayer player) {
        entries.clear();
        centreHover = false;
        Inventory inventory = player.getInventory();
        List<ItemStack> carriedPouches = new ArrayList<>();
        for (int slot = 0; slot < MAIN_SLOTS; slot++) {
            ItemStack stack = inventory.getItem(slot);
            register(stack);
            if (isPouch(stack)) {
                carriedPouches.add(stack);
            }
        }
        for (ItemStack pouch : carriedPouches) {
            registerContents(pouch);
        }
        for (ItemStack pouch : curiosPouches(player)) {
            registerContents(pouch);
        }
    }

    private static List<ItemStack> curiosPouches(LocalPlayer player) {
        if (!ModList.get().isLoaded(TTIds.CURIOS)) {
            return List.of();
        }
        return ThaumaturgeCuriosCompat.equippedPouches(player, RadialFocusOverlay::isPouch).stream().map(CurioPouchRef::stack).toList();
    }

    private static boolean isPouch(ItemStack stack) {
        return stack.getItem() instanceof FocusPouchItem;
    }

    private void registerContents(ItemStack pouch) {
        for (ItemStack content : FocusPouchItem.getInventory(pouch)) {
            register(content);
        }
    }

    private void register(ItemStack stack) {
        if (!FocusItems.isFocus(stack)) {
            return;
        }
        String key = FocusItems.sortKey(stack);
        if (key != null) {
            entries.put(key, new FocusEntry(stack.copy()));
        }
    }

    private void drawPicker(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player, ItemStack socketed, float partialTick) {
        int centreX = graphics.guiWidth() / 2;
        int centreY = graphics.guiHeight() / 2;
        float width = BASE_RADIUS + RADIUS_PER_ENTRY * entries.size();
        float angle = partialTick + (player.tickCount % RING_SPIN_PERIOD_TICKS) / RING_SPIN_DIVISOR;
        drawRing(graphics, RING_ONE, centreX, centreY, width * RING_ONE_FACTOR * pickerScale, angle);
        drawRing(graphics, RING_TWO, centreX, centreY, width * RING_TWO_FACTOR * pickerScale, -angle);
        if (!socketed.isEmpty()) {
            graphics.item(socketed, centreX - ITEM_HALF, centreY - ITEM_HALF);
        }
        int cursorX = (int) mc.mouseHandler.getScaledXPos(mc.getWindow());
        int cursorY = (int) mc.mouseHandler.getScaledYPos(mc.getWindow());
        if (!socketed.isEmpty()) {
            handleCentre(mc, socketed, cursorX - centreX, cursorY - centreY);
        }
        drawEntries(graphics, mc, centreX, centreY, width, cursorX, cursorY);
        if (!tooltipStack.isEmpty()) {
            drawTooltip(graphics, mc, tooltipStack, centreX + TOOLTIP_OFFSET_X, centreY + TOOLTIP_OFFSET_Y);
        }
    }

    private static void drawTooltip(GuiGraphicsExtractor graphics, Minecraft mc, ItemStack stack, int x, int y) {
        List<ClientTooltipComponent> lines = new ArrayList<>();
        for (Component text : Screen.getTooltipFromItem(mc, stack)) {
            lines.add(ClientTooltipComponent.create(text.getVisualOrderText()));
        }
        int imageSlot = Math.min(1, lines.size());
        stack.getTooltipImage().ifPresent(image -> lines.add(imageSlot, ClientTooltipComponent.create(image)));
        if (!lines.isEmpty()) {
            graphics.tooltip(mc.font, lines, x, y, DefaultTooltipPositioner.INSTANCE, stack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    private static void drawRing(GuiGraphicsExtractor graphics, Identifier texture, int centreX, int centreY, float side, float degrees) {
        if (side <= 0.0F) {
            return;
        }
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(centreX, centreY);
        matrices.rotate((degrees + RING_ART_OFFSET_DEGREES) * Mth.DEG_TO_RAD);
        float zoom = side / RING_TEXTURE_SIZE;
        matrices.scale(zoom, zoom);
        int corner = -RING_TEXTURE_SIZE / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, corner, corner, 0.0F, 0.0F, RING_TEXTURE_SIZE, RING_TEXTURE_SIZE, RING_TEXTURE_SIZE, RING_TEXTURE_SIZE, RING_TINT);
        matrices.popMatrix();
    }

    private static boolean selectionOpen() {
        return CasterKeyHandler.radialOpen && !CasterKeyHandler.inputLock;
    }

    private static void commit(Minecraft mc, String key) {
        CasterKeyHandler.radialOpen = false;
        CasterKeyHandler.requestFocus(key);
        CasterKeyHandler.regrabMouse(mc);
    }

    private void handleCentre(Minecraft mc, ItemStack socketed, int offsetX, int offsetY) {
        if (!withinHover(offsetX, offsetY)) {
            centreHover = false;
            return;
        }
        tooltipStack = socketed;
        if (!selectionOpen()) {
            return;
        }
        centreHover = true;
        if (pendingClick) {
            commit(mc, CasterManager.REMOVE_FOCUS);
        }
    }

    private void drawEntries(GuiGraphicsExtractor graphics, Minecraft mc, int centreX, int centreY, float width, int cursorX, int cursorY) {
        int count = entries.size();
        if (count == 0) {
            return;
        }
        float start = -START_DEGREES * pickerScale;
        float slice = FULL_TURN_DEGREES / count;
        int index = 0;
        for (Map.Entry<String, FocusEntry> mapEntry : entries.entrySet()) {
            FocusEntry entry = mapEntry.getValue();
            float radians = (start + index++ * slice) * Mth.DEG_TO_RAD;
            float pullX = Mth.cos(radians) * width;
            float pullY = Mth.sin(radians) * width;
            drawEntryIcon(graphics, entry, centreX + (int) pullX * pickerScale, centreY + (int) pullY * pickerScale);
            if (selectionOpen()) {
                testEntry(mc, mapEntry.getKey(), entry, (int) (cursorX - (centreX + pullX)), (int) (cursorY - (centreY + pullY)));
            }
        }
    }

    private static void drawEntryIcon(GuiGraphicsExtractor graphics, FocusEntry entry, float iconX, float iconY) {
        float factor = entry.scale * pickerScale;
        if (factor <= 0.0F) {
            return;
        }
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(iconX, iconY);
        matrices.scale(factor, factor);
        graphics.item(entry.stack, -ITEM_HALF, -ITEM_HALF);
        matrices.popMatrix();
    }

    private void testEntry(Minecraft mc, String key, FocusEntry entry, int offsetX, int offsetY) {
        entry.hovered = withinHover(offsetX, offsetY);
        if (!entry.hovered) {
            return;
        }
        tooltipStack = entry.stack;
        if (pendingClick) {
            commit(mc, key);
        }
    }

    private static boolean withinHover(int offsetX, int offsetY) {
        return Math.max(Math.abs(offsetX), Math.abs(offsetY)) <= HOVER_HALF_SIZE;
    }

    private void sendReleaseRequest() {
        String chosen = releaseKey();
        if (chosen != null) {
            CasterKeyHandler.requestFocus(chosen);
        }
    }

    private @Nullable String releaseKey() {
        if (!selectionClosed()) {
            return null;
        }
        return centreHover ? CasterManager.REMOVE_FOCUS : firstHoveredKey();
    }

    private static boolean selectionClosed() {
        return !CasterKeyHandler.radialOpen && !CasterKeyHandler.inputLock;
    }

    private @Nullable String firstHoveredKey() {
        for (Map.Entry<String, FocusEntry> mapEntry : entries.entrySet()) {
            if (mapEntry.getValue().hovered) {
                return mapEntry.getKey();
            }
        }
        return null;
    }

    private void animate(long now) {
        if (now > lastFrame) {
            float elapsed = now - lastFrame;
            for (FocusEntry entry : entries.values()) {
                entry.scale = entry.hovered ? Math.min(HOVER_SCALE_MAX, entry.scale + elapsed / HOVER_GROW_MS) : Math.max(1.0F, entry.scale - elapsed / HOVER_DECAY_MS);
            }
            float step = elapsed / PICKER_MS;
            if (CasterKeyHandler.radialOpen) {
                pickerScale = Math.min(1.0F, pickerScale + step);
            } else {
                pickerScale -= step;
                if (pickerScale < 0.0F) {
                    pickerScale = 0.0F;
                    CasterKeyHandler.inputLock = false;
                }
            }
        }
        lastActive = CasterKeyHandler.radialOpen;
    }

    private static final class FocusEntry {
        private final ItemStack stack;
        private float scale = 1.0F;
        private boolean hovered;

        private FocusEntry(ItemStack stack) {
            this.stack = stack;
        }
    }
}

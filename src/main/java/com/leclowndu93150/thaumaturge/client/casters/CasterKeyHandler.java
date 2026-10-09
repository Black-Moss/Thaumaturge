package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.client.input.TTKeybinds;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.network.ServerboundCasterKeyPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundFocusChangePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CasterKeyHandler {
    private static final int MODIFIER_NONE = 0;
    private static final int MODIFIER_CONTROL = 1;
    private static final int MODIFIER_SHIFT = 2;

    static boolean radialOpen;
    static boolean inputLock;

    private static boolean focusLatch;
    private static boolean miscLatch;

    private CasterKeyHandler() {}

    public static boolean isRadialActive() {
        return radialOpen;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        boolean holdsCaster = !heldCaster(player).isEmpty();
        discardOffhandSwaps(mc, holdsCaster);
        pollFocusKey(mc, player, holdsCaster);
        pollMiscKey(mc);
    }

    static ItemStack heldCaster(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ICaster) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof ICaster ? off : ItemStack.EMPTY;
    }

    static ItemStack socketedFocus(ItemStack caster) {
        return caster.getItem() instanceof ICaster icaster ? icaster.getFocusStack(caster) : ItemStack.EMPTY;
    }

    static void requestFocus(String focusKey) {
        if (inputLock) {
            return;
        }
        inputLock = true;
        ClientPacketDistributor.sendToServer(new ServerboundFocusChangePayload(focusKey));
    }

    static void regrabMouse(Minecraft mc) {
        if (mc.isWindowActive() && !mc.mouseHandler.isMouseGrabbed()) {
            mc.mouseHandler.grabMouse();
        }
    }

    private static void discardOffhandSwaps(Minecraft mc, boolean holdsCaster) {
        KeyMapping swap = mc.options.keySwapOffhand;
        if (mc.screen != null || !holdsCaster || !TTKeybinds.CHANGE_FOCUS.same(swap)) {
            return;
        }
        while (swap.consumeClick()) {
            continue;
        }
    }

    private static void pollFocusKey(Minecraft mc, LocalPlayer player, boolean holdsCaster) {
        if (!TTKeybinds.CHANGE_FOCUS.isDown()) {
            radialOpen = false;
            focusLatch = false;
            return;
        }
        boolean inGame = mc.screen == null && (mc.mouseHandler.isMouseGrabbed() || radialOpen || RadialFocusOverlay.isAnimating());
        if (!inGame) {
            return;
        }
        if (!focusLatch) {
            inputLock = false;
        }
        if (!inputLock && holdsCaster) {
            if (player.isShiftKeyDown()) {
                requestFocus(CasterManager.REMOVE_FOCUS);
            } else {
                radialOpen = true;
            }
        }
        focusLatch = true;
    }

    private static void pollMiscKey(Minecraft mc) {
        if (!TTKeybinds.MISC_TOGGLE.isDown()) {
            miscLatch = false;
            return;
        }
        if (mc.screen != null || !mc.mouseHandler.isMouseGrabbed() || miscLatch) {
            return;
        }
        miscLatch = true;
        ClientPacketDistributor.sendToServer(new ServerboundCasterKeyPayload(modifierValue(mc)));
    }

    private static int modifierValue(Minecraft mc) {
        if (InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            return MODIFIER_CONTROL;
        }
        return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ? MODIFIER_SHIFT : MODIFIER_NONE;
    }
}

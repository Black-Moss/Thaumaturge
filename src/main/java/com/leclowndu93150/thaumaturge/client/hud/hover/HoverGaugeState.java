package com.leclowndu93150.thaumaturge.client.hud.hover;

import net.minecraft.world.item.ItemStack;

public record HoverGaugeState(ItemStack gear, int middle, int fillHeight, boolean hovering, int tickCount) {
}

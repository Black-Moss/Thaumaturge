package com.leclowndu93150.thaumaturge.client.hud.goggles;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record PositionedLine(Vec3 position, Component text) {
}

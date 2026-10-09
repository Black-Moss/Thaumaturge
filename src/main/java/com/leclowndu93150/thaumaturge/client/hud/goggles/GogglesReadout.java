package com.leclowndu93150.thaumaturge.client.hud.goggles;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record GogglesReadout(List<Component> lines, StackOrder order, Vec3 anchor) {
    public GogglesReadout {
        lines = List.copyOf(lines);
    }
}

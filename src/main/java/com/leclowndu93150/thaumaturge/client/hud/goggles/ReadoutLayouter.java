package com.leclowndu93150.thaumaturge.client.hud.goggles;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class ReadoutLayouter {
    private static final double HALF = 0.5;

    private ReadoutLayouter() {}

    public static List<PositionedLine> layout(BlockPos pos, GogglesReadout readout, ReadoutLayout layout) {
        List<Component> lines = readout.lines();
        Vec3 anchor = readout.anchor();
        double middle = lines.size() * HALF;
        double originX = pos.getX() + anchor.x + layout.blockCentre();
        double originY = pos.getY() + anchor.y + layout.blockCentre();
        double originZ = pos.getZ() + anchor.z + layout.blockCentre();
        List<PositionedLine> placed = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            double offset = readout.order().apply((i - middle) * layout.lineStep());
            placed.add(new PositionedLine(new Vec3(originX, originY + offset, originZ), lines.get(i)));
        }
        return placed;
    }
}

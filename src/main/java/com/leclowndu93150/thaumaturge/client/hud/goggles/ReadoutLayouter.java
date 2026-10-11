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
        Vec3 anchor = new Vec3(pos.getX() + layout.blockCentre(), pos.getY() + layout.blockCentre(), pos.getZ() + layout.blockCentre()).add(readout.anchor());
        double topRise = (lines.size() - 1) * HALF * layout.lineStep();
        List<PositionedLine> placed = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            double rise = topRise - i * layout.lineStep();
            placed.add(new PositionedLine(anchor.add(0.0, rise, 0.0), lines.get(i)));
        }
        return placed;
    }
}

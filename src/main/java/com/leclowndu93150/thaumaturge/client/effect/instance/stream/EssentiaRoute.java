package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class EssentiaRoute {
    private static final double ADVANCE_RADIUS_SQUARED = 0.16;

    private List<Vec3> waypoints;
    private final int fixedTail;
    private int index;

    public EssentiaRoute(Vec3 start, List<Vec3> route, int fixedTail) {
        this.waypoints = new ArrayList<>(route);
        if (this.waypoints.isEmpty()) {
            this.waypoints.add(start);
        }
        this.fixedTail = Mth.clamp(fixedTail, 1, this.waypoints.size());
    }

    public double length(Vec3 origin) {
        double total = 0.0;
        Vec3 previous = origin;
        for (Vec3 waypoint : this.waypoints) {
            total += previous.distanceTo(waypoint);
            previous = waypoint;
        }
        return total;
    }

    public Vec3 current() {
        return this.waypoints.get(this.index);
    }

    public Vec3 destination() {
        return this.waypoints.get(this.waypoints.size() - 1);
    }

    public boolean onFinalLeg() {
        return this.index == this.waypoints.size() - 1;
    }

    public void advanceIfReached(Vec3 head) {
        if (this.index < this.waypoints.size() - 1 && head.distanceToSqr(current()) < ADVANCE_RADIUS_SQUARED) {
            this.index++;
        }
    }

    public boolean canDetour() {
        return this.index <= firstFixedIndex();
    }

    public Vec3 firstFixedWaypoint() {
        return this.waypoints.get(firstFixedIndex());
    }

    public void spliceDetour(List<Vec3> detour) {
        int firstFixed = firstFixedIndex();
        List<Vec3> spliced = new ArrayList<>(detour);
        spliced.addAll(this.waypoints.subList(firstFixed, this.waypoints.size()));
        this.waypoints = spliced;
        this.index = 0;
    }

    private int firstFixedIndex() {
        return this.waypoints.size() - this.fixedTail;
    }
}

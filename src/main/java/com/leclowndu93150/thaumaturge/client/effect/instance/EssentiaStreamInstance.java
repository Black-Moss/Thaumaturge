package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.instance.stream.EssentiaDripEmitter;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.EssentiaRoute;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaStreamInstance extends StreamInstance {
    private static final int MIN_TRAIL_CAPACITY = 20;
    private static final double TICKS_PER_ROUTE_BLOCK = 21.0;
    private static final double DIRECTED_LAUNCH_SPEED = 0.05;
    private static final float DIRECTED_LAUNCH_WOBBLE = 0.005F;
    private static final float FREE_LAUNCH_WOBBLE = 0.015F;
    private static final int MIN_EXTENSION_STEP = 5;
    private static final double STEP_LIMIT = 0.05;
    private static final double PULL_FORCE = 0.01;
    private static final double LAST_LEG_SHRINK = 1.0;
    private static final double EARLY_LEG_SHRINK = 0.0;
    private static final int REROUTE_PERIOD = 40;
    private static final float SNAPSHOT_WOBBLE = 0.03F;
    private static final float SNAPSHOT_RADIUS_SCALE = 1.0F;

    private final EssentiaRoute route;
    private final EssentiaDripEmitter dripEmitter;

    public EssentiaStreamInstance(Vec3 start, List<Vec3> route, @Nullable Vec3 launchDirection, int fixedTail, int color, int count, float scale, int extend, double verticalBoost) {
        super(start.x, start.y, start.z, color, count, scale);
        this.route = new EssentiaRoute(start, route, fixedTail);
        this.dripEmitter = new EssentiaDripEmitter(start, this.colorR, this.colorG, this.colorB);
        this.length = Math.max(MIN_TRAIL_CAPACITY, extend);
        this.maxAge = Math.max(1, (int) (this.route.length(start) * TICKS_PER_ROUTE_BLOCK));
        if (launchDirection != null) {
            launchToward(launchDirection, DIRECTED_LAUNCH_SPEED, DIRECTED_LAUNCH_WOBBLE);
        } else {
            launchMotion(FREE_LAUNCH_WOBBLE, verticalBoost);
        }
    }

    public void extend(int amount) {
        int extension = Math.max(amount, MIN_EXTENSION_STEP);
        this.length += extension;
        this.maxAge += extension;
    }

    @Override
    protected Vec3 seekTarget(ClientLevel level) {
        this.route.advanceIfReached(position());
        return this.route.current();
    }

    @Override
    protected void restrainMotion(double distance) {
        clampMotion(STEP_LIMIT);
    }

    @Override
    protected double pullStrength(double distance) {
        return PULL_FORCE;
    }

    @Override
    protected double shrinkRange() {
        return this.route.onFinalLeg() ? LAST_LEG_SHRINK : EARLY_LEG_SHRINK;
    }

    @Override
    protected void onDissolve(ClientLevel level) {
        this.dripEmitter.dropAtDestination(level, this.route.destination());
    }

    @Override
    protected void afterStep(ClientLevel level) {
        if (this.age % REROUTE_PERIOD == 0) {
            tryDetour(level);
        }
        this.dripEmitter.dripFromTrail(level, this.trail);
    }

    public @Nullable Snapshot snapshot(float partialTick) {
        return buildSnapshot(partialTick, SNAPSHOT_WOBBLE, true, SNAPSHOT_RADIUS_SCALE);
    }

    private void tryDetour(ClientLevel level) {
        if (!this.route.canDetour()) {
            return;
        }
        Vec3 head = position();
        if (StreamPathfinder.hasLineOfSight(level, head, this.route.current())) {
            return;
        }
        List<Vec3> detour = StreamPathfinder.findRoute(level, head, this.route.firstFixedWaypoint());
        if (detour != null) {
            this.route.spliceDetour(detour);
        }
    }
}

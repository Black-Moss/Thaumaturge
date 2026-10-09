package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import java.util.ArrayList;
import java.util.List;

public final class TrailBuffer {
    private static final int NOT_DISSOLVING = -1;

    private final List<TrailPoint> points = new ArrayList<>();
    private int dissolveStartAge = NOT_DISSOLVING;

    public TrailBuffer(int seedPoints, double seedRadius) {
        for (int index = 0; index < seedPoints; index++) {
            this.points.add(new TrailPoint(seedRadius, 0.0, 0.0, 0.0));
        }
    }

    public List<TrailPoint> points() {
        return this.points;
    }

    public int size() {
        return this.points.size();
    }

    public TrailPoint get(int index) {
        return this.points.get(index);
    }

    public void append(TrailPoint point) {
        this.points.add(point);
    }

    public void trimTo(int capacity) {
        int excess = this.points.size() - Math.max(0, capacity);
        if (excess > 0) {
            this.points.subList(0, excess).clear();
        }
    }

    public void markDissolveStart(int age) {
        if (this.dissolveStartAge == NOT_DISSOLVING) {
            this.dissolveStartAge = age;
        }
    }

    public boolean isDissolving() {
        return this.dissolveStartAge != NOT_DISSOLVING;
    }

    public int dissolveStartAge() {
        return this.dissolveStartAge;
    }
}

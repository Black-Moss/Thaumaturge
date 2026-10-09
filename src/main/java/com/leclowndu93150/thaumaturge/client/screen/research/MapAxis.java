package com.leclowndu93150.thaumaturge.client.screen.research;

final class MapAxis {
    private double current;
    private double goal;
    private double previous;

    MapAxis(double value) {
        place(value);
    }

    void place(double value) {
        current = value;
        goal = value;
        previous = value;
    }

    void recentre(double value) {
        current = value;
        goal = value;
    }

    double current() {
        return current;
    }

    double goal() {
        return goal;
    }

    double previous() {
        return previous;
    }

    void setGoal(double value) {
        goal = value;
    }

    double gap() {
        return goal - current;
    }

    void rememberCurrent() {
        previous = current;
    }

    void arrive() {
        current = goal;
    }

    void glide(double amount) {
        current += amount;
    }

    void drag(double amount) {
        place(current - amount);
    }

    boolean isOutside(double low, double high) {
        return current < low || current > high;
    }

    static void follow(MapAxis horizontal, MapAxis vertical, double snapSquared, double factor) {
        horizontal.rememberCurrent();
        vertical.rememberCurrent();
        double dx = horizontal.gap();
        double dy = vertical.gap();
        if (dx * dx + dy * dy < snapSquared) {
            horizontal.arrive();
            vertical.arrive();
        } else {
            horizontal.glide(dx * factor);
            vertical.glide(dy * factor);
        }
    }
}

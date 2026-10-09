package com.leclowndu93150.thaumaturge.client.screen.research;

final class DragTracker {
    private boolean held;
    private int anchorX;
    private int anchorY;

    void release() {
        held = false;
    }

    boolean isHeld() {
        return held;
    }

    int deltaX(int x) {
        return x - anchorX;
    }

    int deltaY(int y) {
        return y - anchorY;
    }

    void anchorAt(int x, int y) {
        held = true;
        anchorX = x;
        anchorY = y;
    }
}

package com.leclowndu93150.thaumaturge.content.essentia.tube.vent;

public enum ClashVerdict {
    CLEAR, CLASH;

    public boolean isClash() {
        return this == CLASH;
    }
}

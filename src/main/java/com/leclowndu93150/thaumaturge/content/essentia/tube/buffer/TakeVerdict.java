package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

public enum TakeVerdict {
    GRANTED, OUTBID;

    public boolean isGranted() {
        return this == GRANTED;
    }
}

package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

public record BufferSideConfig(ChokeLevel choke, boolean open) {
    public static final BufferSideConfig DEFAULT = new BufferSideConfig(ChokeLevel.NORMAL, true);

    public BufferSideConfig withOpen(boolean value) {
        return new BufferSideConfig(choke, value);
    }

    public BufferSideConfig withChoke(ChokeLevel value) {
        return new BufferSideConfig(value, open);
    }
}

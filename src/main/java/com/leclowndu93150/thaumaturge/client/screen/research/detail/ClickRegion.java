package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import java.util.function.BooleanSupplier;

public record ClickRegion(Rect area, BooleanSupplier handler) {
    public static ClickRegion consuming(Rect area, Runnable action) {
        return new ClickRegion(area, () -> {
            action.run();
            return true;
        });
    }

    public static ClickRegion declining(Rect area, BooleanSupplier handler) {
        return new ClickRegion(area, handler);
    }
}

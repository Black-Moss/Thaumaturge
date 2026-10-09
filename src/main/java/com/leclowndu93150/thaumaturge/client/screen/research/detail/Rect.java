package com.leclowndu93150.thaumaturge.client.screen.research.detail;

public record Rect(int x, int y, int width, int height) {
    public static boolean inside(int x, int y, int width, int height, double pointX, double pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public boolean contains(double pointX, double pointY) {
        return inside(x, y, width, height, pointX, pointY);
    }
}

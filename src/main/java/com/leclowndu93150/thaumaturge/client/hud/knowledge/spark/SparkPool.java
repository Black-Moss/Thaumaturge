package com.leclowndu93150.thaumaturge.client.hud.knowledge.spark;

public final class SparkPool {
    public static final int CAPACITY = 200;

    final double[] x = new double[CAPACITY];
    final double[] y = new double[CAPACITY];
    final double[] lastX = new double[CAPACITY];
    final double[] lastY = new double[CAPACITY];
    final double[] velocityX = new double[CAPACITY];
    final double[] velocityY = new double[CAPACITY];
    final int[] delay = new int[CAPACITY];
    final int[] age = new int[CAPACITY];
    final int[] life = new int[CAPACITY];
    final int[] green = new int[CAPACITY];
    final int[] blue = new int[CAPACITY];
    final boolean[] star = new boolean[CAPACITY];
    private int size;

    public int size() {
        return size;
    }

    public boolean isFull() {
        return size >= CAPACITY;
    }

    public void add(double startX, double startY, double startVelocityX, double startVelocityY, int startDelay, int lifeTicks, boolean isStar, int greenChannel, int blueChannel) {
        int index = size++;
        x[index] = startX;
        y[index] = startY;
        lastX[index] = startX;
        lastY[index] = startY;
        velocityX[index] = startVelocityX;
        velocityY[index] = startVelocityY;
        delay[index] = startDelay;
        age[index] = 0;
        life[index] = lifeTicks;
        star[index] = isStar;
        green[index] = greenChannel;
        blue[index] = blueChannel;
    }

    public void clear() {
        size = 0;
    }

    public double x(int index) {
        return x[index];
    }

    public double y(int index) {
        return y[index];
    }

    public double lastX(int index) {
        return lastX[index];
    }

    public double lastY(int index) {
        return lastY[index];
    }

    public int delay(int index) {
        return delay[index];
    }

    public int age(int index) {
        return age[index];
    }

    public int life(int index) {
        return life[index];
    }

    public int green(int index) {
        return green[index];
    }

    public int blue(int index) {
        return blue[index];
    }

    public boolean star(int index) {
        return star[index];
    }

    void move(int from, int to) {
        if (from == to) {
            return;
        }
        x[to] = x[from];
        y[to] = y[from];
        lastX[to] = lastX[from];
        lastY[to] = lastY[from];
        velocityX[to] = velocityX[from];
        velocityY[to] = velocityY[from];
        delay[to] = delay[from];
        age[to] = age[from];
        life[to] = life[from];
        green[to] = green[from];
        blue[to] = blue[from];
        star[to] = star[from];
    }

    void truncate(int newSize) {
        size = newSize;
    }
}

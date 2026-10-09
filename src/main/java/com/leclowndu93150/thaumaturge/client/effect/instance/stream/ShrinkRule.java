package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

public interface ShrinkRule {
    boolean applies(double distance);

    double factor(double distance);
}

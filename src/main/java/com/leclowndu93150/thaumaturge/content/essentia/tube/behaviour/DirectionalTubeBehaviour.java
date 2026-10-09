package com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour;

public final class DirectionalTubeBehaviour extends DefaultTubeBehaviour {
    public static final DirectionalTubeBehaviour INSTANCE = new DirectionalTubeBehaviour();

    private DirectionalTubeBehaviour() {}

    @Override
    public boolean directionalSuction() {
        return true;
    }

    @Override
    public boolean directionalEqualize() {
        return true;
    }
}

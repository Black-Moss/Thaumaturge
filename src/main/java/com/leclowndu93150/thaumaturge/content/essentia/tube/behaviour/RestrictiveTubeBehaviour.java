package com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour;

public final class RestrictiveTubeBehaviour extends DefaultTubeBehaviour {
    public static final RestrictiveTubeBehaviour INSTANCE = new RestrictiveTubeBehaviour();

    private RestrictiveTubeBehaviour() {}

    @Override
    public boolean restrictiveSuction() {
        return true;
    }
}

package com.leclowndu93150.thaumaturge.content.aura.pressure;

public abstract class AbstractFluxPressureEvent implements FluxPressureEvent {
    private final Traits traits;

    protected AbstractFluxPressureEvent(String name, int weight, float cost, boolean allowedNearTaint) {
        this.traits = new Traits(name, weight, cost, allowedNearTaint);
    }

    @Override
    public final String name() {
        return traits.label();
    }

    @Override
    public final int weight() {
        return traits.chance();
    }

    @Override
    public final float cost() {
        return traits.price();
    }

    @Override
    public final boolean allowedNearTaint() {
        return traits.taintSafe();
    }

    private record Traits(String label, int chance, float price, boolean taintSafe) {
    }
}

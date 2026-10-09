package com.leclowndu93150.thaumaturge.content.essentia.tube.valve;

public enum ValveState {
    OPEN(true, false), CLOSED(false, true), HELD_OPEN(true, true), HELD_CLOSED(false, false);

    private final boolean allowsFlow;
    private final boolean poweredAtLastSample;

    ValveState(boolean allowsFlow, boolean poweredAtLastSample) {
        this.allowsFlow = allowsFlow;
        this.poweredAtLastSample = poweredAtLastSample;
    }

    public static ValveState of(boolean allowsFlow, boolean poweredAtLastSample) {
        for (ValveState state : values()) {
            if (state.allowsFlow == allowsFlow && state.poweredAtLastSample == poweredAtLastSample) {
                return state;
            }
        }
        throw new IllegalStateException("Unreachable valve state combination");
    }

    public boolean allowsFlow() {
        return allowsFlow;
    }

    public boolean poweredAtLastSample() {
        return poweredAtLastSample;
    }

    public boolean isEdge(boolean powered) {
        return powered != poweredAtLastSample;
    }

    public ValveState afterSample(boolean powered) {
        return of(!powered, powered);
    }

    public ValveState withFlow(boolean allow) {
        return of(allow, poweredAtLastSample);
    }
}

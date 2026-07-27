package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;

/**
 * Behavior attached to a golem part. Called every entity tick on both sides for each
 * part of the golem that declares a function.
 *
 * @since 1.0.0
 */
public interface IGolemFunction {
    /**
     * Runs once per golem tick.
     *
     * @param golem the golem carrying the part
     */
    void onUpdateTick(IGolemAPI golem);
}

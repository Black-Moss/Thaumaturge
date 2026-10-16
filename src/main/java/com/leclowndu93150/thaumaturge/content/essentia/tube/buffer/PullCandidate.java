package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;

public record PullCandidate(Direction side, IEssentiaConduit peer, Holder<IAspect> aspect) {
}

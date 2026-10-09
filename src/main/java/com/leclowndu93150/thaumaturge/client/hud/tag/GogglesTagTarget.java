package com.leclowndu93150.thaumaturge.client.hud.tag;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record GogglesTagTarget(BlockPos block, AspectList aspects, boolean showAmounts, Direction face, Vec3 origin) {
}

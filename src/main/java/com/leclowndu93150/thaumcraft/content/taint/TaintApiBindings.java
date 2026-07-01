package com.leclowndu93150.thaumcraft.content.taint;

import com.leclowndu93150.thaumcraft.api.taint.TaintApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class TaintApiBindings implements TaintApi.Bindings {

    @Override
    public void addTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.addTaintSeed(level, pos);
    }

    @Override
    public void removeTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.removeTaintSeed(level, pos);
    }

    @Override
    public boolean isNearTaintSeed(Level level, BlockPos pos) {
        return TaintHelper.isNearTaintSeed(level, pos);
    }

    @Override
    public boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return TaintHelper.isAtTaintSeedEdge(level, pos);
    }

    @Override
    public void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        TaintHelper.spreadFibres(level, pos, force);
    }
}

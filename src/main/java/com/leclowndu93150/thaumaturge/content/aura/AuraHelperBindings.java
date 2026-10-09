package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class AuraHelperBindings implements AuraHelper.Bindings {

    @Override
    public IAuraChunk chunkLookup(ServerLevel level, ChunkPos pos) {
        AuraData data = AuraManager.chunkAt(level, pos);
        if (data != null) {
            return data;
        }
        AuraData empty = new AuraData();
        empty.setChunkPos(pos);
        return empty;
    }

    @Override
    public IAuraChunk blockLookup(Level level, BlockPos pos) {
        AuraData data = AuraManager.chunkAt(level, pos);
        if (data != null) {
            return data;
        }
        return new AuraData();
    }

    @Override
    public float getVis(Level level, BlockPos pos) {
        return AuraManager.visAt(level, pos);
    }

    @Override
    public float getFlux(Level level, BlockPos pos) {
        return AuraManager.fluxAt(level, pos);
    }

    @Override
    public int getAuraBase(Level level, BlockPos pos) {
        return AuraManager.baseCapacity(level, pos);
    }

    @Override
    public float getTotalAura(Level level, BlockPos pos) {
        return AuraManager.combinedLevel(level, pos);
    }

    @Override
    public float getFluxSaturation(Level level, BlockPos pos) {
        return AuraManager.fluxRatio(level, pos);
    }

    @Override
    public boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
        return AuraManager.isPreservationDue(level, player, pos);
    }

    @Override
    public void addVis(Level level, BlockPos pos, float amount) {
        AuraManager.creditVis(level, pos, amount);
    }

    @Override
    public void addFlux(Level level, BlockPos pos, float amount) {
        AuraManager.creditFlux(level, pos, amount);
    }

    @Override
    public float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraManager.withdrawVis(level, pos, amount, simulate);
    }

    @Override
    public float drainVis(Level level, BlockPos pos, float amount, TransactionContext transaction) {
        return AuraManager.withdrawVis(level, pos, amount, transaction);
    }

    @Override
    public float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraManager.withdrawFlux(level, pos, amount, simulate);
    }

    @Override
    public void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect) {
        AuraManager.taint(level, pos, amount, showEffect);
    }

    @Override
    public float capacityRemaining(Level level, BlockPos pos) {
        return Math.max(0.0F, AuraManager.baseCapacity(level, pos) - AuraManager.combinedLevel(level, pos));
    }

    @Override
    public boolean canAcceptVis(Level level, BlockPos pos, float amount) {
        return capacityRemaining(level, pos) >= amount;
    }
}

package com.leclowndu93150.thaumcraft.content.aura;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.aura.IAuraChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class AuraHelperBindings implements AuraHelper.Bindings {

    @Override
    public IAuraChunk chunkLookup(ServerLevel level, ChunkPos pos) {
        AuraData data = AuraManager.getAuraChunk(level, pos);
        if (data != null) {
            return data;
        }
        AuraData empty = new AuraData();
        empty.setChunkPos(pos);
        return empty;
    }

    @Override
    public IAuraChunk blockLookup(Level level, BlockPos pos) {
        AuraData data = AuraManager.getAuraChunk(level, pos);
        if (data != null) {
            return data;
        }
        return new AuraData();
    }

    @Override
    public float getVis(Level level, BlockPos pos) {
        return AuraManager.getVis(level, pos);
    }

    @Override
    public float getFlux(Level level, BlockPos pos) {
        return AuraManager.getFlux(level, pos);
    }

    @Override
    public int getAuraBase(Level level, BlockPos pos) {
        return AuraManager.getAuraBase(level, pos);
    }

    @Override
    public float getTotalAura(Level level, BlockPos pos) {
        return AuraManager.getTotalAura(level, pos);
    }

    @Override
    public float getFluxSaturation(Level level, BlockPos pos) {
        return AuraManager.getFluxSaturation(level, pos);
    }

    @Override
    public boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
        return AuraManager.shouldPreserveAura(level, player, pos);
    }

    @Override
    public void addVis(Level level, BlockPos pos, float amount) {
        AuraManager.addVis(level, pos, amount);
    }

    @Override
    public void addFlux(Level level, BlockPos pos, float amount) {
        AuraManager.addFlux(level, pos, amount);
    }

    @Override
    public float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraManager.drainVis(level, pos, amount, simulate);
    }

    @Override
    public float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraManager.drainFlux(level, pos, amount, simulate);
    }

    @Override
    public void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect) {
        AuraManager.polluteAura(level, pos, amount, showEffect);
    }

    @Override
    public float capacityRemaining(Level level, BlockPos pos) {
        return Math.max(0.0F, AuraManager.getAuraBase(level, pos) - AuraManager.getTotalAura(level, pos));
    }

    @Override
    public boolean canAcceptVis(Level level, BlockPos pos, float amount) {
        return capacityRemaining(level, pos) >= amount;
    }
}

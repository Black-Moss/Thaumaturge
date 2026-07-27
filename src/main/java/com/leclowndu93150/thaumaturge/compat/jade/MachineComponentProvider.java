package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MachineComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier UID = TCIds.rl("machine");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof BlockEntitySmelter)) {
            return;
        }
        CompoundTag data = accessor.getServerData();
        int progress = data.getIntOr("SmeltProgress", 0);
        int burn = data.getIntOr("BurnRemaining", 0);
        if (progress > 0) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.progress", progress));
        }
        if (burn > 0) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.heat", burn));
        }
    }
}

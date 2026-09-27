package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum BlockDetailsProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;
    private static final Identifier UID = TCIds.rl("block_details");
    private static final String DATA = "ThaumaturgeDetails";

    @Override
    public Identifier getUid() {
        return UID;
    }
    @Override
    public boolean isRequired() {
        return true;
    }
    @Override
    public int getDefaultPriority() {
        return 1100;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity entity = JadeBlockProviderHolder.resolve(accessor);
        if (entity == null)
            return;
        for (JadeBlockHandler<?> handler : JadeBlockProviderHolder.HANDLERS) {
            if (handler.type().isInstance(entity)) {
                tag.store(DATA, JadeBlockDetails.CODEC, handler.read(entity, accessor.getPlayer()));
                return;
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        accessor.getServerData().read(DATA, JadeBlockDetails.CODEC).ifPresent(data -> {
            if (!JadeConfig.shouldShow(config, data.option(), accessor)) {
                if (data.hideFluid())
                    tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
                return;
            }
            data.title().ifPresent(title -> tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(title)));
            data.summary().forEach(tooltip::add);
            if (accessor.showDetails())
                data.details().forEach(tooltip::add);
        });
    }
}

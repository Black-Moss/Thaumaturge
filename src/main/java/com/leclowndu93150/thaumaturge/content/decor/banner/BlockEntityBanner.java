package com.leclowndu93150.thaumaturge.content.decor.banner;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityBanner extends AbstractSyncedBlockEntity {
    private static final String ASPECT_KEY = "aspect";

    private @Nullable ResourceKey<IAspect> aspect;

    public BlockEntityBanner(BlockPos pos, BlockState state) {
        super(TTBlockEntities.BANNER.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public void setAspect(@Nullable ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
        if (level != null) {
            setChangedAndSync();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String stored = input.getStringOr(ASPECT_KEY, "");
        Identifier id = stored.isEmpty() ? null : Identifier.tryParse(stored);
        aspect = id == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, id);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString(ASPECT_KEY, aspect == null ? "" : aspect.identifier().toString());
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (aspect != null) {
            components.set(TTDataComponents.ASPECT_FILTER.get(), aspect);
        }
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        aspect = components.get(TTDataComponents.ASPECT_FILTER.get());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(ASPECT_KEY);
    }
}

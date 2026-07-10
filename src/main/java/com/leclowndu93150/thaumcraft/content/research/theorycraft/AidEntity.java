package com.leclowndu93150.thaumcraft.content.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

public final class AidEntity implements ITheorycraftAid {
    private static final double SCAN_RANGE = 5.0;

    private final List<ResourceKey<CardFactory>> cards;
    private final Supplier<EntityType<?>> entityType;
    private final Supplier<ItemStack> display;
    private final String descriptionKey;

    public AidEntity(Supplier<EntityType<?>> entityType, Supplier<ItemStack> display,
                     String descriptionKey, List<ResourceKey<CardFactory>> cards) {
        this.entityType = entityType;
        this.display = display;
        this.descriptionKey = descriptionKey;
        this.cards = cards;
    }

    @Override
    public List<ResourceKey<CardFactory>> cards() {
        return cards;
    }

    @Override
    public boolean matches(LevelAccessor level, BlockPos tablePos) {
        if (!(level instanceof Level realLevel)) return false;
        AABB box = new AABB(tablePos).inflate(SCAN_RANGE);
        List<Entity> found = realLevel.getEntities((Entity) null, box,
                entity -> entity.getType() == entityType.get());
        return !found.isEmpty();
    }

    @Override
    public ItemStack displayStack() {
        return display.get();
    }

    @Override
    public Component description() {
        return Component.translatable(descriptionKey);
    }
}

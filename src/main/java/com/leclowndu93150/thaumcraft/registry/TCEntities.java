package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.*;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TCIds.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WispEntity>> WISP = register(
            "wisp",
            () -> EntityType.Builder.of(WispEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<ThaumicSlime>> THAUMIC_SLIME = register(
            "thaumic_slime",
            () -> EntityType.Builder.of(ThaumicSlime::new, MobCategory.MONSTER)
                    .sized(0.52F, 0.52F)
                    .eyeHeight(0.325F)
                    .spawnDimensionsScale(4.0F)
                    .clientTrackingRange(8)
                    .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintCrawler>> TAINT_CRAWLER = register(
            "taint_crawler",
            () -> EntityType.Builder.of(EntityTaintCrawler::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.4F)
                    .eyeHeight(0.1F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSeed>> TAINT_SEED = register(
            "taint_seed",
            () -> EntityType.Builder.of(EntityTaintSeed::new, MobCategory.MONSTER)
                    .sized(1.5F, 1.25F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSeedPrime>> TAINT_SEED_PRIME = register(
            "taint_seed_prime",
            () -> EntityType.Builder.of(EntityTaintSeedPrime::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSwarm>> TAINT_SWARM = register(
            "taint_swarm",
            () -> EntityType.Builder.of(EntityTaintSwarm::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintacle>> TAINTACLE = register(
            "taintacle",
            () -> EntityType.Builder.of(EntityTaintacle::new, MobCategory.MONSTER)
                    .sized(0.8F, 3.0F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintacleSmall>> TAINTACLE_SMALL = register(
            "taintacle_small",
            () -> EntityType.Builder.of(EntityTaintacleSmall::new, MobCategory.MONSTER)
                    .sized(0.22F, 1.0F)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFallingTaint>> FALLING_TAINT = register(
            "falling_taint",
            () -> EntityType.Builder.<EntityFallingTaint>of(EntityFallingTaint::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBottleTaint>> BOTTLE_TAINT = register(
            "bottle_taint",
            () -> EntityType.Builder.<EntityBottleTaint>of(EntityBottleTaint::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpecialItem>> SPECIAL_ITEM = register(
            "special_item",
            () -> EntityType.Builder.<EntitySpecialItem>of(EntitySpecialItem::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .eyeHeight(0.2125F)
                    .clientTrackingRange(8)
                    .updateInterval(20));

    private TCEntities() {}

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(
            String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITIES.register(name, () -> builderSupplier.get().build(
                ResourceKey.create(Registries.ENTITY_TYPE,
                        Identifier.fromNamespaceAndPath(TCIds.MODID, name))));
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}

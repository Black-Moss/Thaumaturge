package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LegacyTaintedAnimals {
    private static final List<Replacement> REPLACEMENTS = List.of(new Replacement(TTEntities.LEGACY_TAINT_CHICKEN, () -> EntityType.CHICKEN),
            new Replacement(TTEntities.LEGACY_TAINT_PIG, () -> EntityType.PIG), new Replacement(TTEntities.LEGACY_TAINT_SHEEP, () -> EntityType.SHEEP));

    private LegacyTaintedAnimals() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob legacy) || !(entity.level() instanceof ServerLevel level) || legacy.isRemoved()) {
            return;
        }
        for (Replacement replacement : REPLACEMENTS) {
            if (legacy.getType() == replacement.legacy().get()) {
                replace(level, legacy, replacement.current().get());
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void replace(ServerLevel level, Mob legacy, EntityType<? extends Mob> current) {
        Mob replacement = current.create(level, EntitySpawnReason.CONVERSION);
        if (replacement == null) {
            Thaumaturge.LOGGER.error("Could not create {} to replace legacy {} at {}", current, legacy.getType(), legacy.blockPosition());
            return;
        }
        List<Entity> riders = List.copyOf(legacy.getPassengers());
        Entity vehicle = legacy.getVehicle();
        legacy.ejectPassengers();
        legacy.stopRiding();
        replacement.restoreFrom(legacy);
        legacy.discard();
        if (!level.addFreshEntity(replacement)) {
            Thaumaturge.LOGGER.error("Could not add {} replacing legacy {} at {}", current, legacy.getType(), legacy.blockPosition());
            return;
        }
        MobTraits.add(replacement, TTMobTraits.TAINTED);
        for (Entity rider : riders) {
            rider.startRiding(replacement);
        }
        if (vehicle != null) {
            replacement.startRiding(vehicle);
        }
        Thaumaturge.LOGGER.debug("Replaced legacy {} with tainted {} at {}", legacy.getType(), current, replacement.blockPosition());
    }

    private record Replacement(Supplier<? extends EntityType<? extends Mob>> legacy, Supplier<EntityType<? extends Mob>> current) {
    }
}

package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSplosion;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.Collection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintCreatureEvents {
    private static final float REPLACEMENT_RADIUS = 1.5F;
    private static final double BLAST_RANGE = 6.0;
    private static final int BLAST_DURATION = 100;
    private static final float BLAST_SPREAD = 5.0F;
    private static final double BODY_MIDDLE = 0.5;
    private static final float INFECTION_HEALTH = 2.0F;
    private static final int BROOD_EXPERIENCE = 2;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Creeper creeper = findBlastingCreeper(event.getExplosion());
        if (creeper == null) {
            return;
        }
        event.setCanceled(true);
        level.explode(creeper, creeper.getX(), creeper.getY(BODY_MIDDLE), creeper.getZ(), REPLACEMENT_RADIUS, false, Level.ExplosionInteraction.NONE);
        AABB reach = creeper.getBoundingBox().inflate(BLAST_RANGE);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, reach)) {
            if (isBlastVictim(creeper, victim)) {
                FluxTaintExposure.expose(victim, BLAST_DURATION, false, true);
            }
        }
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        TaintSplosion.burstAtHeight(level, creeper.blockPosition(), level.getRandom(), BLAST_SPREAD);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (infectsOnLowHealth(victim)) {
            TaintInfection.tryInfect(level, victim);
            return;
        }
        boolean damaging = event.getAmount() > 0.0F;
        if (damaging && event.getSource().getEntity() instanceof LivingEntity attacker && MobTraits.isTainted(attacker)) {
            FluxTaintExposure.exposeQuietly(victim);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity living && MobTraits.has(living, MobTraits.TAINTED) && MobTraitEngine.suppressesNativeAi(living)
                && !event.getItemStack().is(Tags.Items.TOOLS_SHEAR)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living.level() instanceof ServerLevel level)) {
            return;
        }
        if (isBrood(living)) {
            event.setCanceled(true);
            return;
        }
        if (!MobTraits.has(living, MobTraits.TAINTED)) {
            return;
        }
        TaintedProfile profile = TaintedProfile.of(living.getType());
        if (profile == null || profile.lootTable().isEmpty()) {
            return;
        }
        ResourceKey<LootTable> table = profile.lootTable().get();
        Collection<ItemEntity> drops = event.getDrops();
        drops.clear();
        living.dropFromLootTable(level, event.getSource(), event.isRecentlyHit(), table, stack -> drops.add(dropOf(level, living, stack)));
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (isBrood(event.getEntity())) {
            event.setDroppedExperience(BROOD_EXPERIENCE);
        }
    }

    private static Creeper findBlastingCreeper(ServerExplosion explosion) {
        if (explosion.radius() == REPLACEMENT_RADIUS || !(explosion.getDirectSourceEntity() instanceof Creeper creeper)) {
            return null;
        }
        return MobTraits.has(creeper, TTMobTraits.TAINT_BLAST.getKey()) ? creeper : null;
    }

    private static boolean isBlastVictim(Creeper creeper, LivingEntity victim) {
        return victim.distanceTo(creeper) <= BLAST_RANGE && !MobTraits.isTainted(victim) && !victim.is(EntityTypeTags.UNDEAD);
    }

    private static boolean isBrood(LivingEntity living) {
        return MobTraits.has(living, TTMobTraits.TAINT_BROOD.getKey());
    }

    private static boolean infectsOnLowHealth(LivingEntity victim) {
        return victim.getHealth() < INFECTION_HEALTH && !victim.is(EntityTypeTags.UNDEAD) && victim.isAlive() && !(victim instanceof EntityOwnedConstruct) && victim.hasEffect(TTMobEffects.FLUX_TAINT)
                && victim.getRandom().nextBoolean();
    }

    private static ItemEntity dropOf(ServerLevel level, LivingEntity source, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
        drop.setDefaultPickUpDelay();
        return drop;
    }
}

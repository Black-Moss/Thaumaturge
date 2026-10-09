package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.mixin.world.entity.ExperienceOrbAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BaubleEvents {
    private static final long NO_JUMP = 0L;
    private static final long JUMP_GRACE_TICKS = 100L;
    private static final double JUMP_FALL_GRACE = 16.0;
    private static final float SURVIVAL_HEALTH = 1.0F;
    private static final int REGENERATION_TICKS = 900;
    private static final int ABSORPTION_TICKS = 100;
    private static final int SURVIVAL_AMPLIFIER = 1;
    private static final byte TOTEM_EVENT = 35;
    private static final int MIN_ORB_VALUE = 1;
    private static final int ORB_DRAIN_DIVISOR = 2;
    private static final double THEORY_CHANCE_PER_POINT = 0.05;
    private static final double OBSERVATION_CHANCE_PER_POINT = 0.2;
    private static final double CERTAIN = 1.0;
    private static final int KNOWLEDGE_GAIN = 1;

    private static final List<RevivalEffect> REVIVAL_EFFECTS = List.of(new RevivalEffect(MobEffects.REGENERATION, REGENERATION_TICKS), new RevivalEffect(MobEffects.ABSORPTION, ABSORPTION_TICKS));
    private static final List<KnowledgeTier> KNOWLEDGE_TIERS = List.of(new KnowledgeTier(KnowledgeType.THEORY, THEORY_CHANCE_PER_POINT),
            new KnowledgeTier(KnowledgeType.OBSERVATION, OBSERVATION_CHANCE_PER_POINT));

    private BaubleEvents() {}

    private record RevivalEffect(Holder<MobEffect> effect, int duration) {
        void applyTo(LivingEntity target) {
            target.addEffect(new MobEffectInstance(effect, duration, SURVIVAL_AMPLIFIER));
        }
    }

    private record KnowledgeTier(KnowledgeType type, double chancePerPoint) {
        boolean wins(double roll, int drained) {
            return roll < Math.min(CERTAIN, chancePerPoint * drained);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && hasCurio(player, TTItems.CLOUD_RING.get()) && spendCloudJump(player)) {
            double reduced = Math.max(0.0, event.getDistance() - JUMP_FALL_GRACE);
            event.setDistance(reduced);
        }
    }

    private static boolean spendCloudJump(Player player) {
        boolean pending = hasPendingCloudJump(player);
        player.setData(TTAttachments.CLOUD_JUMP_TIME.get(), NO_JUMP);
        return pending;
    }

    public static boolean hasPendingCloudJump(Player player) {
        long jumpTime = player.getData(TTAttachments.CLOUD_JUMP_TIME);
        return jumpTime != NO_JUMP && player.level().getGameTime() - jumpTime <= JUMP_GRACE_TICKS;
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ExperienceOrb orb = event.getOrb();
        int value = orb.getValue();
        if (value <= MIN_ORB_VALUE || !hasCurio(player, TTItems.CURIOSITY_BAND.get())) {
            return;
        }
        int drained = value / ORB_DRAIN_DIVISOR;
        ((ExperienceOrbAccessor) orb).thaumaturge$setValue(value - drained);
        double roll = player.getRandom().nextDouble();
        KNOWLEDGE_TIERS.stream().filter(tier -> tier.wins(roll, drained)).findFirst().ifPresent(tier -> ResearchGrants.grantConvertedKnowledge(player, tier.type(), KNOWLEDGE_GAIN));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        Entity dying = event.getEntity();
        if (dying instanceof ServerPlayer player) {
            claimUndyingCharm(player).ifPresent(charm -> cheatDeath(event, player, charm));
        }
    }

    private static Optional<ItemStack> claimUndyingCharm(ServerPlayer player) {
        if (!ModList.get().isLoaded(TTIds.CURIOS)) {
            return Optional.empty();
        }
        return Optional.of(ThaumaturgeCuriosCompat.extractCurio(player, TTItems.CHARM_UNDYING.get())).filter(stack -> !stack.isEmpty());
    }

    private static void cheatDeath(LivingDeathEvent event, ServerPlayer player, ItemStack charm) {
        event.setCanceled(true);
        player.removeAllEffects();
        REVIVAL_EFFECTS.forEach(revival -> revival.applyTo(player));
        player.setHealth(SURVIVAL_HEALTH);
        player.level().broadcastEntityEvent(player, TOTEM_EVENT);
        CriteriaTriggers.USED_TOTEM.trigger(player, charm);
        player.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
    }

    private static boolean hasCurio(Player player, Item item) {
        return ModList.get().isLoaded(TTIds.CURIOS) && ThaumaturgeCuriosCompat.isCurioEquipped(player, item);
    }
}

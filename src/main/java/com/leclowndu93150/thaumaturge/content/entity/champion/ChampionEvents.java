package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.Collection;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ChampionEvents {
    private static final int ROLL_BOUND = 100;
    private static final double MIN_BASE_HEALTH = 10.0;
    private static final int LENIENT_BONUS = 2;
    private static final int HARD_PENALTY = 2;
    private static final int SPOOKY_PENALTY = 2;
    private static final int SPOOKY_PENALTY_DISABLED = 1;
    private static final int WHITELIST_FLOOR = 1;
    private static final int KILL_EXPERIENCE_BASE = 5;
    private static final int KILL_EXPERIENCE_SPREAD = 3;

    private static final Set<ResourceKey<Level>> SPOOKY_DIMENSIONS = Set.of(Level.NETHER, Level.END);

    private ChampionEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !(event.getEntity() instanceof Monster mob) || mob instanceof EntityCultistPortalLesser || ChampionHelper.rolled(mob)) {
            return;
        }
        if (winsRoll(level, mob)) {
            ChampionHelper.makeChampion(mob, false);
        } else {
            ChampionHelper.markRolled(mob);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (isServerSideEldritch(target)) {
            ShieldChargeSound.playIfShielded(target);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!event.isRecentlyHit() || !MobTraits.isChampion(victim) || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        Player killer = victim.getLastHurtByPlayer();
        if (killer == null || killer instanceof FakePlayer) {
            return;
        }
        ExperienceOrb.award(level, victim.position(), KILL_EXPERIENCE_BASE + victim.getRandom().nextInt(KILL_EXPERIENCE_SPREAD));
        LootTable table = level.getServer().reloadableRegistries().getLootTable(TTLootTables.CHAMPION_BAG);
        Collection<ItemEntity> drops = event.getDrops();
        table.getRandomItems(bagParams(level, victim, killer, event.getSource()), reward -> drops.add(rewardEntity(level, victim, reward)));
    }

    private static ItemEntity rewardEntity(ServerLevel level, LivingEntity victim, ItemStack reward) {
        return new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), reward);
    }

    private static boolean isServerSideEldritch(LivingEntity entity) {
        return !entity.level().isClientSide() && entity.getType().builtInRegistryHolder().is(ThaumaturgeEntityTypeTags.ELDRITCH);
    }

    private static LootParams bagParams(ServerLevel level, LivingEntity victim, Player killer, DamageSource source) {
        LootParams.Builder builder = new LootParams.Builder(level);
        builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer);
        builder.withLuck(killer.getLuck());
        builder.withParameter(LootContextParams.DAMAGE_SOURCE, source);
        builder.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
        builder.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity());
        builder.withParameter(LootContextParams.ORIGIN, victim.position());
        builder.withParameter(LootContextParams.THIS_ENTITY, victim);
        return builder.create(LootContextParamSets.ENTITY);
    }

    private static boolean winsRoll(Level level, Monster mob) {
        Integer weight = mob.getType().builtInRegistryHolder().getData(ChampionDataMaps.CHAMPION_WHITELIST);
        boolean eligible = weight != null && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) >= MIN_BASE_HEALTH;
        if (!eligible) {
            return false;
        }
        boolean enabled = ThaumaturgeCommonConfig.ALLOW_CHAMPION_MOBS.get();
        int roll = mob.getRandom().nextInt(ROLL_BOUND);
        return roll + rollModifier(level, mob, enabled, weight) <= 0;
    }

    private static int rollModifier(Level level, Monster mob, boolean enabled, int weight) {
        int modifier = 0;
        Difficulty difficulty = level.getDifficulty();
        if (difficulty == Difficulty.EASY || !enabled) {
            modifier += LENIENT_BONUS;
        }
        if (difficulty == Difficulty.HARD && enabled) {
            modifier -= HARD_PENALTY;
        }
        if (isSpooky(level, mob)) {
            modifier -= enabled ? SPOOKY_PENALTY : SPOOKY_PENALTY_DISABLED;
        }
        if (enabled && weight > WHITELIST_FLOOR) {
            modifier -= weight - WHITELIST_FLOOR;
        }
        return modifier;
    }

    private static boolean isSpooky(Level level, Monster mob) {
        return SPOOKY_DIMENSIONS.contains(level.dimension()) || level.getBiome(mob.blockPosition()).is(TTBiomeTags.IS_SPOOKY);
    }
}

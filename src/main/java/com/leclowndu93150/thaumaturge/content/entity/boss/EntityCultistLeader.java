package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityCultistLeader extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final String NAME_KEY = "entity.thaumaturge.cultist_leader.name.custom";
    private static final List<String> TITLE_NAMES = List.of("Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe",
            "Obano", "Zelipe");
    private static final BossTitles TITLES = new BossTitles(NAME_KEY, TITLE_NAMES);
    private static final List<Class<? extends Entity>> CULT_MEMBER_TYPES = List.of(EntityCultist.class, EntityCultistLeader.class);
    private static final Map<EquipmentSlot, Float> NO_DROP_OVERRIDES = Map.of();
    private static final double MAX_HEALTH = 150.0;
    private static final double MOVEMENT_SPEED = 0.32;
    private static final double ATTACK_DAMAGE = 5.0;
    private static final int EXPERIENCE = 40;
    private static final int FLOAT_PRIORITY = 0;
    private static final int RANGED_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int HOME_PRIORITY = 6;
    private static final int STROLL_PRIORITY = 7;
    private static final int LOOK_PRIORITY = 8;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_TARGET_PRIORITY = 2;
    private static final double RANGED_SPEED = 1.0;
    private static final double RANGED_MIN_DISTANCE = 16.0;
    private static final int RANGED_MIN_INTERVAL = 30;
    private static final int RANGED_MAX_INTERVAL = 40;
    private static final float RANGED_RADIUS = 24.0F;
    private static final double MELEE_SPEED = 1.1;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 0.8;
    private static final float LOOK_RANGE = 8.0F;
    private static final float WEAPON_ENCHANT_CHANCE = 0.5F;
    private static final int RALLY_INTERVAL = 5;
    private static final double RALLY_RADIUS = 8.0;
    private static final int RALLY_TICKS = 60;
    private static final int RALLY_AMPLIFIER = 1;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 3.0F;
    private static final double ORB_CLEARANCE = 1.0;
    private static final double ORB_ARC_PER_BLOCK = 0.1;
    private static final double CHEST_FRACTION = 0.65;
    private static final float ORB_VOLUME = 1.0F;
    private static final float ORB_PITCH_BASE = 1.0F;
    private static final float ORB_PITCH_SPREAD = 0.1F;

    private int title;

    public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        registerBehaviourGoals();
        registerTargetingGoals();
    }

    private void registerBehaviourGoals() {
        goalSelector.addGoal(FLOAT_PRIORITY, new FloatGoal(this));
        LongRangeAttackGoal ranged = new LongRangeAttackGoal(this, RANGED_SPEED, RANGED_MIN_DISTANCE, RANGED_MIN_INTERVAL, RANGED_MAX_INTERVAL, RANGED_RADIUS);
        goalSelector.addGoal(RANGED_PRIORITY, ranged);
        goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, false));
        goalSelector.addGoal(HOME_PRIORITY, new MoveTowardsRestrictionGoal(this, HOME_SPEED));
        goalSelector.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, STROLL_SPEED));
        goalSelector.addGoal(LOOK_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_RANGE));
        goalSelector.addGoal(LOOK_PRIORITY, new RandomLookAroundGoal(this));
    }

    private void registerTargetingGoals() {
        NearestAttackableTargetGoal<Player> playerHunt = new NearestAttackableTargetGoal<>(this, Player.class, true);
        targetSelector.addGoal(RETALIATE_PRIORITY, new CultistHurtByTargetGoal(this));
        targetSelector.addGoal(PLAYER_TARGET_PRIORITY, playerHunt);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        equip(TTLootTables.EQUIPMENT_CULTIST_LEADER, NO_DROP_OVERRIDES);
        enchantWeapon(level, difficulty);
        ChampionHelper.makeChampion(this, true);
        title = TITLES.roll(getRandom());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    private void enchantWeapon(ServerLevelAccessor level, DifficultyInstance difficulty) {
        ItemStack weapon = getMainHandItem();
        if (!weapon.isEmpty() && getRandom().nextFloat() < WEAPON_ENCHANT_CHANCE * difficulty.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider(weapon, level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, getRandom());
            setItemSlot(EquipmentSlot.MAINHAND, weapon);
        }
    }

    @Override
    public void assignTitle() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(TITLES.name(title, MobTraitNames.of(trait))));
    }

    @Override
    public boolean considersEntityAsAlly(Entity other) {
        return isFellowCultist(other) || super.considersEntityAsAlly(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isFellowCultist(target) && super.canAttack(target);
    }

    private static boolean isFellowCultist(Entity entity) {
        for (Class<? extends Entity> memberType : CULT_MEMBER_TYPES) {
            if (memberType.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % RALLY_INTERVAL == 0) {
            rally(level);
        }
    }

    private void rally(ServerLevel level) {
        List<EntityCultist> needy = level.getEntitiesOfClass(EntityCultist.class, getBoundingBox().inflate(RALLY_RADIUS), EntityCultistLeader::lacksRegeneration);
        needy.forEach(EntityCultistLeader::grantRallyRegeneration);
    }

    private static boolean lacksRegeneration(EntityCultist cultist) {
        return cultist.isAlive() && !cultist.hasEffect(MobEffects.REGENERATION);
    }

    private static void grantRallyRegeneration(EntityCultist cultist) {
        cultist.addEffect(new MobEffectInstance(MobEffects.REGENERATION, RALLY_TICKS, RALLY_AMPLIFIER));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel level) || !hasLineOfSight(target)) {
            return;
        }
        swing(InteractionHand.MAIN_HAND);
        EntityGolemOrb orb = new EntityGolemOrb(level, this, target, true);
        Vec3 aim = arcedAim(orb, target);
        orb.setPos(orb.position().add(aim.normalize().scale(ORB_CLEARANCE)));
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_SPREAD);
        level.addFreshEntity(orb);
        playOrbSound(level);
    }

    private static Vec3 arcedAim(EntityGolemOrb orb, LivingEntity target) {
        Vec3 flat = new Vec3(target.getX() - orb.getX(), 0.0, target.getZ() - orb.getZ());
        double rise = target.getY(CHEST_FRACTION) - orb.getY() + Math.sqrt(flat.lengthSqr()) * ORB_ARC_PER_BLOCK;
        return new Vec3(flat.x, rise, flat.z);
    }

    private void playOrbSound(ServerLevel level) {
        float pitch = ORB_PITCH_BASE + getRandom().nextFloat() * ORB_PITCH_SPREAD;
        level.playSound(null, getX(), getY(), getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_VOLUME, pitch);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(BossTitles.SAVE_KEY, (byte) title);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        title = TITLES.clamp(input.getByteOr(BossTitles.SAVE_KEY, (byte) 0));
    }
}

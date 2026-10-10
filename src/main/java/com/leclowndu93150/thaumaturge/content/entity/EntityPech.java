package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.entity.ai.HoldsStill;
import com.leclowndu93150.thaumaturge.content.entity.ai.ItemCollector;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityPech extends Monster implements RangedAttackMob, HoldsStill, ItemCollector {
    public static final String DROPPED_BY_PECH_TAG = "thaumaturge:released_by_pech";
    private static final String LEGACY_DROPPED_BY_PECH_TAG = "PechDrop";
    public static final int LOOT_SLOTS = 9;
    public static final int TYPE_STALKER = 2;
    public static final int TYPE_MAGE = 1;
    public static final int TYPE_FORAGER = 0;

    private static final EntityDataAccessor<Boolean> DATA_DOMESTICATED = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_RAGE = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);

    private static final String VARIANT_KEY = "variant";
    private static final String RAGE_KEY = "rage_ticks";
    private static final String TAMED_KEY = "is_tamed";
    private static final String LEGACY_VARIANT_KEY = "PechType";
    private static final String LEGACY_RAGE_KEY = "Anger";
    private static final String LEGACY_TAMED_KEY = "Tamed";
    private static final String NAME_KEY_FORAGER = "entity.thaumaturge.pech";
    private static final String NAME_KEY_MAGE = "entity.thaumaturge.pech.mage";
    private static final String NAME_KEY_STALKER = "entity.thaumaturge.pech.stalker";

    private static final double MAX_HEALTH = 30.0;
    private static final double ATTACK_DAMAGE = 6.0;
    private static final double MOVEMENT_SPEED = 0.5;
    private static final double ARMOR = 2.0;
    private static final int EXPERIENCE_REWARD = 8;
    private static final int HEAL_INTERVAL = 40;
    private static final float HEAL_AMOUNT = 1.0F;

    private static final double COMBAT_SPEED = 0.6;
    private static final int RANGED_INTERVAL_MIN = 20;
    private static final int RANGED_INTERVAL_MAX = 50;
    private static final float RANGED_RANGE = 15.0F;

    private static final int TRADE_SOUND_ODDS = 3;
    private static final int AMBIENT_INTERVAL = 120;

    private static final int MAX_PECHS_NEARBY = 4;
    private static final double CROWD_RADIUS = 16.0;
    private static final int DESPAWN_HOARD_LIMIT = 5;
    private static final float HOARD_DROP_CHANCE = 0.33F;
    private static final float HOARD_DROP_LIFT = 1.1F;
    private static final float SOUND_VOLUME = 0.45F;

    private static final float DROP_CHANCE = 0.2F;
    private static final float WAND_DROP_CHANCE = 0.1F;
    private static final float PICKUP_LOOT_CHANCE = 0.75F;

    public NonNullList<ItemStack> loot = PechHoard.empty();
    public boolean atTradeTable;
    public float chatterLevel;

    private @Nullable Goal combatGoal;
    private int chargeCooldown;

    public EntityPech(EntityType<? extends EntityPech> type, Level level) {
        super(type, level);
        this.getNavigation().setCanOpenDoors(true);
        this.xpReward = EXPERIENCE_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ARMOR, ARMOR);
    }

    public static boolean checkPechSpawnRules(EntityType<EntityPech> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return !level.getBiome(pos).is(TTBiomeTags.IS_TAINTED) && level.getEntitiesOfClass(EntityPech.class, new AABB(pos).inflate(CROWD_RADIUS)).size() < MAX_PECHS_NEARBY
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        PechGoals.register(this, this.goalSelector, this.targetSelector);
        refreshCombatGoal();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DOMESTICATED, false).define(DATA_RAGE, 0).define(DATA_VARIANT, (byte) TYPE_FORAGER);
    }

    public boolean isDomesticated() {
        return getEntityData().get(DATA_DOMESTICATED);
    }

    public void setDomesticated(boolean tamed) {
        getEntityData().set(DATA_DOMESTICATED, tamed);
    }

    public int variant() {
        return getEntityData().get(DATA_VARIANT).intValue();
    }

    public void assignVariant(int type) {
        getEntityData().set(DATA_VARIANT, Byte.valueOf((byte) type));
    }

    public int rageTicks() {
        return getEntityData().get(DATA_RAGE).intValue();
    }

    public void setRageTicks(int anger) {
        getEntityData().set(DATA_RAGE, Integer.valueOf(anger));
    }

    private static String nameKeyFor(int type) {
        return switch (type) {
            case TYPE_MAGE -> NAME_KEY_MAGE;
            case TYPE_STALKER -> NAME_KEY_STALKER;
            default -> NAME_KEY_FORAGER;
        };
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(nameKeyFor(variant()));
    }

    private static boolean isRangedWeapon(ItemStack held) {
        return held.is(Items.BOW) || held.is(TTItems.PECH_WAND.get());
    }

    private Goal createCombatGoal() {
        if (!isRangedWeapon(this.getMainHandItem())) {
            return new MeleeAttackGoal(this, COMBAT_SPEED, false);
        }
        return new RangedAttackGoal(this, COMBAT_SPEED, RANGED_INTERVAL_MIN, RANGED_INTERVAL_MAX, RANGED_RANGE);
    }

    public void refreshCombatGoal() {
        if (this.level().isClientSide()) {
            return;
        }
        Goal stale = this.combatGoal;
        if (stale != null) {
            this.goalSelector.removeGoal(stale);
        }
        Goal fresh = createCombatGoal();
        this.goalSelector.addGoal(PechGoals.COMBAT_PRIORITY, fresh);
        this.combatGoal = fresh;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        switch (variant()) {
            case TYPE_STALKER -> PechAttacks.shootArrow(this, target, power);
            case TYPE_MAGE -> PechAttacks.castSpell(this, target);
            default -> {
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (slot == EquipmentSlot.MAINHAND) {
            refreshCombatGoal();
        }
    }

    private static int variantFor(ItemStack held, boolean wand) {
        if (wand) {
            return TYPE_MAGE;
        }
        return held.is(Items.BOW) ? TYPE_STALKER : -1;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        RandomSource random = level.getRandom();
        ItemStack held = new ItemStack(PechLoadout.roll(random));
        boolean wand = held.is(TTItems.PECH_WAND.get());
        int rolledVariant = variantFor(held, wand);
        assignVariant(rolledVariant < 0 ? TYPE_FORAGER : rolledVariant);
        this.setItemSlot(EquipmentSlot.MAINHAND, held);
        if (!wand) {
            this.enchantSpawnedWeapon(level, random, difficulty);
        }
        this.setDropChance(EquipmentSlot.MAINHAND, wand ? WAND_DROP_CHANCE : DROP_CHANCE);
        this.setDropChance(EquipmentSlot.OFFHAND, DROP_CHANCE);
        this.setCanPickUpLoot(random.nextFloat() < PICKUP_LOOT_CHANCE * difficulty.getSpecialMultiplier());
        return data;
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    public void playAmbientSound() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (this.random.nextInt(TRADE_SOUND_ODDS) == 0 && PechCensus.hasPeer(this, server)) {
            this.playSound(TTSounds.PECH_TRADE.get(), this.getSoundVolume(), this.getVoicePitch());
            PechMoods.gossip(this);
            return;
        }
        super.playAmbientSound();
        PechMoods.mutter(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.PECH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.PECH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.PECH_DEATH.get();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isInvulnerableTo(level, source) && source.getEntity() instanceof Player player) {
            PechTemper.raiseAlarm(this, level, player);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        PechMoods.decay(this);
        if (this.level().isClientSide()) {
            PechMoods.tickClient(this);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        tickRegeneration();
        int rage = PechTemper.decayRage(this);
        PechTemper.tickCharge(this, rage);
    }

    private void tickRegeneration() {
        if (this.tickCount % HEAL_INTERVAL == 0) {
            this.heal(HEAL_AMOUNT);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (!PechMoods.handleEvent(this, id)) {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return PechHoard.filledSlots(this.loot) < DESPAWN_HOARD_LIMIT;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty()) {
                maybeDropHoarded(level, stack);
            }
        }
    }

    private void maybeDropHoarded(ServerLevel level, ItemStack stack) {
        if (this.random.nextFloat() >= HOARD_DROP_CHANCE) {
            return;
        }
        ItemEntity dropped = this.spawnAtLocation(level, stack.copy(), HOARD_DROP_LIFT);
        if (dropped != null) {
            dropped.addTag(DROPPED_BY_PECH_TAG);
        }
    }

    @Override
    public boolean holdingStill() {
        return isDomesticated() && this.atTradeTable;
    }

    @Override
    public void releaseHold() {
        this.atTradeTable = false;
    }

    @Override
    public boolean wantsToCollect(ItemEntity item) {
        if (item.entityTags().contains(DROPPED_BY_PECH_TAG) || item.entityTags().contains(LEGACY_DROPPED_BY_PECH_TAG)) {
            return false;
        }
        return wouldTake(item.getItem());
    }

    public boolean wouldTake(ItemStack stack) {
        return PechAppraisal.wouldTake(this, stack);
    }

    public boolean isPrizedItem(ItemStack stack) {
        return PechAppraisal.isPrized(this.level().registryAccess(), stack);
    }

    public int getValue(ItemStack stack) {
        return PechAppraisal.valueOf(this.level().registryAccess(), stack);
    }

    @Override
    public ItemStack collect(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (!isDomesticated() && isPrizedItem(stack)) {
            int value = getValue(stack);
            ItemStack rest = stack.copyWithCount(stack.getCount() - 1);
            if (PechAppraisal.winsTrust(this.random, value)) {
                setDomesticated(true);
                PechMoods.delight(this);
                refreshCombatGoal();
            }
            return rest;
        }
        return PechHoard.store(this.loot, stack);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() || player.getItemInHand(hand).is(Items.NAME_TAG) || !isDomesticated()) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, menuPlayer) -> new MenuPech(containerId, inventory, this), this.getDisplayName()),
                    buffer -> buffer.writeVarInt(this.getId()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        PechHoard.save(output, this.loot);
        output.putBoolean(TAMED_KEY, isDomesticated());
        output.putShort(RAGE_KEY, (short) rageTicks());
        output.putByte(VARIANT_KEY, (byte) variant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.loot = PechHoard.load(input);
        setDomesticated(input.getBooleanOr(TAMED_KEY, input.getBooleanOr(LEGACY_TAMED_KEY, false)));
        int storedRage = input.getIntOr(RAGE_KEY, input.getIntOr(LEGACY_RAGE_KEY, 0));
        setRageTicks(Mth.clamp(storedRage, 0, Short.MAX_VALUE));
        assignVariant(input.getIntOr(VARIANT_KEY, input.getIntOr(LEGACY_VARIANT_KEY, TYPE_FORAGER)));
        refreshCombatGoal();
    }

    private static final class PechCensus {
        private static final double ALERT_HORIZONTAL = 32.0;
        private static final double ALERT_VERTICAL = 16.0;
        private static final double PEER_HORIZONTAL = 4.0;
        private static final double PEER_VERTICAL = 2.0;

        private PechCensus() {}

        static boolean hasPeer(EntityPech pech, ServerLevel level) {
            return !peersAround(pech, level, PEER_HORIZONTAL, PEER_VERTICAL, false).isEmpty();
        }

        static List<EntityPech> alertable(EntityPech pech, ServerLevel level) {
            return peersAround(pech, level, ALERT_HORIZONTAL, ALERT_VERTICAL, true);
        }

        private static List<EntityPech> peersAround(EntityPech pech, ServerLevel level, double horizontal, double vertical, boolean centreInside) {
            AABB area = pech.getBoundingBox().inflate(horizontal, vertical, horizontal);
            Predicate<EntityPech> filter = centreInside ? peer -> peer != pech && area.contains(peer.position()) : peer -> peer != pech;
            return level.getEntitiesOfClass(EntityPech.class, area, filter);
        }
    }

    private static final class PechTemper {
        private static final int ANGER_MIN = 400;
        private static final int ANGER_SPREAD = 400;
        private static final int CHARGE_INTERVAL = 100;

        private PechTemper() {}

        static void raiseAlarm(EntityPech pech, ServerLevel level, Player culprit) {
            for (EntityPech other : PechCensus.alertable(pech, level)) {
                provoke(other, culprit);
            }
            provoke(pech, culprit);
        }

        static void provoke(EntityPech pech, Player culprit) {
            if (culprit.isCreative() || culprit.isSpectator()) {
                return;
            }
            if (pech.rageTicks() <= 0) {
                PechMoods.flareUp(pech);
                playCharge(pech);
            }
            pech.setTarget(culprit);
            pech.setRageTicks(ANGER_MIN + pech.getRandom().nextInt(ANGER_SPREAD + 1));
            pech.setDomesticated(false);
            pech.refreshCombatGoal();
        }

        static void tickCharge(EntityPech pech, int rage) {
            if (rage <= 0 || pech.getTarget() == null) {
                return;
            }
            if (PechMoods.isQuiet(pech)) {
                PechMoods.gossip(pech);
            }
            if (--pech.chargeCooldown <= 0) {
                playCharge(pech);
            }
        }

        private static void playCharge(EntityPech pech) {
            pech.playSound(TTSounds.PECH_CHARGE.get(), pech.getSoundVolume(), pech.getVoicePitch());
            pech.chargeCooldown = CHARGE_INTERVAL;
        }

        static int decayRage(EntityPech pech) {
            int rage = pech.rageTicks();
            if (rage > 0) {
                rage--;
                pech.setRageTicks(rage);
            }
            return rage;
        }

    }

    private static final class PechAppraisal {
        private static final int PEARL_VALUE = 15;
        private static final int MAX_VALUE = 32;
        private static final int VALUE_DIVISOR = 2;
        private static final int TAME_ROLL = 10;

        private PechAppraisal() {}

        static boolean isPrized(HolderLookup.Provider registries, ItemStack stack) {
            return stack.is(Items.ENDER_PEARL) || desideriumOf(registries, stack) > 1;
        }

        static int valueOf(HolderLookup.Provider registries, ItemStack stack) {
            return stack.is(Items.ENDER_PEARL) ? PEARL_VALUE : Math.min(MAX_VALUE, desideriumOf(registries, stack) / VALUE_DIVISOR);
        }

        static boolean wouldTake(EntityPech pech, ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return (!pech.isDomesticated() && pech.isPrizedItem(stack)) || PechHoard.canStore(pech.loot, stack);
        }

        static boolean winsTrust(RandomSource random, int value) {
            return random.nextInt(TAME_ROLL) < value;
        }

        private static int desideriumOf(HolderLookup.Provider registries, ItemStack stack) {
            return AspectIndexAccess.of(stack).amountOf(TTAspects.DESIDERIUM, registries);
        }
    }
}

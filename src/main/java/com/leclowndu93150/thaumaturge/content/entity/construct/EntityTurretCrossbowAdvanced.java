package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;

public class EntityTurretCrossbowAdvanced extends EntityTurretCrossbow {
    private static final EntityDataAccessor<Byte> TARGET_FLAGS = SynchedEntityData.defineId(EntityTurretCrossbowAdvanced.class, EntityDataSerializers.BYTE);
    private static final String TARGET_FLAGS_KEY = "target_flags";
    private static final String LEGACY_TARGET_FLAGS_KEY = "targets";
    private static final double MAX_HEALTH = 40.0;
    private static final double ARMOR = 8.0;
    private static final int ATTACK_INTERVAL_MIN = 20;
    private static final int ATTACK_INTERVAL_MAX = 40;
    private static final double MOVE_DAMPING = 15.0;

    public EntityTurretCrossbowAdvanced(EntityType<? extends EntityTurretCrossbowAdvanced> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAdvancedAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.ARMOR, ARMOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_FLAGS, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        setFilter(TurretFilter.MOBS, true);
        goalSelector.addGoal(RANGED_PRIORITY, new RangedAttackGoal(this, HOLD_POSITION, ATTACK_INTERVAL_MIN, ATTACK_INTERVAL_MAX, ATTACK_RADIUS));
        goalSelector.addGoal(LOOK_PRIORITY, new WatchTargetGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        targetSelector.addGoal(SCAN_PRIORITY, scanGoal(this, this::acceptsTarget));
    }

    private boolean acceptsTarget(LivingEntity candidate, ServerLevel level) {
        return canEngage(candidate);
    }

    public boolean canEngage(LivingEntity candidate) {
        if (candidate == this || !candidate.isAlive()) {
            return false;
        }
        if (!kindAllowed(candidate)) {
            return false;
        }
        boolean friendly = isFilterOn(TurretFilter.FRIENDLY);
        if (!teamAllows(candidate, friendly)) {
            return false;
        }
        return isOwned() ? ownedRuleAllows(candidate, friendly) : unownedRuleAllows(candidate, friendly);
    }

    private boolean kindAllowed(LivingEntity candidate) {
        TurretFilter required = filterForKind(candidate);
        if (required == null) {
            return false;
        }
        if (required == TurretFilter.PLAYERS && pvpBlocksPlayers()) {
            setFilter(TurretFilter.PLAYERS, false);
            return false;
        }
        return isFilterOn(required);
    }

    private static TurretFilter filterForKind(LivingEntity candidate) {
        if (candidate instanceof Player) {
            return TurretFilter.PLAYERS;
        }
        if (candidate instanceof Enemy) {
            return TurretFilter.MOBS;
        }
        if (candidate instanceof Animal || candidate instanceof WaterAnimal) {
            return TurretFilter.ANIMALS;
        }
        return null;
    }

    private boolean pvpBlocksPlayers() {
        return level() instanceof ServerLevel server && !server.isPvpAllowed() && !isFilterOn(TurretFilter.FRIENDLY);
    }

    private boolean teamAllows(LivingEntity candidate, boolean friendly) {
        PlayerTeam team = getTeam();
        return team == null || team.equals(candidate.getTeam()) == friendly;
    }

    private boolean unownedRuleAllows(LivingEntity candidate, boolean friendly) {
        if (friendly) {
            return true;
        }
        return !(candidate instanceof Player player) || !player.getAbilities().invulnerable;
    }

    private boolean ownedRuleAllows(LivingEntity candidate, boolean friendly) {
        EntityReference<LivingEntity> owner = getOwnerReference();
        if (friendly) {
            if (candidate instanceof OwnableEntity ownable) {
                EntityReference<LivingEntity> candidateOwner = ownable.getOwnerReference();
                return candidateOwner != null && candidateOwner.equals(owner);
            }
            return candidate instanceof Player;
        }
        if (isOwner(candidate)) {
            return false;
        }
        return !(candidate instanceof OwnableEntity ownable) || !owner.equals(ownable.getOwnerReference());
    }

    public boolean isFilterOn(TurretFilter filter) {
        return (entityData.get(TARGET_FLAGS) & filter.mask()) != 0;
    }

    public void flipFilter(TurretFilter filter) {
        setFilter(filter, !isFilterOn(filter));
        setTarget(null);
    }

    private void setFilter(TurretFilter filter, boolean enabled) {
        int flags = entityData.get(TARGET_FLAGS);
        int updated = enabled ? flags | filter.mask() : flags & ~filter.mask();
        entityData.set(TARGET_FLAGS, (byte) updated);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && !server.isPvpAllowed() && getTarget() instanceof Player player && !isOwner(player)) {
            setTarget(null);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(TARGET_FLAGS, input.getByteOr(TARGET_FLAGS_KEY, input.getByteOr(LEGACY_TARGET_FLAGS_KEY, (byte) 0)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(TARGET_FLAGS_KEY, entityData.get(TARGET_FLAGS));
    }

    @Override
    protected ItemStack placerItem() {
        return new ItemStack(TTItems.TURRET_ADVANCED.get());
    }

    @Override
    protected void openTurretMenu(Player player) {
        MenuTurretAdvanced.open(player, this);
    }

    @Override
    protected double moveDamping() {
        return MOVE_DAMPING;
    }
}

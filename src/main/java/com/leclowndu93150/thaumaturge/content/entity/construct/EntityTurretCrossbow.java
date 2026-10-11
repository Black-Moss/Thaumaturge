package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityTurretCrossbow extends EntityOwnedConstruct implements RangedAttackMob {
    public static final double FOLLOW_RANGE = 24.0;

    static final float ATTACK_RADIUS = 24.0F;
    static final double HOLD_POSITION = 0.0;
    static final int RANGED_PRIORITY = 1;
    static final int LOOK_PRIORITY = 2;
    static final int RETALIATE_PRIORITY = 1;
    static final int SCAN_PRIORITY = 2;

    private static final String DISABLED_KEY = "disabled";
    private static final double MAX_HEALTH = 30.0;
    private static final double ARMOR = 2.0;
    private static final int ATTACK_INTERVAL_MIN = 20;
    private static final int ATTACK_INTERVAL_MAX = 60;
    private static final int SCAN_CHANCE = 5;
    private static final int REGEN_INTERVAL = 80;
    private static final float REGEN_AMOUNT = 1.0F;
    private static final double MOVE_DAMPING = 20.0;
    private static final double KNOCKBACK_SHARE = 0.2;
    private static final float NO_STEP = 0.0F;
    private static final int MAX_HEAD_PITCH = 90;
    private static final int HEAD_TURN_SPEED = 24;
    private static final double BASE_DAMAGE = 2.25;
    private static final double RANGE_DAMAGE = 2.0;
    private static final double DAMAGE_SPREAD = 0.25;
    private static final float LAUNCH_SPEED = 2.0F;
    private static final float LAUNCH_INACCURACY = 2.0F;
    private static final double AIM_LIFT = 3.5;
    private static final double GROUND_MUZZLE_OFFSET = -0.2;
    private static final double MOUNTED_MUZZLE_OFFSET = 1.0;
    private static final float SHOT_VOLUME = 1.0F;
    private static final float SHOT_PITCH_BASE = 0.8F;
    private static final float SHOT_PITCH_SPREAD = 0.4F;
    private static final float RELOAD_VOLUME = 0.5F;
    private static final float RELOAD_PITCH = 1.0F;
    private static final byte EVENT_FIRE = 80;
    private static final byte EVENT_RELOAD = 81;
    private static final int FIRE_ANIM_TICKS = 7;
    private static final int RELOAD_ANIM_TICKS = 10;

    public float swingAnim;

    private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
    private boolean disabled;
    private int fireAnimTicks;
    private int reloadAnimTicks;
    private float loadProgress;
    private float prevLoadProgress;

    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level) {
        super(type, level);
        this.lookControl = new ConstructLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.ARMOR, ARMOR);
    }

    static NearestAttackableTargetGoal<LivingEntity> scanGoal(EntityTurretCrossbow turret, TargetingConditions.Selector selector) {
        return new NearestAttackableTargetGoal<>(turret, LivingEntity.class, SCAN_CHANCE, true, false, selector);
    }

    static Goal retaliateGoal(EntityTurretCrossbow turret) {
        return new RetaliateGoal(turret);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(RANGED_PRIORITY, new RangedAttackGoal(this, HOLD_POSITION, ATTACK_INTERVAL_MIN, ATTACK_INTERVAL_MAX, ATTACK_RADIUS));
        goalSelector.addGoal(LOOK_PRIORITY, new WatchTargetGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, retaliateGoal(this));
        targetSelector.addGoal(SCAN_PRIORITY, scanGoal(this, EntityTurretCrossbow::isMonster));
    }

    private static boolean isMonster(LivingEntity candidate, ServerLevel level) {
        return candidate instanceof Enemy;
    }

    protected boolean mayRetaliateAgainst(LivingEntity attacker) {
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new StationaryNavigation(this, level);
    }

    public boolean isValidAmmo(ItemStack stack) {
        return stack.getItem() instanceof ArrowItem;
    }

    public float getLoadProgress(float partialTick) {
        return Mth.lerp(partialTick, prevLoadProgress, loadProgress);
    }

    protected ItemStack placerItem() {
        return new ItemStack(TTItems.TURRET_BASIC.get());
    }

    protected void openTurretMenu(Player player) {
        MenuTurretBasic.open(player, this);
    }

    protected double moveDamping() {
        return MOVE_DAMPING;
    }

    protected double knockbackShare() {
        return KNOCKBACK_SHARE;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            advanceAnimations();
            return;
        }
        if (isRemoved()) {
            return;
        }
        swivelWithHead();
        followActivatorRail();
        applySwitch();
        regenerate(REGEN_INTERVAL, REGEN_AMOUNT);
        if (getMainHandItem().isEmpty()) {
            reloadFromDispenser((ServerLevel) level());
        }
    }

    private void followActivatorRail() {
        BlockState rail = ActivatorRails.find(level(), blockPosition(), scratch);
        if (rail == null) {
            return;
        }
        boolean off = ActivatorRails.powered(rail);
        if (off && !disabled) {
            setTarget(null);
        }
        disabled = off;
    }

    private void applySwitch() {
        boolean enabled = !disabled;
        goalSelector.setControlFlag(Goal.Flag.MOVE, enabled);
        goalSelector.setControlFlag(Goal.Flag.LOOK, enabled);
        targetSelector.setControlFlag(Goal.Flag.TARGET, enabled);
    }

    private void reloadFromDispenser(ServerLevel level) {
        Entity vehicle = getVehicle();
        BlockPos standing = vehicle != null ? vehicle.blockPosition() : blockPosition();
        BlockPos below = scratch.setWithOffset(standing, Direction.DOWN);
        BlockState state = level.getBlockState(below);
        if (!(state.getBlock() instanceof DispenserBlock) || state.getValue(DispenserBlock.FACING) != Direction.UP) {
            return;
        }
        if (!(level.getBlockEntity(below) instanceof DispenserBlockEntity dispenser)) {
            return;
        }
        for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
            if (isValidAmmo(dispenser.getItem(slot))) {
                setItemSlot(EquipmentSlot.MAINHAND, dispenser.removeItemNoUpdate(slot));
                dispenser.setChanged();
                playSound(TTSounds.TICKS.get(), RELOAD_VOLUME, RELOAD_PITCH);
                level.broadcastEntityEvent(this, EVENT_RELOAD);
                return;
            }
        }
    }

    private void advanceAnimations() {
        if (fireAnimTicks > 0) {
            fireAnimTicks--;
            swingAnim = fireAnimTicks == 0 ? 0.0F : 1.0F - (float) fireAnimTicks / FIRE_ANIM_TICKS;
        }
        prevLoadProgress = loadProgress;
        if (reloadAnimTicks > 0) {
            reloadAnimTicks--;
            loadProgress = 1.0F - (float) reloadAnimTicks / RELOAD_ANIM_TICKS;
            if (reloadAnimTicks == 0) {
                loadProgress = 0.0F;
                prevLoadProgress = 0.0F;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_FIRE) {
            fireAnimTicks = FIRE_ANIM_TICKS;
            return;
        }
        if (id == EVENT_RELOAD) {
            reloadAnimTicks = RELOAD_ANIM_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack ammo = getMainHandItem();
        if (!(level() instanceof ServerLevel server) || !(ammo.getItem() instanceof ArrowItem arrowItem)) {
            return;
        }
        AbstractArrow arrow = arrowItem.createArrow(server, ammo, this, null);
        Vec3 eye = getEyePosition();
        Vec3 aimPoint = new Vec3(target.getX(), target.getEyeY() + AIM_LIFT * power * power, target.getZ());
        Vec3 start = eye.add(aimPoint.subtract(eye).normalize().scale(isPassenger() ? MOUNTED_MUZZLE_OFFSET : GROUND_MUZZLE_OFFSET));
        Vec3 path = aimPoint.subtract(start);
        arrow.setPos(start);
        arrow.setBaseDamage(BASE_DAMAGE + RANGE_DAMAGE * power + random.nextGaussian() * DAMAGE_SPREAD);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        Projectile.spawnProjectileUsingShoot(arrow, server, ammo, path.x, path.y, path.z, LAUNCH_SPEED, LAUNCH_INACCURACY);
        ammo.shrink(1);
        playSound(SoundEvents.ARROW_SHOOT, SHOT_VOLUME, 1.0F / (random.nextFloat() * SHOT_PITCH_SPREAD + SHOT_PITCH_BASE));
        server.broadcastEntityEvent(this, EVENT_FIRE);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        jolt();
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!dead && !isRemoved()) {
            dropHeld();
        }
        super.die(source);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved() || !isOwner(player) || player.getItemInHand(hand).is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            dropHeld();
            dismantle(player, hand, placerItem());
        } else {
            openTurretMenu(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void knockback(double power, double xd, double zd) {
        super.knockback(power * knockbackShare(), xd, zd);
        capRise();
    }

    @Override
    public void move(MoverType type, Vec3 delta) {
        super.move(type, type == MoverType.SELF ? dampHorizontal(delta, moveDamping()) : delta);
    }

    @Override
    public float maxUpStep() {
        return NO_STEP;
    }

    @Override
    public boolean isPushable() {
        return isAlive();
    }

    @Override
    public int getMaxHeadXRot() {
        return MAX_HEAD_PITCH;
    }

    @Override
    public int getHeadRotSpeed() {
        return HEAD_TURN_SPEED;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(DISABLED_KEY, disabled);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        disabled = input.getBooleanOr(DISABLED_KEY, false);
    }

    private static final class RetaliateGoal extends HurtByTargetGoal {
        private final EntityTurretCrossbow turret;

        private RetaliateGoal(EntityTurretCrossbow turret) {
            super(turret);
            this.turret = turret;
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = turret.getLastHurtByMob();
            return super.canUse() && attacker != null && turret.mayRetaliateAgainst(attacker);
        }
    }
}

package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityTurretCrossbow extends EntityOwnedConstruct implements RangedAttackMob {
    static final double HOLD_POSITION = 0.0;
    static final float ATTACK_RADIUS = 24.0F;
    static final int RANGED_PRIORITY = 1;
    static final int LOOK_PRIORITY = 2;
    static final int RETALIATE_PRIORITY = 1;
    static final int SCAN_PRIORITY = 2;
    static final double FOLLOW_RANGE = 24.0;
    private static final Set<Item> AMMO_ITEMS = Set.of(Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW);
    static final TargetingConditions.Selector HOSTILE_ONLY = (target, level) -> target instanceof Enemy;

    private static final double MAX_HEALTH = 30.0;
    private static final double ARMOR = 2.0;
    private static final int ATTACK_INTERVAL_MIN = 20;
    private static final int ATTACK_INTERVAL_MAX = 60;
    private static final int SCAN_ODDS = 5;
    private static final double VERTICAL_AGGRO = 4.0;
    private static final int HEAL_INTERVAL = 80;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final double MOVE_DAMPING = 20.0;
    private static final int MAX_HEAD_PITCH = 90;
    private static final int HEAD_TURN_SPEED = 20;
    private static final byte EVENT_SHOT = 16;
    private static final byte EVENT_LOAD = 17;
    private static final int SWING_TICKS = 6;
    private static final int LOAD_TICKS = 10;
    private static final double BASE_DAMAGE = 2.25;
    private static final double DISTANCE_DAMAGE = 2.0;
    private static final double DAMAGE_SPREAD = 0.25;
    private static final double BACK_OFFSET = 0.9;
    private static final double MOUNTED_OFFSET = 1.75;
    private static final double ARC_COMPENSATION = 3.0;
    private static final float ARROW_SPEED = 2.0F;
    private static final float ARROW_INACCURACY = 2.0F;
    private static final float SHOT_VOLUME = 1.0F;
    private static final float SHOT_PITCH_BASE = 0.8F;
    private static final float SHOT_PITCH_SPREAD = 0.4F;
    private static final float LOAD_VOLUME = 1.0F;
    private static final float LOAD_PITCH = 1.0F;

    public float swingAnim;

    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
    private boolean swinging;
    private int swingTick;
    private boolean loading;
    private int loadTick;
    private float loadPrevious;
    private float loadCurrent;

    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.ARMOR, ARMOR);
    }

    @Override
    public void die(DamageSource source) {
        dropAmmo();
        super.die(source);
    }

    @Override
    public int getMaxHeadXRot() {
        return MAX_HEAD_PITCH;
    }

    @Override
    public int getHeadRotSpeed() {
        return HEAD_TURN_SPEED;
    }

    static NearestAttackableTargetGoal<LivingEntity> scanGoal(EntityTurretCrossbow turret, TargetingConditions.Selector selector) {
        return new TurretScanGoal(turret, selector);
    }

    @Override
    protected void registerGoals() {
        Goal retaliate = new HurtByTargetGoal(this);
        Goal watch = new WatchTargetGoal(this);
        goalSelector.addGoal(RANGED_PRIORITY, newShootingGoal());
        goalSelector.addGoal(LOOK_PRIORITY, watch);
        targetSelector.addGoal(SCAN_PRIORITY, scanGoal(this, HOSTILE_ONLY));
        targetSelector.addGoal(RETALIATE_PRIORITY, retaliate);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack ammo = getMainHandItem();
        if (ammo.isEmpty() || !(level() instanceof ServerLevel server)) {
            return;
        }
        AbstractArrow bolt = ProjectileUtil.getMobArrow(this, ammo, distanceFactor, null);
        bolt.setBaseDamage(BASE_DAMAGE + DISTANCE_DAMAGE * distanceFactor + random.nextGaussian() * DAMAGE_SPREAD);
        bolt.pickup = AbstractArrow.Pickup.DISALLOWED;
        shiftAlongView(bolt);
        launchAt(bolt, target, distanceFactor);
        server.addFreshEntity(bolt);
        server.broadcastEntityEvent(this, EVENT_SHOT);
        float pitch = 1.0F / (SHOT_PITCH_BASE + random.nextFloat() * SHOT_PITCH_SPREAD);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.ARROW_SHOOT, getSoundSource(), SHOT_VOLUME, pitch);
        spendOne(ammo);
    }

    private void shiftAlongView(AbstractArrow bolt) {
        Vec3 view = getViewVector(1.0F).scale(isPassenger() ? MOUNTED_OFFSET : -BACK_OFFSET);
        bolt.setPos(bolt.position().add(view));
    }

    private void launchAt(AbstractArrow bolt, LivingEntity target, float distanceFactor) {
        double lift = ARC_COMPENSATION * distanceFactor * distanceFactor;
        double rise = target.getY() + target.getEyeHeight() + lift - bolt.getY();
        bolt.shoot(target.getX() - getX(), rise, target.getZ() - getZ(), ARROW_SPEED, ARROW_INACCURACY);
    }

    private void spendOne(ItemStack ammo) {
        int remaining = ammo.getCount() - 1;
        setItemSlot(EquipmentSlot.MAINHAND, remaining > 0 ? ammo.copyWithCount(remaining) : ItemStack.EMPTY);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SHOT -> beginSwing();
            case EVENT_LOAD -> beginLoad();
            default -> super.handleEntityEvent(id);
        }
    }

    private void beginSwing() {
        if (swinging) {
            return;
        }
        swinging = true;
        swingTick = 0;
    }

    private void beginLoad() {
        if (loading) {
            return;
        }
        loading = true;
        loadTick = 0;
    }

    public boolean isValidAmmo(ItemStack stack) {
        return AMMO_ITEMS.contains(stack.getItem());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            tickAnimations();
        } else if (!isRemoved()) {
            tickServer(server);
        }
    }

    private void tickServer(ServerLevel server) {
        if (getMainHandItem().isEmpty()) {
            refillFromDispenser(server);
        }
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || isAlliedTo(target))) {
            setTarget(null);
        }
        setYBodyRot(getYHeadRot());
        if (tickCount % HEAL_INTERVAL == 0) {
            heal(HEAL_AMOUNT);
        }
        BlockState rail = railStateBelow();
        if (rail != null) {
            setNoAi(ActivatorRails.powered(rail));
        }
    }

    private void tickAnimations() {
        if (swinging && ++swingTick >= SWING_TICKS) {
            swinging = false;
            swingTick = 0;
        }
        swingAnim = swinging ? (float) swingTick / SWING_TICKS : 0.0F;
        loadPrevious = loadCurrent;
        if (loading && ++loadTick >= LOAD_TICKS) {
            loading = false;
            loadTick = 0;
        }
        loadCurrent = loading ? (float) loadTick / LOAD_TICKS : 0.0F;
    }

    public float getLoadProgress(float partialTick) {
        if (loadCurrent >= loadPrevious) {
            return Mth.lerp(partialTick, loadPrevious, loadCurrent);
        }
        return Mth.lerp(partialTick, loadPrevious, 1.0F);
    }

    private void refillFromDispenser(ServerLevel server) {
        probe.setWithOffset(blockPosition(), Direction.DOWN);
        BlockState state = server.getBlockState(probe);
        if (!state.hasProperty(DispenserBlock.FACING) || state.getValue(DispenserBlock.FACING) != Direction.UP) {
            return;
        }
        if (!(server.getBlockEntity(probe) instanceof DispenserBlockEntity dispenser)) {
            return;
        }
        int slot = firstAmmoSlot(dispenser);
        if (slot < 0) {
            return;
        }
        setItemSlot(EquipmentSlot.MAINHAND, dispenser.removeItemNoUpdate(slot));
        dispenser.setChanged();
        server.playSound(null, getX(), getY(), getZ(), TTSounds.TICKS.get(), getSoundSource(), LOAD_VOLUME, LOAD_PITCH);
        server.broadcastEntityEvent(this, EVENT_LOAD);
    }

    private int firstAmmoSlot(DispenserBlockEntity dispenser) {
        int size = dispenser.getContainerSize();
        int slot = 0;
        while (slot < size && !isValidAmmo(dispenser.getItem(slot))) {
            slot++;
        }
        return slot < size ? slot : -1;
    }

    protected @Nullable BlockState railStateBelow() {
        return ActivatorRails.find(level(), blockPosition(), probe);
    }

    private RangedAttackGoal newShootingGoal() {
        return new RangedAttackGoal(this, HOLD_POSITION, ATTACK_INTERVAL_MIN, ATTACK_INTERVAL_MAX, ATTACK_RADIUS);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        jolt();
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!canBeHandledBy(player)) {
            return super.mobInteract(player, hand);
        }
        if (player.isShiftKeyDown()) {
            dropAmmo();
            dismantle(player, hand, placerItem());
        } else {
            openTurretMenu(player);
        }
        return InteractionResult.SUCCESS;
    }

    private boolean canBeHandledBy(Player player) {
        return isAlive() && !level().isClientSide() && isOwner(player);
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

    @Override
    public void knockback(double power, double xd, double zd) {
        super.knockback(power, xd, zd);
        capRise();
    }

    @Override
    public void move(MoverType type, Vec3 delta) {
        Vec3 damped = dampHorizontal(delta, moveDamping());
        super.move(type, damped);
    }

    protected void dropAmmo() {
        dropHeld();
    }

    private static final class TurretScanGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private TurretScanGoal(EntityTurretCrossbow turret, TargetingConditions.Selector selector) {
            super(turret, LivingEntity.class, SCAN_ODDS, true, false, selector);
        }

        @Override
        protected AABB getTargetSearchArea(double followDistance) {
            return mob.getBoundingBox().inflate(followDistance, VERTICAL_AGGRO, followDistance);
        }
    }
}

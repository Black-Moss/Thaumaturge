package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreCore;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreHost;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreTool;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;

public class EntityArcaneBore extends EntityOwnedConstruct implements ArcaneBoreHost {
    private static final EntityDataAccessor<Direction> FACING = SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.BOOLEAN);
    private static final String CHARGE_KEY = "vis_charge";
    private static final String LEGACY_CHARGE_KEY = "charge";
    private static final String FACING_KEY = "facing";
    private static final String ACTIVE_KEY = "running";
    private static final String LEGACY_ACTIVE_KEY = "active";
    private static final double MAX_HEALTH = 50.0;
    private static final double FOLLOW_RANGE = 32.0;
    private static final int HEAL_INTERVAL = 50;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final double MOVE_DAMPING = 5.0;
    private static final int MAX_HEAD_PITCH = 90;
    private static final int HEAD_TURN_SPEED = 10;
    private static final byte EVENT_DIG_START = 16;
    private static final byte EVENT_DIG_STOP = 17;
    private static final int SMOOTHING_TICKS = 4;
    private static final double OUTPUT_BACK_OFFSET = 0.75;
    private static final double OUTPUT_HEIGHT = 0.5;

    public boolean remoteDigFlag;

    private final ArcaneBoreCore engine = new ArcaneBoreCore();
    private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
    private boolean announcedDigging;
    private long lastDigEndTick = -(SMOOTHING_TICKS + 1);

    public EntityArcaneBore(EntityType<? extends EntityArcaneBore> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.MAX_HEALTH, MAX_HEALTH);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
        builder.define(FACING, Direction.NORTH);
    }

    public Direction heading() {
        return entityData.get(FACING);
    }

    public void turnTo(Direction facing) {
        entityData.set(FACING, facing);
    }

    public boolean holdsValidTool() {
        return ArcaneBoreTool.valid(getMainHandItem());
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && !isRemoved()) {
            serverUpdate(server);
        }
    }

    private void serverUpdate(ServerLevel server) {
        setYBodyRot(getYHeadRot());
        regenerate();
        followRailSignal(server);
        engine.serverTick(this, server, tickCount);
    }

    private void regenerate() {
        if (tickCount % HEAL_INTERVAL != 0) {
            return;
        }
        heal(HEAL_AMOUNT);
    }

    private void followRailSignal(ServerLevel server) {
        BlockState rail = ActivatorRails.find(server, blockPosition(), scratchPos);
        if (rail == null) {
            followBlockBelow(server);
            return;
        }
        setActive(!ActivatorRails.powered(rail));
    }

    private void followBlockBelow(ServerLevel server) {
        if (isPassenger()) {
            return;
        }
        scratchPos.setWithOffset(blockPosition(), Direction.DOWN);
        setActive(server.hasNeighborSignal(scratchPos));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public void setActive(boolean running) {
        if (running != isActive()) {
            entityData.set(ACTIVE, running);
        }
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    private void faceToward(LivingEntity attacker) {
        Vec3 offset = attacker.position().subtract(position());
        Direction nearest = Direction.getApproximateNearest(offset.x, offset.y, offset.z);
        if (nearest != Direction.DOWN) {
            turnTo(nearest);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker && isOwner(attacker)) {
            faceToward(attacker);
            return false;
        }
        jolt();
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void die(DamageSource source) {
        dropHeld();
        super.die(source);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide() || !isAlive() || !isOwner(player)) {
            return super.mobInteract(player, hand);
        }
        return useAsOwner(player, hand);
    }

    private InteractionResult useAsOwner(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            dropHeld();
            dismantle(player, hand, new ItemStack(TTItems.ARCANE_BORE.get()));
        } else {
            MenuArcaneBore.open(player, this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void move(MoverType type, Vec3 delta) {
        super.move(type, dampHorizontal(delta, MOVE_DAMPING));
    }

    @Override
    public void knockback(double strength, double dx, double dz) {
        super.knockback(strength, dx, dz);
        capRise();
    }

    @Override
    public int getMaxHeadXRot() {
        return MAX_HEAD_PITCH;
    }

    @Override
    public int getHeadRotSpeed() {
        return HEAD_TURN_SPEED;
    }

    public boolean clientDiggingSmoothed() {
        long sinceDigEnd = level().getGameTime() - lastDigEndTick;
        return remoteDigFlag || sinceDigEnd <= SMOOTHING_TICKS;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_DIG_START -> remoteDigFlag = true;
            case EVENT_DIG_STOP -> {
                remoteDigFlag = false;
                lastDigEndTick = level().getGameTime();
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat(CHARGE_KEY, engine.charge());
        output.putByte(FACING_KEY, (byte) heading().get3DDataValue());
        output.putBoolean(ACTIVE_KEY, isActive());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setActive(input.getBooleanOr(ACTIVE_KEY, input.getBooleanOr(LEGACY_ACTIVE_KEY, false)));
        turnTo(Direction.from3DDataValue(input.getByteOr(FACING_KEY, (byte) 0)));
        engine.setCharge(input.getFloatOr(CHARGE_KEY, input.getFloatOr(LEGACY_CHARGE_KEY, 0.0F)));
    }

    @Override
    public Level boreLevel() {
        return level();
    }

    @Override
    public BlockPos borePos() {
        return blockPosition();
    }

    @Override
    public Vec3 borePosition() {
        return position();
    }

    @Override
    public float boreEyeHeight() {
        return getEyeHeight();
    }

    @Override
    public Direction boreFacing() {
        return heading();
    }

    @Override
    public boolean boreActive() {
        return isActive();
    }

    @Override
    public RandomSource boreRandom() {
        return random;
    }

    @Override
    public CollisionContext boreCollisionContext() {
        return CollisionContext.of(this);
    }

    @Override
    public ItemStack boreTool() {
        return getMainHandItem();
    }

    @Override
    public void setBoreTool(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    public void hurtBoreTool() {
        boreTool().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
    }

    @Override
    public void aimBore(double x, double y, double z, float yawStep, float pitchStep) {
        getLookControl().setLookAt(x, y, z, yawStep, pitchStep);
    }

    @Override
    public void setBoreDigging(boolean digging) {
        if (digging != announcedDigging) {
            announcedDigging = digging;
            level().broadcastEntityEvent(this, digging ? EVENT_DIG_START : EVENT_DIG_STOP);
        }
    }

    @Override
    public void playBoreSound(SoundEvent sound, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.BLOCKS, volume, pitch);
    }

    @Override
    public FakePlayer boreDigger(ServerLevel level) {
        return TTFakePlayer.BORE.at(level, this);
    }

    @Override
    public void dropBoreOutput(ServerLevel level, ItemStack stack) {
        Direction behind = heading().getOpposite();
        double dropX = getX() + behind.getStepX() * OUTPUT_BACK_OFFSET;
        double dropZ = getZ() + behind.getStepZ() * OUTPUT_BACK_OFFSET;
        level.addFreshEntity(new ItemEntity(level, dropX, getY() + OUTPUT_HEIGHT, dropZ, stack));
    }

    @Override
    public void showBoreDig(ServerLevel level, BlockPos target, int delay) {
        Effects.boreDig(level, target, this, delay);
    }

    @Override
    public float boreHealth() {
        return getHealth();
    }

    @Override
    public float boreMaxHealth() {
        return getMaxHealth();
    }

    @Override
    public boolean boreValid() {
        return isAlive();
    }

    @Override
    public Component boreDisplayName() {
        return getDisplayName();
    }

    @Override
    public void writeBoreRef(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(false);
        buf.writeVarInt(getId());
    }
}

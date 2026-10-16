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
import net.minecraft.world.item.Items;
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
    private static final int MAX_HEAD_PITCH = 90;
    private static final int SMOOTHING_TICKS = 4;
    private static final double OUTPUT_BACK_OFFSET = 0.75;
    private static final double OUTPUT_HEIGHT = 0.5;
    private static final double MOVE_DAMPING = 5.0;
    private static final int HEAD_TURN_SPEED = 10;
    private static final byte EVENT_DIG_START = 82;
    private static final byte EVENT_DIG_STOP = 83;
    private static final double OWNER_NEAR_RANGE = 2.0;
    private static final double OWNER_ABOVE_RANGE = 2.0;
    private static final double BLOCK_CENTER = 0.5;

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
        regenerate(HEAL_INTERVAL, HEAL_AMOUNT);
        if (!followRailSignal(server) && !isPassenger()) {
            followBlockBelow(server);
        }
        engine.serverTick(this, server, tickCount);
        swivelWithHead();
    }

    private boolean followRailSignal(ServerLevel server) {
        BlockState rail = ActivatorRails.find(server, blockPosition(), scratchPos);
        if (rail == null) {
            return false;
        }
        setActive(!ActivatorRails.powered(rail));
        return true;
    }

    private void followBlockBelow(ServerLevel server) {
        setActive(server.hasNeighborSignal(scratchPos.setWithOffset(blockPosition(), Direction.DOWN)));
    }

    private void faceToward(LivingEntity owner) {
        Direction facing = placementFacing(owner);
        if (facing != Direction.DOWN) {
            turnTo(facing);
        }
    }

    private Direction placementFacing(LivingEntity owner) {
        BlockPos pos = blockPosition();
        boolean near = Math.abs(owner.getX() - (pos.getX() + BLOCK_CENTER)) < OWNER_NEAR_RANGE && Math.abs(owner.getZ() - (pos.getZ() + BLOCK_CENTER)) < OWNER_NEAR_RANGE;
        if (near) {
            double rise = owner.getEyeY() - pos.getY();
            if (rise > OWNER_ABOVE_RANGE) {
                return Direction.UP;
            }
            if (rise < 0.0) {
                return Direction.DOWN;
            }
        }
        return owner.getDirection().getOpposite();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && isOwner(attacker)) {
            faceToward(attacker);
            return false;
        }
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
        if (!level().isClientSide()) {
            useAsOwner(player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    private void useAsOwner(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            dropHeld();
            dismantle(player, hand, new ItemStack(TTItems.ARCANE_BORE.get()));
            return;
        }
        MenuArcaneBore.open(player, this);
    }

    @Override
    public void knockback(double power, double xd, double zd) {
        super.knockback(power, xd, zd);
        capRise();
    }

    @Override
    public void move(MoverType type, Vec3 delta) {
        super.move(type, type == MoverType.SELF ? dampHorizontal(delta, MOVE_DAMPING) : delta);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_DIG_START) {
            remoteDigFlag = true;
            return;
        }
        if (id == EVENT_DIG_STOP) {
            remoteDigFlag = false;
            lastDigEndTick = tickCount;
            return;
        }
        super.handleEntityEvent(id);
    }

    public boolean clientDiggingSmoothed() {
        return remoteDigFlag || tickCount - lastDigEndTick <= SMOOTHING_TICKS;
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
        playSound(sound, volume, pitch);
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

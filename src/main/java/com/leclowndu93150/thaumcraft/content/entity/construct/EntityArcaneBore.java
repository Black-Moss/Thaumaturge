package com.leclowndu93150.thaumcraft.content.entity.construct;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumcraft.api.items.InvHelper;
import com.leclowndu93150.thaumcraft.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumcraft.content.equipment.RefiningResults;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.ItemTags;

public class EntityArcaneBore extends EntityOwnedConstruct {
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.BOOLEAN);

    private static final int HEAL_INTERVAL = 50;
    private static final int RECHARGE_INTERVAL = 10;
    private static final float MAX_CHARGE = 10.0F;
    private static final float DIG_COST = 0.25F;
    private static final int DURABILITY_PER_BREAKS = 50;
    private static final double MOVE_DAMPING = 5.0;
    private static final byte EVENT_DIG_START = 16;
    private static final byte EVENT_DIG_STOP = 17;
    private static final long SOUND_DELAY_TICKS = 24;

    private BlockPos digTarget;
    private BlockPos digTargetPrev;
    private long soundDelay;
    private int breakCounter;
    private int digDelay;
    private int digDelayMax;
    private float radInc;
    private int spiral;
    private float currentRadius;
    private float charge;
    public boolean clientDigging;

    public EntityArcaneBore(EntityType<? extends EntityArcaneBore> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(FACING, Direction.NORTH);
        entityData.define(ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            yBodyRot = yHeadRot;
            if (tickCount % HEAL_INTERVAL == 0) {
                heal(1.0F);
            }
            if (tickCount % RECHARGE_INTERVAL == 0 && charge < MAX_CHARGE) {
                charge += AuraHelper.drainVis(level(), blockPosition(), MAX_CHARGE, false);
            }
            updateActiveFromRedstone();
        }
        Direction facing = getFacing();
        if (!isActive()) {
            digTarget = null;
            getLookControl().setLookAt(getX() + facing.getStepX(), getY(), getZ() + facing.getStepZ(), 10.0F, 33.0F);
        }
        if (digTarget != null && charge >= DIG_COST && !level().isClientSide()) {
            getLookControl().setLookAt(digTarget.getX() + 0.5, digTarget.getY(), digTarget.getZ() + 0.5, 10.0F, 90.0F);
            if (digDelay-- <= 0 && dig()) {
                charge -= DIG_COST;
                if (soundDelay < level().getGameTime()) {
                    soundDelay = level().getGameTime() + SOUND_DELAY_TICKS + random.nextInt(2);
                    playSound(TCSounds.RUMBLE.get(), 0.25F, 0.9F + random.nextFloat() * 0.2F);
                }
            }
        }
        if (!level().isClientSide() && digTarget == null && isActive() && validInventory()) {
            findNextBlockToDig();
            if (digTarget != null) {
                level().broadcastEntityEvent(this, EVENT_DIG_START);
            } else {
                level().broadcastEntityEvent(this, EVENT_DIG_STOP);
                getLookControl().setLookAt(
                        getX() + facing.getStepX() * 2,
                        getY() + facing.getStepY() * 2 + getEyeHeight(),
                        getZ() + facing.getStepZ() * 2,
                        10.0F, 33.0F);
            }
        }
    }

    private void updateActiveFromRedstone() {
        int x = Mth.floor(getX());
        int y = Mth.floor(getY());
        int z = Mth.floor(getZ());
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level().getBlockState(pos);
        if (!state.is(TCBlocks.ACTIVATOR_RAIL.get())) {
            pos = pos.below();
            state = level().getBlockState(pos);
        }
        if (state.is(TCBlocks.ACTIVATOR_RAIL.get())) {
            setActive(!state.getValue(BlockStateProperties.POWERED));
        } else if (!isPassenger()) {
            setActive(level().hasNeighborSignal(blockPosition().below()));
        }
    }

    public boolean validInventory() {
        ItemStack held = getMainHandItem();
        if (held.isEmpty() || !held.is(ItemTags.PICKAXES)) {
            return false;
        }
        return !held.isDamageableItem() || held.getDamageValue() + 1 < held.getMaxDamage();
    }

    public int getDigRadius() {
        int radius = 0;
        ItemStack held = getMainHandItem();
        if (!held.isEmpty() && held.is(ItemTags.PICKAXES)) {
            Enchantable enchantable = held.get(DataComponents.ENCHANTABLE);
            radius = (enchantable == null ? 0 : enchantable.value()) / 3;
            radius += InfusionEnchantmentHelper.level(held, InfusionEnchantment.DESTRUCTIVE) * 2;
        }
        return radius <= 1 ? 2 : radius;
    }

    public int getDigDepth() {
        return getDigRadius() * 8
                + InfusionEnchantmentHelper.level(getMainHandItem(), InfusionEnchantment.BURROWING) * 16;
    }

    public int getFortune() {
        if (!validInventory()) {
            return 0;
        }
        ItemStack held = getMainHandItem();
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(
                level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), held);
        return Math.max(fortune, InfusionEnchantmentHelper.level(held, InfusionEnchantment.SOUNDING));
    }

    public int getDigSpeed(BlockState state) {
        if (!validInventory()) {
            return 0;
        }
        ItemStack held = getMainHandItem();
        int speed = (int) (held.getDestroySpeed(state) / 2.0F);
        speed += EnchantmentHelper.getItemEnchantmentLevel(
                level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), held);
        return speed;
    }

    public int getRefining() {
        return InfusionEnchantmentHelper.level(getMainHandItem(), InfusionEnchantment.REFINING);
    }

    public boolean hasSilkTouch() {
        ItemStack held = getMainHandItem();
        return !held.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(
                level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), held) > 0;
    }

    private boolean dig() {
        boolean dug = false;
        if (digTarget != null && !level().isEmptyBlock(digTarget) && level() instanceof ServerLevel serverLevel) {
            BlockState state = serverLevel.getBlockState(digTarget);
            ItemStack held = getMainHandItem();
            List<ItemStack> items = new ArrayList<>();
            if (held.isCorrectToolForDrops(state) || !state.requiresCorrectToolForDrops()) {
                items.addAll(Block.getDrops(state, serverLevel, digTarget, serverLevel.getBlockEntity(digTarget), this, held));
            }
            List<ItemEntity> nearby = serverLevel.getEntitiesOfClass(ItemEntity.class,
                    new AABB(digTarget).inflate(1.5, 1.5, 1.5));
            for (ItemEntity item : nearby) {
                items.add(item.getItem().copy());
                item.discard();
            }
            int refining = getRefining();
            boolean silk = hasSilkTouch();
            for (ItemStack drop : items) {
                ItemStack ejected = drop;
                if (!silk && refining > 0 && random.nextFloat() < (refining + 1) * 0.125F) {
                    Item cluster = RefiningResults.clusterFor(state);
                    if (cluster != null) {
                        ejected = new ItemStack(cluster, drop.getCount());
                    }
                }
                ejectStack(serverLevel, ejected);
            }
            breakCounter++;
            if (!held.isEmpty()) {
                if (breakCounter >= DURABILITY_PER_BREAKS) {
                    breakCounter -= DURABILITY_PER_BREAKS;
                    held.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
                }
            } else {
                breakCounter = 0;
            }
            dug = serverLevel.destroyBlock(digTarget, false, this);
        }
        digTarget = null;
        return dug;
    }

    private void ejectStack(ServerLevel serverLevel, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (Direction face : Direction.values()) {
            BlockPos side = blockPosition().relative(face);
            if (InvHelper.getItemHandlerAt(serverLevel, side, face.getOpposite()) != null) {
                ItemStack remainder = InvHelper.insertStackAt(serverLevel, side, face.getOpposite(), stack, false);
                if (remainder.isEmpty()) {
                    return;
                }
                stack = remainder;
            }
        }
        Direction back = getFacing().getOpposite();
        ItemEntity entity = new ItemEntity(serverLevel,
                getX() + back.getStepX() * 0.75, getY() + 0.5, getZ() + back.getStepZ() * 0.75, stack);
        serverLevel.addFreshEntity(entity);
    }

    private void findNextBlockToDig() {
        int digRadius = getDigRadius();
        if (digTargetPrev == null
                || digTargetPrev.distToCenterSqr(position()) > (digRadius + 1) * (digRadius + 1)) {
            digTargetPrev = blockPosition();
        }
        if (radInc == 0.0F) {
            radInc = 1.0F;
        }
        int digDepth = getDigDepth();
        int x = digTargetPrev.getX();
        int y = digTargetPrev.getY();
        int z = digTargetPrev.getZ();
        Direction facing = getFacing();
        BlockPos end = new BlockPos(
                x + facing.getStepX() * digDepth,
                y + facing.getStepY() * digDepth,
                z + facing.getStepZ() * digDepth);
        BlockHitResult hit = level().clip(new ClipContext(
                Vec3.atCenterOf(digTargetPrev), Vec3.atCenterOf(end),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 digger = new Vec3(
                    getX() + facing.getStepX(),
                    getY() + getEyeHeight() + facing.getStepY(),
                    getZ() + facing.getStepZ());
            hit = level().clip(new ClipContext(digger, Vec3.atCenterOf(hit.getBlockPos()),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.MISS) {
                BlockPos target = hit.getBlockPos();
                BlockState state = level().getBlockState(target);
                if (state.getDestroySpeed(level(), target) > -1.0F
                        && !state.getCollisionShape(level(), target).isEmpty()) {
                    digDelay = Math.max(10 - getDigSpeed(state),
                            (int) (state.getDestroySpeed(level(), target) * 2.0F) - getDigSpeed(state) * 2);
                    if (digDelay < 1) {
                        digDelay = 1;
                    }
                    digDelayMax = digDelay;
                    if (!target.equals(blockPosition()) && !target.equals(blockPosition().below())) {
                        digTarget = target;
                        return;
                    }
                }
            }
        }
        while (x == digTargetPrev.getX() && z == digTargetPrev.getZ() && y == digTargetPrev.getY()) {
            if (Math.abs(currentRadius) > digRadius) {
                currentRadius = digRadius;
            }
            spiral = (int) (spiral + (3.0F + Math.max(0.0F, (10.0F - Math.abs(currentRadius)) * 2.0F)));
            if (spiral >= 360) {
                spiral -= 360;
                currentRadius += radInc;
                if (currentRadius > digRadius || currentRadius < -digRadius) {
                    currentRadius = 0.0F;
                }
            }
            Vec3 source = new Vec3(
                    (int) getX() + 0.5 + facing.getStepX(),
                    getY() + facing.getStepY() + getEyeHeight(),
                    (int) getZ() + 0.5 + facing.getStepZ());
            Vec3 target = new Vec3(0.0, currentRadius, 0.0);
            target = rotateAroundZ(target, spiral / 180.0F * (float) Math.PI);
            target = rotateAroundY(target, (float) (Math.PI / 2) * facing.getStepX());
            target = rotateAroundX(target, (float) (Math.PI / 2) * facing.getStepY());
            Vec3 result = source.add(target);
            x = Mth.floor(result.x);
            y = Mth.floor(result.y);
            z = Mth.floor(result.z);
        }
        digTargetPrev = new BlockPos(x, y, z);
    }

    private static Vec3 rotateAroundX(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x, vec.y * cos - vec.z * sin, vec.y * sin + vec.z * cos);
    }

    private static Vec3 rotateAroundY(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static Vec3 rotateAroundZ(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos - vec.y * sin, vec.x * sin + vec.y * cos, vec.z);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity living && isOwner(living)) {
            Direction face = Direction.getApproximateNearest(
                    living.getX() - getX(), living.getY() - getY(), living.getZ() - getZ());
            if (face != Direction.DOWN) {
                setFacing(face);
            }
            return false;
        }
        setYRot((float) (getYRot() + random.nextGaussian() * 45.0));
        setXRot((float) (getXRot() + random.nextGaussian() * 20.0));
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!level().isClientSide()) {
            dropHeld();
        }
    }

    private void dropHeld() {
        if (!getMainHandItem().isEmpty() && level() instanceof ServerLevel serverLevel) {
            spawnAtLocation(serverLevel, getMainHandItem(), 0.5F);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && isOwner(player) && isAlive()) {
            if (player.isShiftKeyDown()) {
                playSound(TCSounds.ZAP.get(), 1.0F, 1.0F);
                dropHeld();
                spawnAtLocation((ServerLevel) level(), new ItemStack(TCItems.TURRET_BORE.get()), 0.5F);
                discard();
                player.swing(hand);
            } else {
                MenuArcaneBore.open(player, this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 movement = getDeltaMovement();
        if (movement.y > 0.1) {
            setDeltaMovement(movement.x, 0.1, movement.z);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(movement.x / MOVE_DAMPING, movement.y, movement.z / MOVE_DAMPING));
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
    }

    public float getCharge() {
        return charge;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        charge = input.getFloatOr("charge", 0.0F);
        setFacing(Direction.values()[input.getByteOr("facing", (byte) 0)]);
        setActive(input.getBooleanOr("active", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("charge", charge);
        output.putByte("facing", (byte) getFacing().ordinal());
        output.putBoolean("active", isActive());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        float bonus = 0.0F;
        if (random.nextFloat() < 0.2F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.MIND_CLOCKWORK.get()), 0.5F);
        }
        if (random.nextFloat() < 0.2F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.MORPHIC_RESONATOR.get()), 0.5F);
        }
        if (random.nextFloat() < 0.2F + bonus) {
            spawnAtLocation(level, new ItemStack(TCBlocks.CRYSTAL_AER.get()), 0.5F);
        }
        if (random.nextFloat() < 0.2F + bonus) {
            spawnAtLocation(level, new ItemStack(TCBlocks.CRYSTAL_TERRA.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.MECHANISM_SIMPLE.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.PLATE_BRASS.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCBlocks.PLANK_GREATWOOD.get()), 0.5F);
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    public int getHeadRotSpeed() {
        return 10;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_DIG_START) {
            clientDigging = true;
        } else if (event == EVENT_DIG_STOP) {
            clientDigging = false;
        } else {
            super.handleEntityEvent(event);
        }
    }
}

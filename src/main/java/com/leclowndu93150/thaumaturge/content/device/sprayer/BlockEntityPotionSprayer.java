package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class BlockEntityPotionSprayer extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    public static final int MAX_CHARGES = 8;

    private static final String POTION_KEY = "Potion";
    private static final String RECIPE_KEY = "Recipe";
    private static final String PROGRESS_KEY = "Progress";
    private static final String CHARGES_KEY = "Charges";
    private static final String COLOR_KEY = "Color";
    private static final int DEFAULT_COLOR = 0x333333;
    private static final int COLOR_MASK = 0xFFFFFF;
    private static final int ESSENTIA_INTERVAL = 5;
    private static final int SUCTION = 128;
    private static final int PULL_AMOUNT = 1;
    private static final int SPRAY_REACH = 2;
    private static final double SPRAY_SIDE = 3.0;
    private static final int VENT_EVENT = 0;
    private static final int VENT_DURATION = 15;
    private static final int VENT_PARTICLE_DIVISOR = 2;
    private static final float VENT_SIZE = 4.0F;
    private static final double VENT_CENTER = 0.5;
    private static final double VENT_JITTER = 0.1;
    private static final double VENT_SPREAD = 0.06;
    private static final double VENT_SPEED = 0.25;
    private static final float SPRAY_VOLUME = 0.25F;
    private static final float SPRAY_PITCH = 2.6F;
    private static final float SPRAY_PITCH_SPREAD = 0.8F;
    private static final double INSTANT_EFFECT_SCALE = 1.0;
    private static final int NOTHING = 0;
    private static final boolean OUTPUT_ALLOWED = false;
    private static final @Nullable Holder<IAspect> NO_ASPECT = null;

    private ItemStack potion = ItemStack.EMPTY;
    private ItemStack appliedPotion = ItemStack.EMPTY;
    private AspectList essentiaNeeded = AspectList.EMPTY;
    private AspectList essentiaStored = AspectList.EMPTY;
    private int sprayShots;
    private int color = DEFAULT_COLOR;
    private int essentiaTimer;
    private int ventTicks;
    private boolean powerEdgeLatched;
    private @Nullable Holder<IAspect> requested;

    public BlockEntityPotionSprayer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static boolean holdsPotionItem(ItemStack stack) {
        return stack.getItem() instanceof PotionItem;
    }

    public ItemStack getPotion() {
        return potion;
    }

    public void setPotion(ItemStack stack) {
        potion = stack;
        if (level instanceof ServerLevel serverLevel && !ItemStack.matches(appliedPotion, stack)) {
            recalculate(serverLevel);
        }
    }

    private void recalculate(ServerLevel serverLevel) {
        appliedPotion = potion.copy();
        essentiaNeeded = potion.isEmpty() ? AspectList.EMPTY : PotionAspects.of(serverLevel, potion);
        essentiaStored = AspectList.EMPTY;
        sprayShots = 0;
        requested = null;
        color = potion.isEmpty() ? DEFAULT_COLOR : potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor() & COLOR_MASK;
        setChangedAndSync();
    }

    public AspectList essentiaNeeded() {
        return essentiaNeeded;
    }

    public AspectList essentiaStored() {
        return essentiaStored;
    }

    public int sprayShots() {
        return sprayShots;
    }

    public int color() {
        return color;
    }

    private Direction facing() {
        return getBlockState().getValue(BlockPotionSprayer.FACING);
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            vent(level, pos, state.getValue(BlockPotionSprayer.FACING));
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        trackPowerEdge(serverLevel, pos, state);
        boolean due = essentiaTimer == 0;
        essentiaTimer = (essentiaTimer + 1) % ESSENTIA_INTERVAL;
        if (due) {
            gatherEssentia(serverLevel, pos, state.getValue(BlockPotionSprayer.FACING));
        }
    }

    private void trackPowerEdge(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getValue(BlockStateProperties.ENABLED)) {
            powerEdgeLatched = false;
            return;
        }
        if (powerEdgeLatched) {
            return;
        }
        powerEdgeLatched = true;
        if (sprayShots > 0) {
            discharge(level, pos, state);
        }
    }

    private void discharge(ServerLevel level, BlockPos pos, BlockState state) {
        sprayShots--;
        applyPotion(level, pos, state.getValue(BlockPotionSprayer.FACING));
        RandomSource random = level.getRandom();
        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, SPRAY_VOLUME, SPRAY_PITCH + (random.nextFloat() - random.nextFloat()) * SPRAY_PITCH_SPREAD);
        level.blockEvent(pos, state.getBlock(), VENT_EVENT, 0);
        setChangedAndSync();
    }

    private void applyPotion(ServerLevel level, BlockPos pos, Direction facing) {
        Iterable<MobEffectInstance> effects = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
        AABB area = AABB.ofSize(pos.relative(facing, SPRAY_REACH).getCenter(), SPRAY_SIDE, SPRAY_SIDE, SPRAY_SIDE);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, BlockEntityPotionSprayer::canReceiveSpray)) {
            effects.forEach(effect -> applyEffect(level, target, effect));
        }
    }

    private static boolean canReceiveSpray(LivingEntity entity) {
        return entity.isAlive() && entity.isAffectedByPotions();
    }

    private static void applyEffect(ServerLevel level, LivingEntity target, MobEffectInstance effect) {
        Holder<MobEffect> holder = effect.getEffect();
        if (!holder.value().isInstantenous()) {
            target.addEffect(new MobEffectInstance(holder, effect.getDuration(), effect.getAmplifier()));
            return;
        }
        holder.value().applyInstantenousEffect(level, null, null, target, effect.getAmplifier(), INSTANT_EFFECT_SCALE);
    }

    private void gatherEssentia(ServerLevel level, BlockPos pos, Direction facing) {
        if (potion.isEmpty() || sprayShots >= MAX_CHARGES) {
            requested = null;
            return;
        }
        if (!essentiaNeeded.isEmpty() && firstUnmet() == null) {
            sprayShots++;
            essentiaStored = AspectList.EMPTY;
            setChangedAndSync();
        }
        requested = sprayShots >= MAX_CHARGES ? null : firstUnmet();
        if (requested != null) {
            pull(level, pos, facing, requested);
        }
    }

    private @Nullable Holder<IAspect> firstUnmet() {
        for (AspectInstance entry : essentiaNeeded.entries()) {
            if (essentiaStored.amountOf(entry.aspect()) < entry.amount()) {
                return entry.aspect();
            }
        }
        return null;
    }

    private void pull(ServerLevel level, BlockPos pos, Direction facing, Holder<IAspect> aspect) {
        for (Direction side : Direction.values()) {
            if (side == facing) {
                continue;
            }
            BlockPos neighbour = pos.relative(side);
            if (pullFrom(level, pos, neighbour, side.getOpposite(), aspect) || pullFrom(level, pos, neighbour.above(), side.getOpposite(), aspect)) {
                return;
            }
        }
    }

    private boolean pullFrom(ServerLevel level, BlockPos self, BlockPos candidate, Direction face, Holder<IAspect> aspect) {
        if (candidate.equals(self) || !level.hasChunkAt(candidate)) {
            return false;
        }
        IEssentiaTransport transport = level.getCapability(EssentiaCapabilities.TRANSPORT, candidate, face);
        if (transport == null || !aspect.equals(transport.getEssentiaType(face)) || transport.getEssentiaAmount(face) <= 0 || transport.getSuctionAmount(face) >= SUCTION
                || SUCTION < transport.getMinimumSuction()) {
            return false;
        }
        int taken = transport.takeEssentia(aspect, PULL_AMOUNT, face);
        if (taken > 0) {
            essentiaStored = essentiaStored.add(aspect, taken);
            setChangedAndSync();
        }
        return true;
    }

    private void vent(Level level, BlockPos pos, Direction facing) {
        if (ventTicks <= 0) {
            return;
        }
        RandomSource random = level.getRandom();
        int puffs = (ventTicks - 1) / VENT_PARTICLE_DIVISOR;
        while (puffs-- > 0) {
            double driftX = ventDrift(random, facing.getStepX());
            double driftY = ventDrift(random, facing.getStepY());
            double driftZ = ventDrift(random, facing.getStepZ());
            double x = ventOrigin(pos.getX(), facing.getStepX(), random);
            double y = ventOrigin(pos.getY(), facing.getStepY(), random);
            double z = ventOrigin(pos.getZ(), facing.getStepZ(), random);
            level.addParticle(new VentParticleOptions(driftX, driftY, driftZ, color, VENT_SIZE, false), x, y, z, 0.0, 0.0, 0.0);
        }
        ventTicks--;
    }

    private static double ventDrift(RandomSource random, int step) {
        return random.nextGaussian() * VENT_SPREAD + step * VENT_SPEED;
    }

    private static double ventOrigin(int blockCoordinate, int step, RandomSource random) {
        double faceOffset = VENT_CENTER * (1 + step);
        return blockCoordinate + faceOffset + Mth.nextDouble(random, -VENT_JITTER, VENT_JITTER);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        boolean isVent = id == VENT_EVENT;
        if (isVent && level != null && level.isClientSide()) {
            ventTicks = VENT_DURATION;
        }
        return isVent || super.triggerEvent(id, param);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide() || potion.isEmpty()) {
            return;
        }
        ItemStack leftover = potion;
        potion = ItemStack.EMPTY;
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), leftover);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!potion.isEmpty()) {
            output.store(POTION_KEY, ItemStack.CODEC, potion);
        }
        output.store(RECIPE_KEY, AspectList.CODEC, essentiaNeeded);
        output.store(PROGRESS_KEY, AspectList.CODEC, essentiaStored);
        output.putInt(CHARGES_KEY, sprayShots);
        output.putInt(COLOR_KEY, color);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentiaNeeded = readAspects(input, RECIPE_KEY);
        essentiaStored = readAspects(input, PROGRESS_KEY);
        sprayShots = input.getIntOr(CHARGES_KEY, 0);
        color = input.getIntOr(COLOR_KEY, DEFAULT_COLOR);
        potion = input.read(POTION_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        appliedPotion = potion.copy();
    }

    private static AspectList readAspects(ValueInput input, String key) {
        return input.read(key, AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {
        requested = aspect;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return requested;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return requested == null ? 0 : SUCTION;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return OUTPUT_ALLOWED;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        boolean wanted = requested != null && requested.equals(aspect) && face != facing();
        int room = wanted ? Math.max(0, essentiaNeeded.amountOf(aspect) - essentiaStored.amountOf(aspect)) : NOTHING;
        int accepted = Math.min(Math.max(amount, 0), room);
        if (accepted > NOTHING) {
            essentiaStored = essentiaStored.add(aspect, accepted);
            setChangedAndSync();
        }
        return accepted;
    }

    @Override
    public int getMinimumSuction() {
        return NOTHING;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return NOTHING;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return NOTHING;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return NO_ASPECT;
    }
}

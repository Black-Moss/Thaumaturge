package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record EjectEffect(boolean consume, EjectAftermath aftermath, IntProvider mitigation, IntProvider pollution, float explosionPower,
        Optional<Float> consumeAtOrBelow) implements InstabilityEffect {
    private static final int MITIGATION_MIN = 5;
    private static final int MITIGATION_MAX = 10;
    private static final int POLLUTION_MIN = 5;
    private static final int POLLUTION_MAX = 9;
    private static final double CENTER_OFFSET = 0.5;
    private static final double DROP_HEIGHT = 1.0;
    private static final double PEDESTAL_TOP = 1.0;
    private static final float FLUX_GOO_CHANCE = 0.5F;
    private static final float SLOSH_VOLUME = 0.3F;
    private static final float SLOSH_PITCH = 1.0F;
    private static final float PUFF_RED = 0.3F;
    private static final float PUFF_GREEN = 0.0F;
    private static final float PUFF_BLUE = 0.35F;
    private static final int WARD_FLASH_COUNT = 6;
    private static final double WARD_FLASH_SPREAD = 0.35;
    private static final double WARD_FLASH_LIFT = 0.6;
    private static final int WARD_FLASH_COLOR = ARGB.colorFromFloat(1.0F, 0.7F, 0.6F, 1.0F);
    private static final float WARD_FLASH_ALPHA = 0.8F;
    private static final float WARD_FLASH_SCALE = 0.5F;
    private static final int WARD_FLASH_AGE = 10;
    private static final int WARD_FLASH_DELAY = 0;
    private static final int WARD_ARC_COLOR = 0x6A7CFF;

    public static final IntProvider DEFAULT_MITIGATION = UniformInt.of(MITIGATION_MIN, MITIGATION_MAX);
    public static final IntProvider DEFAULT_POLLUTION = UniformInt.of(POLLUTION_MIN, POLLUTION_MAX);
    public static final float DEFAULT_EXPLOSION_POWER = 1.0F;

    public static final MapCodec<EjectEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.BOOL.optionalFieldOf("consume", false).forGetter(EjectEffect::consume),
            EjectAftermath.CODEC.optionalFieldOf("aftermath", EjectAftermath.NONE).forGetter(EjectEffect::aftermath),
            IntProviders.CODEC.optionalFieldOf("mitigation", DEFAULT_MITIGATION).forGetter(EjectEffect::mitigation),
            IntProviders.CODEC.optionalFieldOf("pollution", DEFAULT_POLLUTION).forGetter(EjectEffect::pollution),
            Codec.FLOAT.optionalFieldOf("explosion_power", DEFAULT_EXPLOSION_POWER).forGetter(EjectEffect::explosionPower),
            Codec.FLOAT.optionalFieldOf("consume_at_or_below").forGetter(EjectEffect::consumeAtOrBelow)).apply(instance, EjectEffect::new));

    public static EjectEffect of(boolean consume, EjectAftermath aftermath) {
        return new EjectEffect(consume, aftermath, DEFAULT_MITIGATION, DEFAULT_POLLUTION, DEFAULT_EXPLOSION_POWER, Optional.empty());
    }

    public EjectEffect consumingAtOrBelow(float stability) {
        return new EjectEffect(consume, aftermath, mitigation, pollution, explosionPower, Optional.of(stability));
    }

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.EJECT.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        BlockEntityPedestal target = pickStocked(context);
        if (target == null) {
            return;
        }
        if (!warded(context, target)) {
            disrupt(context, target);
        }
    }

    private static @Nullable BlockEntityPedestal pickStocked(InstabilityContext context) {
        ServerLevel level = context.level();
        List<BlockEntityPedestal> stocked = new ArrayList<>();
        for (BlockPos pos : context.pedestals()) {
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal && !pedestal.getItem().isEmpty()) {
                stocked.add(pedestal);
            }
        }
        return stocked.isEmpty() ? null : stocked.get(context.random().nextInt(stocked.size()));
    }

    private boolean warded(InstabilityContext context, BlockEntityPedestal pedestal) {
        BlockPos device = pedestal.linkedStabilizer();
        if (device == null || !(context.level().getBlockEntity(device) instanceof BlockEntityStabilizer stabilizer)) {
            return false;
        }
        if (!stabilizer.spendForShield(mitigation.sample(context.random()))) {
            return false;
        }
        BlockPos spot = pedestal.getBlockPos();
        flashWard(context, spot);
        Vec3 top = topOf(spot);
        context.arcTo(top);
        EffectDispatch.spawnArc(context.level(), top, Vec3.atCenterOf(device), WARD_ARC_COLOR, 0.0F);
        return true;
    }

    private void disrupt(InstabilityContext context, BlockEntityPedestal pedestal) {
        ServerLevel level = context.level();
        BlockPos spot = pedestal.getBlockPos();
        ItemStack held = pedestal.getItem();
        pedestal.setItem(ItemStack.EMPTY);
        if (!destroys(context.stability())) {
            drop(level, spot, held);
        }
        runAftermath(level, context.random(), spot);
        Effects.bamf(level, topOf(spot)).color(PUFF_RED, PUFF_GREEN, PUFF_BLUE).send();
        context.arcTo(topOf(spot));
    }

    private boolean destroys(float stability) {
        return consume || consumeAtOrBelow.map(limit -> stability <= limit).orElse(false);
    }

    private static void flashWard(InstabilityContext context, BlockPos spot) {
        RandomSource random = context.random();
        Vec3 top = topOf(spot);
        ShieldSparkParticleOptions flash = new ShieldSparkParticleOptions(WARD_FLASH_COLOR, WARD_FLASH_ALPHA, WARD_FLASH_SCALE, WARD_FLASH_AGE, WARD_FLASH_DELAY, true);
        for (int index = 0; index < WARD_FLASH_COUNT; index++) {
            double x = top.x + random.triangle(0.0, WARD_FLASH_SPREAD);
            double y = top.y + random.nextDouble() * WARD_FLASH_LIFT;
            double z = top.z + random.triangle(0.0, WARD_FLASH_SPREAD);
            Effects.spawn(context.level(), flash, x, y, z);
        }
    }

    private static Vec3 topOf(BlockPos spot) {
        return new Vec3(spot.getX() + CENTER_OFFSET, spot.getY() + PEDESTAL_TOP, spot.getZ() + CENTER_OFFSET);
    }

    private static void drop(ServerLevel level, BlockPos origin, ItemStack held) {
        level.addFreshEntity(new ItemEntity(level, origin.getX() + CENTER_OFFSET, origin.getY() + DROP_HEIGHT, origin.getZ() + CENTER_OFFSET, held));
    }

    private void runAftermath(ServerLevel level, RandomSource random, BlockPos origin) {
        if (aftermath == EjectAftermath.FLUX) {
            leakFlux(level, random, origin);
        } else if (aftermath == EjectAftermath.POLLUTE) {
            AuraHelper.polluteAura(level, origin, pollution.sample(random), true);
        } else if (aftermath == EjectAftermath.EXPLODE) {
            detonate(level, origin);
        }
    }

    private static void leakFlux(ServerLevel level, RandomSource random, BlockPos origin) {
        boolean goo = random.nextFloat() < FLUX_GOO_CHANCE;
        placeFlux(level, origin.above(), goo);
        level.playSound(null, origin, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, SLOSH_VOLUME, SLOSH_PITCH);
    }

    private static void placeFlux(ServerLevel level, BlockPos target, boolean goo) {
        if (goo) {
            PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
            return;
        }
        PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
    }

    private void detonate(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        level.explode(null, center.x, center.y, center.z, explosionPower, Level.ExplosionInteraction.NONE);
    }
}

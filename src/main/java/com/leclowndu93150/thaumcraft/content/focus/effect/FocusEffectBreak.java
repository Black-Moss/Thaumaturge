package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectBreak extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("break");

    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int SILK_COMPLEXITY_FACTOR = 4;
    private static final int FORTUNE_COMPLEXITY_FACTOR = 3;
    private static final float HARDNESS_TO_DURABILITY = 100.0F;
    private static final float DELAY_DIVISOR = 3.0F;
    private static final float BASE_VIS_COST = 0.25F;
    private static final float SILK_VIS_COST = 0.25F;
    private static final float FORTUNE_VIS_COST = 0.1F;
    private static final int PARTICLE_START_BASE = 704;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.PERDITIO;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * POWER_COMPLEXITY_FACTOR
                + getSettingValue("silk") * SILK_COMPLEXITY_FACTOR
                + (getSettingValue("fortune") == 0 ? 0 : (getSettingValue("fortune") + 1) * FORTUNE_COMPLEXITY_FACTOR);
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return true;
        }
        if (target instanceof BlockHitResult blockHit) {
            FocusFX.impact(level, Vec3.atCenterOf(blockHit.getBlockPos()), getKey());
            boolean silk = getSettingValue("silk") > 0;
            int fortune = getSettingValue("fortune");
            float strength = getSettingValue("power") * finalPower;
            float dur = level.getBlockState(blockHit.getBlockPos()).getDestroySpeed(level, blockHit.getBlockPos())
                    * HARDNESS_TO_DURABILITY;
            dur = (float) Math.sqrt(dur);
            if (getPackage().getCaster() instanceof Player player) {
                BlockBreakerEngine.addBreaker(level, blockHit.getBlockPos(),
                        level.getBlockState(blockHit.getBlockPos()), player, true, silk, fortune,
                        strength, dur, dur, (int) (dur / strength / DELAY_DIVISOR * num),
                        BASE_VIS_COST + (silk ? SILK_VIS_COST : 0.0F) + fortune * FORTUNE_VIS_COST);
            }
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] silk = new int[]{0, 1};
        String[] silkDesc = new String[]{"focus.common.no", "focus.common.yes"};
        int[] fortune = new int[]{0, 1, 2, 3, 4};
        String[] fortuneDesc = new String[]{"focus.common.no", "I", "II", "III", "IV"};
        return new NodeSetting[]{
                new NodeSetting("power", "focus.break.power", new NodeSettingIntRange(1, 5)),
                new NodeSetting("fortune", "focus.common.fortune", new NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk", new NodeSettingIntList(silk, silkDesc))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        int q = level.getRandom().nextInt(4);
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .maxAge(6 + level.getRandom().nextInt(6))
                .particles(PARTICLE_START_BASE + q * 3, 3, 1)
                .slowDown(0.8)
                .scale((float) (1.7F + level.getRandom().nextGaussian() * 0.3F))
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.END_GATEWAY_SPAWN,
                SoundSource.PLAYERS, 0.1F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}

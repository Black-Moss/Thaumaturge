package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumcraft.content.particle.ShieldSparkParticleOptions;
import net.minecraft.util.ARGB;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectExchange extends FocusEffect implements IFocusBlockPicker {
    private static final Identifier KEY = TCIds.rl("exchange");

    private static final int BASE_COMPLEXITY = 5;
    private static final int SILK_COMPLEXITY_FACTOR = 4;
    private static final int FORTUNE_COMPLEXITY_FACTOR = 3;
    private static final int SWAP_FX_COLOR = 8038177;
    private static final float BASE_VIS_COST = 0.25F;
    private static final float SILK_VIS_COST = 0.25F;
    private static final float FORTUNE_VIS_COST = 0.1F;
    private static final int PARTICLE_START = 448;
    private static final int PARTICLE_NUM = 9;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_exchange"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.PERMUTATIO;
    }

    @Override
    public int getComplexity() {
        return BASE_COMPLEXITY + getSettingValue("silk") * SILK_COMPLEXITY_FACTOR + getSettingValue("fortune") == 0
                ? 0
                : (getSettingValue("fortune") + 1) * FORTUNE_COMPLEXITY_FACTOR;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        LivingEntity caster = getPackage().getCaster();
        if (caster == null) {
            return false;
        }
        ItemStack casterStack = ItemStack.EMPTY;
        if (caster.getMainHandItem().getItem() instanceof ICaster) {
            casterStack = caster.getMainHandItem();
        } else if (caster.getOffhandItem().getItem() instanceof ICaster) {
            casterStack = caster.getOffhandItem();
        }
        if (casterStack.isEmpty()) {
            return false;
        }
        boolean silk = getSettingValue("silk") > 0;
        int fortune = getSettingValue("fortune");
        BlockState picked = ((ICaster) casterStack.getItem()).getPickedBlock(casterStack);
        if (caster instanceof Player player && picked != null && !picked.isAir()) {
            BlockBreakerEngine.addSwapper(level, blockHit.getBlockPos(),
                    level.getBlockState(blockHit.getBlockPos()), picked, true, 0, player, true, false,
                    SWAP_FX_COLOR, true, silk, fortune, BlockBreakerEngine.DEFAULT_PREDICATE,
                    BASE_VIS_COST + (silk ? SILK_VIS_COST : 0.0F) + fortune * FORTUNE_VIS_COST);
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
                new NodeSetting("fortune", "focus.common.fortune", new NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk", new NodeSettingIntList(silk, silkDesc))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        float shade = 0.25F + level.getRandom().nextFloat() * 0.25F;
        ShieldSparkParticleOptions data = new ShieldSparkParticleOptions(
                ARGB.colorFromFloat(1.0F, shade, shade, shade), 0.6F, 0.5F, 9, true);
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.2F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}

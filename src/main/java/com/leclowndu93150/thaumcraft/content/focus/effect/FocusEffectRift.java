package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.registry.TCBlockTags;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectRift extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("rift");

    private static final int BASE_COMPLEXITY = 3;
    private static final int DURATION_COMPLEXITY_DIVISOR = 2;
    private static final int DEPTH_COMPLEXITY_DIVISOR = 4;
    private static final int DURATION_TICKS_FACTOR = 20;
    private static final float INDESTRUCTIBLE = -1.0F;
    private static final int PARTICLE_START_BASE = 384;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_rift"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.ALIENIS;
    }

    @Override
    public int getComplexity() {
        return BASE_COMPLEXITY + getSettingValue("duration") / DURATION_COMPLEXITY_DIVISOR
                + getSettingValue("depth") / DEPTH_COMPLEXITY_DIVISOR;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        Level level = getPackage().getLevel();
        float maxdis = getSettingValue("depth") * finalPower;
        int dur = DURATION_TICKS_FACTOR * getSettingValue("duration");
        int distance = 0;
        BlockPos pos = blockHit.getBlockPos();
        for (distance = 0; distance < maxdis; distance++) {
            BlockState bi = level.getBlockState(pos);
            if (bi.is(TCBlockTags.PORTABLE_HOLE_BLACKLIST)
                    || bi.is(Blocks.BEDROCK)
                    || bi.is(TCBlocks.HOLE.get())
                    || bi.isAir()
                    || bi.getDestroySpeed(level, pos) == INDESTRUCTIBLE) {
                break;
            }
            pos = pos.relative(blockHit.getDirection().getOpposite());
        }
        createHole(level, blockHit.getBlockPos(), blockHit.getDirection(), distance + 1, dur);
        return true;
    }

    public static boolean createHole(Level level, BlockPos pos, @Nullable Direction side, int count, int max) {
        BlockState bs = level.getBlockState(pos);
        if (level.isClientSide()
                || level.getBlockEntity(pos) != null
                || bs.is(TCBlockTags.PORTABLE_HOLE_BLACKLIST)
                || bs.is(Blocks.BEDROCK)
                || bs.is(TCBlocks.HOLE.get())
                || (!bs.isAir() && bs.canBeReplaced())
                || bs.getDestroySpeed(level, pos) == INDESTRUCTIBLE) {
            return false;
        }
        if (level.setBlock(pos, TCBlocks.HOLE.get().defaultBlockState(), Block.UPDATE_ALL)
                && level.getBlockEntity(pos) instanceof BlockEntityHole hole) {
            hole.configure(bs, max, count, side);
            hole.setChanged();
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] depth = new int[]{8, 16, 24, 32};
        String[] depthDesc = new String[]{"8", "16", "24", "32"};
        return new NodeSetting[]{
                new NodeSetting("depth", "focus.rift.depth", new NodeSettingIntList(depth, depthDesc)),
                new NodeSetting("duration", "focus.common.duration", new NodeSettingIntRange(2, 10))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .maxAge(16 + level.getRandom().nextInt(16))
                .particles(PARTICLE_START_BASE + level.getRandom().nextInt(16), 1, 1)
                .slowDown(0.75)
                .alpha(1.0F, 0.0F)
                .scale((float) (0.7F + level.getRandom().nextGaussian() * 0.3F))
                .color(0.25F, 0.25F, 1.0F)
                .random(0.01F, 0.01F, 0.01F)
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.2F, 0.7F);
    }
}

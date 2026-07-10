package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusMedium;
import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.api.items.IArchitect;
import com.leclowndu93150.thaumcraft.content.casters.CasterManager;
import com.leclowndu93150.thaumcraft.content.focus.FocusRayTrace;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FocusMediumPlan extends FocusMedium implements IArchitect {
    private static final Identifier KEY = TCIds.rl("plan");

    private static final int COMPLEXITY = 4;
    private static final int METHOD_FULL = 0;
    private static final double PLAN_RANGE = 16.0;

    private final List<BlockPos> checked = new ArrayList<>();

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_plan"), Optional.empty(), false);
    }

    @Override
    public int getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.FABRICO;
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] method = new int[]{0, 1};
        String[] methodDesc = new String[]{"focus.plan.full", "focus.plan.surface"};
        return new NodeSetting[]{new NodeSetting("method", "focus.plan.method", new NodeSettingIntList(method, methodDesc))};
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null || !(getPackage().getCaster() instanceof Player player)) {
            return new HitResult[0];
        }
        List<HitResult> targets = new ArrayList<>();
        ItemStack casterStack = heldCaster(player);
        Trajectory[] supplied = getParent().supplyTrajectories();
        if (supplied != null) {
            for (Trajectory sT : supplied) {
                Vec3 end = sT.direction().normalize().scale(PLAN_RANGE).add(sT.source());
                BlockHitResult target = FocusRayTrace.clipBlocks(getPackage().getLevel(), player, sT.source(), end);
                if (target.getType() != HitResult.Type.BLOCK) {
                    continue;
                }
                List<BlockPos> found = getArchitectBlocks(casterStack, getPackage().getLevel(),
                        target.getBlockPos(), target.getDirection(), player);
                found.sort(Comparator.comparingDouble(p -> p.distSqr(target.getBlockPos())));
                for (BlockPos p : found) {
                    targets.add(new BlockHitResult(Vec3.atCenterOf(p), target.getDirection(), p, false));
                }
            }
        }
        return targets.toArray(new HitResult[0]);
    }

    private static ItemStack heldCaster(LivingEntity caster) {
        if (caster.getMainHandItem().getItem() instanceof ICaster) {
            return caster.getMainHandItem();
        }
        if (caster.getOffhandItem().getItem() instanceof ICaster) {
            return caster.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @Nullable HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster) {
        Vec3 start = caster.position().add(0.0, caster.getEyeHeight(), 0.0);
        Vec3 end = caster.getLookAngle().scale(PLAN_RANGE).add(start);
        return FocusRayTrace.clipBlocks(level, caster, start, end);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis) {
        if (stack.isEmpty()) {
            return false;
        }
        int dim = CasterManager.getAreaDim(stack);
        if (getSettingValue("method") == METHOD_FULL) {
            return switch (axis) {
                case Y -> dim == 0 || dim == 3;
                case Z -> dim == 0 || dim == 2;
                case X -> dim == 0 || dim == 1;
            };
        }
        return switch (side.getAxis()) {
            case Y -> axis == EnumAxis.X && (dim == 0 || dim == 1)
                    || axis == EnumAxis.Z && (dim == 0 || dim == 2);
            case Z -> axis == EnumAxis.Y && (dim == 0 || dim == 1)
                    || axis == EnumAxis.X && (dim == 0 || dim == 2);
            case X -> axis == EnumAxis.Y && (dim == 0 || dim == 1)
                    || axis == EnumAxis.Z && (dim == 0 || dim == 2);
        };
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        List<BlockPos> out = new ArrayList<>();
        if (stack.isEmpty()) {
            return out;
        }
        checked.clear();
        if (getSettingValue("method") == METHOD_FULL) {
            checkNeighboursFull(level, pos, pos,
                    side, CasterManager.getAreaX(stack), CasterManager.getAreaY(stack), CasterManager.getAreaZ(stack), out);
        } else {
            BlockState bi = level.getBlockState(pos);
            if (side.getAxis() == Direction.Axis.Z) {
                checkNeighboursSurface(level, pos, bi, pos, side,
                        CasterManager.getAreaZ(stack), CasterManager.getAreaY(stack), CasterManager.getAreaX(stack), out);
            } else {
                checkNeighboursSurface(level, pos, bi, pos, side,
                        CasterManager.getAreaX(stack), CasterManager.getAreaY(stack), CasterManager.getAreaZ(stack), out);
            }
        }
        return out;
    }

    private void checkNeighboursFull(Level level, BlockPos pos1, BlockPos pos2, Direction side,
            int sizeX, int sizeY, int sizeZ, List<BlockPos> list) {
        if (checked.contains(pos2)) {
            return;
        }
        checked.add(pos2);
        if (!level.getBlockState(pos2).isAir()) {
            list.add(pos2);
        }
        int xs = -sizeX + pos1.getX() - sizeX * side.getStepX();
        int xe = sizeX + pos1.getX() - sizeX * side.getStepX();
        int ys = -sizeY + pos1.getY() - sizeY * side.getStepY();
        int ye = sizeY + pos1.getY() - sizeY * side.getStepY();
        int zs = -sizeZ + pos1.getZ() - sizeZ * side.getStepZ();
        int ze = sizeZ + pos1.getZ() - sizeZ * side.getStepZ();
        for (Direction dir : Direction.values()) {
            BlockPos q = pos2.relative(dir);
            if (q.getX() >= xs && q.getX() <= xe && q.getY() >= ys && q.getY() <= ye && q.getZ() >= zs && q.getZ() <= ze) {
                checkNeighboursFull(level, pos1, q, side, sizeX, sizeY, sizeZ, list);
            }
        }
    }

    private void checkNeighboursSurface(Level level, BlockPos pos1, BlockState bi, BlockPos pos2, Direction side,
            int sizeX, int sizeY, int sizeZ, List<BlockPos> list) {
        if (checked.contains(pos2)) {
            return;
        }
        checked.add(pos2);
        switch (side.getAxis()) {
            case Y -> {
                if (Math.abs(pos2.getX() - pos1.getX()) > sizeX || Math.abs(pos2.getZ() - pos1.getZ()) > sizeZ) {
                    return;
                }
            }
            case Z -> {
                if (Math.abs(pos2.getX() - pos1.getX()) > sizeX || Math.abs(pos2.getY() - pos1.getY()) > sizeZ) {
                    return;
                }
            }
            case X -> {
                if (Math.abs(pos2.getY() - pos1.getY()) > sizeX || Math.abs(pos2.getZ() - pos1.getZ()) > sizeZ) {
                    return;
                }
            }
        }
        BlockState current = level.getBlockState(pos2);
        if (current == bi && isBlockExposed(level, pos2) && !current.isAir()) {
            list.add(pos2);
            for (Direction dir : Direction.values()) {
                if (dir != side && dir.getOpposite() != side) {
                    checkNeighboursSurface(level, pos1, bi, pos2.relative(dir), side, sizeX, sizeY, sizeZ, list);
                }
            }
        }
    }

    private static boolean isBlockExposed(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (!level.getBlockState(pos.relative(face)).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        return true;
    }
}

package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.spell.casting.CasterHands;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record ArchitectDelivery(double range) implements SpellBehavior, IArchitect {
    private static final double MIN_RANGE = 1.0;
    private static final double MAX_RANGE = 64.0;
    private static final double DEFAULT_RANGE = 16.0;
    private static final String METHOD = "method";
    private static final int METHOD_FULL = 0;
    private static final int METHOD_SURFACE = 1;
    private static final int DIM_ALL = 0;
    private static final int DIM_FIRST = 1;
    private static final int DIM_SECOND = 2;
    private static final int DIM_THIRD = 3;
    private static final int DIM_NONE = -1;

    public static final MapCodec<ArchitectDelivery> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.doubleRange(MIN_RANGE, MAX_RANGE).optionalFieldOf("range", DEFAULT_RANGE).forGetter(ArchitectDelivery::range)).apply(i, ArchitectDelivery::new));

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ARCHITECT.get();
    }

    @Override
    public boolean standalone() {
        return false;
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        Player player = ctx.caster() instanceof Player caster ? caster : null;
        ItemStack stack = player == null ? ItemStack.EMPTY : CasterHands.held(player);
        if (stack.isEmpty()) {
            return;
        }
        int mode = ctx.setting(METHOD);
        List<SpellTarget> out = new ArrayList<>();
        for (SpellTarget target : incoming) {
            BlockHitResult hit = probe(ctx.level(), player, target.position(), target.direction());
            if (hit.getType() == HitResult.Type.BLOCK) {
                collect(out, hit, target.direction(), blocks(ctx.level(), hit.getBlockPos(), hit.getDirection(), mode, stack));
            }
        }
        ctx.proceed(out);
    }

    private BlockHitResult probe(Level level, Entity looker, Vec3 origin, Vec3 heading) {
        ClipContext clip = new ClipContext(origin, origin.add(heading.scale(range)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, looker);
        return level.clip(clip);
    }

    private static void collect(List<SpellTarget> out, BlockHitResult hit, Vec3 heading, List<BlockPos> positions) {
        positions.forEach(pos -> out.add(SpellTarget.of(new BlockHitResult(Vec3.atCenterOf(pos), hit.getDirection(), pos, false), heading)));
    }

    @Override
    public HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        return probe(level, caster, caster.getEyePosition(), caster.getLookAngle());
    }

    @Override
    public List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        return stack.isEmpty() ? List.of() : blocks(level, pos, side, methodOf(stack, level), stack);
    }

    @Override
    public boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        int slot = methodOf(stack, level) == METHOD_SURFACE ? surfaceSlot(new Plane(side.getAxis()), axis) : volumeSlot(axis);
        return !stack.isEmpty() && slot != DIM_NONE && matchesDimension(CasterManager.areaMode(stack), slot);
    }

    private static boolean matchesDimension(int mode, int slot) {
        return mode == DIM_ALL || mode == slot;
    }

    private static int volumeSlot(Direction.Axis axis) {
        return switch (axis) {
            case X -> DIM_FIRST;
            case Z -> DIM_SECOND;
            case Y -> DIM_THIRD;
        };
    }

    private static int surfaceSlot(Plane plane, Direction.Axis axis) {
        if (axis == plane.first()) {
            return DIM_FIRST;
        }
        return axis == plane.second() ? DIM_SECOND : DIM_NONE;
    }

    public static int methodOf(ItemStack stack, Level level) {
        if (!(stack.getItem() instanceof ICaster caster)) {
            return METHOD_FULL;
        }
        ItemStack focus = caster.getFocusStack(stack);
        Spell spell = focus.isEmpty() ? null : Spells.spellOf(focus);
        return spell == null ? METHOD_FULL : methodOfSpell(spell, level.registryAccess());
    }

    private static int methodOfSpell(Spell spell, RegistryAccess access) {
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = Spells.part(access, node.part());
            if (part.isPresent() && part.get().behavior() instanceof ArchitectDelivery) {
                return part.get().setting(METHOD).map(setting -> setting.clamp(node.settings().getOrDefault(METHOD, setting.defaultValue()))).orElse(METHOD_FULL);
            }
        }
        return METHOD_FULL;
    }

    @Override
    public boolean replacesBlockHighlight(ItemStack stack) {
        return false;
    }

    private static List<BlockPos> blocks(Level level, BlockPos hit, Direction face, int method, ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        BlockPos origin = hit.immutable();
        List<BlockPos> found = method == METHOD_SURFACE ? surface(level, origin, face, stack) : full(level, origin, face, stack);
        found.sort(Comparator.comparingDouble(pos -> origin.distSqr(pos)));
        return found;
    }

    private static List<BlockPos> full(Level level, BlockPos origin, Direction face, ItemStack stack) {
        Iterable<BlockPos> region = BlockPos.betweenClosed(cornerOf(origin, face, stack, -1), cornerOf(origin, face, stack, 1));
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : region) {
            if (!level.getBlockState(pos).isAir()) {
                found.add(pos.immutable());
            }
        }
        return found;
    }

    private static BlockPos cornerOf(BlockPos origin, Direction face, ItemStack stack, int sign) {
        return origin.offset(CasterManager.reachX(stack) * (sign - face.getStepX()), CasterManager.reachY(stack) * (sign - face.getStepY()), CasterManager.reachZ(stack) * (sign - face.getStepZ()));
    }

    private static List<BlockPos> surface(Level level, BlockPos origin, Direction face, ItemStack stack) {
        Plane plane = new Plane(face.getAxis());
        BlockState reference = level.getBlockState(origin);
        int firstLimit = CasterManager.reachX(stack);
        int secondLimit = CasterManager.reachZ(stack);
        Set<BlockPos> visited = new HashSet<>(List.of(origin));
        List<BlockPos> found = new ArrayList<>(List.of(origin));
        for (int cursor = 0; cursor < found.size(); cursor++) {
            BlockPos current = found.get(cursor);
            for (int side = 0; side < 4; side++) {
                Direction.Axis axis = side < 2 ? plane.first() : plane.second();
                int limit = side < 2 ? firstLimit : secondLimit;
                BlockPos next = current.relative(axis, side % 2 == 0 ? -1 : 1);
                if (Math.abs(next.get(axis) - origin.get(axis)) <= limit && visited.add(next) && accepts(level, next, reference)) {
                    found.add(next);
                }
            }
        }
        return found;
    }

    private static boolean accepts(Level level, BlockPos pos, BlockState reference) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.equals(reference) && exposed(level, pos);
    }

    private static boolean exposed(Level level, BlockPos pos) {
        return Direction.stream().anyMatch(direction -> !level.getBlockState(pos.relative(direction)).isSolidRender());
    }

    private record Plane(Direction.Axis first, Direction.Axis second) {
        Plane(Direction.Axis faceAxis) {
            this(faceAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y, faceAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z);
        }
    }
}

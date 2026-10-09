package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.aura.relay.LinkedRelaySource;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityRechargePedestal extends BlockEntityPedestal {
    private static final String DRAIN_POS_KEY = "DrainPos";
    private static final String DRAIN_COLOR_KEY = "DrainColor";
    private static final int DEFAULT_DRAIN_COLOR = 0xFFFFFF;
    private static final int DRAIN_LINGER_TICKS = 10;
    private static final int WAND_DRAW_INTERVAL = 5;
    private static final int RECHARGE_INTERVAL = 10;
    private static final int RECHARGE_UNITS = 5;
    private static final int NODE_SEARCH_RADIUS = 8;
    private static final int NODE_DRAW_VIS = 1;
    private static final int SPARKLE_SPREAD = 3;
    private static final double SPARKLE_START_HEIGHT = 1.0;
    private static final double SPARKLE_END_BASE_XZ = 0.4;
    private static final double SPARKLE_END_BASE_Y = 1.4;
    private static final double SPARKLE_END_RANGE = 0.2;

    private int tickCounter;
    private int drainTicks;
    private @Nullable BlockPos drainPos;
    private int drainColor = DEFAULT_DRAIN_COLOR;

    public BlockEntityRechargePedestal(BlockPos pos, BlockState state) {
        super(TTBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public @Nullable BlockPos getDrainPos() {
        return drainPos;
    }

    public int getDrainColor() {
        return drainColor;
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemWand || RechargeAccess.isRechargeable(stack));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityRechargePedestal pedestal) {
        if (level instanceof ServerLevel server) {
            pedestal.tickServer(server);
        }
    }

    private void tickServer(ServerLevel level) {
        tickCounter++;
        if (drainTicks > 0 && --drainTicks == 0) {
            drainPos = null;
            setChangedAndSync();
        }
        ItemStack stack = getItem();
        if (!accepts(stack)) {
            return;
        }
        if (stack.getItem() instanceof ItemWand) {
            if (tickCounter % WAND_DRAW_INTERVAL == 0 && (drawFromNode(level, stack) || drawFromRelay(level, stack))) {
                commitCharge(level, stack);
            }
        } else if (tickCounter % RECHARGE_INTERVAL == 0 && RechargeAccess.rechargeItem(level, stack, worldPosition, null, RECHARGE_UNITS) > 0.0F) {
            commitCharge(level, stack);
        }
    }

    private void commitCharge(ServerLevel level, ItemStack stack) {
        setItem(stack);
        setChangedAndSync();
        sendSparkle(level);
    }

    private boolean drawFromNode(ServerLevel level, ItemStack wand) {
        WandParts parts = WandVisHelper.partsOf(wand);
        boolean lenient = parts.rod() == TTWandParts.ROD_WOOD.get() || parts.cap() == TTWandParts.CAP_IRON.get();
        int reserve = lenient ? 0 : 1;
        int maximum = WandVisHelper.capacityOf(wand);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -NODE_SEARCH_RADIUS; dx <= NODE_SEARCH_RADIUS; dx++) {
            for (int dy = -NODE_SEARCH_RADIUS; dy <= NODE_SEARCH_RADIUS; dy++) {
                for (int dz = -NODE_SEARCH_RADIUS; dz <= NODE_SEARCH_RADIUS; dz++) {
                    cursor.setWithOffset(worldPosition, dx, dy, dz);
                    if (level.hasChunkAt(cursor) && level.getBlockEntity(cursor) instanceof BlockEntityNode node && !(node instanceof BlockEntityJarNode)
                            && drawFrom(level, wand, node, reserve, maximum)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean drawFrom(ServerLevel level, ItemStack wand, BlockEntityNode node, int reserve, int maximum) {
        for (AspectInstance entry : node.getAspects().entries()) {
            Holder<IAspect> aspect = entry.aspect();
            ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
            if (key == null || !aspect.value().isPrimal() || entry.amount() <= reserve || WandVisHelper.storedIn(wand, key) >= maximum) {
                continue;
            }
            if (node.accepts(aspect)) {
                node.drain(aspect, NODE_DRAW_VIS);
            }
            WandVisHelper.topUp(wand, key, NODE_DRAW_VIS, true);
            BlockPos nodePos = node.getBlockPos();
            node.setChanged();
            level.sendBlockUpdated(nodePos, node.getBlockState(), node.getBlockState(), Block.UPDATE_CLIENTS);
            startDrain(nodePos, aspect.value().color());
            return true;
        }
        return false;
    }

    private boolean drawFromRelay(ServerLevel level, ItemStack wand) {
        BlockEntityVisRelay relay = VisRelayNetwork.findRelayNear(level, worldPosition);
        LinkedRelaySource source = relay == null ? null : relay.resolveSource(level);
        if (source == null) {
            return false;
        }
        int maximum = WandVisHelper.capacityOf(wand);
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Holder<IAspect> aspect = Aspects.resolve(level, key);
            int room = maximum - WandVisHelper.storedIn(wand, key);
            if (aspect == null || room <= 0) {
                continue;
            }
            int delivered = VisRelayNetwork.drainNow(source, key, Math.min(WandEconomy.CENTIVIS_PER_VIS, room));
            if (delivered > 0) {
                WandVisHelper.topUpCentivis(wand, key, delivered, true);
                relay.triggerConsumeEffect(level, aspect);
                startDrain(source.position(), aspect.value().color());
                return true;
            }
        }
        return false;
    }

    private void startDrain(BlockPos source, int color) {
        drainPos = source.immutable();
        drainColor = color;
        drainTicks = DRAIN_LINGER_TICKS;
    }

    private void sendSparkle(ServerLevel level) {
        RandomSource random = level.getRandom();
        List<Holder.Reference<IAspect>> primals = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(aspect -> aspect.value().isPrimal()).toList();
        int color = primals.isEmpty() ? DEFAULT_DRAIN_COLOR : primals.get(random.nextInt(primals.size())).value().color();
        Vec3 origin = new Vec3(worldPosition.getX() + scatter(random), worldPosition.getY() + SPARKLE_START_HEIGHT + random.nextInt(SPARKLE_SPREAD) + random.nextFloat(),
                worldPosition.getZ() + scatter(random));
        Vec3 target = new Vec3(worldPosition.getX() + SPARKLE_END_BASE_XZ + SPARKLE_END_RANGE * random.nextFloat(), worldPosition.getY() + SPARKLE_END_BASE_Y + SPARKLE_END_RANGE * random.nextFloat(),
                worldPosition.getZ() + SPARKLE_END_BASE_XZ + SPARKLE_END_RANGE * random.nextFloat());
        EffectDispatch.spawnVisSparkle(level, origin, target, color);
    }

    private static double scatter(RandomSource random) {
        return random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD) + random.nextFloat();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (drainPos != null) {
            output.store(DRAIN_POS_KEY, BlockPos.CODEC, drainPos);
            output.putInt(DRAIN_COLOR_KEY, drainColor);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        drainPos = input.read(DRAIN_POS_KEY, BlockPos.CODEC).orElse(null);
        drainColor = input.getIntOr(DRAIN_COLOR_KEY, DEFAULT_DRAIN_COLOR);
    }
}

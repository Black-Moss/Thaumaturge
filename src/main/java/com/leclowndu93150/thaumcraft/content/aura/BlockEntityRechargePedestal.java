package com.leclowndu93150.thaumcraft.content.aura;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.items.RechargeAccess;
import com.leclowndu93150.thaumcraft.content.fx.TCParticleDispatch;
import com.leclowndu93150.thaumcraft.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityRechargePedestal extends BlockEntityPedestal {
    private static final int RECHARGE_INTERVAL_TICKS = 10;
    private static final int RECHARGE_AMOUNT = 5;
    private static final int SPARKLE_SPREAD = 3;

    private int counter;

    public BlockEntityRechargePedestal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityRechargePedestal pedestal) {
        if (level instanceof ServerLevel serverLevel) {
            pedestal.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel level) {
        if (counter++ % RECHARGE_INTERVAL_TICKS != 0 || getItem().isEmpty()) {
            return;
        }
        if (RechargeAccess.rechargeItem(level, getItem(), worldPosition, null, RECHARGE_AMOUNT) > 0.0F) {
            setChanged();
            syncToClient();
            sendSparkle(level);
        }
    }

    private void sendSparkle(ServerLevel level) {
        RandomSource rand = level.getRandom();
        List<Holder.Reference<IAspect>> primals = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .filter(holder -> holder.value().isPrimal())
                .toList();
        int color = primals.isEmpty() ? 0xFFFFFF
                : primals.get(rand.nextInt(primals.size())).value().color();
        Vec3 from = new Vec3(
                worldPosition.getX() + rand.nextInt(SPARKLE_SPREAD) - rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat(),
                worldPosition.getY() + 1 + rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat(),
                worldPosition.getZ() + rand.nextInt(SPARKLE_SPREAD) - rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat());
        Vec3 to = new Vec3(
                worldPosition.getX() + 0.4 + rand.nextFloat() * 0.2F,
                worldPosition.getY() + 1 + 0.4 + rand.nextFloat() * 0.2F,
                worldPosition.getZ() + 0.4 + rand.nextFloat() * 0.2F);
        TCParticleDispatch.spawnVisSparkle(level, from, to, color);
    }
}

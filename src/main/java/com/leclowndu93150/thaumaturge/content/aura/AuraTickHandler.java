package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvents;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxAuraFloor;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxOutbreaks;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraTickHandler {
    private static final int BALANCE_INTERVAL = 20;
    private static final float[] VIS_REGEN_BY_MOON_DISTANCE = {0.25F, 0.15F, 0.10F, 0.05F, 0.0F};
    private static final float[] CAPACITY_BY_MOON_DISTANCE = {1.15F, 1.05F, 1.0F, 0.95F, 0.85F};
    private static final float REGEN_AND_FLUX_STEP_TOTAL = 0.25F;
    private static final float FLOOR_RISE_PER_PASS = 0.5F;
    private static final float MAX_TRANSFER = 1.0F;
    private static final float VIS_SHARE_RATIO = 0.75F;
    private static final float FLUX_SHARE_MINIMUM = 5.0F;
    private static final float FLUX_SHARE_BASE_FRACTION = 0.1F;
    private static final float FLUX_SHARE_DIVISOR = 1.75F;
    private static final float OVERFLOW_RATIO = 1.25F;
    private static final float STARVED_RATIO = 0.1F;
    private static final float SETTLE_CHANCE = 0.1F;
    private static final float EVENT_THRESHOLD_RATIO = 0.75F;
    private static final float RIFT_DIVISOR = 5000.0F;
    private static final float PRESSURE_DIVISOR = 100.0F;
    private static final float MIN_PRESSURE_SCALE = 1.0F;
    private static final int[][] NEIGHBOUR_OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private AuraTickHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % BALANCE_INTERVAL != 0 || !level.tickRateManager().runsNormally()) {
            return;
        }
        Set<ChunkPos> loaded = AuraManager.loadedChunksSnapshot(level);
        if (loaded.isEmpty()) {
            return;
        }
        BalancePass pass = new BalancePass(level);
        for (ChunkPos pos : loaded) {
            pass.balance(pos);
        }
    }

    private static int moonDistanceFromFull(ServerLevel level, ChunkPos pos) {
        Vec3 centre = new Vec3(pos.getMiddleBlockX(), level.getSeaLevel(), pos.getMiddleBlockZ());
        int phase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, centre).index();
        return Math.min(phase, MoonPhase.COUNT - phase);
    }

    private static final class BalancePass {
        private final ServerLevel level;
        private final RandomSource random;
        private float visRegen;
        private float fluxStep;
        private float capacityScale;
        private @Nullable LevelChunk visTargetChunk;
        private @Nullable AuraData visTarget;
        private @Nullable LevelChunk fluxTargetChunk;
        private @Nullable AuraData fluxTarget;

        private BalancePass(ServerLevel level) {
            this.level = level;
            this.random = level.getRandom();
        }

        private void readMoon(ChunkPos pos) {
            int moonDistance = moonDistanceFromFull(level, pos);
            visRegen = VIS_REGEN_BY_MOON_DISTANCE[moonDistance];
            fluxStep = REGEN_AND_FLUX_STEP_TOTAL - visRegen;
            capacityScale = CAPACITY_BY_MOON_DISTANCE[moonDistance];
        }

        private void balance(ChunkPos pos) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            if (chunk == null) {
                return;
            }
            AuraData aura = chunk.getData(TTAttachments.AURA.get());
            aura.setChunkPos(pos);
            AuraGenHandler.initializeIfNeeded(level, chunk, aura);
            if (aura.getBase() == 0) {
                return;
            }
            readMoon(pos);
            float scaledBase = aura.getBase() * capacityScale;
            boolean changed = applyPhysicalFlux(chunk, aura);
            findReceivers(pos);
            changed |= shareVis(aura);
            changed |= shareFlux(aura);
            changed |= settle(aura, scaledBase);
            raiseEvents(pos, aura, scaledBase);
            if (changed) {
                chunk.markUnsaved();
            }
            visTargetChunk = null;
            visTarget = null;
            fluxTargetChunk = null;
            fluxTarget = null;
        }

        private boolean applyPhysicalFlux(LevelChunk chunk, AuraData aura) {
            PhysicalFluxOutbreaks.tryOutbreak(level, chunk, random);
            float floor = PhysicalFluxAuraFloor.target(chunk, aura.getBase());
            float flux = aura.getFlux();
            if (flux >= floor) {
                return false;
            }
            aura.setFlux(Math.min(floor, flux + FLOOR_RISE_PER_PASS));
            return true;
        }

        private void findReceivers(ChunkPos pos) {
            int visTies = 0;
            int fluxTies = 0;
            for (int[] offset : NEIGHBOUR_OFFSETS) {
                LevelChunk neighbour = level.getChunkSource().getChunkNow(pos.x() + offset[0], pos.z() + offset[1]);
                AuraData other = neighbour == null ? null : neighbour.getExistingDataOrNull(TTAttachments.AURA.get());
                if (other == null || other.getBase() == 0) {
                    continue;
                }
                if (other.getVis() + other.getFlux() < other.getBase() * capacityScale) {
                    AuraData current = visTarget;
                    if (current == null || other.getVis() < current.getVis()) {
                        visTarget = other;
                        visTargetChunk = neighbour;
                        visTies = 1;
                    } else if (other.getVis() == current.getVis() && random.nextInt(++visTies) == 0) {
                        visTarget = other;
                        visTargetChunk = neighbour;
                    }
                }
                AuraData currentFlux = fluxTarget;
                if (currentFlux == null || other.getFlux() < currentFlux.getFlux()) {
                    fluxTarget = other;
                    fluxTargetChunk = neighbour;
                    fluxTies = 1;
                } else if (other.getFlux() == currentFlux.getFlux() && random.nextInt(++fluxTies) == 0) {
                    fluxTarget = other;
                    fluxTargetChunk = neighbour;
                }
            }
        }

        private boolean shareVis(AuraData aura) {
            AuraData receiver = visTarget;
            LevelChunk receiverChunk = visTargetChunk;
            float vis = aura.getVis();
            if (receiver == null || receiverChunk == null || receiver.getVis() >= vis || receiver.getVis() >= vis * VIS_SHARE_RATIO) {
                return false;
            }
            float moved = Math.min(MAX_TRANSFER, vis - receiver.getVis());
            aura.setVis(vis - moved);
            receiver.setVis(receiver.getVis() + moved);
            receiverChunk.markUnsaved();
            return true;
        }

        private boolean shareFlux(AuraData aura) {
            AuraData receiver = fluxTarget;
            LevelChunk receiverChunk = fluxTargetChunk;
            float flux = aura.getFlux();
            float threshold = Math.max(FLUX_SHARE_MINIMUM, aura.getBase() * FLUX_SHARE_BASE_FRACTION);
            if (receiver == null || receiverChunk == null || flux <= threshold || receiver.getFlux() >= flux / FLUX_SHARE_DIVISOR) {
                return false;
            }
            float moved = Math.min(MAX_TRANSFER, flux - receiver.getFlux());
            aura.setFlux(flux - moved);
            receiver.setFlux(receiver.getFlux() + moved);
            receiverChunk.markUnsaved();
            return true;
        }

        private boolean settle(AuraData aura, float scaledBase) {
            float vis = aura.getVis();
            float flux = aura.getFlux();
            float room = scaledBase - (vis + flux);
            if (room > 0.0F) {
                float gain = Math.min(visRegen, room);
                if (gain <= 0.0F) {
                    return false;
                }
                aura.setVis(vis + gain);
                return true;
            }
            if (vis > scaledBase * OVERFLOW_RATIO) {
                if (fluxStep <= 0.0F || random.nextFloat() >= SETTLE_CHANCE) {
                    return false;
                }
                aura.setVis(vis - fluxStep);
                aura.setFlux(flux + fluxStep);
                return true;
            }
            if (vis <= scaledBase * STARVED_RATIO && vis >= flux) {
                if (fluxStep <= 0.0F || random.nextFloat() >= SETTLE_CHANCE) {
                    return false;
                }
                aura.setFlux(flux + fluxStep);
                return true;
            }
            return false;
        }

        private void raiseEvents(ChunkPos pos, AuraData aura, float scaledBase) {
            float flux = aura.getFlux();
            if (flux <= scaledBase * EVENT_THRESHOLD_RATIO) {
                return;
            }
            if (random.nextFloat() < flux / RIFT_DIVISOR) {
                AuraManager.queueRiftTrigger(level, new BlockPos(pos.getMinBlockX(), 0, pos.getMinBlockZ()));
            } else if (random.nextFloat() < flux / (PRESSURE_DIVISOR * Math.max(MIN_PRESSURE_SCALE, scaledBase))) {
                FluxPressureEvents.queue(level, pos);
            }
        }
    }
}

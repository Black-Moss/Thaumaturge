package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockEntityArcaneBore;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BoreDigEffect implements IFXInstance {
    private static final int TOTAL_PARTICLES = 50;
    private static final int SPARKLE_ONE_IN = 4;
    private static final float SPARKLE_RED = 0.2F;
    private static final float SPARKLE_GREEN_BASE = 0.6F;
    private static final float SPARKLE_GREEN_SPREAD = 0.3F;
    private static final float SPARKLE_BLUE = 0.2F;
    private static final double BLOCK_CENTRE = 0.5;
    private static final double EYE_HEIGHT = BlockEntityArcaneBore.EYE_HEIGHT;

    private final ClientLevel level;
    private final BlockPos target;
    private final int boreEntityId;
    private final BlockPos borePos;
    private final BlockState state;
    private final int delay;
    private int elapsed;
    private int emitted;
    private boolean finished;

    public BoreDigEffect(ClientLevel level, BlockPos target, int boreEntityId, BlockPos borePos, BlockState state, int delay) {
        this.level = level;
        this.target = target;
        this.boreEntityId = boreEntityId;
        this.borePos = borePos;
        this.state = state;
        this.delay = Math.max(1, delay);
    }

    @Override
    public void tick() {
        if (this.finished) {
            return;
        }
        Vec3 destination = destination();
        if (destination == null) {
            this.finished = true;
            return;
        }
        this.elapsed++;
        int due = (int) ((long) TOTAL_PARTICLES * Math.min(this.elapsed, this.delay) / this.delay);
        while (this.emitted < due) {
            emit(destination);
            this.emitted++;
        }
        if (this.emitted >= TOTAL_PARTICLES) {
            this.finished = true;
        }
    }

    @Override
    public boolean isExpired() {
        return this.finished;
    }

    private @Nullable Vec3 destination() {
        if (this.boreEntityId == BoreDebrisParticleOptions.NO_ENTITY) {
            return new Vec3(this.borePos.getX() + BLOCK_CENTRE, this.borePos.getY() + EYE_HEIGHT, this.borePos.getZ() + BLOCK_CENTRE);
        }
        Entity entity = this.level.getEntity(this.boreEntityId);
        return entity == null ? null : entity.getEyePosition();
    }

    private void emit(Vec3 destination) {
        RandomSource random = this.level.getRandom();
        double x = this.target.getX() + random.nextDouble();
        double y = this.target.getY() + random.nextDouble();
        double z = this.target.getZ() + random.nextDouble();
        ParticleOptions options;
        if (random.nextInt(SPARKLE_ONE_IN) == 0) {
            options = new BoreSparkleParticleOptions(this.boreEntityId, destination.x, destination.y, destination.z, SPARKLE_RED, SPARKLE_GREEN_BASE + random.nextFloat() * SPARKLE_GREEN_SPREAD,
                    SPARKLE_BLUE);
        } else {
            options = new BoreDebrisParticleOptions(this.state, this.boreEntityId, destination.x, destination.y, destination.z, 0.0, 0.0, 0.0);
        }
        Minecraft.getInstance().particleEngine.createParticle(options, x, y, z, 0.0, 0.0, 0.0);
    }
}

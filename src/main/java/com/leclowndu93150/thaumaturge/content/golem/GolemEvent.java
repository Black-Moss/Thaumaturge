package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public enum GolemEvent {
    TASK_CLAIMED(70, GolemEmoteParticleOptions.ICON_TASK, Tint.WHITE, 6, 0, 2.0F, 0.0F, 0.0D, false, 1, Tint.LIFT), TASK_FAILED(71, GolemEmoteParticleOptions.ICON_FAIL, Tint.CYAN, 10, 0, 2.0F, 0.0F,
            0.025D, false, 1, Tint.LIFT), CONFUSED(72, GolemEmoteParticleOptions.ICON_CONFUSED, Tint.WHITE, 10, 0, 2.0F, 0.0F, 0.05D, false, 1, Tint.LIFT), STAY(73,
                    GolemEmoteParticleOptions.ICON_STAY, Tint.YELLOW, 20, 0, 2.0F, 0.0F, 0.01D, false, 1, Tint.LIFT), FOLLOW(74, GolemEmoteParticleOptions.ICON_TASK, Tint.WHITE, 6, 0, 2.0F, 0.0F,
                            0.0D, false, 1, Tint.LIFT), RANK_UP(75, GolemEmoteParticleOptions.ICON_HEART, Tint.WHITE, 20, 20, 0.3F, 0.4F, 0.02D, true, 5, 0.0D);

    public static final byte HANDOFF_TASK_ID = 5;

    private static final double SCATTER_DEVIATION = 0.01D;
    private static final Map<Byte, GolemEvent> BY_ID = index();

    private final byte id;
    private final int icon;
    private final int color;
    private final int lifetime;
    private final int lifetimeSpread;
    private final float scale;
    private final float scaleSpread;
    private final double rise;
    private final boolean scatter;
    private final int count;
    private final double lift;

    GolemEvent(int id, int icon, int color, int lifetime, int lifetimeSpread, float scale, float scaleSpread, double rise, boolean scatter, int count, double lift) {
        this.id = (byte) id;
        this.icon = icon;
        this.color = color;
        this.lifetime = lifetime;
        this.lifetimeSpread = lifetimeSpread;
        this.scale = scale;
        this.scaleSpread = scaleSpread;
        this.rise = rise;
        this.scatter = scatter;
        this.count = count;
        this.lift = lift;
    }

    public byte id() {
        return id;
    }

    public static @Nullable GolemEvent byId(byte id) {
        return BY_ID.get(id);
    }

    public static boolean emotesEnabled() {
        return ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get();
    }

    public void broadcast(Entity entity) {
        Level level = entity.level();
        if (!level.isClientSide() && emotesEnabled()) {
            level.broadcastEntityEvent(entity, id);
        }
    }

    void play(Entity entity) {
        Level level = entity.level();
        RandomSource random = entity.getRandom();
        double y = entity.getY() + entity.getBbHeight() + lift;
        for (int i = 0; i < count; i++) {
            int age = lifetime + (lifetimeSpread > 0 ? random.nextInt(lifetimeSpread) : 0);
            float size = scale + scaleSpread * random.nextFloat();
            double vx = scatter ? random.nextGaussian() * SCATTER_DEVIATION : 0.0D;
            double vy = scatter ? random.nextDouble() * rise : rise;
            double vz = scatter ? random.nextGaussian() * SCATTER_DEVIATION : 0.0D;
            level.addParticle(new GolemEmoteParticleOptions(color, icon, age, size), entity.getX(), y, entity.getZ(), vx, vy, vz);
        }
    }

    private static Map<Byte, GolemEvent> index() {
        Map<Byte, GolemEvent> index = new HashMap<>();
        for (GolemEvent event : values()) {
            index.put(event.id, event);
        }
        index.put(HANDOFF_TASK_ID, TASK_CLAIMED);
        return Map.copyOf(index);
    }

    private static final class Tint {
        private static final int WHITE = 0xFFFFFFFF;
        private static final int CYAN = 0xFF19FFFF;
        private static final int YELLOW = 0xFFFFFF19;
        private static final double LIFT = 0.1D;

        private Tint() {}
    }
}

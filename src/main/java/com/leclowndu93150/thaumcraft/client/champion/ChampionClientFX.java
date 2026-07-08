package com.leclowndu93150.thaumcraft.client.champion;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumcraft.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.registry.TCParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ChampionClientFX {
    private static final int GRID_16 = 16;
    private static final int SPARK_START_BASE = 8;
    private static final int RUNE_START_BASE = 704;
    private static final int SHIELD_START = 448;
    private static final int SMOKE_START = 640;
    private static final int WISP_START = 69;
    private static final int PURPLE_START = 264;
    private static final int BLOB_START = 1;
    private static final int DRIP_START = 579;

    private ChampionClientFX() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof PathfinderMob mob)
                || !mob.isAlive()) {
            return;
        }
        int type = ChampionHelper.championType(mob);
        if (type < 0 || type >= ChampionModifier.MODS.size()) {
            return;
        }
        showFX(mob, type);
    }

    private static void showFX(LivingEntity mob, int type) {
        Level level = mob.level();
        RandomSource rand = level.getRandom();
        double x = mob.getBoundingBox().minX + rand.nextFloat() * mob.getBbWidth();
        double y = mob.getBoundingBox().minY + rand.nextFloat() * mob.getBbHeight();
        double z = mob.getBoundingBox().minZ + rand.nextFloat() * mob.getBbWidth();
        switch (type) {
            case ChampionModifier.BOLD -> {
                if (rand.nextBoolean()) {
                    double sy = mob.getBoundingBox().minY + rand.nextFloat() * mob.getBbHeight() / 3.0F;
                    addParticle(level, x, sy, z, FXGenericData.builder()
                            .color(0.3F - rand.nextFloat() * 0.1F, 0.0F, 0.8F + rand.nextFloat() * 0.2F)
                            .alpha(1.0F)
                            .scale(0.2F)
                            .grid(GRID_16)
                            .particles(SPARK_START_BASE + rand.nextInt(3) * 16, 8, 1)
                            .maxAge(5 + rand.nextInt(5))
                            .flipped(rand.nextBoolean()));
                }
            }
            case ChampionModifier.SPINED -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.5F + rand.nextFloat() * 0.2F, 0.1F + rand.nextFloat() * 0.2F,
                                    0.1F + rand.nextFloat() * 0.2F)
                            .alpha(0.7F)
                            .scale(1.2F + rand.nextFloat() * 0.3F)
                            .particles(RUNE_START_BASE + rand.nextInt(4) * 3, 3, 1)
                            .maxAge(3));
                }
            }
            case ChampionModifier.ARMORED -> {
                if (rand.nextInt(4) == 0) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.9F, 0.9F, 0.9F + rand.nextFloat() * 0.1F)
                            .alpha(0.7F)
                            .scale(0.6F + rand.nextFloat() * 0.2F)
                            .particles(SHIELD_START, 9, 1)
                            .maxAge(5 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.MIGHTY -> {
                if (rand.nextFloat() <= 0.3F) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.8F + rand.nextFloat() * 0.2F, 0.8F + rand.nextFloat() * 0.2F,
                                    0.8F + rand.nextFloat() * 0.2F)
                            .alpha(0.7F)
                            .scale(1.0F + rand.nextFloat() * 0.3F)
                            .particles(RUNE_START_BASE + rand.nextInt(4) * 3, 3, 1)
                            .maxAge(4 + rand.nextInt(3)));
                }
            }
            case ChampionModifier.GRIM -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .motion(0.0, -0.02, 0.0)
                            .color(rand.nextFloat() * 0.2F, rand.nextFloat() * 0.2F, rand.nextFloat() * 0.2F)
                            .alpha(0.8F)
                            .scale(0.6F + rand.nextFloat() * 0.4F)
                            .particles(SMOKE_START, 10, 1)
                            .maxAge(8 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.WARDED -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.5F + rand.nextFloat() * 0.1F, 0.5F + rand.nextFloat() * 0.1F,
                                    0.5F + rand.nextFloat() * 0.1F)
                            .alpha(0.6F)
                            .scale(0.8F + rand.nextFloat() * 0.3F)
                            .particles(WISP_START, 4, 1)
                            .loop(true)
                            .maxAge(4 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.WARP -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.8F + rand.nextFloat() * 0.2F, 0.0F, 0.9F + rand.nextFloat() * 0.1F)
                            .alpha(0.7F)
                            .scale(0.6F + rand.nextFloat() * 0.4F)
                            .particles(PURPLE_START, 8, 1)
                            .loop(true)
                            .maxAge(10 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.UNDYING -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .motion(0.0, 0.03, 0.0)
                            .color(0.1F + rand.nextFloat() * 0.1F, 0.8F + rand.nextFloat() * 0.2F,
                                    0.1F + rand.nextFloat() * 0.1F)
                            .alpha(0.9F)
                            .scale(0.5F + rand.nextFloat() * 0.2F)
                            .particles(WISP_START, 4, 1)
                            .loop(true)
                            .maxAge(4 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.FIERY -> addParticle(level, x, y, z, FXGenericData.builder()
                    .motion(0.0, 0.03, 0.0)
                    .color(0.9F + rand.nextFloat() * 0.1F, 1.0F, 1.0F)
                    .alpha(0.7F)
                    .scale(0.7F + rand.nextFloat() * 0.2F)
                    .particles(SMOKE_START, 10, 1)
                    .maxAge(8 + rand.nextInt(4)));
            case ChampionModifier.SICKLY -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .motion(0.0, -0.02, 0.0)
                            .color(0.2F, 0.6F + rand.nextFloat() * 0.1F, 0.2F + rand.nextFloat() * 0.1F)
                            .alpha(0.5F)
                            .scale(0.9F + rand.nextFloat() * 0.3F)
                            .particles(BLOB_START, 4, 2)
                            .maxAge(5 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.VENOMOUS -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .motion(0.0, 0.02, 0.0)
                            .color(0.2F, 0.6F + rand.nextFloat() * 0.1F, 0.2F + rand.nextFloat() * 0.1F)
                            .alpha(0.7F)
                            .scale(0.5F + rand.nextFloat() * 0.2F)
                            .rotation(0.5F)
                            .particles(DRIP_START, 4, 1)
                            .maxAge(8 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.VAMPIRIC -> {
                if (rand.nextFloat() <= 0.2F) {
                    addParticle(level, x, y, z, FXGenericData.builder()
                            .color(0.9F + rand.nextFloat() * 0.1F, 0.0F, 0.0F)
                            .alpha(0.9F)
                            .scale(0.5F + rand.nextFloat() * 0.2F)
                            .particles(DRIP_START, 4, 1)
                            .maxAge(4 + rand.nextInt(4)));
                }
            }
            case ChampionModifier.INFESTED -> {
                if (rand.nextBoolean()) {
                    level.addParticle(TCParticles.TAINT_SPLOSION.get(),
                            mob.getX() + (rand.nextFloat() - 0.5) * mob.getBbWidth(),
                            (mob.getBoundingBox().minY + mob.getBoundingBox().maxY) / 2.0,
                            mob.getZ() + (rand.nextFloat() - 0.5) * mob.getBbWidth(),
                            0.0, 0.0, 0.0);
                }
            }
            case ChampionModifier.TAINTED -> addParticle(level, x, y, z, FXGenericData.builder()
                    .motion(0.0, -0.01, 0.0)
                    .color(0.1F + rand.nextFloat() * 0.2F, 0.0F, 0.1F + rand.nextFloat() * 0.1F)
                    .alpha(0.25F)
                    .scale(2.0F + rand.nextFloat())
                    .rotation(0.5F)
                    .layer(1)
                    .particles(BLOB_START, 5, 1)
                    .maxAge(6 + rand.nextInt(6)));
            default -> {
            }
        }
    }

    private static void addParticle(Level level, double x, double y, double z, FXGenericData.Builder builder) {
        level.addParticle(builder.build(), x, y, z, 0.0, 0.0, 0.0);
    }
}

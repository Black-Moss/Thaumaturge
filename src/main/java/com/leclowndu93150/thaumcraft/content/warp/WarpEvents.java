package com.leclowndu93150.thaumcraft.content.warp;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import com.leclowndu93150.thaumcraft.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumcraft.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WarpEvents {
    private static final int GRINNING_DEVIL_MASK = 0;
    private static final int GRINNING_DEVIL_BASE_REDUCTION = 2;
    private static final int GRINNING_DEVIL_RANDOM_REDUCTION = 4;

    private static final int EFFECT_AMP_DIVISOR = 15;
    private static final int MAX_EFFECT_AMP = 3;
    private static final int MAX_GUARDIANS = 8;
    private static final int MAX_SPIDERS = 50;
    private static final int SPAWN_ATTEMPTS = 50;

    private WarpEvents() {}

    public static void checkWarpEvent(ServerPlayer player) {
        WarpData wc = WarpManager.data(player);
        WarpManager.addWarp(player, -1, WarpType.TEMPORARY);
        int tw = wc.get(WarpType.TEMPORARY);
        int nw = wc.get(WarpType.NORMAL);
        int pw = wc.get(WarpType.PERMANENT);
        int warp = tw + nw + pw;
        int gearWarp = WarpManager.getWarpFromGear(player);
        warp += gearWarp;
        int warpCounter = wc.getCounter();
        RandomSource rand = player.getRandom();
        int r = rand.nextInt(100);
        if (warpCounter <= 0 || warp <= 0 || r > Math.sqrt(warpCounter)) {
            return;
        }
        warp = Math.min(100, (warp + warp + warpCounter) / 3);
        warpCounter = (int) (warpCounter - Math.max(5.0, Math.sqrt(warpCounter) * 2.0 - gearWarp * 2));
        wc.setCounter(warpCounter);
        int eff = rand.nextInt(warp) + gearWarp;
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helm.getItem() instanceof FortressArmorItem
                && FortressArmorItem.mask(helm) == GRINNING_DEVIL_MASK) {
            eff -= GRINNING_DEVIL_BASE_REDUCTION + rand.nextInt(GRINNING_DEVIL_RANDOM_REDUCTION);
        }
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.heartbeat());
        if (eff > 0) {
            dispatchEvent(player, eff, warp, nw, rand);
        }
    }

    private static void dispatchEvent(ServerPlayer player, int eff, int warp, int normalWarp, RandomSource rand) {
        ServerLevel level = player.level();
        if (eff <= 4) {
            if (!ThaumcraftCommonConfig.NO_STRESS.get()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, 1.0F, 0.5F);
            }
        } else if (eff <= 8) {
            if (!ThaumcraftCommonConfig.NO_STRESS.get()) {
                level.playSound(null,
                        player.getX() + rand.nextInt(10) - rand.nextInt(10),
                        player.getY() + rand.nextInt(10) - rand.nextInt(10),
                        player.getZ() + rand.nextInt(10) - rand.nextInt(10),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 4.0F,
                        (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);
            }
        } else if (eff <= 12) {
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.11");
        } else if (eff <= 16) {
            applyEffect(player, TCMobEffects.VIS_EXHAUST, 5000, ampFor(warp), "warp.thaumcraft.text.1");
        } else if (eff <= 20) {
            applyEffect(player, TCMobEffects.THAUMARHIA, Math.min(32000, 10 * warp), 0, "warp.thaumcraft.text.15");
        } else if (eff <= 24) {
            applyEffect(player, TCMobEffects.UNNATURAL_HUNGER, 5000, ampFor(warp), "warp.thaumcraft.text.2");
        } else if (eff <= 28) {
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.12");
        } else if (eff <= 32) {
            spawnMist(player, 1);
        } else if (eff <= 36) {
            applyEffect(player, TCMobEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0, null);
        } else if (eff <= 40) {
            applyEffect(player, TCMobEffects.SUN_SCORNED, 5000, ampFor(warp), "warp.thaumcraft.text.5");
        } else if (eff <= 44) {
            applyEffect(player, MobEffects.MINING_FATIGUE, 1200, ampFor(warp), "warp.thaumcraft.text.9");
        } else if (eff <= 48) {
            applyEffect(player, TCMobEffects.INFECTIOUS_VIS_EXHAUST, 6000, ampFor(warp), "warp.thaumcraft.text.1");
        } else if (eff <= 52) {
            applyEffect(player, MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, "warp.thaumcraft.text.10");
        } else if (eff <= 56) {
            applyEffect(player, TCMobEffects.DEATH_GAZE, 6000, ampFor(warp), "warp.thaumcraft.text.4");
        } else if (eff <= 60) {
            suddenlySpiders(player, warp, false);
        } else if (eff <= 64) {
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.13");
        } else if (eff <= 68) {
            spawnMist(player, warp / 30);
        } else if (eff <= 72) {
            applyEffect(player, MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, null);
        } else if (eff == 76) {
            if (normalWarp > 0) {
                WarpManager.addWarp(player, -1, WarpType.NORMAL);
            }
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.14");
        } else if (eff <= 80) {
            applyEffect(player, TCMobEffects.UNNATURAL_HUNGER, 6000, ampFor(warp), "warp.thaumcraft.text.2");
        } else if (eff <= 88) {
            GuardianSpawner.spawnPortal(player);
        } else if (eff <= 92) {
            suddenlySpiders(player, warp, true);
        } else {
            spawnMist(player, warp / 15);
        }
    }

    private static int ampFor(int warp) {
        return Math.min(MAX_EFFECT_AMP, warp / EFFECT_AMP_DIVISOR);
    }

    private static void applyEffect(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier,
                                    String messageKey) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        if (messageKey != null) {
            WarpManager.sendActionBar(player, messageKey);
        }
    }

    private static void spawnMist(ServerPlayer player, int guardians) {
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.mist());
        int count = Math.min(MAX_GUARDIANS, guardians);
        GuardianSpawner.spawn(player, count);
        WarpManager.sendActionBar(player, "warp.thaumcraft.text.6");
    }

    private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
        ServerLevel level = player.level();
        RandomSource rand = player.getRandom();
        int spawns = Math.min(MAX_SPIDERS, warp);
        for (int i = 0; i < spawns; i++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                EntityMindSpider spider = TCEntities.MIND_SPIDER.get().create(level, EntitySpawnReason.EVENT);
                if (spider == null) {
                    return;
                }
                double x = player.getX() + rand.nextInt(15) - rand.nextInt(15);
                double y = player.getY() + rand.nextInt(5) - rand.nextInt(5);
                double z = player.getZ() + rand.nextInt(15) - rand.nextInt(15);
                spider.snapTo(x, y, z, rand.nextFloat() * 360.0F, 0.0F);
                BlockPos below = spider.blockPosition().below();
                if (!level.getBlockState(below).isCollisionShapeFullBlock(level, below)
                        || !level.noCollision(spider)
                        || level.containsAnyLiquid(spider.getBoundingBox())) {
                    spider.discard();
                    continue;
                }
                spider.setTarget(player);
                if (!real) {
                    spider.setViewer(player.getGameProfile().name());
                    spider.setHarmless(true);
                }
                level.addFreshEntity(spider);
                break;
            }
        }
        WarpManager.sendActionBar(player, "warp.thaumcraft.text.7");
    }

    public static void checkDeathGaze(ServerPlayer player) {
        MobEffectInstance gaze = player.getEffect(TCMobEffects.DEATH_GAZE);
        if (gaze == null) {
            return;
        }
        ServerLevel level = player.level();
        int range = Math.min(8 + gaze.getAmplifier() * 3, 24);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range))) {
            if (entity == player || !entity.isAlive() || entity.hasEffect(MobEffects.WITHER)) {
                continue;
            }
            if (entity instanceof ServerPlayer && !level.isPvpAllowed()) {
                continue;
            }
            if (!player.hasLineOfSight(entity)) {
                continue;
            }
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            if (entity instanceof Mob mob) {
                mob.setTarget(player);
            }
        }
    }
}

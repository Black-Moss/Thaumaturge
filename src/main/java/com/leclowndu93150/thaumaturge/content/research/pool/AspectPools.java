package com.leclowndu93150.thaumaturge.content.research.pool;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectGainPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundUpdateJEIAspectListPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AspectPools {
    public static final int SOFT_CAP = 100;

    private static final double HARD_CAP_THRESHOLD = SOFT_CAP * 1.25;
    private static final int FIRST_DISCOVERY_BONUS = 2;
    private static final String MESSAGE_PREFIX = "message.thaumaturge.research.";
    private static final String DISCOVERY_ERROR_KEY = MESSAGE_PREFIX + "discovery_error";
    private static final String DISCOVERY_DERIVE_KEY = DISCOVERY_ERROR_KEY + ".derive";
    private static final String ASPECT_DISCOVERED_KEY = MESSAGE_PREFIX + "aspect_discovered";
    private static final int STARTING_LOW = 15;
    private static final int STARTING_HIGH = 19;
    private static final float PICKUP_VOLUME = 0.2F;
    private static final float PICKUP_PITCH_FLOOR = 0.9F;
    private static final float PICKUP_PITCH_RANGE = 0.2F;
    private static final float LEARN_VOLUME = 0.5F;
    private static final float LEARN_PITCH = 1.0F;
    private static final List<ResourceKey<IAspect>> STARTING_PRIMALS = List.of(TTAspects.AER, TTAspects.TERRA, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.ORDO, TTAspects.PERDITIO);

    private AspectPools() {}

    public static AspectPoolData data(Player player) {
        return player.getData(TTAttachments.ASPECT_POOL);
    }

    public static void sync(ServerPlayer player) {
        data(player).markSyncPending();
    }

    public static void flush(ServerPlayer player) {
        if (!data(player).takeSyncPending()) {
            return;
        }
        player.syncData(TTAttachments.ASPECT_POOL);
        PacketDistributor.sendToPlayer(player, ClientboundUpdateJEIAspectListPayload.INSTANCE);
    }

    public static void seedIfNew(ServerPlayer player) {
        AspectPoolData pool = data(player);
        if (pool.isEmpty()) {
            STARTING_PRIMALS.forEach(primal -> pool.add(primal.identifier(), rollStartingAmount(player)));
            sync(player);
        }
    }

    private static int rollStartingAmount(ServerPlayer player) {
        return player.getRandom().nextIntBetweenInclusive(STARTING_LOW, STARTING_HIGH);
    }

    public static boolean isDiscovered(Player player, Holder<IAspect> aspect) {
        return aspect.value().isPrimal() || data(player).isDiscovered(idOf(aspect));
    }

    public static int amount(Player player, Holder<IAspect> aspect) {
        return data(player).amount(idOf(aspect));
    }

    public static boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect) {
        return aspect.value().components().stream().allMatch(component -> isDiscovered(player, component));
    }

    public static int grant(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        AspectPoolData pool = data(player);
        Identifier id = idOf(aspect);
        boolean isNew = !isDiscovered(player, aspect);
        int requested = isNew ? amount + FIRST_DISCOVERY_BONUS : amount;
        int applied = reduceForCap(pool.amount(id), requested);
        if (applied <= 0 && !isNew) {
            return 0;
        }
        credit(pool, id, Math.max(applied, 0));
        sync(player);
        if (isNew) {
            Component announcement = Component.translatable(ASPECT_DISCOVERED_KEY, AspectComponents.trueName(aspect)).withStyle(ChatFormatting.DARK_PURPLE);
            player.sendSystemMessage(announcement);
        }
        if (applied > 0) {
            if (pool.tryClaimGrantSound(player.level().getGameTime())) {
                playPickup(player);
            }
            sendGain(player, id, applied);
        }
        return applied;
    }

    private static int reduceForCap(int stored, int wanted) {
        if (stored >= HARD_CAP_THRESHOLD) {
            return Math.sqrt(wanted) >= 1 ? 1 : 0;
        }
        return stored >= SOFT_CAP ? (int) Math.sqrt(Math.max(wanted, 0)) : wanted;
    }

    public static void grantAll(ServerPlayer player, AspectList aspects) {
        for (AspectInstance instance : aspects.entries()) {
            Holder<IAspect> aspect = instance.aspect();
            if (hasDiscoveredComponents(player, aspect)) {
                grant(player, aspect, instance.amount());
            } else {
                notifyMissingComponent(player, aspect);
            }
        }
    }

    public static void notifyMissingComponent(ServerPlayer player, Holder<IAspect> aspect) {
        MutableComponent hint = missingComponentHint(player, aspect);
        if (hint != null) {
            player.sendSystemMessage(hint.withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static @Nullable MutableComponent missingComponentHint(Player player, Holder<IAspect> aspect) {
        return aspect.value().components().stream().filter(component -> !isDiscovered(player, component)).findFirst().map(component -> missingComponentMessage(player, component)).orElse(null);
    }

    public static MutableComponent missingComponentMessage(Player player, Holder<IAspect> component) {
        boolean derivable = !component.value().isPrimal() && hasDiscoveredComponents(player, component);
        return derivable ? Component.translatable(DISCOVERY_DERIVE_KEY, AspectComponents.composition(component)) : Component.translatable(DISCOVERY_ERROR_KEY, AspectComponents.help(component));
    }

    public static boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        if (amount(player, aspect) < amount) {
            return false;
        }
        takeFromPool(player, aspect, amount);
        return true;
    }

    public static boolean canAfford(Player player, AspectList cost) {
        return cost.entries().stream().noneMatch(entry -> amount(player, entry.aspect()) < entry.amount());
    }

    public static boolean spendAll(ServerPlayer player, AspectList cost) {
        if (!canAfford(player, cost)) {
            return false;
        }
        cost.entries().forEach(entry -> data(player).add(idOf(entry.aspect()), -entry.amount()));
        sync(player);
        return true;
    }

    public static void refund(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        data(player).add(idOf(aspect), amount);
        sync(player);
        playPickup(player);
    }

    public static int grantAllForCommand(ServerPlayer player, int amount) {
        AspectPoolData pool = data(player);
        List<Holder.Reference<IAspect>> everyAspect = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList();
        applyAndSync(player, everyAspect, id -> credit(pool, id, amount));
        everyAspect.stream().map(reference -> reference.key().identifier()).findFirst().ifPresent(lead -> sendGain(player, lead, 0));
        playLearn(player);
        return everyAspect.size();
    }

    public static void grantForCommand(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        Identifier id = idOf(aspect);
        credit(data(player), id, amount);
        sync(player);
        sendGain(player, id, amount);
        playLearn(player);
    }

    public static void setForCommand(ServerPlayer player, Collection<? extends Holder<IAspect>> aspects, int amount) {
        AspectPoolData pool = data(player);
        applyAndSync(player, aspects, id -> assign(pool, id, amount));
    }

    public static void takeForCommand(ServerPlayer player, Collection<? extends Holder<IAspect>> aspects, int amount) {
        AspectPoolData pool = data(player);
        applyAndSync(player, aspects, id -> pool.add(id, -amount));
    }

    public static void discoverForCommand(ServerPlayer player, Collection<? extends Holder<IAspect>> aspects) {
        applyAndSync(player, aspects, data(player)::discover);
    }

    public static void resetForCommand(ServerPlayer player) {
        data(player).copyFrom(new AspectPoolData());
        seedIfNew(player);
    }

    public static Identifier idOf(Holder<IAspect> aspect) {
        return keyId(aspect);
    }

    private static Identifier keyId(Holder<?> holder) {
        return holder.unwrapKey().orElseThrow().identifier();
    }

    private static void takeFromPool(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        data(player).add(idOf(aspect), -amount);
        sync(player);
    }

    private static void sendGain(ServerPlayer player, Identifier id, int amount) {
        PacketDistributor.sendToPlayer(player, new ClientboundAspectGainPayload(id, amount));
    }

    private static void assign(AspectPoolData pool, Identifier id, int target) {
        pool.discover(id);
        pool.add(id, target - pool.amount(id));
    }

    private static void credit(AspectPoolData pool, Identifier id, int amount) {
        pool.add(id, amount);
        pool.discover(id);
    }

    private static void applyAndSync(ServerPlayer player, Collection<? extends Holder<?>> aspects, Consumer<Identifier> action) {
        for (Holder<?> aspect : aspects) {
            action.accept(keyId(aspect));
        }
        sync(player);
    }

    private static void playPickup(ServerPlayer player) {
        float pitch = PICKUP_PITCH_FLOOR + player.getRandom().nextFloat() * PICKUP_PITCH_RANGE;
        emit(player, SoundEvents.EXPERIENCE_ORB_PICKUP, PICKUP_VOLUME, pitch);
    }

    private static void playLearn(ServerPlayer player) {
        emit(player, TTSounds.LEARN.get(), LEARN_VOLUME, LEARN_PITCH);
    }

    private static void emit(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}

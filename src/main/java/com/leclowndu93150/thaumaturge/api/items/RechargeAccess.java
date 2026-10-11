package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade over the vis charge of rechargeable stacks.
 *
 * <p>A stack is rechargeable exactly when it carries a {@link ChargeProfile}. The charge is held in the {@code thaumaturge:charge}
 * component as a whole number of vis, defaults to zero and is never removed by this class. Every mutation changes the passed stack in
 * place and sends no sync; the normal container update carries the new component to the client. The query members are safe on both
 * sides, and {@link #drawFromAura} must run on the logical server.
 *
 * @since 1.0.0
 */
public final class RechargeAccess {
    /**
     * The value the 1.0 query members return for a stack that is not rechargeable.
     *
     * @deprecated test with {@link #isRechargeable(ItemStack)} instead
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final int NOT_RECHARGEABLE = -1;

    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RechargeAccess");
    private static final int NO_CHARGE = 0;

    private RechargeAccess() {}

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        BINDING.bind(impl);
    }

    /**
     * @param stack the stack to inspect
     * @return the stack's charge profile, or null for an empty stack or a stack without one
     */
    public static @Nullable ChargeProfile profile(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(BINDING.get().profile());
    }

    /**
     * @param stack the stack to test
     * @return whether the stack carries a charge profile; false for an empty stack
     */
    public static boolean isRechargeable(ItemStack stack) {
        return profile(stack) != null;
    }

    /**
     * @param stack the stack to read
     * @return the stored vis; zero for a stack that was never charged or is not rechargeable
     * @apiNote Use {@link #isRechargeable(ItemStack)} to tell an empty store from a stack that has none.
     * @since 1.1.0
     */
    public static int storedVis(ItemStack stack) {
        return isRechargeable(stack) ? storedCharge(stack) : NO_CHARGE;
    }

    /**
     * @param stack the stack to read
     * @return how much more vis the stack can take before reaching its capacity; zero when full, above capacity or not rechargeable
     * @since 1.1.0
     */
    public static int roomFor(ItemStack stack) {
        ChargeProfile profile = profile(stack);
        return profile == null ? NO_CHARGE : Math.max(NO_CHARGE, profile.capacity() - storedCharge(stack));
    }

    /**
     * @param stack the stack to read
     * @return the stored vis divided by the capacity, which exceeds one when the stored charge is above capacity; zero for a stack that
     *         is not rechargeable
     * @since 1.1.0
     */
    public static float fillFraction(ItemStack stack) {
        ChargeProfile profile = profile(stack);
        return profile == null ? NO_CHARGE : (float) storedCharge(stack) / profile.capacity();
    }

    /**
     * Tops a stack up from the vis aura at a position. The request is first limited to the room left in the stack. When a player is
     * given and the aura there must be preserved for that player, nothing is drained. Only whole vis is drained from the aura, so no
     * fraction is lost, and exactly what is drained is added.
     *
     * @param level  the level holding the aura
     * @param stack  the stack to recharge, changed in place
     * @param pos    the position whose aura is drained
     * @param player the player the aura may have to be preserved for, or null to skip that check
     * @param amount the most vis to add; zero or negative adds nothing
     * @return the vis added; zero when the stack is not rechargeable or full, the aura is preserved or empty, or on the client
     * @since 1.1.0
     */
    public static int drawFromAura(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        int request = Math.min(amount, roomFor(stack));
        if (level.isClientSide() || request <= 0) {
            return NO_CHARGE;
        }
        if (player != null && AuraHelper.shouldPreserveAura(level, player, pos)) {
            return NO_CHARGE;
        }
        int available = (int) AuraHelper.drainVis(level, pos, request, true);
        if (available <= 0) {
            return NO_CHARGE;
        }
        int drained = Math.round(AuraHelper.drainVis(level, pos, available, false));
        if (drained > 0) {
            stack.set(BINDING.get().charge(), storedCharge(stack) + drained);
        }
        return Math.max(NO_CHARGE, drained);
    }

    /**
     * Adds charge without touching the aura, limited to the room left in the stack.
     *
     * @param stack  the stack to charge, changed in place
     * @param amount the most vis to add; zero or negative adds nothing
     * @return the vis added; never negative, and zero for a stack that is not rechargeable, full or above capacity
     * @since 1.1.0
     */
    public static int addVis(ItemStack stack, int amount) {
        int added = Math.min(amount, roomFor(stack));
        if (added <= 0) {
            return NO_CHARGE;
        }
        stack.set(BINDING.get().charge(), storedCharge(stack) + added);
        return added;
    }

    /**
     * Pays a cost from the stored charge. All or nothing.
     *
     * @param stack the stack to charge against, changed in place on success
     * @param cost  the vis to deduct; a negative cost is refused
     * @return whether the cost was paid; false leaves the stack unchanged, which includes a stack that is not rechargeable, a charge
     *         below the cost and a negative cost
     * @since 1.1.0
     */
    public static boolean spendVis(ItemStack stack, int cost) {
        if (cost < 0 || !isRechargeable(stack)) {
            return false;
        }
        int stored = storedCharge(stack);
        if (stored < cost) {
            return false;
        }
        if (cost > 0) {
            stack.set(BINDING.get().charge(), stored - cost);
        }
        return true;
    }

    /**
     * @param level  the level holding the aura
     * @param stack  the stack to recharge
     * @param pos    the position whose aura is drained
     * @param player the player the aura may have to be preserved for, or null
     * @param amount the most vis to add
     * @return the vis added
     * @deprecated use {@link #drawFromAura(Level, ItemStack, BlockPos, Player, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        return drawFromAura(level, stack, pos, player, amount);
    }

    /**
     * @param stack  the stack to recharge
     * @param entity unused
     * @param amount the most vis to add
     * @return the vis added; never negative
     * @deprecated use {@link #addVis(ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float rechargeItemBlindly(ItemStack stack, @Nullable LivingEntity entity, int amount) {
        return addVis(stack, amount);
    }

    /**
     * @param stack the stack to read
     * @return the stored vis, or {@link #NOT_RECHARGEABLE} for a stack that is not rechargeable
     * @deprecated use {@link #storedVis(ItemStack)} together with {@link #isRechargeable(ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int getCharge(ItemStack stack) {
        return isRechargeable(stack) ? storedCharge(stack) : NOT_RECHARGEABLE;
    }

    /**
     * @param stack  the stack to read
     * @param entity unused
     * @return the fill fraction, or {@link #NOT_RECHARGEABLE} for a stack that is not rechargeable
     * @deprecated use {@link #fillFraction(ItemStack)} together with {@link #isRechargeable(ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float getChargePercentage(ItemStack stack, @Nullable LivingEntity entity) {
        return isRechargeable(stack) ? fillFraction(stack) : NOT_RECHARGEABLE;
    }

    /**
     * @param stack  the stack to charge against
     * @param entity unused
     * @param cost   the vis to deduct; a negative cost is refused
     * @return whether the cost was paid
     * @deprecated use {@link #spendVis(ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeCharge(ItemStack stack, @Nullable LivingEntity entity, int cost) {
        return spendVis(stack, cost);
    }

    private static int storedCharge(ItemStack stack) {
        return stack.getOrDefault(BINDING.get().charge(), NO_CHARGE);
    }

    /**
     * The component types Thaumaturge supplies behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @return the {@code thaumaturge:charge} component type
         */
        DataComponentType<Integer> charge();

        /**
         * @return the {@code thaumaturge:rechargeable} component type
         */
        DataComponentType<ChargeProfile> profile();
    }
}

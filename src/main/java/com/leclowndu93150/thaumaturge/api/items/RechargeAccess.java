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
 * <p>A stack is rechargeable exactly when it carries a {@link ChargeProfile}. The charge lives in the {@code thaumaturge:charge}
 * component, defaults to zero and is never removed by this class. Every mutation changes the passed stack in place and sends no
 * sync; the normal container update carries the new component to the client. The query members are safe on both sides, and
 * {@link #rechargeItem} must run on the logical server.
 *
 * @since 1.0.0
 */
public final class RechargeAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RechargeAccess");
    private static final int NOT_RECHARGEABLE = -1;
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
     * Drains vis from the aura at a position into the stack. The drain is real. Vis is drained as a fraction and rounded down to a
     * whole number when added, so a fractional remainder is lost.
     *
     * @param level  the level holding the aura; the logical server
     * @param stack  the stack to recharge, changed in place
     * @param pos    the position whose aura is drained
     * @param player the player the aura must be preserved for, or null
     * @param amount the most vis to add; zero or negative adds nothing
     * @return the vis added, zero when the stack is not rechargeable, the aura is preserved or nothing was drained
     */
    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        ChargeProfile profile = profile(stack);
        if (profile == null || amount <= 0 || (player != null && AuraHelper.shouldPreserveAura(level, player, pos))) {
            return NO_CHARGE;
        }
        int charge = storedCharge(stack);
        int room = profile.capacity() - charge;
        if (room <= 0) {
            return NO_CHARGE;
        }
        int gained = Math.min((int) AuraHelper.drainVis(level, pos, Math.min(amount, room), false), room);
        if (gained <= 0) {
            return NO_CHARGE;
        }
        stack.set(BINDING.get().charge(), charge + gained);
        return gained;
    }

    /**
     * Adds charge without touching the aura.
     *
     * @param stack  the stack to recharge, changed in place
     * @param entity unused
     * @param amount the vis to add
     * @return the vis added when positive; zero or negative when the stack is unchanged, which includes a non-rechargeable stack, a
     *         stored charge above capacity and a negative request
     */
    public static float rechargeItemBlindly(ItemStack stack, @Nullable LivingEntity entity, int amount) {
        ChargeProfile profile = profile(stack);
        if (profile == null) {
            return NO_CHARGE;
        }
        int charge = storedCharge(stack);
        int added = Math.min(amount, profile.capacity() - charge);
        if (added > 0) {
            stack.set(BINDING.get().charge(), charge + added);
        }
        return added;
    }

    /**
     * @param stack the stack to read
     * @return the stored vis, zero when none is stored, or minus one for a non-rechargeable stack
     */
    public static int getCharge(ItemStack stack) {
        return isRechargeable(stack) ? storedCharge(stack) : NOT_RECHARGEABLE;
    }

    /**
     * @param stack  the stack to read
     * @param entity unused
     * @return the stored charge divided by capacity, which may exceed one, or minus one for a non-rechargeable stack
     */
    public static float getChargePercentage(ItemStack stack, @Nullable LivingEntity entity) {
        ChargeProfile profile = profile(stack);
        return profile == null ? NOT_RECHARGEABLE : (float) storedCharge(stack) / profile.capacity();
    }

    /**
     * Pays a cost from the stored charge. Negative costs are not guarded.
     *
     * @param stack  the stack to charge against, changed in place on success
     * @param entity unused
     * @param cost   the vis to deduct
     * @return whether the cost was paid; false leaves the stack unchanged
     */
    public static boolean consumeCharge(ItemStack stack, @Nullable LivingEntity entity, int cost) {
        if (!isRechargeable(stack)) {
            return false;
        }
        int charge = storedCharge(stack);
        if (charge < NO_CHARGE || charge < cost) {
            return false;
        }
        stack.set(BINDING.get().charge(), charge - cost);
        return true;
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

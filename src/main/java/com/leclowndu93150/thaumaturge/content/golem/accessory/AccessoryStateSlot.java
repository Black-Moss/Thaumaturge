package com.leclowndu93150.thaumaturge.content.golem.accessory;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryContext;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record AccessoryStateSlot<S>(GolemAccessory accessory, GolemAccessoryBehavior<S> behavior, S state) {

    public AccessoryStateSlot {
        Objects.requireNonNull(state, "state");
    }

    public static <S> AccessoryStateSlot<S> attached(GolemAccessoryContext context, GolemAccessoryBehavior<S> behavior, ItemStack attachedStack) {
        return new AccessoryStateSlot<>(context.accessory(), behavior, behavior.onAttach(context, behavior.initialState(), attachedStack));
    }

    public static <S> AccessoryStateSlot<S> load(GolemAccessory accessory, GolemAccessoryBehavior<S> behavior, ValueInput input) {
        return new AccessoryStateSlot<>(accessory, behavior, input.read(accessory.id().toString(), behavior.stateCodec()).orElseGet(behavior::initialState));
    }

    public static <S> AccessoryStateSlot<S> decode(GolemAccessory accessory, GolemAccessoryBehavior<S> behavior, RegistryFriendlyByteBuf buf) {
        return new AccessoryStateSlot<>(accessory, behavior, syncCodec(behavior).decode(buf));
    }

    public AccessoryStateSlot<S> tick(GolemAccessoryContext context) {
        return with(behavior.serverTick(context, state));
    }

    public void remove(GolemAccessoryContext context, ItemStack returnedStack) {
        behavior.onRemove(context, state, returnedStack);
    }

    public void save(ValueOutput output) {
        output.store(accessory.id().toString(), behavior.stateCodec(), state);
    }

    public boolean synced() {
        return behavior.syncCodec() != null;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        syncCodec(behavior).encode(buf, state);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> stateFor(GolemAccessoryBehavior<T> key) {
        return key == behavior ? Optional.of((T) state) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> AccessoryStateSlot<S> updated(GolemAccessoryBehavior<T> key, UnaryOperator<T> update) {
        return key == behavior ? with((S) update.apply((T) state)) : this;
    }

    private AccessoryStateSlot<S> with(S next) {
        Objects.requireNonNull(next, "accessory state");
        return next.equals(state) ? this : new AccessoryStateSlot<>(accessory, behavior, next);
    }

    private static <S> StreamCodec<RegistryFriendlyByteBuf, S> syncCodec(GolemAccessoryBehavior<S> behavior) {
        StreamCodec<RegistryFriendlyByteBuf, S> codec = behavior.syncCodec();
        if (codec == null) {
            throw new IllegalStateException("Golem accessory behaviour has no sync codec");
        }
        return codec;
    }
}

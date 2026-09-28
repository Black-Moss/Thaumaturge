package com.leclowndu93150.thaumaturge.content.golem.accessory;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class GolemAccessoryStateHolder {
    private final IGolemAPI golem;
    private final Map<Identifier, AccessoryStateSlot<?>> slots = new LinkedHashMap<>();
    private final Map<Identifier, GolemAccessoryContext> contexts = new LinkedHashMap<>();

    public GolemAccessoryStateHolder(IGolemAPI golem) {
        this.golem = golem;
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public void attach(GolemAccessory accessory, ItemStack attachedStack) {
        accessory.behavior().ifPresent(behavior -> slots.put(accessory.id(), AccessoryStateSlot.attached(context(accessory), behavior, attachedStack)));
    }

    public void remove(GolemAccessory accessory, ItemStack returnedStack) {
        AccessoryStateSlot<?> slot = slots.remove(accessory.id());
        if (slot != null) {
            slot.remove(context(accessory), returnedStack);
        }
        contexts.remove(accessory.id());
    }

    public void clear() {
        slots.clear();
        contexts.clear();
    }

    public boolean tick() {
        if (slots.isEmpty()) {
            return false;
        }
        boolean syncedChanged = false;
        for (Map.Entry<Identifier, AccessoryStateSlot<?>> entry : slots.entrySet()) {
            AccessoryStateSlot<?> slot = entry.getValue();
            AccessoryStateSlot<?> next = slot.tick(context(slot.accessory()));
            if (next != slot) {
                entry.setValue(next);
                syncedChanged |= next.synced();
            }
        }
        return syncedChanged;
    }

    public <S> Optional<S> state(GolemAccessoryBehavior<S> behavior) {
        for (AccessoryStateSlot<?> slot : slots.values()) {
            Optional<S> state = slot.stateFor(behavior);
            if (state.isPresent()) {
                return state;
            }
        }
        return Optional.empty();
    }

    public <S> boolean update(GolemAccessoryBehavior<S> behavior, UnaryOperator<S> update) {
        for (Map.Entry<Identifier, AccessoryStateSlot<?>> entry : slots.entrySet()) {
            if (entry.getValue().behavior() == behavior) {
                entry.setValue(entry.getValue().updated(behavior, update));
                return true;
            }
        }
        return false;
    }

    public GolemAccessoryStates synced() {
        return new GolemAccessoryStates(slots.values().stream().filter(AccessoryStateSlot::synced).toList());
    }

    public void save(ValueOutput output) {
        for (AccessoryStateSlot<?> slot : slots.values()) {
            slot.save(output);
        }
    }

    public void load(ValueInput input, List<GolemAccessory> worn) {
        clear();
        for (GolemAccessory accessory : worn) {
            accessory.behavior().ifPresent(behavior -> slots.put(accessory.id(), AccessoryStateSlot.load(accessory, behavior, input)));
        }
    }

    private GolemAccessoryContext context(GolemAccessory accessory) {
        GolemAccessoryContext context = contexts.get(accessory.id());
        if (context == null) {
            context = new GolemAccessoryContext(golem, accessory);
            contexts.put(accessory.id(), context);
        }
        return context;
    }
}

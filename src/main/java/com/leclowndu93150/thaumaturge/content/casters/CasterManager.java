package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat.CurioPouchRef;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

public final class CasterManager {
    public static final String REMOVE_FOCUS = "REMOVE";

    private static final int EXHAUST_PENALTY_PER_LEVEL = 10;
    private static final int NO_EFFECT = -1;
    private static final float PERCENT = 100.0F;
    private static final float FOCUS_SWAP_VOLUME = 0.3F;
    private static final float SWAP_PITCH = 1.0F;
    private static final float REMOVAL_PITCH = 0.9F;
    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_AREA = 3;
    private static final int MODE_ALL = 0;
    private static final int MODE_X = 1;
    private static final int MODE_Z = 2;
    private static final int MODE_Y = 3;
    private static final int MODE_COUNT = 4;
    private static final int[] AXIS_MODES = {MODE_X, MODE_Y, MODE_Z};
    private static final int KEY_GROW_EXTENTS = 0;
    private static final int KEY_CYCLE_MODE = 1;

    private CasterManager() {}

    public static float visDiscountOf(@Nullable Player player) {
        return player == null ? 0.0F : netDiscount(player);
    }

    private static float netDiscount(Player player) {
        int gearPercent = Math.round((float) player.getAttributeValue(TTAttributes.VIS_DISCOUNT) * PERCENT);
        int netPercent = gearPercent - exhaustPenalty(player);
        return netPercent / PERCENT;
    }

    private static int exhaustPenalty(Player player) {
        int highest = Math.max(amplifier(player.getEffect(TTMobEffects.VIS_EXHAUST)), amplifier(player.getEffect(TTMobEffects.INFECTIOUS_VIS_EXHAUST)));
        return highest == NO_EFFECT ? 0 : EXHAUST_PENALTY_PER_LEVEL * (highest + 1);
    }

    private static int amplifier(@Nullable MobEffectInstance effect) {
        return effect == null ? NO_EFFECT : effect.getAmplifier();
    }

    public static float cooldownSeconds(LivingEntity entity) {
        float remaining = (float) (entity.getData(TTAttachments.CASTER_COOLDOWN) - entity.level().getGameTime());
        return Math.max(remaining, 0.0F) / TICKS_PER_SECOND;
    }

    public static boolean isCoolingDown(LivingEntity entity) {
        return cooldownSeconds(entity) > 0.0F;
    }

    public static void startCooldown(LivingEntity entity, int ticks) {
        long deadline = 0L;
        if (ticks != 0) {
            deadline = entity.level().getGameTime() + ticks;
        }
        entity.setData(TTAttachments.CASTER_COOLDOWN, deadline);
    }

    public static boolean drawVisFromPack(Player player, float cost) {
        Inventory inventory = player.getInventory();
        return IntStream.range(0, Inventory.INVENTORY_SIZE).anyMatch(slot -> canPay(inventory.getItem(slot), player, cost));
    }

    private static boolean canPay(ItemStack stack, Player player, float cost) {
        return stack.getItem() instanceof ICaster caster && caster.consumeVis(stack, player, cost, true, false);
    }

    public static void applyFocusChoice(ItemStack casterStack, Level level, Player player, @Nullable String focusKey) {
        if (level.isClientSide() || !(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        TreeMap<String, FocusLocation> carried = carriedFoci(player);
        if (carried.isEmpty() || REMOVE_FOCUS.equals(focusKey)) {
            removeFocus(casterStack, caster, level, player);
            return;
        }
        selectFocus(casterStack, caster, level, player, choose(carried, focusKey, caster.getFocusStack(casterStack)));
    }

    private static FocusLocation choose(TreeMap<String, FocusLocation> carried, @Nullable String focusKey, ItemStack equipped) {
        FocusLocation exact = focusKey == null ? null : carried.get(focusKey);
        if (exact != null) {
            return exact;
        }
        String base = focusKey != null ? focusKey : equipped.isEmpty() ? null : FocusItems.sortKey(equipped);
        Map.Entry<String, FocusLocation> next = base == null ? null : carried.higherEntry(base);
        return (next != null ? next : carried.firstEntry()).getValue();
    }

    private static void removeFocus(ItemStack casterStack, ICaster caster, Level level, Player player) {
        ItemStack equipped = caster.getFocusStack(casterStack);
        if (equipped.isEmpty() || !storeAway(player, equipped.copy())) {
            return;
        }
        caster.setFocus(casterStack, ItemStack.EMPTY);
        playSwapSound(level, player, REMOVAL_PITCH);
    }

    private static void selectFocus(ItemStack casterStack, ICaster caster, Level level, Player player, FocusLocation chosen) {
        ItemStack picked = chosen.get();
        if (!FocusItems.isFocus(picked)) {
            return;
        }
        ItemStack previous = caster.getFocusStack(casterStack).copy();
        chosen.set(ItemStack.EMPTY);
        if (previous.isEmpty() || storeAway(player, previous)) {
            equip(casterStack, caster, level, player, picked);
            return;
        }
        chosen.set(picked);
    }

    private static void equip(ItemStack casterStack, ICaster caster, Level level, Player player, ItemStack focus) {
        caster.setFocus(casterStack, focus);
        playSwapSound(level, player, SWAP_PITCH);
    }

    private static void playSwapSound(Level level, Player player, float pitch) {
        level.playSound(null, player, TTSounds.TICKS.get(), SoundSource.PLAYERS, FOCUS_SWAP_VOLUME, pitch);
    }

    private static TreeMap<String, FocusLocation> carriedFoci(Player player) {
        TreeMap<String, FocusLocation> carried = new TreeMap<>();
        Stream.concat(Stream.of(new InventoryStore(player.getInventory())), pouchStores(player).stream()).forEach(store -> store.slots().forEach(slot -> register(carried, store, slot)));
        return carried;
    }

    private static void register(TreeMap<String, FocusLocation> carried, FocusStore store, int slot) {
        ItemStack stack = store.get(slot);
        if (!FocusItems.isFocus(stack)) {
            return;
        }
        String key = FocusItems.sortKey(stack);
        if (key != null) {
            carried.putIfAbsent(key, new FocusLocation(store, slot));
        }
    }

    private static List<FocusStore> pouchStores(Player player) {
        Inventory inventory = player.getInventory();
        List<FocusStore> stores = IntStream.range(0, Inventory.INVENTORY_SIZE).filter(slot -> isPouch(inventory.getItem(slot))).<FocusStore>mapToObj(slot -> pouchInInventory(inventory, slot))
                .collect(Collectors.toCollection(ArrayList::new));
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            ThaumaturgeCuriosCompat.equippedPouches(player, CasterManager::isPouch).forEach(ref -> stores.add(pouchWorn(ref)));
        }
        return stores;
    }

    private static PouchStore pouchInInventory(Inventory inventory, int slot) {
        return new PouchStore(() -> inventory.getItem(slot), pouch -> inventory.setChanged());
    }

    private static PouchStore pouchWorn(CurioPouchRef ref) {
        return new PouchStore(ref::stack, pouch -> ref.handler().setStackInSlot(ref.slot(), pouch.copy()));
    }

    private static boolean isPouch(ItemStack stack) {
        return stack.is(TTItems.FOCUS_POUCH.get());
    }

    private static boolean storeAway(Player player, ItemStack focus) {
        List<FocusStore> targets = new ArrayList<>(pouchStores(player));
        targets.add(new InventoryStore(player.getInventory()));
        return targets.stream().anyMatch(target -> placeInFirstEmpty(target, focus));
    }

    private static boolean placeInFirstEmpty(FocusStore store, ItemStack stack) {
        OptionalInt vacancy = IntStream.range(0, store.size()).filter(slot -> store.get(slot).isEmpty()).findFirst();
        vacancy.ifPresent(slot -> store.set(slot, stack));
        return vacancy.isPresent();
    }

    public static int areaMode(ItemStack stack) {
        return storedArea(stack).dim();
    }

    public static int reachX(ItemStack stack) {
        return reachAlong(stack, CasterArea::x);
    }

    public static int reachY(ItemStack stack) {
        return reachAlong(stack, CasterArea::y);
    }

    public static int reachZ(ItemStack stack) {
        return reachAlong(stack, CasterArea::z);
    }

    private static int reachAlong(ItemStack stack, ToIntFunction<CasterArea> axis) {
        return cappedExtent(stack, axis.applyAsInt(storedArea(stack)));
    }

    public static void adjustArchitectArea(ItemStack stack, Level level, Player player, int mod) {
        if (level.isClientSide() || !(stack.getItem() instanceof ICaster caster) || !hasArchitect(caster.getFocusStack(stack), level.registryAccess())) {
            return;
        }
        CasterArea area = storedArea(stack);
        if (mod == KEY_GROW_EXTENTS) {
            stack.set(TTDataComponents.CASTER_AREA.get(), growExtents(area));
        } else if (mod == KEY_CYCLE_MODE) {
            stack.set(TTDataComponents.CASTER_AREA.get(), cycleMode(area));
        }
    }

    private static CasterArea cycleMode(CasterArea area) {
        return new CasterArea(area.x(), area.y(), area.z(), (area.dim() + 1) % MODE_COUNT);
    }

    private static boolean hasArchitect(ItemStack focus, HolderLookup.Provider registries) {
        if (focus.isEmpty()) {
            return false;
        }
        Spell spell = Spells.spellOf(focus);
        return spell != null && spell.nodes().stream().anyMatch(node -> isArchitectPart(registries, node));
    }

    private static boolean isArchitectPart(HolderLookup.Provider registries, SpellNode node) {
        return Spells.part(registries, node.part()).map(SpellPart::behavior).filter(IArchitect.class::isInstance).isPresent();
    }

    private static CasterArea growExtents(CasterArea area) {
        int mode = area.dim();
        int[] extents = {area.x(), area.y(), area.z()};
        for (int axis = 0; axis < AXIS_MODES.length; axis++) {
            if (affects(mode, AXIS_MODES[axis])) {
                extents[axis] = stepExtent(extents[axis]);
            }
        }
        return new CasterArea(extents[0], extents[1], extents[2], mode);
    }

    private static boolean affects(int mode, int axisMode) {
        return mode == MODE_ALL || mode == axisMode;
    }

    private static int stepExtent(int extent) {
        return extent >= MAX_AREA ? 0 : extent + 1;
    }

    private static CasterArea storedArea(ItemStack stack) {
        return stack.getOrDefault(TTDataComponents.CASTER_AREA.get(), CasterArea.DEFAULT);
    }

    private static int cappedExtent(ItemStack stack, int stored) {
        int ceiling = stack.getItem() instanceof ICaster ? MAX_AREA : 0;
        return Mth.clamp(stored, 0, ceiling);
    }

    private interface FocusStore {
        int size();

        ItemStack get(int slot);

        void set(int slot, ItemStack stack);

        default IntStream slots() {
            return IntStream.range(0, size());
        }
    }

    private record FocusLocation(FocusStore store, int slot) {
        ItemStack get() {
            return store.get(slot);
        }

        void set(ItemStack stack) {
            store.set(slot, stack);
        }
    }

    private record InventoryStore(Inventory inventory) implements FocusStore {
        @Override
        public int size() {
            return Inventory.INVENTORY_SIZE;
        }

        @Override
        public ItemStack get(int slot) {
            return inventory.getItem(slot);
        }

        @Override
        public void set(int slot, ItemStack stack) {
            inventory.setItem(slot, stack);
        }
    }

    private record PouchStore(Supplier<ItemStack> source, Consumer<ItemStack> commit) implements FocusStore {
        @Override
        public int size() {
            return FocusPouchItem.SIZE;
        }

        @Override
        public ItemStack get(int slot) {
            ItemStack pouch = source.get();
            return isPouch(pouch) ? FocusPouchItem.getInventory(pouch).get(slot) : ItemStack.EMPTY;
        }

        @Override
        public void set(int slot, ItemStack stack) {
            ItemStack pouch = source.get();
            if (!isPouch(pouch)) {
                return;
            }
            NonNullList<ItemStack> contents = FocusPouchItem.getInventory(pouch);
            contents.set(slot, stack);
            FocusPouchItem.setInventory(pouch, contents);
            commit.accept(pouch);
        }
    }
}

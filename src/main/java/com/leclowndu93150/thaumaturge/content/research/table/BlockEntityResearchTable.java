package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchTableAid;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.NoteRules;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityResearchTable extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int SLOT_SCRIBE_TOOLS = 0;
    public static final int SLOT_NOTE = 1;
    public static final int SLOT_COUNT = 2;
    public static final Identifier RESEARCH_EXPERTISE = TTIds.rl("research_expertise");
    public static final Identifier RESEARCH_MASTERY = TTIds.rl("research_mastery");
    public static final Identifier RESEARCH_DUPLICATION = TTIds.rl("research_duplication");

    private static final int BONUS_INTERVAL_TICKS = 600;
    private static final int BONUS_AMOUNT = 1;
    private static final int PLACE_COST = 1;
    private static final int COMBINE_COST = 1;
    private static final int IDENTICAL_COMBINE_COST = 2;
    private static final float MAX_SAVE_CHANCE = 0.5F;
    private static final float MASTERY_FREE_CHANCE = 0.1F;
    private static final float MASTERY_REFUND_CHANCE = 0.5F;
    private static final float EXPERTISE_REFUND_CHANCE = 0.25F;
    private static final float ORB_VOLUME = 0.2F;
    private static final float COMBINE_VOLUME = 0.3F;
    private static final float WRITE_VOLUME = 0.2F;
    private static final float ERASE_VOLUME = 0.2F;
    private static final float LEARN_VOLUME = 1.0F;
    private static final float DUPLICATE_VOLUME = 0.5F;
    private static final float NEUTRAL_PITCH = 1.0F;
    private static final float PITCH_SPREAD = 0.2F;
    private static final float ERASE_PITCH_SPREAD = 0.1F;
    private static final float ORB_PITCH_BASE = 0.9F;
    private static final String BONUS_KEY = "bonus_aspects";
    private static final String TITLE_KEY = "gui.thaumaturge.research_table.title";

    private final ResearchInventory items = new ResearchInventory();
    private AspectList bonus = AspectList.EMPTY;
    private int bonusTicks;

    public BlockEntityResearchTable(BlockPos pos, BlockState state) {
        super(TTBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStacksResourceHandler items() {
        return items;
    }

    public AspectList bonusAspects() {
        return bonus;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(TITLE_KEY);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuResearchTable(containerId, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityResearchTable table) {
        if (level.isClientSide() || ++table.bonusTicks < BONUS_INTERVAL_TICKS) {
            return;
        }
        table.bonusTicks = 0;
        AspectList updated = ResearchTableBonuses.recalculate(level, pos, table.bonus, level.getRandom());
        if (updated != table.bonus) {
            table.bonus = updated;
            table.setChangedAndSync();
        }
    }

    public void ensureNotePuzzle() {
        if (level == null || level.isClientSide()) {
            return;
        }
        HolderLookup.Provider registries = level.registryAccess();
        ResearchNoteData data = noteData();
        IResearchEntry entry = data != null && isUnsolvedBlank(data) ? entryOf(registries, data.entry()) : null;
        if (entry == null) {
            return;
        }
        AspectList anchors = ResearchNotes.anchors(registries, entry);
        storeGenerated(NoteGenerator.generate(data.entry(), data.index(), anchors, entry.complexity(), level.getRandom()));
    }

    private void storeGenerated(ResearchNoteData generated) {
        ItemStack note = stackIn(SLOT_NOTE);
        note.set(TTDataComponents.RESEARCH_NOTE.get(), generated);
        commit(SLOT_NOTE, note, Math.max(1, note.getCount()));
    }

    public @Nullable ResearchNoteData noteData() {
        return ResearchNotes.dataOf(stackIn(SLOT_NOTE));
    }

    private static boolean isUnsolvedBlank(ResearchNoteData data) {
        return !data.complete() && data.cells().isEmpty();
    }

    private @Nullable ResearchNoteData editableNote() {
        ResearchNoteData data = noteData();
        return level == null || data == null || data.complete() || !hasInkReady() ? null : data;
    }

    private ResearchNoteData settle(ServerPlayer player, ResearchNoteData edited) {
        NoteRules.Completion completion = NoteRules.checkCompletion(edited, usable -> AspectPools.isDiscovered(player, usable));
        if (!completion.complete()) {
            return edited;
        }
        ResearchNoteData finished = edited.withCells(completion.prunedCells()).asComplete();
        playAtTable(TTSounds.LEARN.get(), LEARN_VOLUME, NEUTRAL_PITCH);
        return finished;
    }

    public void placeAspect(ServerPlayer player, HexGrid.Hex hex, @Nullable Holder<IAspect> aspect) {
        ResearchNoteData data = editableNote();
        ResearchNoteData.Cell cell = data == null ? null : data.cellAt(hex);
        if (cell == null) {
            return;
        }
        ResearchNoteData edited = aspect == null ? rubOut(cell, data, player) : inscribe(cell, aspect, data, player);
        if (edited != null) {
            writeNote(settle(player, edited));
        }
    }

    public void combineAspects(ServerPlayer player, Holder<IAspect> first, Holder<IAspect> second, boolean firstFromBonus, boolean secondFromBonus) {
        Holder<IAspect> result = level == null ? null : AspectCombinations.result(level.registryAccess(), first, second);
        if (result == null) {
            return;
        }
        int firstCost = first.equals(second) && firstFromBonus == secondFromBonus ? IDENTICAL_COMBINE_COST : COMBINE_COST;
        if (available(player, second, secondFromBonus) < COMBINE_COST || available(player, first, firstFromBonus) < firstCost) {
            return;
        }
        RandomSource random = player.getRandom();
        boolean paid = consume(player, first, firstFromBonus, random);
        paid = paid && consume(player, second, secondFromBonus, random);
        setChangedAndSync();
        if (!paid) {
            return;
        }
        AspectPools.grant(player, result, BONUS_AMOUNT);
        playAtTable(SoundEvents.EXPERIENCE_ORB_PICKUP, COMBINE_VOLUME, NEUTRAL_PITCH);
    }

    public void duplicateNote(ServerPlayer player) {
        ResearchNoteData data = noteData();
        boolean eligible = level != null && data != null && data.complete();
        if (!eligible || !completed(player, RESEARCH_DUPLICATION)) {
            return;
        }
        Inventory inventory = player.getInventory();
        AspectList cost = duplicationCost(player, data);
        int paperSlot = paperSlot(inventory);
        boolean payable = cost != null && paperSlot >= 0 && ResearchNotes.consumeInk(player, true) && AspectPools.canAfford(player, cost);
        if (!payable || !ResearchNotes.consumeInk(player, false)) {
            return;
        }
        inventory.getItem(paperSlot).shrink(1);
        AspectPools.spendAll(player, cost);
        writeNote(data.withCopies(data.copies() + 1));
        ItemStack copy = stackIn(SLOT_NOTE).copyWithCount(1);
        if (!inventory.add(copy)) {
            player.drop(copy, false);
        }
        playAtTable(TTSounds.WRITE.get(), DUPLICATE_VOLUME, NEUTRAL_PITCH);
    }

    public @Nullable AspectList duplicationCost(Player player, ResearchNoteData data) {
        IResearchEntry entry = entryOf(player.registryAccess(), data.entry());
        if (entry == null) {
            return null;
        }
        AspectList cost = AspectList.EMPTY;
        for (AspectInstance instance : entry.noteAspects().entries()) {
            cost = cost.add(instance.aspect(), instance.amount() + data.copies());
        }
        return cost;
    }

    public boolean consumeInk() {
        if (!hasInkReady()) {
            return false;
        }
        ItemStack tools = stackIn(SLOT_SCRIBE_TOOLS);
        int worn = tools.getDamageValue() + 1;
        tools.setDamageValue(worn);
        commit(SLOT_SCRIBE_TOOLS, tools, tools.getCount());
        return true;
    }

    public boolean hasInkReady() {
        return inkLeft(stackIn(SLOT_SCRIBE_TOOLS)) > 0;
    }

    private static int inkLeft(ItemStack tools) {
        return tools.isDamageableItem() && !tools.isEmpty() ? tools.getMaxDamage() - tools.getDamageValue() : 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            for (int slot = SLOT_SCRIBE_TOOLS; slot < SLOT_COUNT; slot++) {
                spill(server, pos, stackIn(slot));
            }
        }
    }

    private static void spill(Level world, BlockPos at, ItemStack stack) {
        if (!stack.isEmpty()) {
            Containers.dropItemStack(world, at.getX(), at.getY(), at.getZ(), stack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bonus = input.read(BONUS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        items.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output);
        output.store(BONUS_KEY, AspectList.CODEC, bonus);
    }

    private @Nullable ResearchNoteData inscribe(ResearchNoteData.Cell cell, Holder<IAspect> aspect, ResearchNoteData data, ServerPlayer player) {
        boolean open = cell.type() == ResearchNoteData.TYPE_BLANK && AspectPools.isDiscovered(player, aspect);
        RandomSource random = player.getRandom();
        Boolean free = open ? payForStroke(player, aspect, random) : null;
        if (free == null) {
            return null;
        }
        if (free) {
            playAtTable(SoundEvents.EXPERIENCE_ORB_PICKUP, ORB_VOLUME, ORB_PITCH_BASE + random.nextFloat() * PITCH_SPREAD);
        }
        ResearchNoteData placed = data.withCell(cell.hex(), ResearchNoteData.TYPE_PLACED, aspect);
        return strokeDone(placed, TTSounds.WRITE.get(), WRITE_VOLUME, NEUTRAL_PITCH);
    }

    private @Nullable Boolean payForStroke(ServerPlayer player, Holder<IAspect> aspect, RandomSource random) {
        if (completed(player, RESEARCH_MASTERY) && random.nextFloat() < MASTERY_FREE_CHANCE) {
            return Boolean.TRUE;
        }
        if (AspectPools.amount(player, aspect) > 0) {
            if (random.nextFloat() < aspectSaveChance()) {
                return Boolean.TRUE;
            }
            AspectPools.spend(player, aspect, PLACE_COST);
            return Boolean.FALSE;
        }
        if (bonus.amountOf(aspect) <= 0) {
            return null;
        }
        bonus = bonus.remove(aspect, PLACE_COST);
        return Boolean.FALSE;
    }

    private @Nullable ResearchNoteData rubOut(ResearchNoteData.Cell cell, ResearchNoteData data, ServerPlayer player) {
        if (cell.type() != ResearchNoteData.TYPE_PLACED) {
            return null;
        }
        RandomSource random = player.getRandom();
        Holder<IAspect> held = cell.aspectOrNull();
        if (held != null && random.nextFloat() < refundChance(player)) {
            AspectPools.refund(player, held, PLACE_COST);
        }
        ResearchNoteData blanked = data.withCell(cell.hex(), ResearchNoteData.TYPE_BLANK, null);
        return strokeDone(blanked, TTSounds.ERASE.get(), ERASE_VOLUME, NEUTRAL_PITCH + random.nextFloat() * ERASE_PITCH_SPREAD);
    }

    private ResearchNoteData strokeDone(ResearchNoteData result, SoundEvent sound, float volume, float pitch) {
        consumeInk();
        playAtTable(sound, volume, pitch);
        return result;
    }

    private static float refundChance(Player player) {
        if (completed(player, RESEARCH_MASTERY)) {
            return MASTERY_REFUND_CHANCE;
        }
        return completed(player, RESEARCH_EXPERTISE) ? EXPERTISE_REFUND_CHANCE : 0.0F;
    }

    private static boolean completed(Player player, Identifier research) {
        return KnowledgeAccess.of(player).isResearchComplete(research);
    }

    private int available(Player player, Holder<IAspect> aspect, boolean fromBonus) {
        return fromBonus ? bonus.amountOf(aspect) : AspectPools.amount(player, aspect);
    }

    private boolean consume(ServerPlayer player, Holder<IAspect> aspect, boolean fromBonus, RandomSource random) {
        if (fromBonus) {
            if (bonus.amountOf(aspect) <= 0) {
                return false;
            }
            bonus = bonus.remove(aspect, COMBINE_COST);
            return true;
        }
        if (AspectPools.amount(player, aspect) > 0 && random.nextFloat() < aspectSaveChance()) {
            return true;
        }
        return AspectPools.spend(player, aspect, COMBINE_COST);
    }

    private float aspectSaveChance() {
        if (level == null) {
            return 0.0F;
        }
        BlockPos partner = worldPosition.relative(getBlockState().getValue(BlockResearchTable.FACING));
        return Math.min(MAX_SAVE_CHANCE, aidChance(worldPosition) + aidChance(partner));
    }

    private float aidChance(BlockPos tablePos) {
        BlockPos above = tablePos.above();
        BlockState state = level.getBlockState(above);
        return state.getBlock() instanceof IResearchTableAid aid ? aid.aspectSaveChance(level, above, state) : 0.0F;
    }

    private void writeNote(ResearchNoteData data) {
        ItemStack note = stackIn(SLOT_NOTE);
        note.set(TTDataComponents.RESEARCH_NOTE.get(), data);
        note.set(TTDataComponents.NOTE_COMPLETE.get(), data.complete() ? Boolean.TRUE : note.get(TTDataComponents.NOTE_COMPLETE.get()));
        commit(SLOT_NOTE, note, note.getCount());
        setChangedAndSync();
    }

    private void commit(int slot, ItemStack stack, int amount) {
        items.set(slot, ItemResource.of(stack), amount);
    }

    private ItemStack stackIn(int slot) {
        return items.getResource(slot).toStack(items.getAmountAsInt(slot));
    }

    private void playAtTable(SoundEvent sound, float volume, float pitch) {
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
    }

    private static int paperSlot(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(Items.PAPER)) {
                return slot;
            }
        }
        return -1;
    }

    private static @Nullable IResearchEntry entryOf(HolderLookup.Provider registries, Identifier id) {
        return registries.lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id)).map(Holder::value).orElse(null);
    }

    private final class ResearchInventory extends ItemStacksResourceHandler {
        ResearchInventory() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            ItemStack stack = resource.toStack(1);
            return index == SLOT_SCRIBE_TOOLS ? stack.is(TTItemTags.SCRIBING_TOOLS) : ResearchNotes.dataOf(stack) != null;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChangedAndSync();
            if (index == SLOT_NOTE) {
                ensureNotePuzzle();
            }
        }
    }
}

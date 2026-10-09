package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityDeconstructionTable extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int BREAK_TIME_TICKS = 40;

    private static final int SLOT_COUNT = 1;
    private static final int RESULT_ROLL_RANGE = 80;
    private static final float BREAK_SOUND_VOLUME = 0.3F;
    private static final float PITCH_BASE = 0.9F;
    private static final float PITCH_SPREAD = 0.2F;
    private static final String RESULT_KEY = "result_aspect";
    private static final String BREAK_TIME_KEY = "break_time";
    private static final String TITLE_KEY = "gui.thaumaturge.deconstruction_table.title";

    private final DeconstructionInventory items = new DeconstructionInventory();
    private @Nullable Identifier resultAspect;
    private int breakTime = BREAK_TIME_TICKS;
    private ItemResource validatedResource = ItemResource.EMPTY;
    private boolean validatedHasAspects;

    public BlockEntityDeconstructionTable(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state);
    }

    public ItemStacksResourceHandler items() {
        return items;
    }

    public @Nullable Identifier resultAspect() {
        return resultAspect;
    }

    public int breakTime() {
        return breakTime;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(TITLE_KEY);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuDeconstructionTable(containerId, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDeconstructionTable table) {
        if (!table.canBreak()) {
            if (table.breakTime != BREAK_TIME_TICKS) {
                table.breakTime = BREAK_TIME_TICKS;
                table.setChangedAndSync();
            }
            return;
        }
        if (--table.breakTime <= 0) {
            table.breakTime = BREAK_TIME_TICKS;
            table.breakInput(level, pos);
        }
    }

    public void collect(ServerPlayer player) {
        if (resultAspect == null) {
            return;
        }
        Holder<IAspect> aspect = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, resultAspect)).orElse(null);
        if (aspect == null) {
            return;
        }
        AspectPools.grant(player, aspect, 1);
        resultAspect = null;
        setChangedAndSync();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack stack = items.getResource(SLOT_INPUT).toStack(items.getAmountAsInt(SLOT_INPUT));
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input);
        resultAspect = input.read(RESULT_KEY, LegacyIds.IDENTIFIER_CODEC).orElse(null);
        breakTime = input.getIntOr(BREAK_TIME_KEY, BREAK_TIME_TICKS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output);
        if (resultAspect != null) {
            output.store(RESULT_KEY, Identifier.CODEC, resultAspect);
        }
        output.putInt(BREAK_TIME_KEY, breakTime);
    }

    private boolean canBreak() {
        ItemResource resource = items.getResource(SLOT_INPUT);
        if (resource.isEmpty() || resultAspect != null) {
            return false;
        }
        return hasAspects(resource);
    }

    private boolean hasAspects(ItemResource resource) {
        if (!resource.equals(validatedResource)) {
            validatedResource = resource;
            validatedHasAspects = !resource.isEmpty() && !AspectIndexAccess.of(resource.toStack(1)).isEmpty();
        }
        return validatedHasAspects;
    }

    private void breakInput(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        ItemResource resource = items.getResource(SLOT_INPUT);
        AspectList primals = AspectPrimals.reduce(AspectIndexAccess.of(resource.toStack(1)));
        int total = primals.totalAmount();
        if (!primals.isEmpty() && (total >= RESULT_ROLL_RANGE || random.nextInt(RESULT_ROLL_RANGE) < total)) {
            AspectInstance chosen = primals.entries().get(random.nextInt(primals.size()));
            resultAspect = chosen.aspect().unwrapKey().orElseThrow().identifier();
        }
        int remaining = items.getAmountAsInt(SLOT_INPUT) - 1;
        items.set(SLOT_INPUT, remaining > 0 ? resource : ItemResource.EMPTY, Math.max(remaining, 0));
        level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, BREAK_SOUND_VOLUME, PITCH_BASE + random.nextFloat() * PITCH_SPREAD);
        setChangedAndSync();
    }

    private final class DeconstructionInventory extends ItemStacksResourceHandler {
        DeconstructionInventory() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return hasAspects(resource);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChangedAndSync();
        }
    }
}

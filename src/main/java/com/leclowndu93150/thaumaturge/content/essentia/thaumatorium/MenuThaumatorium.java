package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload.Entry;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuThaumatorium extends AbstractTTMenu implements BlockMenu<BlockEntityThaumatorium> {
    public static final int CATALYST_X = 56;
    public static final int CATALYST_Y = 24;

    private static final int PLAYER_GRID_X = 8;
    private static final int PLAYER_GRID_Y = 135;
    private static final int HOTBAR_Y = 193;
    private static final int CATALYST_SLOTS = 1;
    private static final int CATALYST_SLOT = 0;
    private static final double REACH_BUFFER = 4.0;
    private static final int QUEUE_UNSENT = -1;

    public final @Nullable BlockEntityThaumatorium blockEntity;
    public List<Entry> clientRecipes = List.of();

    private final Player viewer;
    private @Nullable ItemStack lastCatalyst;
    private int lastQueueSize = QUEUE_UNSENT;

    public MenuThaumatorium(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private static @Nullable BlockEntityThaumatorium clientBlockEntity(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityThaumatorium machine ? machine : null;
    }

    public MenuThaumatorium(int containerId, Inventory playerInventory, @Nullable BlockEntityThaumatorium blockEntity) {
        super(TTMenus.THAUMATORIUM.get(), containerId);
        this.blockEntity = blockEntity;
        this.viewer = playerInventory.player;
        ItemStacksResourceHandler catalyst = blockEntity != null ? blockEntity.catalyst() : new ItemStacksResourceHandler(CATALYST_SLOTS);
        addSlot(new ResourceHandlerSlot(catalyst, catalyst::set, CATALYST_SLOT, CATALYST_X, CATALYST_Y));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    @Override
    public @Nullable BlockEntityThaumatorium blockEntity() {
        return blockEntity;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null || !(viewer instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack catalyst = blockEntity.catalystStack();
        int queueSize = blockEntity.queue().size();
        if (lastCatalyst != null && queueSize == lastQueueSize && ItemStack.matches(catalyst, lastCatalyst)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new ClientboundThaumatoriumRecipesPayload(containerId, buildEntries(level, player, catalyst)));
        lastCatalyst = catalyst.copy();
        lastQueueSize = queueSize;
    }

    private List<Entry> buildEntries(ServerLevel level, ServerPlayer player, ItemStack catalyst) {
        List<Identifier> ids = new ArrayList<>();
        List<CrucibleRecipe> recipes = blockEntity.candidateRecipes(level, player, ids);
        List<Entry> entries = new ArrayList<>(recipes.size());
        CrucibleRecipeInput input = new CrucibleRecipeInput(catalyst, AspectList.EMPTY);
        for (int i = 0; i < recipes.size(); i++) {
            CrucibleRecipe recipe = recipes.get(i);
            Identifier id = ids.get(i);
            entries.add(new Entry(id, recipe.assemble(input), blockEntity.queue().contains(id), recipe.aspects()));
        }
        return entries;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(slotIndex, CATALYST_SLOTS, stack -> true);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved() && player.isWithinBlockInteractionRange(blockEntity.getBlockPos(), REACH_BUFFER);
    }
}

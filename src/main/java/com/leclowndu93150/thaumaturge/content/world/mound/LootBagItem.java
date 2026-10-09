package com.leclowndu93150.thaumaturge.content.world.mound;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class LootBagItem extends Item {
    private static final float OPEN_VOLUME = 0.75F;
    private static final String USAGE_KEY = "tooltip.thaumaturge.loot_bag.use";

    private final ResourceKey<LootTable> lootTable;

    public LootBagItem(ResourceKey<LootTable> lootTable, Properties properties) {
        super(properties);
        this.lootTable = lootTable;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Component usage = Component.translatable(USAGE_KEY);
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(usage);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel server) {
            spill(server, player);
            player.playSound(TTSounds.COINS.get(), OPEN_VOLUME, 1.0F);
        }
        held.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private void spill(ServerLevel server, Player player) {
        LootParams.Builder builder = new LootParams.Builder(server);
        builder.withParameter(LootContextParams.ORIGIN, player.position());
        LootParams params = builder.create(LootContextParamSets.CHEST);
        LootTable table = server.getServer().reloadableRegistries().getLootTable(lootTable);
        table.getRandomItems(params).forEach(loot -> {
            ItemEntity drop = new ItemEntity(server, player.getX(), player.getY(), player.getZ(), loot.copy());
            server.addFreshEntity(drop);
        });
    }
}

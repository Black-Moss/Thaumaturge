package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CardInfuse extends TheorycraftCard {
    private static final List<Supplier<ItemStack>> OPTIONS = List.of(
            () -> new ItemStack(TCItems.ALUMENTUM.get()),
            () -> new ItemStack(TCItems.NITORS.get(DyeColor.YELLOW).get()),
            () -> new ItemStack(TCItems.AMBER.get()),
            () -> new ItemStack(TCItems.BRAIN.get()),
            () -> new ItemStack(Items.GUNPOWDER),
            () -> new ItemStack(Items.BOW),
            () -> new ItemStack(Items.GOLDEN_SWORD),
            () -> new ItemStack(Items.IRON_SWORD),
            () -> new ItemStack(Items.IRON_PICKAXE),
            () -> new ItemStack(Items.GOLDEN_PICKAXE),
            () -> new ItemStack(Items.QUARTZ),
            () -> new ItemStack(Items.APPLE));

    private @Nullable Holder<IAspect> aspect;
    private ItemStack stack = ItemStack.EMPTY;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.INFUSION;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.infuse.name");
    }

    @Override
    public Component description() {
        if (aspect == null || stack.isEmpty()) {
            return Component.translatable("card.thaumcraft.infuse.text", Component.empty(), Component.empty(), 0);
        }
        return Component.translatable("card.thaumcraft.infuse.text",
                CardHelper.boldAspectName(aspect), stack.getHoverName(), value());
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        aspect = CardHelper.randomCompound(player.registryAccess(), rng);
        stack = OPTIONS.get(rng.nextInt(OPTIONS.size())).get();
        return aspect != null && !stack.isEmpty();
    }

    private int value() {
        return 10 + (int) (Math.sqrt(CardHelper.visSize(stack)) * 1.5);
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        if (aspect == null || stack.isEmpty()) return List.of();
        return List.of(new CardItemRequirement(stack.copy(), true),
                new CardItemRequirement(CardHelper.phial(aspect), true));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.INFUSION, value());
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (aspect != null) {
            aspect.unwrapKey().ifPresent(key -> output.store("aspect", Identifier.CODEC, key.identifier()));
        }
        if (!stack.isEmpty()) {
            output.store("stack", ItemStack.CODEC, stack);
        }
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        aspect = input.read("aspect", Identifier.CODEC)
                .flatMap(id -> registries.lookupOrThrow(IAspect.REGISTRY_KEY)
                        .get(ResourceKey.create(IAspect.REGISTRY_KEY, id)))
                .map(holder -> (Holder<IAspect>) holder)
                .orElse(null);
        stack = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}

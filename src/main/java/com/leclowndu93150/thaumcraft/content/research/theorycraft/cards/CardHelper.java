package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class CardHelper {
    private CardHelper() {}

    static List<Holder.Reference<IAspect>> compoundAspects(HolderLookup.Provider registries) {
        List<Holder.Reference<IAspect>> compounds = new ArrayList<>();
        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(holder -> {
            if (!holder.value().components().isEmpty()) {
                compounds.add(holder);
            }
        });
        compounds.sort((a, b) -> a.key().identifier().compareTo(b.key().identifier()));
        return compounds;
    }

    static ItemStack crystal(Holder<IAspect> aspect) {
        ItemStack stack = new ItemStack(TCItems.ESSENTIA_CRYSTAL.get());
        stack.set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
        return stack;
    }

    static ItemStack phial(Holder<IAspect> aspect) {
        ItemStack stack = new ItemStack(TCItems.PHIAL.get());
        stack.set(TCDataComponents.ASPECTS.get(), AspectList.of(new AspectInstance(aspect, PhialItem.BASE_AMOUNT)));
        return stack;
    }

    static int visSize(ItemStack stack) {
        return AspectIndexHolder.get().of(stack).totalAmount();
    }

    static ResourceKey<IResearchCategory> randomCategory(Player player) {
        List<ResourceKey<IResearchCategory>> keys = allCategories(player);
        return keys.get(player.getRandom().nextInt(keys.size()));
    }

    static List<ResourceKey<IResearchCategory>> allCategories(Player player) {
        List<ResourceKey<IResearchCategory>> keys = new ArrayList<>();
        player.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).listElements()
                .forEach(holder -> holder.unwrapKey().ifPresent(keys::add));
        keys.sort((a, b) -> a.identifier().compareTo(b.identifier()));
        return keys;
    }

    static Component boldAspectName(Holder<IAspect> aspect) {
        return Component.translatable("aspect.thaumcraft." + aspect.value().tag()).withStyle(ChatFormatting.BOLD);
    }

    static Holder.Reference<IAspect> randomCompound(HolderLookup.Provider registries, Random rng) {
        List<Holder.Reference<IAspect>> compounds = compoundAspects(registries);
        return compounds.get(rng.nextInt(compounds.size()));
    }
}

package com.leclowndu93150.thaumcraft.content.item;

import com.leclowndu93150.thaumcraft.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumcraft.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

public class ShareBookItem extends Item {
    public ShareBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && !stack.has(TCDataComponents.DISCOVERED_ASPECTS)){
            stack.set(TCDataComponents.DISCOVERED_ASPECTS, AspectPools.data(player));
            IPlayerKnowledge knowledge = ResearchManager.of((ServerPlayer) player);
            if (knowledge instanceof PlayerKnowledge pKnowledge)
                stack.set(TCDataComponents.KNOWLEDGE, pKnowledge);
            stack.set(DataComponents.PROFILE,player.getProfile());
        } else if (!player.isCrouching() && stack.has(TCDataComponents.DISCOVERED_ASPECTS)){
            ResolvableProfile profile = stack.get(DataComponents.PROFILE);
            if (profile == null || profile.equals(player.getProfile())) return InteractionResult.PASS;
            AspectPoolData aspects = stack.get(TCDataComponents.DISCOVERED_ASPECTS);
            PlayerKnowledge knowledge = stack.get(TCDataComponents.KNOWLEDGE);
            IPlayerKnowledge currentKnowledge = ResearchManager.of((ServerPlayer) player);
            if (aspects == null || knowledge == null || !(currentKnowledge instanceof PlayerKnowledge playerKnowledge)) return InteractionResult.PASS;
            AspectPools.data(player).copyFrom(aspects);
            playerKnowledge.copyFrom(knowledge);
            playerKnowledge.sync((ServerPlayer) player);
            AspectPools.sync((ServerPlayer) player);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    
}

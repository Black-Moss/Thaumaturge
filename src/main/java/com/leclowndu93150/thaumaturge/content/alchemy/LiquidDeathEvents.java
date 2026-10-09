package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LiquidDeathEvents {
    private static final int VIS_PER_EXTRA_CRYSTAL = 10;

    private LiquidDeathEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().is(TTDamageTypes.DISSOLVE) && event.getEntity().level() instanceof ServerLevel level) {
            dropCrystals(event.getDrops(), level, event.getEntity());
        }
    }

    private static void dropCrystals(Collection<ItemEntity> drops, ServerLevel level, LivingEntity entity) {
        AspectList aspects = EntityAspects.of(entity);
        if (aspects.isEmpty()) {
            return;
        }
        double height = entity.getY() + entity.getEyeHeight();
        for (Holder<IAspect> aspect : rollAspects(aspects, entity.getRandom())) {
            ItemStack crystal = EssentiaCrystalFactory.of(aspect);
            drops.add(new ItemEntity(level, entity.getX(), height, entity.getZ(), crystal, 0.0, 0.0, 0.0));
        }
    }

    private static List<Holder<IAspect>> rollAspects(AspectList aspects, RandomSource random) {
        List<Holder<IAspect>> pool = new ArrayList<>(aspects.aspects());
        int count = 1 + random.nextInt(aspects.totalAmount() / VIS_PER_EXTRA_CRYSTAL + 1);
        List<Holder<IAspect>> picks = new ArrayList<>(count);
        for (int remaining = count; remaining > 0; remaining--) {
            int slot = random.nextInt(pool.size());
            picks.add(pool.get(slot));
        }
        return picks;
    }
}

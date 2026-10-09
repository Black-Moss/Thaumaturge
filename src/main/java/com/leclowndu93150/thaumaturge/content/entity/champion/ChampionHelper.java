package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;

public final class ChampionHelper {
    private ChampionHelper() {}

    public static List<Holder<MobTrait>> championTraits() {
        return TTMobTraits.registry().listElements().filter(entry -> entry.value().isChampion()).<Holder<MobTrait>>map(entry -> entry).collect(Collectors.toList());
    }

    public static boolean rolled(LivingEntity mob) {
        Boolean flag = mob.getExistingDataOrNull(TTAttachments.CHAMPION_ROLLED);
        return flag != null && flag;
    }

    public static void markRolled(LivingEntity mob) {
        mob.setData(TTAttachments.CHAMPION_ROLLED, Boolean.TRUE);
    }

    public static void makeChampion(Mob mob, boolean persist) {
        List<Holder<MobTrait>> pool = championTraits();
        if (pool.isEmpty()) {
            return;
        }
        Holder<MobTrait> chosen = mob instanceof Creeper ? TTMobTraits.BOLD : pool.get(mob.getRandom().nextInt(pool.size()));
        makeChampion(mob, persist, chosen);
    }

    public static void makeChampion(Mob mob, boolean persist, Holder<MobTrait> trait) {
        if (rolled(mob)) {
            return;
        }
        markRolled(mob);
        MobTraits.add(mob, trait);
        if (persist) {
            mob.setPersistenceRequired();
        }
    }
}

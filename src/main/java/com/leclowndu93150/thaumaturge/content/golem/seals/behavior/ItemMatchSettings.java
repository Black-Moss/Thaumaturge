package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import net.minecraft.world.item.ItemStack;

public final class ItemMatchSettings {
    public static final SealSetting MATCH_DAMAGE = new SealSetting("match_damage", "gui.thaumaturge.seal.setting.meta", true);
    public static final SealSetting MATCH_COMPONENTS = new SealSetting("match_components", "gui.thaumaturge.seal.setting.nbt", true);
    public static final SealSetting MATCH_TAGS = new SealSetting("match_tags", "gui.thaumaturge.seal.setting.ore", false);
    public static final SealSetting MATCH_MOD = new SealSetting("match_mod", "gui.thaumaturge.seal.setting.mod", false);
    public static final SealSetting[] ALL = {MATCH_DAMAGE, MATCH_COMPONENTS, MATCH_TAGS, MATCH_MOD};

    private ItemMatchSettings() {}

    public static ItemMatch of(ISealEntity seal) {
        return ItemMatch.EXACT.ignoringDamage(!seal.setting(MATCH_DAMAGE)).ignoringComponents(!seal.setting(MATCH_COMPONENTS)).byTag(seal.setting(MATCH_TAGS)).byMod(seal.setting(MATCH_MOD));
    }

    static ISealFilter filterOf(ISealEntity seal) {
        return seal.filter().orElseThrow();
    }

    static boolean accepts(ISealEntity seal, ItemStack stack) {
        ISealFilter filter = filterOf(seal);
        return StackFilter.of(filter.stacks(), filter.isBlacklist(), of(seal)).accepts(stack);
    }
}

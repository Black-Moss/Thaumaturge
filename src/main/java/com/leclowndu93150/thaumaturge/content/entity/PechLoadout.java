package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

final class PechLoadout {
    private static final int WEIGHT_TOTAL = 100;
    private static final int WAND_WEIGHT = 10;
    private static final int BOW_WEIGHT = 25;
    private static final int TOOL_WEIGHT = 5;
    private static final int EMPTY_WEIGHT = 30;

    private static final List<Option> OPTIONS = List.of(new Option(WAND_WEIGHT, TTItems.PECH_WAND::get), new Option(TOOL_WEIGHT, () -> Items.STONE_SWORD), new Option(BOW_WEIGHT, () -> Items.BOW),
            new Option(TOOL_WEIGHT, () -> Items.STONE_AXE), new Option(TOOL_WEIGHT, () -> Items.IRON_SWORD), new Option(TOOL_WEIGHT, () -> Items.IRON_AXE),
            new Option(TOOL_WEIGHT, () -> Items.FISHING_ROD), new Option(TOOL_WEIGHT, () -> Items.STONE_PICKAXE), new Option(TOOL_WEIGHT, () -> Items.IRON_PICKAXE),
            new Option(EMPTY_WEIGHT, () -> Items.AIR));

    private PechLoadout() {}

    static Item roll(RandomSource random) {
        int roll = random.nextInt(WEIGHT_TOTAL);
        for (Option option : OPTIONS) {
            roll -= option.weight();
            if (roll < 0) {
                return option.item().get();
            }
        }
        return Items.AIR;
    }

    private record Option(int weight, Supplier<? extends Item> item) {
    }
}

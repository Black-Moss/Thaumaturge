package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ItemGolemAccessory extends Item {
    private static final Map<GolemAccessory, ItemGolemAccessory> BY_ACCESSORY = new LinkedHashMap<>();

    private final GolemAccessory accessory;

    public ItemGolemAccessory(GolemAccessory accessory, Properties properties) {
        super(properties);
        this.accessory = accessory;
        BY_ACCESSORY.put(accessory, this);
    }

    public GolemAccessory accessory() {
        return accessory;
    }

    public static ItemStack stackFor(@Nullable GolemAccessory accessory) {
        ItemGolemAccessory item = BY_ACCESSORY.get(accessory);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}

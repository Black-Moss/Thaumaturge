package com.leclowndu93150.thaumcraft.mixin.world.item.alchemy;

import java.util.List;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {
    @Accessor("potionMixes")
    List<?> thaumcraft$getPotionMixes();
}

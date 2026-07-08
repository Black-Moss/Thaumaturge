package com.leclowndu93150.thaumcraft.content.golem.seals;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.golems.GolemTrait;
import com.leclowndu93150.thaumcraft.api.golems.seals.ISealConfigToggles;
import net.minecraft.resources.Identifier;

public class SealBreakerAdvanced extends SealBreaker {
    public SealBreakerAdvanced() {
        props = new ISealConfigToggles.SealToggle[]{
                new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
                new ISealConfigToggles.SealToggle(false, "psilk", "golem.prop.silk")
        };
    }

    @Override
    public Identifier getKey() {
        return TCIds.rl("breaker_advanced");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_breaker_advanced.png");
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{GolemTrait.BREAKER, GolemTrait.SMART};
    }
}

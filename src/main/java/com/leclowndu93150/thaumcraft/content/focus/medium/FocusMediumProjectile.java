package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusMedium;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.entity.EntityFocusProjectile;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class FocusMediumProjectile extends FocusMedium {
    private static final Identifier KEY = TCIds.rl("projectile");

    private static final int BASE_COMPLEXITY = 4;
    private static final int BOUNCY_COMPLEXITY = 3;
    private static final int SEEKING_COMPLEXITY = 5;
    private static final float SPEED_DIVISOR = 3.0F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_projectile"), Optional.of(1), false);
    }

    @Override
    public int getComplexity() {
        int complexity = BASE_COMPLEXITY + (getSettingValue("speed") - 1) / 2;
        switch (getSettingValue("option")) {
            case EntityFocusProjectile.SPECIAL_BOUNCY -> complexity += BOUNCY_COMPLEXITY;
            case EntityFocusProjectile.SPECIAL_SEEK_ENEMY, EntityFocusProjectile.SPECIAL_SEEK_FRIENDLY ->
                    complexity += SEEKING_COMPLEXITY;
        }
        return complexity;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET, EnumSupplyType.TRAJECTORY};
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        float speed = getSettingValue("speed") / SPEED_DIVISOR;
        FocusPackage remaining = getRemainingPackage();
        if (remaining == null || remaining.getCaster() == null) {
            return false;
        }
        EntityFocusProjectile projectile = new EntityFocusProjectile(remaining, speed, trajectory, getSettingValue("option"));
        return getPackage().getCaster().level().addFreshEntity(projectile);
    }

    @Override
    public boolean hasIntermediary() {
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] option = new int[]{
                EntityFocusProjectile.SPECIAL_NONE,
                EntityFocusProjectile.SPECIAL_BOUNCY,
                EntityFocusProjectile.SPECIAL_SEEK_ENEMY,
                EntityFocusProjectile.SPECIAL_SEEK_FRIENDLY
        };
        String[] optionDesc = new String[]{
                "focus.common.none", "focus.projectile.bouncy", "focus.projectile.seeking.hostile", "focus.projectile.seeking.friendly"
        };
        return new NodeSetting[]{
                new NodeSetting("option", "focus.common.options", new NodeSettingIntList(option, optionDesc)),
                new NodeSetting("speed", "focus.projectile.speed", new NodeSettingIntRange(1, 5))
        };
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.MOTUS;
    }
}

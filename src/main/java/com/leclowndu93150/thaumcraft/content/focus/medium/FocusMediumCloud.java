package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusMedium;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.entity.EntityFocusCloud;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class FocusMediumCloud extends FocusMedium {
    private static final Identifier KEY = TCIds.rl("cloud");

    private static final int BASE_COMPLEXITY = 4;
    private static final int RADIUS_COMPLEXITY_FACTOR = 2;
    private static final int DURATION_COMPLEXITY_DIVISOR = 5;
    private static final float POWER_MULTIPLIER = 0.5F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_cloud"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.ALKIMIA;
    }

    @Override
    public int getComplexity() {
        return BASE_COMPLEXITY + getSettingValue("radius") * RADIUS_COMPLEXITY_FACTOR
                + getSettingValue("duration") / DURATION_COMPLEXITY_DIVISOR;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        FocusPackage remaining = getRemainingPackage();
        if (remaining == null || getPackage().getCaster() == null) {
            return false;
        }
        EntityFocusCloud cloud = new EntityFocusCloud(remaining, trajectory,
                getSettingValue("radius"), getSettingValue("duration"));
        return getPackage().getCaster().level().addFreshEntity(cloud);
    }

    @Override
    public boolean hasIntermediary() {
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("radius", "focus.common.radius", new NodeSettingIntRange(1, 3)),
                new NodeSetting("duration", "focus.common.duration", new NodeSettingIntRange(5, 30))
        };
    }

    @Override
    public float getPowerMultiplier() {
        return POWER_MULTIPLIER;
    }
}

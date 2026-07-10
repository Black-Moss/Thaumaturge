package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusMedium;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.entity.EntitySpellBat;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class FocusMediumSpellBat extends FocusMedium {
    private static final Identifier KEY = TCIds.rl("spellbat");

    private static final int COMPLEXITY = 8;
    private static final float POWER_MULTIPLIER = 0.33F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_spellbat"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.BESTIA;
    }

    @Override
    public int getComplexity() {
        return COMPLEXITY;
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
        EntitySpellBat bat = new EntitySpellBat(remaining, getSettingValue("target") == 1);
        bat.setPos(trajectory.source().x, trajectory.source().y, trajectory.source().z);
        return getPackage().getCaster().level().addFreshEntity(bat);
    }

    @Override
    public boolean hasIntermediary() {
        return true;
    }

    @Override
    public float getPowerMultiplier() {
        return POWER_MULTIPLIER;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] friend = new int[]{0, 1};
        String[] friendDesc = new String[]{"focus.common.enemy", "focus.common.friend"};
        return new NodeSetting[]{new NodeSetting("target", "focus.common.target", new NodeSettingIntList(friend, friendDesc))};
    }
}

package com.leclowndu93150.thaumcraft.content.focus.mod;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.casters.FocusMod;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntList;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class FocusModScatter extends FocusMod {
    private static final Identifier KEY = TCIds.rl("scatter");

    private static final float MIN_COMPLEXITY = 2.0F;
    private static final float CONE_COMPLEXITY_DIVISOR = 45.0F;
    private static final double JITTER_PER_DEGREE = 0.0075F;
    private static final float POWER_FORKS_DIVISOR = 2.0F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public int getComplexity() {
        return (int) Math.max(MIN_COMPLEXITY,
                2.0F * (getSettingValue("forks") - getSettingValue("cone") / CONE_COMPLEXITY_DIVISOR));
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] angles = new int[]{10, 30, 60, 90, 180, 270, 360};
        String[] anglesDesc = new String[]{"10", "30", "60", "90", "180", "270", "360"};
        return new NodeSetting[]{
                new NodeSetting("forks", "focus.scatter.forks", new NodeSettingIntRange(2, 10)),
                new NodeSetting("cone", "focus.scatter.cone", new NodeSettingIntList(angles, anglesDesc))
        };
    }

    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        if (getParent() == null) {
            return new Trajectory[0];
        }
        List<Trajectory> out = new ArrayList<>();
        int forks = getSettingValue("forks");
        int angle = getSettingValue("cone");
        Trajectory[] supplied = getParent().supplyTrajectories();
        if (supplied != null) {
            RandomSource rand = getPackage().getLevel().getRandom();
            for (Trajectory sT : supplied) {
                for (int a = 0; a < forks; a++) {
                    Vec3 direction = sT.direction().normalize().add(
                            rand.nextGaussian() * JITTER_PER_DEGREE * angle,
                            rand.nextGaussian() * JITTER_PER_DEGREE * angle,
                            rand.nextGaussian() * JITTER_PER_DEGREE * angle);
                    out.add(new Trajectory(sT.source(), direction.normalize()));
                }
            }
        }
        return out.toArray(new Trajectory[0]);
    }

    @Override
    public float getPowerMultiplier() {
        return 1.0F / (getSettingValue("forks") / POWER_FORKS_DIVISOR);
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean isExclusive() {
        return true;
    }
}

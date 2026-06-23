package com.leclowndu93150.thaumcraft.content.world.tree;

import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public final class TCTreeGrowers {
    public static final TreeGrower GREATWOOD = new TreeGrower(
            "thaumcraft:greatwood", Optional.empty(), Optional.empty(), Optional.empty()
    );
    public static final TreeGrower SILVERWOOD = new TreeGrower(
            "thaumcraft:silverwood", Optional.empty(), Optional.empty(), Optional.empty()
    );

    private TCTreeGrowers() {}

    public static void touch() {}
}

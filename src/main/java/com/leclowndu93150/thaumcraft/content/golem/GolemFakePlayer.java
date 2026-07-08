package com.leclowndu93150.thaumcraft.content.golem;

import com.leclowndu93150.thaumcraft.api.golems.IGolemAPI;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class GolemFakePlayer {
    private static final GameProfile PROFILE =
            new GameProfile(UUID.fromString("6f9f0f3f-3b5e-4b6d-9d5c-8e2c9b6a1d42"), "FakeThaumcraftGolem");

    private GolemFakePlayer() {}

    public static FakePlayer get(ServerLevel level) {
        return FakePlayerFactory.get(level, PROFILE);
    }

    public static FakePlayer at(ServerLevel level, IGolemAPI golem) {
        FakePlayer player = get(level);
        player.snapTo(golem.getGolemEntity().getX(), golem.getGolemEntity().getY(), golem.getGolemEntity().getZ(),
                golem.getGolemEntity().getYRot(), golem.getGolemEntity().getXRot());
        return player;
    }
}

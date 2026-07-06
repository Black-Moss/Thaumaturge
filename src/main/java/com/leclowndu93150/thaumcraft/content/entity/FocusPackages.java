package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.Thaumcraft;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class FocusPackages {
    private static final String PACK_KEY = "pack";

    private FocusPackages() {}

    public static void write(RegistryFriendlyByteBuf buffer, @Nullable FocusPackage focusPackage) {
        buffer.writeBoolean(focusPackage != null);
        if (focusPackage != null) {
            FocusPackage.STREAM_CODEC.encode(buffer, focusPackage);
        }
    }

    public static @Nullable FocusPackage read(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        try {
            return FocusPackage.STREAM_CODEC.decode(buffer);
        } catch (Exception e) {
            Thaumcraft.LOGGER.error("Failed to decode focus package from spawn data", e);
            return null;
        }
    }

    public static void save(ValueOutput output, @Nullable FocusPackage focusPackage) {
        output.storeNullable(PACK_KEY, FocusPackage.CODEC, focusPackage);
    }

    public static @Nullable FocusPackage load(ValueInput input) {
        return input.read(PACK_KEY, FocusPackage.CODEC).orElse(null);
    }

    public static List<FocusEffect> effects(@Nullable FocusPackage focusPackage) {
        return focusPackage != null ? focusPackage.getFocusEffects() : List.of();
    }
}

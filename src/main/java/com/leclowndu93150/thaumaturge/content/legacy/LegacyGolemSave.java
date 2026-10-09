package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;

public final class LegacyGolemSave {
    private static final String BUILD_KEY = "props";
    private static final String HOME_KEY = "homepos";
    private static final String FLAGS_KEY = "gflags";
    private static final String RANK_XP_KEY = "rankXP";
    private static final String DYE_KEY = "color";
    private static final int PART_MASK = 0xFF;
    private static final int BITS_PER_FIELD = 8;
    private static final int TOP_FIELD_SHIFT = Long.SIZE - BITS_PER_FIELD;
    private static final int MATERIAL_FIELD = 0;
    private static final int HEAD_FIELD = 1;
    private static final int ARMS_FIELD = 2;
    private static final int LEGS_FIELD = 3;
    private static final int ADDON_FIELD = 4;
    private static final int RANK_FIELD = 5;
    private static final int MIN_RANK = 0;

    private LegacyGolemSave() {}

    public static Optional<GolemProperties> readBuild(ValueInput input) {
        Optional<GolemProperties> encoded = input.read(BUILD_KEY, GolemProperties.CODEC);
        if (encoded.isPresent()) {
            return encoded;
        }
        return input.getLong(BUILD_KEY).map(LegacyGolemSave::unpack);
    }

    public static Optional<BlockPos> readHome(ValueInput input) {
        Optional<BlockPos> encoded = input.read(HOME_KEY, BlockPos.CODEC);
        if (encoded.isPresent()) {
            return encoded;
        }
        return input.getLong(HOME_KEY).map(BlockPos::of);
    }

    public static byte readFlags(ValueInput input, byte defaultValue) {
        return input.getByteOr(FLAGS_KEY, defaultValue);
    }

    public static int readRankXp(ValueInput input, int defaultValue) {
        return input.getInt(RANK_XP_KEY).orElse(defaultValue);
    }

    public static byte readDye(ValueInput input, byte defaultValue) {
        return input.getByteOr(DYE_KEY, defaultValue);
    }

    private static GolemProperties unpack(long bits) {
        GolemProperties fallback = GolemProperties.createDefault();
        GolemMaterial material = part(TTGolemParts.materials(), bits, MATERIAL_FIELD, fallback.material());
        GolemHead head = part(TTGolemParts.heads(), bits, HEAD_FIELD, fallback.head());
        GolemArm arms = part(TTGolemParts.arms(), bits, ARMS_FIELD, fallback.arms());
        GolemLeg legs = part(TTGolemParts.legs(), bits, LEGS_FIELD, fallback.legs());
        GolemAddon addon = part(TTGolemParts.addons(), bits, ADDON_FIELD, fallback.addon());
        int rank = Mth.clamp(field(bits, RANK_FIELD), MIN_RANK, EntityThaumaturgeGolem.MAX_RANK);
        Thaumaturge.LOGGER.debug("Migrated legacy golem build {}", bits);
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    private static int field(long bits, int index) {
        return (int) ((bits >>> (TOP_FIELD_SHIFT - index * BITS_PER_FIELD)) & PART_MASK);
    }

    private static <T> T part(Registry<T> registry, long bits, int index, T fallback) {
        T resolved = registry.byId(field(bits, index));
        return resolved == null ? fallback : resolved;
    }
}

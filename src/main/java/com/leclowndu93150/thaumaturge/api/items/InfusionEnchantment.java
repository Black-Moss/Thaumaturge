package com.leclowndu93150.thaumaturge.api.items;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The closed catalogue of infusion enchantments. Each constant lists the tool classes it may be applied to and its highest level.
 *
 * <p>The ordinal is the network id and the serialized name is the persistent id, so neither may change. The tool class tokens are
 * interpreted by the infusion enchantment helper; tokens that no constant uses stay available to addons.
 *
 * @since 1.0.0
 */
public enum InfusionEnchantment implements StringRepresentable {
    /** Draws harvested and combat drops toward the player. */
    COLLECTOR("collector", Tokens.MAX_ONE, Tokens.AXE, Tokens.PICKAXE, Tokens.SHOVEL, Tokens.WEAPON),
    /** Breaks a plane of blocks around the mined block. */
    DESTRUCTIVE("destructive", Tokens.MAX_ONE, Tokens.AXE, Tokens.PICKAXE, Tokens.SHOVEL),
    /** Follows a vein of logs or ore, breaking the furthest connected block. */
    BURROWING("burrowing", Tokens.MAX_ONE, Tokens.AXE, Tokens.PICKAXE),
    /** Pings nearby ore on a sneak right-click. */
    SOUNDING("sounding", Tokens.MAX_FOUR, Tokens.PICKAXE),
    /** Gives a chance to upgrade a drop, scaling with level. */
    REFINING("refining", Tokens.MAX_FOUR, Tokens.PICKAXE),
    /** Chains a fraction of a strike to nearby enemies. */
    ARCING("arcing", Tokens.MAX_FOUR, Tokens.WEAPON),
    /** Drops aspect crystals distilled from slain creatures. */
    ESSENCE("essence", Tokens.MAX_FIVE, Tokens.WEAPON),
    /** Raises the vis capacity of a chargeable item. */
    VISBATTERY("visbattery", Tokens.MAX_THREE, Tokens.CHARGABLE),
    /** Raises the recharge rate of a chargeable item. */
    VISCHARGE("vischarge", Tokens.MAX_ONE, Tokens.CHARGABLE),
    /** Boots movement enchant. */
    SWIFT("swift", Tokens.MAX_FOUR, Tokens.BOOTS),
    /** Leggings movement enchant. */
    AGILE("agile", Tokens.MAX_ONE, Tokens.LEGS),
    /** Chestplate taint enchant. */
    INFESTED("infested", Tokens.MAX_ONE, Tokens.CHEST),
    /** Places a glimmer light in darkness where a block was mined. */
    LAMPLIGHT("lamplight", Tokens.MAX_ONE, Tokens.AXE, Tokens.PICKAXE, Tokens.SHOVEL);

    /** The persistent codec; writes the serialized name and rejects unknown names. Usable as a map key codec. */
    public static final Codec<InfusionEnchantment> CODEC = StringRepresentable.fromEnum(InfusionEnchantment::values);

    /** The network codec; writes the ordinal as a variable-length integer. */
    public static final StreamCodec<ByteBuf, InfusionEnchantment> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], InfusionEnchantment::ordinal);

    private final String serializedName;
    private final int maxLevel;
    private final Set<String> toolClasses;

    InfusionEnchantment(String serializedName, int maxLevel, String... toolClasses) {
        this.serializedName = serializedName;
        this.maxLevel = maxLevel;
        this.toolClasses = Set.of(toolClasses);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * @return the tool class tokens this enchantment may be applied to; never null and never modified by callers
     */
    public Set<String> toolClasses() {
        return toolClasses;
    }

    /**
     * @return the highest level a stack may carry; positive
     */
    public int maxLevel() {
        return maxLevel;
    }

    private static final class Tokens {
        static final String AXE = "axe";
        static final String PICKAXE = "pickaxe";
        static final String SHOVEL = "shovel";
        static final String WEAPON = "weapon";
        static final String CHARGABLE = "chargable";
        static final String BOOTS = "boots";
        static final String LEGS = "legs";
        static final String CHEST = "chest";
        static final int MAX_ONE = 1;
        static final int MAX_THREE = 3;
        static final int MAX_FOUR = 4;
        static final int MAX_FIVE = 5;

        private Tokens() {}
    }
}

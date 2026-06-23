package com.leclowndu93150.thaumcraft.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record Blueprint(int xSize, int ySize, int zSize, List<Optional<BlueprintPart>> cells) {
    public static final ResourceKey<net.minecraft.core.Registry<Blueprint>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(net.minecraft.resources.Identifier.fromNamespaceAndPath("thaumcraft", "blueprint"));

    private static final Codec<Optional<BlueprintPart>> CELL_CODEC = BlueprintPart.CODEC
            .optionalFieldOf("part")
            .codec();

    public static final Codec<Blueprint> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x_size").forGetter(Blueprint::xSize),
            Codec.INT.fieldOf("y_size").forGetter(Blueprint::ySize),
            Codec.INT.fieldOf("z_size").forGetter(Blueprint::zSize),
            CELL_CODEC.listOf().fieldOf("cells").forGetter(Blueprint::cells)
    ).apply(i, Blueprint::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Blueprint> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            Blueprint::xSize,
            ByteBufCodecs.VAR_INT,
            Blueprint::ySize,
            ByteBufCodecs.VAR_INT,
            Blueprint::zSize,
            ByteBufCodecs.optional(BlueprintPart.STREAM_CODEC).apply(ByteBufCodecs.list()),
            Blueprint::cells,
            Blueprint::new
    );

    public Blueprint {
        if (xSize <= 0 || ySize <= 0 || zSize <= 0) {
            throw new IllegalArgumentException("blueprint dims must be > 0");
        }
        int expected = xSize * ySize * zSize;
        if (cells.size() != expected) {
            throw new IllegalArgumentException("blueprint cells size " + cells.size() + " != " + expected);
        }
        cells = List.copyOf(cells);
    }

    public @Nullable BlueprintPart cell(int y, int x, int z) {
        return this.cells.get(index(y, x, z)).orElse(null);
    }

    private int index(int y, int x, int z) {
        return (y * this.xSize * this.zSize) + (x * this.zSize) + z;
    }
}

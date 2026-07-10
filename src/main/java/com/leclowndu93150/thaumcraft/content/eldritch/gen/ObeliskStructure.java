package com.leclowndu93150.thaumcraft.content.eldritch.gen;

import com.leclowndu93150.thaumcraft.registry.TCStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ObeliskStructure extends SinglePieceStructure {
    public static final MapCodec<ObeliskStructure> CODEC = simpleCodec(ObeliskStructure::new);

    public ObeliskStructure(Structure.StructureSettings settings) {
        super(ObeliskPiece::new, ObeliskPiece.SIZE_XZ, ObeliskPiece.SIZE_XZ, settings);
    }

    @Override
    public StructureType<?> type() {
        return TCStructures.ELDRITCH_OBELISK.get();
    }
}

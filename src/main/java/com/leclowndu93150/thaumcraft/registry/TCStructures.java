package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.world.mound.MoundPiece;
import com.leclowndu93150.thaumcraft.content.world.mound.MoundStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TCIds.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, TCIds.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<MoundStructure>> MOUND =
            STRUCTURE_TYPES.register("mound", () -> () -> MoundStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> MOUND_PIECE =
            STRUCTURE_PIECES.register("mound", () -> (StructurePieceType.ContextlessType) MoundPiece::new);

    private TCStructures() {}

    public static void register(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        STRUCTURE_PIECES.register(modBus);
    }
}

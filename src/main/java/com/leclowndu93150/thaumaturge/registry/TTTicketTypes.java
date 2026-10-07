package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTTicketTypes {
    public static final long TRANSIT_LIFETIME = 600L;
    private static final long ENCOUNTER_LIFETIME = 100L;

    public static final DeferredRegister<TicketType> TICKET_TYPES = DeferredRegister.create(Registries.TICKET_TYPE, TTIds.MODID);

    public static final DeferredHolder<TicketType, TicketType> LABYRINTH_TRANSIT = TICKET_TYPES.register("labyrinth_transit", () -> new TicketType(TRANSIT_LIFETIME, TicketType.FLAG_LOADING));
    public static final DeferredHolder<TicketType, TicketType> LABYRINTH_ENCOUNTER = TICKET_TYPES.register("labyrinth_encounter", () -> new TicketType(ENCOUNTER_LIFETIME, TicketType.FLAG_LOADING));

    private TTTicketTypes() {}

    public static void register(IEventBus modBus) {
        TICKET_TYPES.register(modBus);
    }
}

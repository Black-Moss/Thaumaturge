package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.taint.flux.FluxGooFluidType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TCIds.MODID);

    public static final DeferredHolder<FluidType, FluxGooFluidType> FLUX_GOO = FLUID_TYPES.register(
            "flux_goo",
            () -> new FluxGooFluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.thaumcraft.flux_goo")
                    .viscosity(6000)
                    .density(8)
                    .canSwim(false)
                    .canDrown(false)
                    .canPushEntity(true)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .sound(SoundActions.BUCKET_FILL, TCSounds.GORE.get())
                    .sound(SoundActions.BUCKET_EMPTY, TCSounds.GORE.get())
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    .rarity(Rarity.UNCOMMON)));

    private TCFluidTypes() {}

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
    }
}

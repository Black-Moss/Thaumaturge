package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.taint.effect.FluxTaintEffect;
import com.leclowndu93150.thaumcraft.content.taint.effect.InfectiousVisExhaustEffect;
import com.leclowndu93150.thaumcraft.content.taint.effect.VisExhaustEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, TCIds.MODID);

    public static final Holder<MobEffect> VIS_EXHAUST = MOB_EFFECTS.register(
            "vis_exhaust", VisExhaustEffect::new);

    public static final Holder<MobEffect> INFECTIOUS_VIS_EXHAUST = MOB_EFFECTS.register(
            "infectious_vis_exhaust", InfectiousVisExhaustEffect::new);

    public static final Holder<MobEffect> FLUX_TAINT = MOB_EFFECTS.register(
            "flux_taint", FluxTaintEffect::new);

    private TCMobEffects() {}

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}

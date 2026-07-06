package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.casters.FocusElementType;
import com.leclowndu93150.thaumcraft.api.casters.FocusMediumRoot;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectAir;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectBreak;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectCurse;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectEarth;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectExchange;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectFire;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectFlux;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectFrost;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectHeal;
import com.leclowndu93150.thaumcraft.content.focus.effect.FocusEffectRift;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumBolt;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumCloud;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumMine;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumPlan;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumProjectile;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumSpellBat;
import com.leclowndu93150.thaumcraft.content.focus.medium.FocusMediumTouch;
import com.leclowndu93150.thaumcraft.content.focus.mod.FocusModScatter;
import com.leclowndu93150.thaumcraft.content.focus.mod.FocusModSplitTarget;
import com.leclowndu93150.thaumcraft.content.focus.mod.FocusModSplitTrajectory;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCFocusElements {
    public static final DeferredRegister<FocusElementType> ELEMENTS =
            DeferredRegister.create(FocusElementType.REGISTRY_KEY, TCIds.MODID);

    private static final Registry<FocusElementType> REGISTRY =
            ELEMENTS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<FocusElementType, FocusElementType> ROOT =
            element("root", FocusMediumRoot::new, 10066329);

    public static final DeferredHolder<FocusElementType, FocusElementType> TOUCH =
            element("touch", FocusMediumTouch::new, 11371909);
    public static final DeferredHolder<FocusElementType, FocusElementType> BOLT =
            element("bolt", FocusMediumBolt::new, 11377029);
    public static final DeferredHolder<FocusElementType, FocusElementType> PROJECTILE =
            element("projectile", FocusMediumProjectile::new, 11382149);
    public static final DeferredHolder<FocusElementType, FocusElementType> CLOUD =
            element("cloud", FocusMediumCloud::new, 10071429);
    public static final DeferredHolder<FocusElementType, FocusElementType> MINE =
            element("mine", FocusMediumMine::new, 8760709);
    public static final DeferredHolder<FocusElementType, FocusElementType> PLAN =
            element("plan", FocusMediumPlan::new, 8760728);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPELLBAT =
            element("spellbat", FocusMediumSpellBat::new, 8760748);

    public static final DeferredHolder<FocusElementType, FocusElementType> FIRE =
            element("fire", FocusEffectFire::new, 16734721);
    public static final DeferredHolder<FocusElementType, FocusElementType> FROST =
            element("frost", FocusEffectFrost::new, 14811135);
    public static final DeferredHolder<FocusElementType, FocusElementType> AIR =
            element("air", FocusEffectAir::new, 16777086);
    public static final DeferredHolder<FocusElementType, FocusElementType> EARTH =
            element("earth", FocusEffectEarth::new, 5685248);
    public static final DeferredHolder<FocusElementType, FocusElementType> FLUX =
            element("flux", FocusEffectFlux::new, 8388736);
    public static final DeferredHolder<FocusElementType, FocusElementType> BREAK =
            element("break", FocusEffectBreak::new, 9063176);
    public static final DeferredHolder<FocusElementType, FocusElementType> RIFT =
            element("rift", FocusEffectRift::new, 3084645);
    public static final DeferredHolder<FocusElementType, FocusElementType> EXCHANGE =
            element("exchange", FocusEffectExchange::new, 5735255);
    public static final DeferredHolder<FocusElementType, FocusElementType> CURSE =
            element("curse", FocusEffectCurse::new, 6946821);
    public static final DeferredHolder<FocusElementType, FocusElementType> HEAL =
            element("heal", FocusEffectHeal::new, 14548997);

    public static final DeferredHolder<FocusElementType, FocusElementType> SCATTER =
            element("scatter", FocusModScatter::new, 10066329);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPLIT_TARGET =
            element("split_target", FocusModSplitTarget::new, 10066329);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPLIT_TRAJECTORY =
            element("split_trajectory", FocusModSplitTrajectory::new, 10066329);

    private TCFocusElements() {}

    private static DeferredHolder<FocusElementType, FocusElementType> element(String path,
            Supplier<? extends IFocusElement> factory, int color) {
        return ELEMENTS.register(path, () -> new FocusElementType(factory,
                TCIds.rl("textures/foci/" + path + ".png"), color));
    }

    public static Registry<FocusElementType> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        ELEMENTS.register(modBus);
    }
}

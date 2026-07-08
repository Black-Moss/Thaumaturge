package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaList;
import com.leclowndu93150.thaumcraft.content.casters.CasterArea;
import com.leclowndu93150.thaumcraft.content.essentia.EssentiaContentsComponent;
import com.leclowndu93150.thaumcraft.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumcraft.content.golem.GolemProperties;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TCIds.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> ASPECTS =
            DATA_COMPONENTS.registerComponentType("aspects", builder -> builder
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EssentiaList>> ESSENTIA_CONTENTS =
            DATA_COMPONENTS.registerComponentType("essentia_contents", builder -> builder
                    .persistent(EssentiaContentsComponent.CODEC)
                    .networkSynchronized(EssentiaContentsComponent.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<IAspect>>> ASPECT_FILTER =
            DATA_COMPONENTS.registerComponentType("aspect_filter", builder -> builder
                    .persistent(ResourceKey.codec(IAspect.REGISTRY_KEY))
                    .networkSynchronized(ResourceKey.streamCodec(IAspect.REGISTRY_KEY)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CelestialBody>> CELESTIAL_BODY =
            DATA_COMPONENTS.registerComponentType("celestial_body", builder -> builder
                    .persistent(CelestialBody.CODEC)
                    .networkSynchronized(CelestialBody.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectInstance>> CRYSTAL_ASPECT =
            DATA_COMPONENTS.registerComponentType("crystal_aspect", builder -> builder
                    .persistent(AspectInstance.CODEC)
                    .networkSynchronized(AspectInstance.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FocusPackage>> FOCUS_PACKAGE =
            DATA_COMPONENTS.registerComponentType("focus_package", builder -> builder
                    .persistent(FocusPackage.CODEC)
                    .networkSynchronized(FocusPackage.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackTemplate>> SOCKETED_FOCUS =
            DATA_COMPONENTS.registerComponentType("socketed_focus", builder -> builder
                    .persistent(ItemStackTemplate.CODEC)
                    .networkSynchronized(ItemStackTemplate.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CasterArea>> CASTER_AREA =
            DATA_COMPONENTS.registerComponentType("caster_area", builder -> builder
                    .persistent(CasterArea.CODEC)
                    .networkSynchronized(CasterArea.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> PICKED_BLOCK =
            DATA_COMPONENTS.registerComponentType("picked_block", builder -> builder
                    .persistent(BlockState.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BlockState.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            DATA_COMPONENTS.registerComponentType("charge", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfusionEnchantments>> INFUSION_ENCHANTMENTS =
            DATA_COMPONENTS.registerComponentType("infusion_enchantments", builder -> builder
                    .persistent(InfusionEnchantments.CODEC)
                    .networkSynchronized(InfusionEnchantments.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STACK_WARP =
            DATA_COMPONENTS.registerComponentType("warp", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_ORIENTATION =
            DATA_COMPONENTS.registerComponentType("tool_orientation", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.registerComponentType("energy", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GolemProperties>> GOLEM_PROPERTIES =
            DATA_COMPONENTS.registerComponentType("golem_properties", builder -> builder
                    .persistent(GolemProperties.CODEC)
                    .networkSynchronized(GolemProperties.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GOLEM_XP =
            DATA_COMPONENTS.registerComponentType("golem_xp", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private TCDataComponents() {}

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}

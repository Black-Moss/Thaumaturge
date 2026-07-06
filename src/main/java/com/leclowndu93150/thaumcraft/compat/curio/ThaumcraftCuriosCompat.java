package com.leclowndu93150.thaumcraft.compat.curio;

import com.leclowndu93150.thaumcraft.api.items.IGoggles;
import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumcraft.registry.TCAttributes;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public final class ThaumcraftCuriosCompat {

    private ThaumcraftCuriosCompat() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ThaumcraftCuriosCompat::registerCurio);
        modBus.addListener(ThaumcraftCuriosCompat::onClientSetup);
        NeoForge.EVENT_BUS.addListener(ThaumcraftCuriosCompat::onItemAttributeModifier);
    }

    private static void registerCurio(RegisterCapabilitiesEvent event){
        event.registerItem(CuriosCapability.ITEM,
                (stack,ctx)-> new ICurio() {
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }


                }, TCItems.GOGGLES_REVEALING.get());
    }

    private static void onClientSetup(FMLClientSetupEvent event){
        //ICurioRenderer.register(TCItems.GOGGLES_REVEALING.get(), new GoggleCurioRenderer());
    }
    public static boolean checkForGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) return false;
        ICuriosItemHandler inv = invOpt.get();
        Optional<SlotResult> slotOpt = inv.findFirstCurio(stack->stack.getItem() instanceof IGoggles);
        if (slotOpt.isEmpty()) return false;
        SlotResult slot = slotOpt.get();
        int index = slot.slotContext().index();
        ItemStack stack = inv.getEquippedCurios().getStackInSlot(index);
        if (stack.isEmpty() || !(stack.getItem() instanceof IGoggles g)) {
            return false;
        }
        return g.showIngamePopups(stack, entity);
    }


    private static void onItemAttributeModifier(CurioAttributeModifierEvent event){
        if (event.getItemStack().getItem() instanceof IVisDiscountGear gear){
            float contribution = (float) gear.getVisDiscount(event.getItemStack()) / 100;
            if (contribution != 0){
                event.addModifier(TCAttributes.VIS_DISCOUNT,new AttributeModifier(
                        BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()),
                        contribution,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

}

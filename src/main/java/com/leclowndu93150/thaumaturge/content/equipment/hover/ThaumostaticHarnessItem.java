package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ThaumostaticHarnessItem extends Item implements IHoverGear, IVisDiscountGear {
    private static final int VIS_DISCOUNT = 2;
    private static final int FUEL_PER_TICK = 1;

    public ThaumostaticHarnessItem(Properties properties) {
        super(properties);
    }

    public static ItemStack getJar(ItemStack harness) {
        ItemStackTemplate jar = harness.get(TTDataComponents.HARNESS_JAR.get());
        return jar == null ? ItemStack.EMPTY : jar.create();
    }

    public static void setJar(ItemStack harness, ItemStack jar) {
        if (jar.isEmpty()) {
            harness.remove(TTDataComponents.HARNESS_JAR.get());
        } else {
            harness.set(TTDataComponents.HARNESS_JAR.get(), ItemStackTemplate.fromNonEmptyStack(jar));
        }
    }

    public static boolean isFuelJar(ItemStack jar) {
        return potentia(jar) != null;
    }

    private static @Nullable AspectInstance potentia(ItemStack jar) {
        if (!(jar.getItem() instanceof JarItem)) {
            return null;
        }
        for (AspectInstance entry : ComponentEssentia.jar(jar).getAspects().entries()) {
            if (entry.aspect().is(TTAspects.POTENTIA) && entry.amount() > 0) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer opener) {
            openHarness(opener, hand);
        }
        return InteractionResult.SUCCESS;
    }

    private static void openHarness(ServerPlayer player, InteractionHand hand) {
        Component title = player.getItemInHand(hand).getHoverName();
        MenuConstructor factory = (containerId, inventory, viewer) -> new MenuThaumostaticHarness(containerId, inventory, hand);
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        player.openMenu(new SimpleMenuProvider(factory, title), buf -> buf.writeBoolean(mainHand));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot == EquipmentSlot.CHEST) {
            tickHover(entity, stack);
        }
    }

    private void tickHover(Entity wearer, ItemStack stack) {
        if (wearer instanceof ServerPlayer player) {
            HoverManager.tick(player, stack, this);
        }
    }

    @Override
    public int getMaxHoverFuel(ItemStack stack) {
        return BlockEntityJar.CAPACITY;
    }

    @Override
    public int getHoverFuel(ItemStack stack) {
        return Optional.ofNullable(potentia(getJar(stack))).map(AspectInstance::amount).orElse(0);
    }

    @Override
    public void consumeHoverFuel(ItemStack stack) {
        ItemStack jar = getJar(stack);
        Optional.ofNullable(potentia(jar)).ifPresent(fuel -> {
            drainOne(jar, fuel);
            setJar(stack, jar);
        });
    }

    private static void drainOne(ItemStack jar, AspectInstance fuel) {
        ComponentEssentia<?> contents = ComponentEssentia.jar(jar);
        contents.setAspects(contents.getAspects().remove(fuel.aspect(), FUEL_PER_TICK));
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ItemStack jar = getJar(stack);
        if (jar.getItem() instanceof JarItem) {
            for (AspectInstance entry : ComponentEssentia.jar(jar).getAspects().sortedByTag()) {
                tooltip.accept(Component.translatable("tooltip.thaumaturge.thaumostatic_harness.fuel", AspectComponents.name(entry.aspect()), entry.amount()).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}

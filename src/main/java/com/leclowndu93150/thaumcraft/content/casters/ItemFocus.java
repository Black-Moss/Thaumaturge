package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.casters.FocusMediumRoot;
import com.leclowndu93150.thaumcraft.api.casters.FocusModSplit;
import com.leclowndu93150.thaumcraft.api.casters.FocusNode;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class ItemFocus extends Item {
    public static final int WHITE = 0xFFFFFF;
    private static final int VIS_COST_DIVISOR = 5;
    private static final int MIN_ACTIVATION_TICKS = 5;
    private static final int ACTIVATION_COST_DIVISOR = 5;
    private static final int ACTIVATION_SCALE_DIVISOR = 4;
    private static final String INDENT = "  ";

    private final int maxComplexity;

    public ItemFocus(Properties properties, int maxComplexity) {
        super(properties);
        this.maxComplexity = maxComplexity;
    }

    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        focusStack.set(TCDataComponents.FOCUS_PACKAGE.get(), core);
    }

    public static @Nullable FocusPackage getPackage(ItemStack focusStack) {
        if (focusStack.isEmpty()) {
            return null;
        }
        return focusStack.get(TCDataComponents.FOCUS_PACKAGE.get());
    }

    public static int getFocusColor(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            ItemStackTemplate socketed = focusStack.get(TCDataComponents.SOCKETED_FOCUS.get());
            if (socketed != null) {
                core = socketed.get(TCDataComponents.FOCUS_PACKAGE.get());
            }
        }
        return getFocusColor(core);
    }

    public static int getFocusColor(@Nullable FocusPackage core) {
        if (core == null) {
            return WHITE;
        }
        List<FocusEffect> effects = core.getFocusEffects();
        if (effects.isEmpty()) {
            return WHITE;
        }
        int r = 0;
        int g = 0;
        int b = 0;
        for (FocusEffect effect : effects) {
            int color = FocusEngine.getElementColor(effect.getKey());
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }
        r /= effects.size();
        g /= effects.size();
        b /= effects.size();
        return (r << 16) | (g << 8) | b;
    }

    public @Nullable String getSortingHelper(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return null;
        }
        return focusStack.getHoverName().getString() + core.getSortingHelper();
    }

    public float getVisCost(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        return core == null ? 0.0F : (float) core.getComplexity() / VIS_COST_DIVISOR;
    }

    public int getActivationTime(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return 0;
        }
        int complexity = core.getComplexity();
        return Math.max(MIN_ACTIVATION_TICKS, complexity / ACTIVATION_COST_DIVISOR * (complexity / ACTIVATION_SCALE_DIVISOR));
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public static Component formatVis(float amount) {
        float rounded = Math.round(amount * 10.0F) / 10.0F;
        if (Mth.equal(rounded, (int) rounded)) {
            return Component.literal(String.valueOf((int) rounded));
        }
        return Component.literal(String.valueOf(rounded));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag flag) {
        addFocusInformation(stack, builder);
    }

    public void addFocusInformation(ItemStack focusStack, Consumer<Component> builder) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return;
        }
        builder.accept(Component.translatable("tooltip.thaumcraft.focus.vis_cost", formatVis(getVisCost(focusStack))));
        for (IFocusElement element : core.getNodes()) {
            if (element instanceof FocusNode node && !(node instanceof FocusMediumRoot)) {
                buildInfo(builder, node, 0);
            }
        }
    }

    private void buildInfo(Consumer<Component> builder, FocusNode node, int depth) {
        MutableComponent line = Component.literal(INDENT.repeat(depth));
        line.append(Component.translatable(node.getNameKey()).withStyle(ChatFormatting.DARK_PURPLE));
        if (!node.getSettingList().isEmpty()) {
            MutableComponent settings = Component.literal(" [");
            boolean following = false;
            for (String settingKey : node.getSettingList()) {
                NodeSetting setting = node.getSetting(settingKey);
                if (setting == null) {
                    continue;
                }
                if (following) {
                    settings.append(", ");
                }
                settings.append(setting.getName()).append(" ").append(setting.getValueText());
                following = true;
            }
            settings.append("]");
            line.append(settings.withStyle(ChatFormatting.DARK_AQUA));
        }
        builder.accept(line);
        if (node instanceof FocusModSplit split) {
            for (FocusPackage branch : split.getSplitPackages()) {
                for (IFocusElement element : branch.getNodes()) {
                    if (element instanceof FocusNode child && !(child instanceof FocusMediumRoot)) {
                        buildInfo(builder, child, depth + 1);
                    }
                }
            }
        }
    }
}

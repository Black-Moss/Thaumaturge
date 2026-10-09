package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class FocusItem extends Item {
    private static final String INDENT = "  ";
    private static final String WITH_ASPECT_KEY = "tooltip.thaumaturge.focus.with_aspect";
    private static final String WITH_SETTINGS_KEY = "tooltip.thaumaturge.focus.with_settings";
    private static final String SETTING_KEY = "tooltip.thaumaturge.focus.setting";
    private static final String LIST_KEY = "tooltip.thaumaturge.list";

    public FocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        describe(stack, context.registries(), builder);
    }

    public static void describe(ItemStack focus, HolderLookup.@Nullable Provider registries, Consumer<Component> builder) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null || registries == null) {
            builder.accept(SpellText.blank());
            return;
        }
        Optional<SpellSummary> summary = FocusItems.summary(focus, registries, null);
        if (summary.isEmpty()) {
            return;
        }
        builder.accept(SpellText.visCost(FocusItems.formatVis(summary.get().vis())));
        builder.accept(SpellText.styleLine(spell.style()));
        for (SpellProblem problem : summary.get().problems()) {
            if (problem.fatal()) {
                builder.accept(SpellText.invalid());
                builder.accept(problem.message().copy().withStyle(ChatFormatting.RED));
                break;
            }
        }
        for (SpellNode child : spell.root().children()) {
            describeNode(child, registries, builder, 0);
        }
    }

    private static void describeNode(SpellNode node, HolderLookup.Provider registries, Consumer<Component> builder, int depth) {
        MutableComponent text = SpellText.partName(node.part()).withStyle(ChatFormatting.DARK_PURPLE);
        Optional<SpellPart> part = Spells.part(registries, node.part());
        if (part.isPresent()) {
            text = decorate(text, node, part.get(), registries);
        }
        builder.accept(Component.literal(INDENT.repeat(depth)).append(text));
        for (SpellNode child : node.children()) {
            describeNode(child, registries, builder, depth + 1);
        }
    }

    private static MutableComponent decorate(MutableComponent name, SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        MutableComponent text = name;
        if (part.aspect().selectable()) {
            Optional<ResourceKey<IAspect>> aspect = part.aspect().resolve(node.aspect(), registries);
            if (aspect.isPresent()) {
                text = Component.translatable(WITH_ASPECT_KEY, text, SpellText.aspectName(aspect.get()).withStyle(ChatFormatting.GOLD));
            }
        }
        MutableComponent values = null;
        for (SettingSpec spec : part.settings()) {
            Component value = Component.translatable(SETTING_KEY, SpellText.setting(spec), spec.label(node.settings().getOrDefault(spec.key(), spec.defaultValue())));
            values = values == null ? value.copy() : Component.translatable(LIST_KEY, values, value);
        }
        if (values != null) {
            text = Component.translatable(WITH_SETTINGS_KEY, text, values.withStyle(ChatFormatting.DARK_AQUA));
        }
        return text;
    }
}

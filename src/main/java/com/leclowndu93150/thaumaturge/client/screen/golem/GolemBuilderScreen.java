package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTButtonIcon;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTHoverButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTScrollButton;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.GolemStats;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.MenuGolemBuilder;
import com.leclowndu93150.thaumaturge.network.ServerboundGolemPressPayload;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderScreen extends AbstractTTContainerScreen<MenuGolemBuilder> {
    private static final Identifier SHEET = TTIds.rl("textures/gui/gui_golembuilder.png");
    private static final int SHEET_SIZE = 256;
    private static final int IMAGE_WIDTH = 208;
    private static final int IMAGE_HEIGHT = 224;

    private static final int NO_TINT = 0xFFFFFFFF;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int MARK_TINT = 0x80FFFFFF;
    private static final int CRAFT_ALPHA_SHIFT = 24;
    private static final int RGB_MASK = 0x00FFFFFF;

    private static final int SLOT_MATERIAL = 0;
    private static final int SLOT_HEAD = 1;
    private static final int SLOT_ARMS = 2;
    private static final int SLOT_LEGS = 3;
    private static final int SLOT_ADDON = 4;

    private static final String KIND_HEAD = "head";
    private static final String KIND_ARMS = "arm";
    private static final String KIND_LEGS = "leg";
    private static final String KIND_ADDON = "addon";

    private static final int ICON_SIZE = 16;
    private static final int MATERIAL_X = 24;
    private static final int MATERIAL_Y = 24;
    private static final int HEAD_X = 120;
    private static final int HEAD_Y = 24;
    private static final int ARMS_X = 120;
    private static final int ARMS_Y = 48;
    private static final int LEGS_X = 120;
    private static final int LEGS_Y = 72;
    private static final int ADDON_X = 24;
    private static final int ADDON_Y = 72;

    private static final int ARROW_LEFT_OFFSET_X = -19;
    private static final int ARROW_RIGHT_OFFSET_X = 9;
    private static final int ARROW_OFFSET_Y = -5;

    private static final int TRAIT_CENTER_X = 72;
    private static final int TRAIT_COLUMN_HEIGHT = 4;
    private static final int TRAIT_PITCH = 16;
    private static final int TRAIT_CENTER_Y = 48;
    private static final int TRAIT_COLUMN_SHIFT = 8;

    private static final int MACHINA_X = 152;
    private static final int MACHINA_Y = 24;
    private static final int COMPONENT_ROWS = 3;
    private static final int COMPONENT_PITCH = 16;
    private static final int MARK_X = 144;
    private static final int MARK_Y = 16;
    private static final int MARK_U = 240;
    private static final int MARK_V = 0;

    private static final int PROGRESS_X = 145;
    private static final int PROGRESS_Y = 89;
    private static final int PROGRESS_U = 209;
    private static final int PROGRESS_V = 89;
    private static final int PROGRESS_WIDTH = 46;
    private static final int PROGRESS_HEIGHT = 6;

    private static final int CRAFT_X = 120;
    private static final int CRAFT_Y = 104;
    private static final int CRAFT_WIDTH = 24;
    private static final int CRAFT_HEIGHT = 16;
    private static final int CRAFT_U = 216;
    private static final int CRAFT_V = 64;
    private static final int CRAFT_DISABLED_V = 40;

    private static final int[] SOCKET_X = {12, 12, 108, 108, 108};
    private static final int[] SOCKET_Y = {12, 60, 12, 36, 60};
    private static final int SOCKET_SIZE = 24;
    private static final int SOCKET_U = 228;
    private static final int SOCKET_V = 124;

    private static final int COST_RIGHT = 162;
    private static final int COST_TOP = 24;
    private static final int COST_PER_TRAIT = 2;

    private static final int STAT_COUNT = 3;
    private static final int[] STAT_CENTER_X = {48, 72, 97};
    private static final int STAT_ICON_TOP = 92;
    private static final int STAT_ICON_SIZE = 9;
    private static final int STAT_ICON_HALF = 4;
    private static final int STAT_TEXT_TOP = 108;
    private static final double STAT_DISPLAY_SCALE = 0.5D;
    private static final int STAT_DECIMALS = 1;
    private static final String[] STAT_KEYS = {"gui.thaumaturge.golembuilder.stat.health", "gui.thaumaturge.golembuilder.stat.armor", "gui.thaumaturge.golembuilder.stat.damage"};

    private static final Component NEWLINE = Component.literal("\n");

    private final List<AbstractWidget> controls = new ArrayList<>();
    private final Component[] statNames = new Component[STAT_COUNT];
    private final Component[] statTexts = new Component[STAT_COUNT];

    private GolemPartChoice<GolemMaterial> materials;
    private GolemPartChoice<GolemHead> heads;
    private GolemPartChoice<GolemArm> arms;
    private GolemPartChoice<GolemLeg> legs;
    private GolemPartChoice<GolemAddon> addons;
    private GolemProperties properties = GolemProperties.createDefault();
    private boolean complete;
    private List<ItemStack> components = List.of();
    private boolean[] owned = new boolean[0];
    private boolean @Nullable [] lastStuff;
    private @Nullable Component costText;
    private boolean inProgress;
    private CraftButton craftButton;

    public GolemBuilderScreen(MenuGolemBuilder menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SHEET, IMAGE_WIDTH, IMAGE_HEIGHT);
        for (int i = 0; i < STAT_COUNT; i++) {
            statNames[i] = Component.translatable(STAT_KEYS[i]);
        }
    }

    @Override
    protected void init() {
        super.init();
        Player player = minecraft.player;
        materials = new GolemPartChoice<>(SLOT_MATERIAL, TTGolemParts.materials(), GolemMaterial::research, player);
        heads = new GolemPartChoice<>(SLOT_HEAD, TTGolemParts.heads(), GolemPart::research, player);
        arms = new GolemPartChoice<>(SLOT_ARMS, TTGolemParts.arms(), GolemPart::research, player);
        legs = new GolemPartChoice<>(SLOT_LEGS, TTGolemParts.legs(), GolemPart::research, player);
        addons = new GolemPartChoice<>(SLOT_ADDON, TTGolemParts.addons(), GolemPart::research, player);
        rebuildSelection();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshOwnership(false);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (int k = 0; k < owned.length; k++) {
            if (!owned[k]) {
                blit(graphics, leftPos + MARK_X + COMPONENT_PITCH * (k / COMPONENT_ROWS), topPos + MARK_Y + COMPONENT_PITCH * (1 + k % COMPONENT_ROWS), MARK_U, MARK_V, ICON_SIZE, ICON_SIZE,
                        MARK_TINT);
            }
        }
        int cost = menu.cost();
        if (cost > 0) {
            int width = Math.max(0, (int) (PROGRESS_WIDTH * (1.0F - (float) cost / Math.max(1, menu.maxCost()))));
            if (width > 0) {
                blit(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y, PROGRESS_U, PROGRESS_V, width, PROGRESS_HEIGHT, NO_TINT);
            }
        }
        for (int i = 0; i < STAT_COUNT; i++) {
            graphics.text(font, statTexts[i], leftPos + STAT_CENTER_X[i] - font.width(statTexts[i]) / 2, topPos + STAT_TEXT_TOP, TEXT_WHITE, true);
            int iconX = leftPos + STAT_CENTER_X[i] - STAT_ICON_HALF;
            if (mouseX >= iconX && mouseY >= topPos + STAT_ICON_TOP && mouseX < iconX + STAT_ICON_SIZE && mouseY < topPos + STAT_ICON_TOP + STAT_ICON_SIZE) {
                graphics.setTooltipForNextFrame(statNames[i], mouseX, mouseY);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (costText != null) {
            graphics.text(font, costText, COST_RIGHT - font.width(costText), COST_TOP, TEXT_WHITE, true);
        }
        for (int i = 0; i < SOCKET_X.length; i++) {
            blit(graphics, SOCKET_X[i], SOCKET_Y[i], SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, NO_TINT);
        }
    }

    private void rebuildSelection() {
        clearWidgets();
        controls.clear();
        complete = !materials.isEmpty() && !heads.isEmpty() && !arms.isEmpty() && !legs.isEmpty() && !addons.isEmpty();
        properties = complete ? new GolemProperties(materials.selected(), heads.selected(), arms.selected(), legs.selected(), addons.selected(), 0) : GolemProperties.createDefault();
        components = complete ? properties.components() : List.of();
        refreshStats();
        refreshCost();

        addPartIcons();
        addArrows(materials, MATERIAL_X, MATERIAL_Y);
        addArrows(heads, HEAD_X, HEAD_Y);
        addArrows(arms, ARMS_X, ARMS_Y);
        addArrows(legs, LEGS_X, LEGS_Y);
        addArrows(addons, ADDON_X, ADDON_Y);
        if (complete) {
            addTraitIcons();
            addComponentIcons();
        }
        craftButton = addRenderableWidget(new CraftButton(leftPos + CRAFT_X, topPos + CRAFT_Y, Component.translatable("gui.thaumaturge.golembuilder.craft"), this::craft));

        owned = new boolean[components.size()];
        BlockEntityGolemBuilder machine = menu.blockEntity();
        lastStuff = machine == null ? null : machine.hasStuff;
        refreshOwnership(true);
        if (complete && machine != null) {
            ClientPacketDistributor.sendToServer(new ServerboundGolemPressPayload(machine.getBlockPos(), properties, false));
        }
    }

    private void refreshStats() {
        double health = complete ? GolemStats.health(properties) : 0.0D;
        double armor = complete ? GolemStats.armor(properties) : 0.0D;
        double damage = complete ? GolemStats.meleeDamage(properties) : 0.0D;
        statTexts[0] = formatStat(health);
        statTexts[1] = formatStat(armor);
        statTexts[2] = formatStat(damage);
    }

    private static Component formatStat(double stat) {
        return Component.literal(BigDecimal.valueOf(stat * STAT_DISPLAY_SCALE).setScale(STAT_DECIMALS, RoundingMode.HALF_UP).toPlainString());
    }

    private void refreshCost() {
        if (components.isEmpty()) {
            costText = null;
            return;
        }
        int cost = COST_PER_TRAIT * properties.traits().size();
        for (ItemStack stack : components) {
            cost += stack.getCount();
        }
        costText = Component.literal(Integer.toString(cost));
    }

    private void addPartIcons() {
        if (!materials.isEmpty()) {
            GolemMaterial material = materials.selected();
            Identifier id = materials.selectedId();
            ItemStack placer = new ItemStack(TTItems.GOLEM_PLACER.get());
            placer.set(TTDataComponents.GOLEM_PROPERTIES.get(), GolemProperties.createDefault().withMaterial(material).withRank(0));
            addControl(icon(MATERIAL_X, MATERIAL_Y, new TTButtonIcon.StackIcon(placer), Component.translatable(GolemMaterial.nameKey(id)), Component.translatable(GolemMaterial.descriptionKey(id))));
        }
        addPartIcon(heads, KIND_HEAD, HEAD_X, HEAD_Y);
        addPartIcon(arms, KIND_ARMS, ARMS_X, ARMS_Y);
        addPartIcon(legs, KIND_LEGS, LEGS_X, LEGS_Y);
        if (!addons.isEmpty() && addons.selected() != TTGolemParts.ADDON_NONE.get()) {
            addPartIcon(addons, KIND_ADDON, ADDON_X, ADDON_Y);
        }
    }

    private void addPartIcon(GolemPartChoice<? extends GolemPart> choice, String kind, int centerX, int centerY) {
        if (choice.isEmpty()) {
            return;
        }
        Identifier id = choice.selectedId();
        addControl(icon(centerX, centerY, new TTButtonIcon.TextureIcon(choice.selected().icon()), Component.translatable(GolemPart.nameKey(kind, id)),
                Component.translatable(GolemPart.descriptionKey(kind, id))));
    }

    private void addArrows(GolemPartChoice<?> choice, int centerX, int centerY) {
        if (!choice.hasAlternatives()) {
            return;
        }
        addControl(TTScrollButton.of(leftPos + centerX + ARROW_LEFT_OFFSET_X, topPos + centerY + ARROW_OFFSET_Y, TTScrollButton.Direction.LEFT,
                Component.translatable("gui.thaumaturge.golem_builder.previous"), () -> step(choice, -1)));
        addControl(TTScrollButton.of(leftPos + centerX + ARROW_RIGHT_OFFSET_X, topPos + centerY + ARROW_OFFSET_Y, TTScrollButton.Direction.RIGHT,
                Component.translatable("gui.thaumaturge.golem_builder.next"), () -> step(choice, 1)));
    }

    private void addTraitIcons() {
        List<GolemTrait> traits = new ArrayList<>(properties.traits());
        int count = traits.size();
        if (count == 0) {
            return;
        }
        int columns = (count + TRAIT_COLUMN_HEIGHT - 1) / TRAIT_COLUMN_HEIGHT;
        int firstColumnRows = Math.min(count, TRAIT_COLUMN_HEIGHT);
        int top = TRAIT_CENTER_Y - TRAIT_PITCH / 2 * (firstColumnRows - 1);
        int left = TRAIT_CENTER_X - TRAIT_COLUMN_SHIFT * (columns - 1);
        for (int i = 0; i < count; i++) {
            GolemTrait trait = traits.get(i);
            Identifier id = GolemPartChoice.idOf(TTGolemTraits.registry(), trait);
            addControl(icon(left + TRAIT_PITCH * (i / TRAIT_COLUMN_HEIGHT), top + TRAIT_PITCH * (i % TRAIT_COLUMN_HEIGHT), new TTButtonIcon.TextureIcon(trait.icon()),
                    Component.translatable(GolemTrait.nameKey(id)), Component.translatable(GolemTrait.descriptionKey(id))));
        }
    }

    private void addComponentIcons() {
        if (components.isEmpty()) {
            return;
        }
        Holder<IAspect> machina = minecraft.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(TTAspects.MACHINA).<Holder<IAspect>>map(reference -> reference).orElse(null);
        if (machina != null) {
            addControl(icon(MACHINA_X, MACHINA_Y, new TTButtonIcon.AspectIcon(machina), AspectComponents.name(machina), AspectComponents.description(machina)));
        }
        for (int k = 0; k < components.size(); k++) {
            ItemStack stack = components.get(k);
            addControl(icon(MACHINA_X + COMPONENT_PITCH * (k / COMPONENT_ROWS), MACHINA_Y + COMPONENT_PITCH * (1 + k % COMPONENT_ROWS), new TTButtonIcon.StackIcon(stack.copy()), stack.getHoverName(),
                    null));
        }
    }

    private TTHoverButton icon(int centerX, int centerY, TTButtonIcon icon, Component name, @Nullable Component description) {
        TTHoverButton button = TTHoverButton.centered(leftPos + centerX, topPos + centerY, ICON_SIZE, icon, name, GolemBuilderScreen::ignore);
        if (description != null) {
            button.setDescription(description);
        }
        return button;
    }

    private void addControl(AbstractWidget widget) {
        controls.add(addRenderableWidget(widget));
    }

    private static void ignore() {}

    private void step(GolemPartChoice<?> choice, int delta) {
        choice.step(delta);
        rebuildSelection();
    }

    private void craft() {
        BlockEntityGolemBuilder machine = menu.blockEntity();
        if (machine == null) {
            return;
        }
        ClientPacketDistributor.sendToServer(new ServerboundGolemPressPayload(machine.getBlockPos(), properties, true));
        inProgress = true;
        applyStates();
        refreshCraftTooltip();
    }

    private void refreshOwnership(boolean force) {
        BlockEntityGolemBuilder machine = menu.blockEntity();
        boolean[] stuff = machine == null ? null : machine.hasStuff;
        boolean dirty = force || stuff != lastStuff;
        lastStuff = stuff;
        int cost = menu.cost();
        boolean wasInProgress = inProgress;
        inProgress = cost > 0;
        dirty |= wasInProgress != inProgress;
        for (int k = 0; k < owned.length; k++) {
            boolean now = isOwned(k, stuff);
            if (now != owned[k]) {
                owned[k] = now;
                dirty = true;
            }
        }
        if (dirty) {
            applyStates();
            refreshCraftTooltip();
        }
    }

    private boolean isOwned(int index, boolean @Nullable [] stuff) {
        if (stuff != null && index < stuff.length && stuff[index]) {
            return true;
        }
        return minecraft.player != null && InvHelper.isPlayerCarryingAmount(minecraft.player, components.get(index), true);
    }

    private boolean allOwned() {
        for (boolean flag : owned) {
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    private void applyStates() {
        for (AbstractWidget widget : controls) {
            widget.active = !inProgress;
        }
        craftButton.active = complete && allOwned() && !inProgress;
    }

    private void refreshCraftTooltip() {
        MutableComponent tip = Component.translatable("gui.thaumaturge.golembuilder.craft");
        if (inProgress) {
            tip.append(NEWLINE).append(Component.translatable("gui.thaumaturge.golembuilder.problem.in_progress").withStyle(ChatFormatting.RED));
        } else if (components.isEmpty()) {
            tip.append(NEWLINE).append(Component.translatable("gui.thaumaturge.golembuilder.problem.no_parts").withStyle(ChatFormatting.RED));
        } else {
            for (int k = 0; k < owned.length; k++) {
                if (!owned[k]) {
                    ItemStack stack = components.get(k);
                    tip.append(NEWLINE).append(Component.translatable("gui.thaumaturge.golembuilder.problem.component", stack.getCount(), stack.getHoverName()).withStyle(ChatFormatting.RED));
                }
            }
            if (allOwned()) {
                tip.append(NEWLINE).append(Component.translatable("gui.thaumaturge.golembuilder.problem.ready").withStyle(ChatFormatting.GREEN));
            }
        }
        craftButton.setTooltip(Tooltip.create(tip));
    }

    private static void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, x, y, (float) u, (float) v, width, height, width, height, SHEET_SIZE, SHEET_SIZE, color);
    }

    static final class CraftButton extends TTButton {
        CraftButton(int x, int y, Component message, Runnable onPress) {
            super(x, y, CRAFT_WIDTH, CRAFT_HEIGHT, message, onPress);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int alpha = isHovered() && active ? HOVER_ALPHA : IDLE_ALPHA;
            GolemBuilderScreen.blit(graphics, getX(), getY(), CRAFT_U, CRAFT_V, CRAFT_WIDTH, CRAFT_HEIGHT, (alpha << CRAFT_ALPHA_SHIFT) | RGB_MASK);
            if (!active) {
                GolemBuilderScreen.blit(graphics, getX(), getY(), CRAFT_U, CRAFT_DISABLED_V, CRAFT_WIDTH, CRAFT_HEIGHT, NO_TINT);
            }
        }
    }
}

package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderScreen extends AbstractTTContainerScreen<MenuGolemBuilder> {
    private static final Identifier TEXTURE = TTIds.rl("textures/gui/gui_golembuilder.png");

    private static final int IMAGE_WIDTH = 208;
    private static final int IMAGE_HEIGHT = 224;
    private static final int MISSING_U = 240;
    private static final int MISSING_V = 0;
    private static final int COMPONENT_GRID_X = 144;
    private static final int COMPONENT_GRID_Y = 16;
    private static final int PROGRESS_X = 145;
    private static final int PROGRESS_Y = 89;
    private static final int PROGRESS_U = 209;
    private static final int PROGRESS_V = 89;
    private static final int PROGRESS_WIDTH = 46;
    private static final int PROGRESS_HEIGHT = 6;
    private static final int SOCKET_U = 228;
    private static final int SOCKET_V = 124;
    private static final int SOCKET_SIZE = 24;
    private static final int STAT_Y = 108;
    private static final int STAT_HEARTS_X = 48;
    private static final int STAT_ARMOR_X = 72;
    private static final int STAT_DAMAGE_X = 97;
    private static final int STAT_ICON_Y = 92;
    private static final int STAT_ICON_SIZE = 9;
    private static final int COST_RIGHT_X = 162;
    private static final int COST_Y = 24;
    private static final int CRAFT_X = 120;
    private static final int CRAFT_Y = 104;
    private static final int CRAFT_WIDTH = 24;
    private static final int CRAFT_HEIGHT = 16;
    private static final int CRAFT_U = 216;
    private static final int CRAFT_V = 64;
    private static final int CRAFT_DISABLED_V = 40;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ICON_SIZE = 16;
    private static final int CELL = 16;
    private static final int HALF_CELL = CELL / 2;
    private static final int ARROW_BACK_OFFSET = 19;
    private static final int ARROW_FORWARD_OFFSET = 9;
    private static final int ARROW_HALF_HEIGHT = 5;
    private static final int MATERIAL_X = 24;
    private static final int MATERIAL_Y = 24;
    private static final int ADDON_X = 24;
    private static final int ADDON_Y = 72;
    private static final int PART_COLUMN_X = 120;
    private static final int HEAD_Y = 24;
    private static final int ARMS_Y = 48;
    private static final int LEGS_Y = 72;
    private static final int RING_X = 72;
    private static final int RING_Y = 48;
    private static final int TRAITS_PER_COLUMN = 4;
    private static final float STAT_DISPLAY_SCALE = 0.5F;
    private static final float ONE_DECIMAL = 10.0F;
    private static final int PLAIN_TINT = 0xFFFFFF;
    private static final String PREVIOUS_KEY = "gui.thaumaturge.golem_builder.previous";
    private static final String NEXT_KEY = "gui.thaumaturge.golem_builder.next";

    private static int headIndex;
    private static int matIndex;
    private static int armIndex;
    private static int legIndex;
    private static int addonIndex;

    private final List<GolemHead> valHeads = new ArrayList<>();
    private final List<GolemMaterial> valMats = new ArrayList<>();
    private final List<GolemArm> valArms = new ArrayList<>();
    private final List<GolemLeg> valLegs = new ArrayList<>();
    private final List<GolemAddon> valAddons = new ArrayList<>();

    private GolemProperties props = GolemProperties.createDefault();
    private float hearts;
    private float armor;
    private float damage;
    private int cost;
    private boolean allFound;
    private List<ItemStack> components = List.of();
    private boolean[] owns = new boolean[0];
    private boolean disableAll;
    private boolean[] lastStuff;
    private CraftButton craftButton;

    public GolemBuilderScreen(MenuGolemBuilder menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        valHeads.clear();
        TTGolemParts.heads().forEach(head -> {
            if (knowsAll(head.research()))
                valHeads.add(head);
        });
        valMats.clear();
        TTGolemParts.materials().forEach(mat -> {
            if (knowsAll(mat.research()))
                valMats.add(mat);
        });
        valArms.clear();
        TTGolemParts.arms().forEach(arm -> {
            if (knowsAll(arm.research()))
                valArms.add(arm);
        });
        valLegs.clear();
        TTGolemParts.legs().forEach(leg -> {
            if (knowsAll(leg.research()))
                valLegs.add(leg);
        });
        valAddons.clear();
        TTGolemParts.addons().forEach(addon -> {
            if (knowsAll(addon.research()))
                valAddons.add(addon);
        });
        if (headIndex >= valHeads.size()) {
            headIndex = 0;
        }
        if (matIndex >= valMats.size()) {
            matIndex = 0;
        }
        if (armIndex >= valArms.size()) {
            armIndex = 0;
        }
        if (legIndex >= valLegs.size()) {
            legIndex = 0;
        }
        if (addonIndex >= valAddons.size()) {
            addonIndex = 0;
        }
        gatherInfo();
    }

    private boolean knowsAll(List<Identifier> research) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        for (Identifier id : research) {
            if (!knowledge.isResearchComplete(id)) {
                return false;
            }
        }
        return true;
    }

    private void gatherInfo() {
        clearWidgets();
        boolean complete = !valHeads.isEmpty() && !valMats.isEmpty() && !valArms.isEmpty() && !valLegs.isEmpty() && !valAddons.isEmpty();
        props = complete
                ? new GolemProperties(valMats.get(matIndex), valHeads.get(headIndex), valArms.get(armIndex), valLegs.get(legIndex), valAddons.get(addonIndex), 0)
                : GolemProperties.createDefault();
        components = List.of();
        owns = new boolean[0];
        if (!valMats.isEmpty()) {
            GolemMaterial material = valMats.get(matIndex);
            Identifier id = keyOf(TTGolemParts.materials(), material);
            TTHoverButton button = TTHoverButton.centered(leftPos + MATERIAL_X, topPos + MATERIAL_Y, ICON_SIZE, new TTButtonIcon.StackIcon(materialStack(material)),
                    Component.translatable(GolemMaterial.nameKey(id)), () -> {
                    });
            button.setDescription(Component.translatable(GolemMaterial.descriptionKey(id)));
            addRenderableWidget(button);
        }
        addArrows(MATERIAL_X, MATERIAL_Y, valMats.size(), () -> matIndex, index -> matIndex = index);
        addPart(PART_COLUMN_X, HEAD_Y, valHeads, headIndex, TTGolemParts.heads(), "head");
        addArrows(PART_COLUMN_X, HEAD_Y, valHeads.size(), () -> headIndex, index -> headIndex = index);
        addPart(PART_COLUMN_X, ARMS_Y, valArms, armIndex, TTGolemParts.arms(), "arm");
        addArrows(PART_COLUMN_X, ARMS_Y, valArms.size(), () -> armIndex, index -> armIndex = index);
        addPart(PART_COLUMN_X, LEGS_Y, valLegs, legIndex, TTGolemParts.legs(), "leg");
        addArrows(PART_COLUMN_X, LEGS_Y, valLegs.size(), () -> legIndex, index -> legIndex = index);
        if (!valAddons.isEmpty() && valAddons.get(addonIndex) != TTGolemParts.ADDON_NONE.get()) {
            addPart(ADDON_X, ADDON_Y, valAddons, addonIndex, TTGolemParts.addons(), "addon");
        }
        addArrows(ADDON_X, ADDON_Y, valAddons.size(), () -> addonIndex, index -> addonIndex = index);
        if (complete) {
            addTraitIcons();
            hearts = shown(GolemStats.health(props));
            armor = shown(GolemStats.armor(props));
            damage = shown(GolemStats.meleeDamage(props));
        } else {
            hearts = 0.0F;
            armor = 0.0F;
            damage = 0.0F;
        }
        craftButton = addRenderableWidget(new CraftButton(leftPos + CRAFT_X, topPos + CRAFT_Y, this::craft));
        if (complete) {
            redoComps();
            BlockEntityGolemBuilder builder = menu.blockEntity();
            if (builder != null) {
                ClientPacketDistributor.sendToServer(new ServerboundGolemPressPayload(builder.getBlockPos(), props, false));
            }
        } else {
            computeOwnership();
        }
    }

    private static float shown(double stat) {
        return Math.round(stat * STAT_DISPLAY_SCALE * ONE_DECIMAL) / ONE_DECIMAL;
    }

    private <T extends GolemPart> void addPart(int centreX, int centreY, List<T> options, int index, Registry<T> registry, String kind) {
        if (options.isEmpty()) {
            return;
        }
        T part = options.get(index);
        addPartButton(centreX, centreY, new TTButtonIcon.TextureIcon(part.icon()), kind, keyOf(registry, part), PLAIN_TINT);
    }

    private void addArrows(int centreX, int centreY, int size, IntSupplier current, IntConsumer select) {
        if (size <= 1) {
            return;
        }
        addRenderableWidget(TTScrollButton.of(leftPos + centreX - ARROW_BACK_OFFSET, topPos + centreY - ARROW_HALF_HEIGHT, TTScrollButton.Direction.LEFT, Component.translatable(PREVIOUS_KEY),
                () -> step(current, select, size, -1)));
        addRenderableWidget(TTScrollButton.of(leftPos + centreX + ARROW_FORWARD_OFFSET, topPos + centreY - ARROW_HALF_HEIGHT, TTScrollButton.Direction.RIGHT, Component.translatable(NEXT_KEY),
                () -> step(current, select, size, 1)));
    }

    private void step(IntSupplier current, IntConsumer select, int size, int delta) {
        select.accept(Math.floorMod(current.getAsInt() + delta, size));
        gatherInfo();
    }

    private void addTraitIcons() {
        List<GolemTrait> traits = List.copyOf(props.traits());
        int count = traits.size();
        if (count == 0) {
            return;
        }
        int columns = (count + TRAITS_PER_COLUMN - 1) / TRAITS_PER_COLUMN;
        int top = RING_Y - HALF_CELL * (Math.min(count, TRAITS_PER_COLUMN) - 1);
        for (int k = 0; k < count; k++) {
            GolemTrait trait = traits.get(k);
            Identifier id = keyOf(TTGolemTraits.registry(), trait);
            int x = RING_X + CELL * (k / TRAITS_PER_COLUMN) - HALF_CELL * (columns - 1);
            int y = top + CELL * (k % TRAITS_PER_COLUMN);
            TTHoverButton button = TTHoverButton.centered(leftPos + x, topPos + y, ICON_SIZE, new TTButtonIcon.TextureIcon(trait.icon()), Component.translatable(GolemTrait.nameKey(id)), () -> {
            });
            button.setDescription(Component.translatable(GolemTrait.descriptionKey(id)));
            addRenderableWidget(button);
        }
    }

    private void addPartButton(int x, int y, TTButtonIcon icon, String kind, Identifier id, int color) {
        TTHoverButton button = TTHoverButton.centered(leftPos + x, topPos + y, 16, icon, Component.translatable(GolemPart.nameKey(kind, id)), () -> {
        });
        button.setDescription(Component.translatable(GolemPart.descriptionKey(kind, id)));
        button.setTintColor(ARGB.opaque(color));
        addRenderableWidget(button);
    }

    private static ItemStack materialStack(GolemMaterial material) {
        GolemProperties defaults = GolemProperties.createDefault();
        ItemStack stack = new ItemStack(TTItems.GOLEM_PLACER.get());
        stack.set(TTDataComponents.GOLEM_PROPERTIES.get(), new GolemProperties(material, defaults.head(), defaults.arms(), defaults.legs(), defaults.addon(), 0));
        return stack;
    }

    private static <T> Identifier keyOf(Registry<T> registry, T value) {
        Identifier key = registry.getKey(value);
        return key == null ? Identifier.fromNamespaceAndPath("thaumaturge", "unknown") : key;
    }

    private void computeOwnership() {
        if (valHeads.isEmpty() || valMats.isEmpty() || valArms.isEmpty() || valLegs.isEmpty() || valAddons.isEmpty()) {
            allFound = false;
            if (craftButton != null) {
                craftButton.active = false;
            }
            updateCraftTooltip();
            return;
        }
        allFound = true;
        cost = props.traits().size() * 2;
        components = props.components();
        BlockEntityGolemBuilder builder = menu.blockEntity();
        Player player = minecraft.player;
        owns = new boolean[components.size()];
        for (int i = 0; i < components.size(); i++) {
            cost += components.get(i).getCount();
            owns[i] = builder != null && builder.hasStuff != null && builder.hasStuff.length > i && builder.hasStuff[i];
            if (!owns[i] && player != null) {
                owns[i] = InvHelper.isPlayerCarryingAmount(player, components.get(i), true);
            }
            if (!owns[i]) {
                allFound = false;
            }
        }
        for (var widget : children()) {
            if (widget instanceof TTButton button) {
                button.active = !disableAll;
            }
        }
        if (!disableAll && craftButton != null) {
            craftButton.active = allFound;
        }
        updateCraftTooltip();
    }

    private void updateCraftTooltip() {
        if (craftButton == null) {
            return;
        }
        MutableComponent text = Component.translatable("gui.thaumaturge.golembuilder.craft").copy();
        if (disableAll) {
            text.append(newline("gui.thaumaturge.golembuilder.problem.in_progress"));
        } else if (components.isEmpty()) {
            text.append(newline("gui.thaumaturge.golembuilder.problem.no_parts"));
        } else {
            for (int i = 0; i < components.size(); i++) {
                if (i < owns.length && !owns[i]) {
                    ItemStack stack = components.get(i);
                    text.append(newline("gui.thaumaturge.golembuilder.problem.component", stack.getCount(), stack.getHoverName()));
                }
            }
            if (allFound) {
                text.append(Component.literal("\n").append(Component.translatable("gui.thaumaturge.golembuilder.problem.ready").withStyle(ChatFormatting.GREEN)));
            }
        }
        craftButton.setTooltip(Tooltip.create(text));
    }

    private static Component newline(String key, Object... args) {
        return Component.literal("\n").append(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }

    private void redoComps() {
        computeOwnership();
        if (!components.isEmpty()) {
            Holder<IAspect> machina = machinaHolder();
            if (machina != null) {
                TTHoverButton aspectButton = TTHoverButton.centered(leftPos + 152, topPos + 24, 16, new TTButtonIcon.AspectIcon(machina),
                        Component.translatable("aspect.thaumaturge." + machina.value().tag()), () -> {
                        });
                aspectButton.setDescription(Component.translatable("aspect.thaumaturge." + machina.value().tag() + ".desc"));
                addRenderableWidget(aspectButton);
            }
            int row = 1;
            int col = 0;
            for (ItemStack stack : components) {
                TTHoverButton componentButton = TTHoverButton.centered(leftPos + 152 + col * 16, topPos + 24 + 16 * row, 16, new TTButtonIcon.StackIcon(stack), Component.empty(), () -> {
                });
                componentButton.setMessage(stack.getHoverName());
                addRenderableWidget(componentButton);
                if (++row > 3) {
                    row = 0;
                    col++;
                }
            }
        }
        for (var widget : children()) {
            if (widget instanceof TTButton button) {
                button.active = !disableAll;
            }
        }
        if (!disableAll && craftButton != null) {
            craftButton.active = allFound;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        computeOwnership();
    }

    private @Nullable Holder<IAspect> machinaHolder() {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(TTAspects.MACHINA).orElse(null);
    }

    private void craft() {
        if (!allFound) {
            return;
        }
        BlockEntityGolemBuilder builder = menu.blockEntity();
        if (builder != null) {
            ClientPacketDistributor.sendToServer(new ServerboundGolemPressPayload(builder.getBlockPos(), props, true));
            disableAll = true;
        }
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!components.isEmpty()) {
            int row = 1;
            int col = 0;
            for (int i = 0; i < components.size(); i++) {
                if (owns.length > i && !owns[i]) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + COMPONENT_GRID_X + col * 16, topPos + COMPONENT_GRID_Y + 16 * row, MISSING_U, MISSING_V, 16, 16, 256, 256,
                            ARGB.color(128, 0xFFFFFF));
                }
                if (++row > 3) {
                    row = 0;
                    col++;
                }
            }
        }
        if (menu.cost() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + PROGRESS_X, topPos + PROGRESS_Y, PROGRESS_U, PROGRESS_V,
                    (int) (PROGRESS_WIDTH * (1.0F - (float) menu.cost() / Math.max(1, menu.maxCost()))), PROGRESS_HEIGHT, 256, 256);
            if (!disableAll) {
                disableAll = true;
                computeOwnership();
            }
        } else if (disableAll) {
            disableAll = false;
            computeOwnership();
        }
        BlockEntityGolemBuilder builder = menu.blockEntity();
        if (builder != null && builder.hasStuff != lastStuff) {
            lastStuff = builder.hasStuff;
            computeOwnership();
        }
        drawCentered(graphics, String.valueOf(hearts), leftPos + STAT_HEARTS_X, topPos + STAT_Y);
        drawCentered(graphics, String.valueOf(armor), leftPos + STAT_ARMOR_X, topPos + STAT_Y);
        drawCentered(graphics, String.valueOf(damage), leftPos + STAT_DAMAGE_X, topPos + STAT_Y);
        statTooltip(graphics, STAT_HEARTS_X, "gui.thaumaturge.golembuilder.stat.health", mouseX, mouseY);
        statTooltip(graphics, STAT_ARMOR_X, "gui.thaumaturge.golembuilder.stat.armor", mouseX, mouseY);
        statTooltip(graphics, STAT_DAMAGE_X, "gui.thaumaturge.golembuilder.stat.damage", mouseX, mouseY);
    }

    private void statTooltip(GuiGraphicsExtractor graphics, int centerX, String tooltipKey, int mouseX, int mouseY) {
        int x = leftPos + centerX - STAT_ICON_SIZE / 2;
        int y = topPos + STAT_ICON_Y;
        if (mouseX >= x && mouseX < x + STAT_ICON_SIZE && mouseY >= y && mouseY < y + STAT_ICON_SIZE) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable(tooltipKey)), mouseX, mouseY);
        }
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int centerX, int y) {
        graphics.text(font, text, centerX - font.width(text) / 2, y, WHITE, true);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!components.isEmpty()) {
            String costText = String.valueOf(cost);
            graphics.text(font, costText, COST_RIGHT_X - font.width(costText), COST_Y, WHITE, true);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 12, 12, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 12, 60, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 108, 12, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 108, 36, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 108, 60, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
    }

    static final class CraftButton extends TTButton {
        private CraftButton(int x, int y, Runnable onPress) {
            super(x, y, CRAFT_WIDTH, CRAFT_HEIGHT, Component.empty(), onPress);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int alpha = isHovered() && active ? 255 : 230;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), CRAFT_U, CRAFT_V, CRAFT_WIDTH, CRAFT_HEIGHT, 256, 256, ARGB.color(alpha, 0xFFFFFF));
            if (!active) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), CRAFT_U, CRAFT_DISABLED_V, CRAFT_WIDTH, CRAFT_HEIGHT, 256, 256);
            }
        }
    }
}

package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class DustTriggerMultiblockRecipe implements DustTrigger {
    public static final MapCodec<DustTriggerMultiblockRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Identifier.CODEC.fieldOf("blueprint").forGetter(r -> r.blueprintId),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result), ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, DustTriggerMultiblockRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DustTriggerMultiblockRecipe> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, r -> r.blueprintId,
            ItemStackTemplate.STREAM_CODEC, r -> r.result, ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, DustTriggerMultiblockRecipe::new);

    public static final RecipeSerializer<DustTriggerMultiblockRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Identifier blueprintId;
    private final ItemStackTemplate result;
    private final Optional<ResearchGate> research;

    public DustTriggerMultiblockRecipe(Identifier blueprintId, ItemStackTemplate result, Optional<ResearchGate> research) {
        this.blueprintId = blueprintId;
        this.result = result;
        this.research = research;
    }

    public Identifier blueprintId() {
        return this.blueprintId;
    }

    public ItemStack result() {
        return this.result.create();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new MultiblockRecipeDisplay(this.blueprintId, new SlotDisplay.ItemStackSlotDisplay(this.result)));
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return this.research;
    }

    @Override
    public boolean isMultiblock() {
        return true;
    }

    @Override
    public boolean matches(DustTriggerInput input, Level level) {
        return place(level, input.pos()) != null;
    }

    @Override
    public @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) {
        return place(input.level(), input.pos());
    }

    @Override
    public ItemStack assemble(DustTriggerInput input) {
        return this.result.create();
    }

    @Override
    public List<BlockPos> sparkle(Level level, Player player, BlockPos clicked, @Nullable DustTriggerPlacement placement) {
        Blueprint blueprint = blueprint(level);
        if (blueprint == null || placement == null || placement.facing() == null) {
            return List.of(clicked);
        }
        BlockPos origin = originOf(clicked, placement);
        List<BlockPos> positions = new ArrayList<>();
        for (BlueprintCell cell : cellsOf(blueprint, placement.facing())) {
            positions.add(origin.offset(cell.offset()));
        }
        return positions;
    }

    @Override
    public void execute(DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace) {
        if (!(input.level() instanceof ServerLevel level) || placement == null || placement.facing() == null) {
            return;
        }
        Blueprint blueprint = blueprint(level);
        if (blueprint == null) {
            return;
        }
        Direction orientation = placement.facing();
        BlockPos origin = originOf(input.pos(), placement);
        for (BlueprintCell cell : cellsOf(blueprint, orientation)) {
            apply(level, origin.offset(cell.offset()), cell.part(), orientation, useFace, player);
        }
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<DustTriggerMultiblockRecipe> getSerializer() {
        return TTRecipeSerializers.DUST_TRIGGER_MULTIBLOCK.get();
    }

    @Override
    public RecipeType<DustTrigger> getType() {
        return TTRecipeTypes.DUST_TRIGGER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    private @Nullable DustTriggerPlacement place(Level level, BlockPos clicked) {
        Blueprint blueprint = blueprint(level);
        return blueprint == null ? null : MultiblockMatcher.find(level, clicked, blueprint);
    }

    private @Nullable Blueprint blueprint(Level level) {
        Registry<Blueprint> registry = level.registryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        return registry.get(ResourceKey.create(Blueprint.REGISTRY_KEY, this.blueprintId)).map(Holder::value).orElse(null);
    }

    private static BlockPos originOf(BlockPos clicked, DustTriggerPlacement placement) {
        return clicked.offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
    }

    private static List<BlueprintCell> cellsOf(Blueprint blueprint, Direction orientation) {
        return new RotatedBlueprint(blueprint, MultiblockMatcher.rotationsFor(orientation)).cells();
    }

    private static void apply(ServerLevel level, BlockPos pos, BlueprintPart part, Direction orientation, Direction useFace, Player player) {
        BlockState original = level.getBlockState(pos);
        int delay = part.priority();
        switch (part.target()) {
            case BlueprintTarget.Keep keep -> {
            }
            case BlueprintTarget.Air air -> DustTriggerSwapQueue.enqueueClear(level, pos, original, delay);
            case BlueprintTarget.BlockTarget block -> DustTriggerSwapQueue.enqueuePlace(level, pos, original, faced(block, orientation, useFace, player), delay);
            case BlueprintTarget.StateTarget state -> DustTriggerSwapQueue.enqueuePlace(level, pos, original, state.state(), delay);
            case BlueprintTarget.StackTarget stack -> DustTriggerSwapQueue.enqueueDrop(level, pos, original, stack.stack(), delay);
        }
    }

    private static BlockState faced(BlueprintTarget.BlockTarget target, Direction orientation, Direction useFace, Player player) {
        BlockState state = target.block().defaultBlockState();
        Direction facing = facingFor(target, orientation, useFace, player);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, facing);
        }
        return state;
    }

    private static Direction facingFor(BlueprintTarget.BlockTarget target, Direction orientation, Direction useFace, Player player) {
        if (target.applyPlayerFacing()) {
            return useFace.getAxis().isHorizontal() ? useFace : player.getDirection().getOpposite();
        }
        return target.opposite() ? orientation.getOpposite() : orientation;
    }
}

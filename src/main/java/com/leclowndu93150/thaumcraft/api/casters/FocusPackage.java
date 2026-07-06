package com.leclowndu93150.thaumcraft.api.casters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The serialized form of a complete spell: an ordered list of {@link IFocusElement focus
 * elements} plus the accumulated power, complexity, and caster identity. Packages nest; a
 * package can appear as a node inside another package.
 *
 * <p>NBT serialization is replaced by {@link #CODEC}; elements are materialized on decode through
 * {@link FocusEngine#getElement(Identifier)}, so decoding requires the focus element registry to be
 * bound.
 *
 * @since 1.0.0
 */
public class FocusPackage implements IFocusElement {
    private static final Identifier KEY = Identifier.fromNamespaceAndPath("thaumcraft", "package");
    private static final float DEFAULT_POWER = 1.0F;

    /**
     * Serializes power, complexity, the caster id when known, and every node with its
     * settings and, for splits, branch packages. Decoding fails when a node references an
     * element id absent from the bound registry.
     */
    public static final Codec<FocusPackage> CODEC = Codec.recursive("FocusPackage", self -> {
        Codec<NodeEntry> entryCodec = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("key").forGetter(NodeEntry::key),
                Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("settings", Map.of()).forGetter(NodeEntry::settings),
                self.listOf().optionalFieldOf("packages", List.of()).forGetter(NodeEntry::packages)
        ).apply(i, NodeEntry::new));
        Codec<IFocusElement> nodeCodec = entryCodec.flatXmap(FocusPackage::materialize, FocusPackage::describe);
        return RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("power", DEFAULT_POWER).forGetter(FocusPackage::getPower),
                Codec.INT.optionalFieldOf("complexity", 0).forGetter(FocusPackage::getComplexity),
                UUIDUtil.CODEC.optionalFieldOf("caster").forGetter(p -> Optional.ofNullable(p.getCasterUUID())),
                nodeCodec.listOf().fieldOf("nodes").forGetter(FocusPackage::getNodes)
        ).apply(i, FocusPackage::new));
    });

    /** Network encoding mirroring {@link #CODEC}. */
    public static final StreamCodec<RegistryFriendlyByteBuf, FocusPackage> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    Level level;
    private LivingEntity caster;
    private UUID casterUUID;
    private float power = DEFAULT_POWER;
    private int complexity;
    private int index;
    private UUID uid = UUID.randomUUID();
    private final List<IFocusElement> nodes = Collections.synchronizedList(new ArrayList<>());

    /** Creates an empty package with no caster. */
    public FocusPackage() {
    }

    /**
     * Creates an empty package owned by a caster, capturing its level and id.
     *
     * @param caster the casting entity
     */
    public FocusPackage(LivingEntity caster) {
        this.level = caster.level();
        this.caster = caster;
        this.casterUUID = caster.getUUID();
    }

    private FocusPackage(float power, int complexity, Optional<UUID> casterUUID, List<IFocusElement> nodes) {
        this.power = power;
        this.complexity = complexity;
        this.casterUUID = casterUUID.orElse(null);
        this.nodes.addAll(nodes);
    }

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public EnumUnitType getType() {
        return EnumUnitType.PACKAGE;
    }

    /**
     * The total complexity of the focus this package was compiled from.
     *
     * @return the complexity value
     */
    public int getComplexity() {
        return complexity;
    }

    /**
     * Sets the compiled complexity of this package.
     *
     * @param complexity the complexity value
     */
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    /**
     * The id of this cast, shared by every sub-package spawned from the same cast. Used to
     * pair delayed executions with their origin.
     *
     * @return the cast id, never null
     */
    public UUID getUniqueID() {
        return uid;
    }

    /**
     * Assigns the cast id. {@link FocusEngine#castFocusPackage(LivingEntity, FocusPackage)}
     * assigns a fresh one per cast.
     *
     * @param id the cast id
     */
    public void setUniqueID(UUID id) {
        this.uid = id;
    }

    /**
     * The index of the node currently executing.
     *
     * @return the execution index
     */
    public int getExecutionIndex() {
        return index;
    }

    /**
     * Sets the index of the node currently executing. Managed by {@link FocusEngine}.
     *
     * @param index the execution index
     */
    public void setExecutionIndex(int index) {
        this.index = index;
    }

    /**
     * Appends a node to this package.
     *
     * @param element the node to append
     */
    public void addNode(IFocusElement element) {
        nodes.add(element);
    }

    /**
     * The live node list of this package, in execution order.
     *
     * @return the mutable node list
     */
    public List<IFocusElement> getNodes() {
        return nodes;
    }

    /**
     * The id of the casting entity.
     *
     * @return the caster id, or null when the package has no caster
     */
    public @Nullable UUID getCasterUUID() {
        if (caster != null) {
            casterUUID = caster.getUUID();
        }
        return casterUUID;
    }

    /**
     * Assigns the caster by id without resolving the entity.
     *
     * @param casterUUID the caster id
     */
    public void setCasterUUID(UUID casterUUID) {
        this.casterUUID = casterUUID;
    }

    /**
     * The casting entity, resolved from the caster id through this package's level when not
     * already known.
     *
     * @return the caster, or null when it cannot be resolved
     */
    public @Nullable LivingEntity getCaster() {
        if (caster == null && level != null && getCasterUUID() != null
                && level.getEntity(getCasterUUID()) instanceof LivingEntity living) {
            caster = living;
        }
        return caster;
    }

    /**
     * The level this package executes in.
     *
     * @return the level, or null before {@link #initialize(LivingEntity)}
     */
    public @Nullable Level getLevel() {
        return level;
    }

    /**
     * Collects every effect in this package, descending into nested packages and split
     * branches.
     *
     * @return the effects in encounter order; empty when none
     */
    public List<FocusEffect> getFocusEffects() {
        List<FocusEffect> out = new ArrayList<>();
        collectFocusEffects(this, out);
        return out;
    }

    private static void collectFocusEffects(FocusPackage pack, List<FocusEffect> out) {
        for (IFocusElement element : pack.nodes) {
            if (element instanceof FocusEffect effect) {
                out.add(effect);
            } else if (element instanceof FocusPackage nested) {
                collectFocusEffects(nested, out);
            } else if (element instanceof FocusModSplit split) {
                for (FocusPackage branch : split.getSplitPackages()) {
                    collectFocusEffects(branch, out);
                }
            }
        }
    }

    /**
     * The accumulated power of this package. Node multipliers fold into it during execution.
     *
     * @return the current power
     */
    public float getPower() {
        return power;
    }

    /**
     * Folds a multiplier into this package's power.
     *
     * @param multiplier the factor to apply
     */
    public void multiplyPower(float multiplier) {
        power *= multiplier;
    }

    /**
     * Deep-copies this package through its codec and binds the copy to a caster.
     *
     * @param caster the caster the copy belongs to
     * @return the independent copy
     * @throws IllegalStateException when the package fails to round-trip
     */
    public FocusPackage copy(LivingEntity caster) {
        Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(IllegalStateException::new);
        FocusPackage copy = CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(IllegalStateException::new);
        copy.level = caster.level();
        copy.caster = caster;
        copy.casterUUID = caster.getUUID();
        return copy;
    }

    /**
     * Binds this package to its caster's level and, when the first node is an unconfigured
     * {@link FocusMediumRoot}, aims it from the caster.
     *
     * @param caster the casting entity
     */
    public void initialize(LivingEntity caster) {
        this.level = caster.level();
        if (!nodes.isEmpty() && nodes.get(0) instanceof FocusMediumRoot root && root.supplyTargets() == null) {
            root.setupFromCaster(caster);
        }
    }

    /**
     * A hash over every node key and setting value, used to group identical foci.
     *
     * @return the sorting hash
     */
    public int getSortingHelper() {
        StringBuilder sb = new StringBuilder();
        for (IFocusElement element : nodes) {
            sb.append(element.getKey());
            if (element instanceof FocusNode node) {
                for (String settingKey : node.getSettingList()) {
                    sb.append(node.getSettingValue(settingKey));
                }
            }
        }
        return sb.toString().hashCode();
    }

    private static DataResult<IFocusElement> materialize(NodeEntry entry) {
        if (KEY.equals(entry.key())) {
            if (entry.packages().isEmpty()) {
                return DataResult.error(() -> "Nested focus package entry is missing its package payload");
            }
            return DataResult.success(entry.packages().get(0));
        }
        IFocusElement element = FocusEngine.getElement(entry.key());
        if (element == null) {
            return DataResult.error(() -> "Unknown focus element: " + entry.key());
        }
        if (element instanceof FocusNode node) {
            for (Map.Entry<String, Integer> setting : entry.settings().entrySet()) {
                NodeSetting nodeSetting = node.getSetting(setting.getKey());
                if (nodeSetting != null) {
                    nodeSetting.setValue(setting.getValue());
                }
            }
            if (node instanceof FocusModSplit split) {
                split.getSplitPackages().addAll(entry.packages());
            }
        }
        return DataResult.success(element);
    }

    private static DataResult<NodeEntry> describe(IFocusElement element) {
        if (element instanceof FocusPackage pack) {
            return DataResult.success(new NodeEntry(pack.getKey(), Map.of(), List.of(pack)));
        }
        if (element instanceof FocusNode node) {
            Map<String, Integer> settings = new HashMap<>();
            for (String settingKey : node.getSettingList()) {
                settings.put(settingKey, node.getSettingValue(settingKey));
            }
            List<FocusPackage> packages = node instanceof FocusModSplit split
                    ? List.copyOf(split.getSplitPackages())
                    : List.of();
            return DataResult.success(new NodeEntry(node.getKey(), settings, packages));
        }
        return DataResult.error(() -> "Focus element is neither a node nor a package: " + element.getKey());
    }

    private record NodeEntry(Identifier key, Map<String, Integer> settings, List<FocusPackage> packages) {
    }
    /**
     * Compares packages by spell content: complexity and the node structure hash.
     *
     * <p>Required by the data component contract. Runtime execution state (index, caster,
     * level) does not participate in equality.
     *
     * @param obj the object to compare
     * @return {@code true} when both packages encode the same spell
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof FocusPackage other
                && complexity == other.complexity
                && Float.compare(power, other.power) == 0
                && getSortingHelper() == other.getSortingHelper();
    }

    /**
     * Hashes the spell content consistently with {@link #equals(Object)}.
     *
     * @return the content hash
     */
    @Override
    public int hashCode() {
        return 31 * complexity + getSortingHelper();
    }
}

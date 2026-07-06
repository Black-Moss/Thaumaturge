package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.casters.FocusNode;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class FocusElementNode {
    public static final Codec<FocusElementNode> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("x").forGetter(n -> n.x),
            Codec.INT.fieldOf("y").forGetter(n -> n.y),
            Codec.INT.fieldOf("id").forGetter(n -> n.id),
            Codec.BOOL.optionalFieldOf("target", false).forGetter(n -> n.target),
            Codec.BOOL.optionalFieldOf("trajectory", false).forGetter(n -> n.trajectory),
            Codec.INT.optionalFieldOf("parent", -1).forGetter(n -> n.parent),
            Codec.INT.listOf().optionalFieldOf("children", List.of()).forGetter(FocusElementNode::childList),
            Codec.FLOAT.optionalFieldOf("complexity", 1.0F).forGetter(n -> n.complexityMultiplier),
            Identifier.CODEC.optionalFieldOf("key").forGetter(FocusElementNode::nodeKey),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("settings", Map.of()).forGetter(FocusElementNode::settingValues)
    ).apply(inst, FocusElementNode::decode));

    public int x;
    public int y;
    public int id;
    public boolean target;
    public boolean trajectory;
    public int parent = -1;
    public int[] children = new int[0];
    public float complexityMultiplier = 1.0F;
    public @Nullable FocusNode node;

    public float getPower(Map<Integer, FocusElementNode> data) {
        if (node == null) {
            return 1.0F;
        }
        float pow = node.getPowerMultiplier();
        FocusElementNode p = data.get(parent);
        if (p != null && p.node != null) {
            pow *= p.getPower(data);
        }
        return pow;
    }

    private List<Integer> childList() {
        List<Integer> list = new java.util.ArrayList<>(children.length);
        for (int c : children) {
            list.add(c);
        }
        return list;
    }

    private Optional<Identifier> nodeKey() {
        return node == null ? Optional.empty() : Optional.of(node.getKey());
    }

    private Map<String, Integer> settingValues() {
        if (node == null || node.getSettingList() == null || node.getSettingList().isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> values = new java.util.HashMap<>();
        for (String key : node.getSettingList()) {
            values.put(key, node.getSettingValue(key));
        }
        return values;
    }

    private static FocusElementNode decode(int x, int y, int id, boolean target, boolean trajectory, int parent,
                                           List<Integer> children, float complexity,
                                           Optional<Identifier> key, Map<String, Integer> settings) {
        FocusElementNode result = new FocusElementNode();
        result.x = x;
        result.y = y;
        result.id = id;
        result.target = target;
        result.trajectory = trajectory;
        result.parent = parent;
        result.children = new int[children.size()];
        for (int i = 0; i < children.size(); i++) {
            result.children[i] = children.get(i);
        }
        result.complexityMultiplier = complexity;
        key.ifPresent(k -> {
            IFocusElement element = FocusEngine.getElement(k);
            if (element instanceof FocusNode focusNode) {
                result.node = focusNode;
                for (Map.Entry<String, Integer> entry : settings.entrySet()) {
                    if (focusNode.getSetting(entry.getKey()) != null) {
                        focusNode.getSetting(entry.getKey()).setValue(entry.getValue());
                    }
                }
            }
        });
        return result;
    }
}

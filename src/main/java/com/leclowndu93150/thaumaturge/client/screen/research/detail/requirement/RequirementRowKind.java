package com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement;

public enum RequirementRowKind {
    RESEARCH(232, "gui.thaumaturge.thaumonomicon.need.research"), OBTAIN(216, "gui.thaumaturge.thaumonomicon.need.obtain"), CRAFT(200, "gui.thaumaturge.thaumonomicon.need.craft"), KNOWLEDGE(184,
            "gui.thaumaturge.thaumonomicon.need.know");

    private final int labelV;
    private final String tooltipKey;

    RequirementRowKind(int labelV, String tooltipKey) {
        this.labelV = labelV;
        this.tooltipKey = tooltipKey;
    }

    public int labelV() {
        return labelV;
    }

    public String tooltipKey() {
        return tooltipKey;
    }
}

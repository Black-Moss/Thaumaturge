package com.leclowndu93150.thaumaturge.client.entity;

record PortalContribution(float heightDelta, float widthDelta, float alphaFactor) {
    static final PortalContribution NONE = new PortalContribution(0.0F, 0.0F, 1.0F);

    PortalContribution plus(PortalContribution other) {
        return new PortalContribution(heightDelta + other.heightDelta, widthDelta + other.widthDelta, alphaFactor * other.alphaFactor);
    }
}

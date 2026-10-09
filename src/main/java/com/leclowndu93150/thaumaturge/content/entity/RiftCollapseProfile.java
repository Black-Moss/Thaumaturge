package com.leclowndu93150.thaumaturge.content.entity;

record RiftCollapseProfile(float taintSeconds, float weaknessSeconds, float warpPoints) {
    static final RiftCollapseProfile NONE = new RiftCollapseProfile(0.0F, 0.0F, 0.0F);

    boolean isInert() {
        return this.taintSeconds <= 0.0F && this.weaknessSeconds <= 0.0F && this.warpPoints <= 0.0F;
    }
}

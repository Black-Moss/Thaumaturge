package com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge;

public enum KnowledgeGridLayout {
    IN_PAGE(20) {
        @Override
        public int columnStride(int categoryCount) {
            return IN_PAGE_COLUMN_STRIDE;
        }
    },
    INSERT(28) {
        @Override
        public int columnStride(int categoryCount) {
            return (int) (INSERT_COLUMN_SPAN / (float) categoryCount);
        }
    };

    private static final int IN_PAGE_COLUMN_STRIDE = 18;
    private static final int INSERT_COLUMN_SPAN = 164;

    private final int rowStride;

    KnowledgeGridLayout(int rowStride) {
        this.rowStride = rowStride;
    }

    public abstract int columnStride(int categoryCount);

    public int rowStride() {
        return rowStride;
    }
}

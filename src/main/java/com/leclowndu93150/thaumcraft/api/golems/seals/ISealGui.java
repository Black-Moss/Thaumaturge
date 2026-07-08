package com.leclowndu93150.thaumcraft.api.golems.seals;

/**
 * Declares which configuration category tabs a seal's UI shows.
 *
 * @since 1.0.0
 */
public interface ISealGui {
    int CAT_PRIORITY = 0;
    int CAT_FILTER = 1;
    int CAT_AREA = 2;
    int CAT_TOGGLES = 3;
    int CAT_TAGS = 4;

    /**
     * @return the category tabs to show, from the {@code CAT_} constants
     */
    int[] getGuiCategories();
}

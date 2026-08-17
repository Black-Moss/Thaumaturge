package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * Configuration surface for seals with boolean options shown in the seal UI.
 *
 * @since 1.0.0
 */
public interface ISealConfigToggles {
    /**
     * @return the seal's toggles; the array and its entries are live
     */
    SealToggle[] getToggles();

    /**
     * @param index the toggle index
     * @param value the new value
     */
    void setToggle(int index, boolean value);

    /**
     * One boolean option of a seal.
     *
     * @since 1.0.0
     */
    class SealToggle {
        private boolean value;
        private final String key;
        private final String name;

        /**
         * @param value the initial value
         * @param key the NBT key the value persists under
         * @param name the translation key suffix shown in the seal UI
         */
        public SealToggle(boolean value, String key, String name) {
            this.value = value;
            this.key = key;
            this.name = name;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }
    }
}

package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangFMetals {
    private TCLangFMetals() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.metal_thaumium", "Thaumium Block");
        provider.add("block.thaumcraft.metal_brass", "Brass Block");
        provider.add("block.thaumcraft.metal_void", "Void Metal Block");
        provider.add("block.thaumcraft.metal_infused", "Infused Metal Block");

        provider.add("item.thaumcraft.ingot_thaumium", "Thaumium Ingot");
        provider.add("item.thaumcraft.ingot_brass", "Alchemical Brass Ingot");
        provider.add("item.thaumcraft.ingot_void", "Void Metal Ingot");
        provider.add("item.thaumcraft.ingot_infused", "Infused Metal Ingot");

        provider.add("item.thaumcraft.nugget_thaumium", "Thaumium Nugget");
        provider.add("item.thaumcraft.nugget_brass", "Alchemical Brass Nugget");
        provider.add("item.thaumcraft.nugget_void", "Void Metal Nugget");

        provider.add("item.thaumcraft.cluster_iron", "Native Iron Cluster");
        provider.add("item.thaumcraft.cluster_gold", "Native Gold Cluster");
        provider.add("item.thaumcraft.cluster_copper", "Native Copper Cluster");
        provider.add("item.thaumcraft.cluster_silver", "Native Silver Cluster");
        provider.add("item.thaumcraft.cluster_lead", "Native Lead Cluster");
        provider.add("item.thaumcraft.cluster_tin", "Native Tin Cluster");
        provider.add("item.thaumcraft.cluster_thaumium", "Native Thaumium Cluster");
        provider.add("item.thaumcraft.cluster_brass", "Native Brass Cluster");
    }
}

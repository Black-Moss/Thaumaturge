package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public final class JarUpgradeTests {
    private JarUpgradeTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("parity/jar_upgrade_preserves_data", 20, helper -> {
            ItemStack jar = new ItemStack(TTItems.JAR_NORMAL.get(), 3);
            EssentiaList contents = new EssentiaList(AspectList.EMPTY.add(Aspects.resolve(helper.getLevel(), TTAspects.AER), 48));
            Component name = Component.literal("Air supply");
            jar.set(TTDataComponents.ESSENTIA_CONTENTS, contents);
            jar.set(TTDataComponents.ASPECT_FILTER, TTAspects.AER);
            jar.set(DataComponents.CUSTOM_NAME, name);
            ItemStack original = jar.copy();
            ArcaneCraftingInput input = new ArcaneCraftingInput(2, 1, List.of(ItemStack.EMPTY, jar));
            ItemStackTemplate template = new ItemStackTemplate(TTItems.JAR_VOID.get());
            ItemStack result = ArcaneCraftingRecipe.assembleResult(template, input);
            helper.assertTrue(result.is(TTItems.JAR_VOID) && result.getCount() == 1, "Upgrade must produce one void jar");
            helper.assertTrue(contents.equals(result.get(TTDataComponents.ESSENTIA_CONTENTS)), "Upgrade lost essentia");
            helper.assertTrue(TTAspects.AER.equals(result.get(TTDataComponents.ASPECT_FILTER)), "Upgrade lost the label");
            helper.assertTrue(name.equals(result.get(DataComponents.CUSTOM_NAME)), "Upgrade lost the name");
            helper.assertTrue(ItemStack.matches(jar, original), "Upgrade mutated its input");
            helper.assertTrue(!template.create().has(TTDataComponents.ESSENTIA_CONTENTS), "Upgrade mutated the recipe template");
            ItemStack unrelated = ArcaneCraftingRecipe.assembleResult(new ItemStackTemplate(Items.STONE), input);
            helper.assertTrue(!unrelated.has(TTDataComponents.ESSENTIA_CONTENTS), "Unrelated recipes must not copy jar data");
            helper.succeed();
        });
    }
}

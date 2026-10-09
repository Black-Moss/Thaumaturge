package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class RequirementTracker {
    private static final Comparator<AspectInstance> NEED_ORDER = Comparator.comparing((AspectInstance need) -> aspectId(need).getNamespace()).thenComparing(need -> aspectId(need).getPath());
    private static final int NOTHING = 0;

    private final List<AspectInstance> needs = new ArrayList<>();
    private AspectList required = AspectList.EMPTY;
    private AspectList received = AspectList.EMPTY;

    private static Identifier aspectId(AspectInstance instance) {
        return instance.aspect().unwrapKey().map(ResourceKey::identifier).orElseThrow();
    }

    public void planFor(CrucibleRecipe recipe) {
        required = recipe.aspects();
        needs.clear();
        needs.addAll(required.entries());
        needs.sort(NEED_ORDER);
    }

    public void clearPlan() {
        required = AspectList.EMPTY;
        needs.clear();
    }

    public @Nullable Holder<IAspect> firstUnmet() {
        for (AspectInstance need : needs) {
            if (received.amountOf(need.aspect()) < need.amount()) {
                return need.aspect();
            }
        }
        return null;
    }

    public int accept(Holder<IAspect> aspect, int offered) {
        int accepted = Math.min(offered, required.amountOf(aspect) - received.amountOf(aspect));
        if (accepted <= NOTHING) {
            return NOTHING;
        }
        received = received.add(aspect, accepted);
        return accepted;
    }

    public AspectList received() {
        return received;
    }

    public void restoreReceived(AspectList stored) {
        received = stored;
    }

    public void clearReceived() {
        received = AspectList.EMPTY;
    }
}

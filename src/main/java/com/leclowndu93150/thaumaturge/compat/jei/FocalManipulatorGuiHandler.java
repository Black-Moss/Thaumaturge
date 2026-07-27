package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.client.screen.casters.FocalManipulatorScreen;
import java.util.List;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

public final class FocalManipulatorGuiHandler implements IGuiContainerHandler<FocalManipulatorScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(FocalManipulatorScreen screen) {
        return screen.jeiExtraAreas();
    }
}

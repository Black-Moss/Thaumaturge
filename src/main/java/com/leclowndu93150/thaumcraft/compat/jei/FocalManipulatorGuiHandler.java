package com.leclowndu93150.thaumcraft.compat.jei;

import com.leclowndu93150.thaumcraft.client.screen.casters.FocalManipulatorScreen;
import java.util.List;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

public final class FocalManipulatorGuiHandler implements IGuiContainerHandler<FocalManipulatorScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(FocalManipulatorScreen screen) {
        return screen.jeiExtraAreas();
    }
}

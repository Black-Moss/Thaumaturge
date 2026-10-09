package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record DetailFrame(Font font, Player player, @Nullable Level level, int left, int top, int screenWidth, int screenHeight) {
    public long gameTime() {
        return player.level().getGameTime();
    }

    public int tick() {
        return player.tickCount;
    }

    public int paperLeft() {
        return (screenWidth - BookSprites.PAPER_LAYOUT_SPAN) / 2;
    }

    public int paperTop() {
        return (screenHeight - BookSprites.PAPER_LAYOUT_SPAN) / 2;
    }
}

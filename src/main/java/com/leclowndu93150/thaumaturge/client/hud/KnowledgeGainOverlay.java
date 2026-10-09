package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.ActiveGain;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainEntry;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.TrackerStore;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.motion.ScreenPoint;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.paint.EntryPainter;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.paint.SparkPainter;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.spark.SparkSpawner;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.spark.SparkSystem;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class KnowledgeGainOverlay implements GuiLayer {
    private static final int BOOK_SIZE = 16;
    private static final int BOOK_MARGIN = 17;

    private static final TrackerStore TRACKERS = new TrackerStore();
    private static final SparkSystem SPARKS = new SparkSystem();
    private static final SparkSpawner SPAWNER = new SparkSpawner();

    public KnowledgeGainOverlay() {}

    public static void addTracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, int duration, long seed) {
        TRACKERS.add(GainEntry.knowledge(type, category, duration, seed));
        playLearnSound();
    }

    public static void addAspectTracker(Holder<IAspect> aspect, int duration, long seed) {
        TRACKERS.add(GainEntry.aspect(aspect, duration, seed));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            TRACKERS.clear();
            SPARKS.clear();
            return;
        }
        TRACKERS.tick();
        SPARKS.tick(level.getRandom());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || TRACKERS.bookFade() <= 0 && TRACKERS.isEmpty() && SPARKS.isEmpty()) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        if (TRACKERS.bookFade() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, HudTextures.KNOWLEDGE_BOOK, graphics.guiWidth() - BOOK_MARGIN, graphics.guiHeight() - BOOK_MARGIN, 0, 0, BOOK_SIZE, BOOK_SIZE, BOOK_SIZE,
                    BOOK_SIZE, BOOK_SIZE, BOOK_SIZE, ARGB.white(TRACKERS.bookAlpha()));
        }
        RandomSource random = level.getRandom();
        List<ActiveGain> gains = TRACKERS.entries();
        for (int i = 0; i < gains.size(); i++) {
            ActiveGain gain = gains.get(i);
            ScreenPoint point = EntryPainter.draw(graphics, level, gain, partial);
            float lifeFraction = gain.remainingAt(partial) / gain.entry().life();
            SPAWNER.maybeSpawn(SPARKS, random, lifeFraction, point.x(), point.y());
        }
        SparkPainter.draw(graphics, SPARKS.pool(), partial);
    }

    private static void playLearnSound() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player != null && level != null) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TTSounds.LEARN.value(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
        }
    }
}

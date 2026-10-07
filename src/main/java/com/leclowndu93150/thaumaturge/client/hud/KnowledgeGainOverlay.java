package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class KnowledgeGainOverlay implements GuiLayer {
    private static final Identifier BOOK = TTIds.rl("textures/item/thaumonomicon.png");
    private static final Identifier OBSERVATION = TTIds.rl("textures/research/knowledge_observation.png");
    private static final Identifier THEORY = TTIds.rl("textures/research/knowledge_theory.png");
    private static final int ICON = 16;
    private static final float HALF_ICON = ICON / 2.0F;
    private static final int STRIP_WIDTH = ICON * ParticleTextures.STAR_GLINT_FRAMES;
    private static final int BOOK_INSET = 17;
    private static final float BOOK_FADE_FULL = 40.0F;
    private static final float BOOK_FADE_RISE = 10.0F;
    private static final int THEORY_BONUS_TICKS = 10;
    private static final int TILT_CHOICES = 12;
    private static final int TILT_CENTRE = 6;
    private static final int START_SPREAD = 32;
    private static final int END_SPREAD = 8;
    private static final int CORNER_OFFSET = 12;
    private static final int WIDTH_DIVISOR = 4;
    private static final int HEIGHT_DIVISOR = 3;
    private static final float POP_SHARE = 0.33F;
    private static final float FLIGHT_SHARE = 0.66F;
    private static final float BURST_SHARE = 0.1F;
    private static final float POP_BASE = 1.5F;
    private static final float POP_SWING = 0.5F;
    private static final float CATEGORY_SCALE = 0.75F;
    private static final float APPEAR_BURST = 16.0F;
    private static final float ARRIVAL_BURST = 8.0F;
    private static final int FULL_TURN_DEGREES = 360;
    private static final int GREEN_MIN = 189;
    private static final int BLUE_MIN = 64;
    private static final int CHANNEL_MAX = 255;
    private static final int MAX_SPARKS = 200;
    private static final float SPARK_CHANCE_RANGE = 10.0F;
    private static final double SPARK_SCATTER = 5.0;
    private static final double SPARK_KICK = 1.0;
    private static final int SPARK_DELAY_CHOICES = 5;
    private static final int SPARK_LIFE_MIN = 32;
    private static final int SPARK_LIFE_CHOICES = 8;
    private static final float STAR_SPARK_CHANCE = 0.2F;
    private static final double SPARK_DRAG = 0.9;
    private static final double SPARK_FALL = 0.04;
    private static final double SPARK_WOBBLE = 0.025;
    private static final float SPARK_SIZE_START = 4.8F;
    private static final float SPARK_SIZE_END = 9.6F;
    private static final float SPARK_FADE_SHARE = 1.0F / 6.0F;

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final List<Spark> SPARKS = new ArrayList<>();
    private static float bookFade;

    public static void addTracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, int duration, long seed) {
        ENTRIES.add(new Entry(type, category, null, duration + (type == KnowledgeType.THEORY ? THEORY_BONUS_TICKS : 0), seed));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), TTSounds.LEARN.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
        }
    }

    public static void addAspectTracker(Holder<IAspect> aspect, int duration, long seed) {
        ENTRIES.add(new Entry(null, null, aspect, duration, seed));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ENTRIES.clear();
            SPARKS.clear();
            bookFade = 0.0F;
            return;
        }
        ENTRIES.removeIf(Entry::age);
        bookFade = ENTRIES.isEmpty() ? Math.max(0.0F, bookFade - 1.0F) : Math.min(BOOK_FADE_FULL, bookFade + BOOK_FADE_RISE);
        RandomSource random = mc.level.getRandom();
        Iterator<Spark> sparks = SPARKS.iterator();
        while (sparks.hasNext()) {
            if (sparks.next().tick(random)) {
                sparks.remove();
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || bookFade <= 0.0F && ENTRIES.isEmpty() && SPARKS.isEmpty()) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        if (bookFade > 0.0F) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, width - BOOK_INSET, height - BOOK_INSET, 0.0F, 0.0F, ICON, ICON, ICON, ICON, ARGB.white(bookFade / BOOK_FADE_FULL));
        }
        for (Entry entry : ENTRIES) {
            drawEntry(graphics, mc, entry, width, height, partial);
        }
        for (Spark spark : SPARKS) {
            spark.draw(graphics, partial);
        }
    }

    private static void drawEntry(GuiGraphicsExtractor graphics, Minecraft mc, Entry entry, int width, int height, float partial) {
        float life = entry.life;
        float remaining = Math.max(0.0F, entry.remaining - partial);
        float size;
        float across;
        float down;
        if (remaining >= FLIGHT_SHARE * life) {
            float pop = (life - remaining) / (POP_SHARE * life);
            float pulse = POP_BASE - POP_SWING * Mth.cos(Mth.TWO_PI * pop);
            size = ICON * (pop < 0.5F ? 2.0F * pop * pulse : pulse);
            across = 1.0F;
            down = 1.0F;
        } else {
            float flight = remaining / (FLIGHT_SHARE * life);
            size = ICON * flight;
            down = 0.5F - 0.5F * Mth.cos(Mth.PI * flight);
            across = Mth.sin(down * Mth.HALF_PI);
        }
        float x = width - CORNER_OFFSET + entry.endX - (width / WIDTH_DIVISOR + entry.startX) * across;
        float y = height - CORNER_OFFSET + entry.endY - (height / HEIGHT_DIVISOR + entry.startY) * down;
        if (remaining > (1.0F - BURST_SHARE) * life) {
            float wave = (life - remaining) / (BURST_SHARE * life);
            drawBurst(graphics, x, y, APPEAR_BURST * (1.0F - Mth.cos(Mth.TWO_PI * wave)), entry.tilt + entry.appearSpin, entry.appearFrame, entry.appearColor);
        } else if (remaining < BURST_SHARE * life) {
            float wave = 1.0F - remaining / (BURST_SHARE * life);
            drawBurst(graphics, x, y, ARRIVAL_BURST * (1.0F - Mth.cos(Mth.TWO_PI * wave)), entry.tilt + entry.arrivalSpin, entry.arrivalFrame, entry.arrivalColor);
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().rotate((float) Math.toRadians(entry.tilt));
        graphics.pose().scale(size / ICON, size / ICON);
        if (entry.aspect != null) {
            drawIcon(graphics, entry.aspect.value().texture(), ARGB.opaque(entry.aspect.value().color()));
        } else {
            drawIcon(graphics, entry.type == KnowledgeType.THEORY ? THEORY : OBSERVATION, ARGB.white(1.0F));
            Identifier categoryIcon = categoryIcon(mc, entry.category);
            if (categoryIcon != null) {
                graphics.pose().scale(CATEGORY_SCALE, CATEGORY_SCALE);
                drawIcon(graphics, categoryIcon, ARGB.white(1.0F));
            }
        }
        graphics.pose().popMatrix();
        if (SPARKS.size() < MAX_SPARKS) {
            RandomSource random = mc.level.getRandom();
            if (random.nextInt(Mth.floor(1.0F + SPARK_CHANCE_RANGE * remaining / life)) == 0) {
                SPARKS.add(new Spark(x, y, random));
            }
        }
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, Identifier texture, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) -HALF_ICON, (int) -HALF_ICON, 0.0F, 0.0F, ICON, ICON, ICON, ICON, color);
    }

    private static void drawBurst(GuiGraphicsExtractor graphics, float x, float y, float size, float degrees, int frame, int color) {
        drawFrame(graphics, TTRenderPipelines.GUI_TEXTURED_ADDITIVE, ParticleTextures.STAR_GLINT, x, y, size, degrees, frame, color);
    }

    private static void drawFrame(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier strip, float x, float y, float size, float degrees, int frame, int color) {
        if (size <= 0.0F) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().rotate((float) Math.toRadians(degrees));
        graphics.pose().scale(size / ICON, size / ICON);
        graphics.blit(pipeline, strip, (int) -HALF_ICON, (int) -HALF_ICON, frame * ICON, 0.0F, ICON, ICON, ICON, ICON, STRIP_WIDTH, ICON, color);
        graphics.pose().popMatrix();
    }

    private static @Nullable Identifier categoryIcon(Minecraft mc, @Nullable ResourceKey<IResearchCategory> category) {
        if (category == null || mc.level == null) {
            return null;
        }
        return mc.level.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).get(category).map(holder -> holder.value().icon()).orElse(null);
    }

    private static int roll(long seed, int salt, int bound) {
        return (int) Math.floorMod(HashCommon.mix(seed + salt), (long) bound);
    }

    private static int glintColor(long seed, int salt) {
        return ARGB.color(CHANNEL_MAX, CHANNEL_MAX, GREEN_MIN + roll(seed, salt, CHANNEL_MAX - GREEN_MIN + 1), BLUE_MIN + roll(seed, salt + 1, CHANNEL_MAX - BLUE_MIN + 1));
    }

    private static final class Entry {
        private final @Nullable KnowledgeType type;
        private final @Nullable ResourceKey<IResearchCategory> category;
        private final @Nullable Holder<IAspect> aspect;
        private final int life;
        private final int tilt;
        private final int startX;
        private final int startY;
        private final int endX;
        private final int endY;
        private final int appearFrame;
        private final int appearSpin;
        private final int appearColor;
        private final int arrivalFrame;
        private final int arrivalSpin;
        private final int arrivalColor;
        private int remaining;

        private Entry(@Nullable KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, @Nullable Holder<IAspect> aspect, int life, long seed) {
            this.type = type;
            this.category = category;
            this.aspect = aspect;
            this.life = life;
            this.remaining = life;
            this.tilt = TILT_CENTRE - roll(seed, 0, TILT_CHOICES);
            this.startX = roll(seed, 1, START_SPREAD);
            this.startY = roll(seed, 2, START_SPREAD);
            this.endX = roll(seed, 3, END_SPREAD);
            this.endY = roll(seed, 4, END_SPREAD);
            this.appearFrame = roll(seed, 5, ParticleTextures.STAR_GLINT_FRAMES);
            this.appearSpin = roll(seed, 6, FULL_TURN_DEGREES);
            this.appearColor = glintColor(seed, 7);
            this.arrivalFrame = roll(seed, 9, ParticleTextures.STAR_GLINT_FRAMES);
            this.arrivalSpin = roll(seed, 10, FULL_TURN_DEGREES);
            this.arrivalColor = glintColor(seed, 11);
        }

        private boolean age() {
            return --remaining <= 0;
        }
    }

    private static final class Spark {
        private final boolean star;
        private final int greenBlue;
        private final int life;
        private int delay;
        private int age;
        private double x;
        private double y;
        private double lastX;
        private double lastY;
        private double motionX;
        private double motionY;

        private Spark(double centreX, double centreY, RandomSource random) {
            this.x = centreX + random.nextGaussian() * SPARK_SCATTER;
            this.y = centreY + random.nextGaussian() * SPARK_SCATTER;
            this.lastX = x;
            this.lastY = y;
            this.motionX = random.nextGaussian() * SPARK_KICK;
            this.motionY = random.nextGaussian() * SPARK_KICK;
            this.delay = random.nextInt(SPARK_DELAY_CHOICES);
            this.life = SPARK_LIFE_MIN + random.nextInt(SPARK_LIFE_CHOICES);
            this.star = random.nextFloat() < STAR_SPARK_CHANCE;
            this.greenBlue = ARGB.color(0, 0, GREEN_MIN + random.nextInt(CHANNEL_MAX - GREEN_MIN + 1), BLUE_MIN + random.nextInt(CHANNEL_MAX - BLUE_MIN + 1));
        }

        private boolean tick(RandomSource random) {
            if (delay > 0) {
                delay--;
                return false;
            }
            lastX = x;
            lastY = y;
            x += motionX;
            y += motionY;
            motionX = motionX * SPARK_DRAG + random.nextGaussian() * SPARK_WOBBLE;
            motionY = motionY * SPARK_DRAG + SPARK_FALL + random.nextGaussian() * SPARK_WOBBLE;
            return ++age >= life;
        }

        private void draw(GuiGraphicsExtractor graphics, float partial) {
            if (delay > 0) {
                return;
            }
            float progress = (age + partial) / life;
            float alpha = progress < SPARK_FADE_SHARE ? progress / SPARK_FADE_SHARE : progress > 1.0F - SPARK_FADE_SHARE ? (1.0F - progress) / SPARK_FADE_SHARE : 1.0F;
            int color = ARGB.color(Mth.clamp((int) (alpha * CHANNEL_MAX), 0, CHANNEL_MAX), CHANNEL_MAX, ARGB.green(greenBlue), ARGB.blue(greenBlue));
            float size = Mth.lerp(Mth.clamp(progress, 0.0F, 1.0F), SPARK_SIZE_START, SPARK_SIZE_END);
            int frames = star ? ParticleTextures.STAR_GLINT_FRAMES : ParticleTextures.ORB_GLOW_FRAMES;
            drawFrame(graphics, TTRenderPipelines.GUI_TEXTURED_ADDITIVE, star ? ParticleTextures.STAR_GLINT : ParticleTextures.ORB_GLOW, (float) Mth.lerp(partial, lastX, x),
                    (float) Mth.lerp(partial, lastY, y), size, 0.0F, age % frames, color);
        }
    }
}

package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.IScannable;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedSky;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ScanSky implements IScannable {
    private static final Identifier NODES = TTIds.rl("nodes");
    private static final int FULL_TURN = 360;
    private static final int HALF_TURN = 180;
    private static final int QUARTER_TURN = 90;
    private static final int FACING_OFFSET = 90;
    private static final int DIRECTION_TOLERANCE = 10;
    private static final int ELEVATION_TOLERANCE = 7;
    private static final int DAY_LAST_CYCLE = 180;
    private static final int NIGHT_FIRST_CYCLE = 181;
    private static final int NIGHT_ARC_BASE = 180;
    private static final long TICKS_PER_DAY = 24000L;
    private static final String SUN_NAME = "sun";
    private static final String MOON_PREFIX = "moon";
    private static final String STAR_PREFIX = "star";
    private static final String CELESTIAL_SEGMENT = "celestial/";
    private static final String ALREADY_STUDIED_KEY = "message.thaumaturge.celestial.already_studied";
    private static final String CANNOT_NOTE_KEY = "message.thaumaturge.celestial.cannot_note";
    private static final int NO_SLOT = -1;
    private static final Map<Direction, Integer> STAR_QUADRANTS = Map.of(Direction.NORTH, 0, Direction.SOUTH, 1, Direction.WEST, 2, Direction.EAST, 3);
    private static final SkyState[] SKY_TABLE = buildSkyTable();

    public ScanSky() {}

    @FunctionalInterface
    private interface BodyResolver {
        @Nullable
        Observation resolve(ServerPlayer player, boolean aligned);
    }

    private enum SkyPhase {
        DAY(0, DAY_LAST_CYCLE, 0, false, ScanSky::resolveDaylight), NIGHT(NIGHT_FIRST_CYCLE, FULL_TURN - 1, NIGHT_ARC_BASE, true, ScanSky::resolveNight);

        private final int firstCycle;
        private final int lastCycle;
        private final int arcBase;
        private final boolean scannableOffAxis;
        private final BodyResolver resolver;

        SkyPhase(int firstCycle, int lastCycle, int arcBase, boolean scannableOffAxis, BodyResolver resolver) {
            this.firstCycle = firstCycle;
            this.lastCycle = lastCycle;
            this.arcBase = arcBase;
            this.scannableOffAxis = scannableOffAxis;
            this.resolver = resolver;
        }
    }

    private enum ArcLeg {
        RISING(QUARTER_TURN, 0, 1, 0), SETTING(HALF_TURN, HALF_TURN, -1, HALF_TURN);

        private final int lastArc;
        private final int elevationBase;
        private final int elevationSlope;
        private final int heading;

        ArcLeg(int lastArc, int elevationBase, int elevationSlope, int heading) {
            this.lastArc = lastArc;
            this.elevationBase = elevationBase;
            this.elevationSlope = elevationSlope;
            this.heading = heading;
        }

        static ArcLeg at(int arc) {
            for (ArcLeg leg : values()) {
                if (arc <= leg.lastArc) {
                    return leg;
                }
            }
            return SETTING;
        }
    }

    private record SkyState(SkyPhase phase, int elevation, int heading) {
        boolean alignedWith(Player player) {
            int headingGap = turnGap((int) (player.getYRot() + FACING_OFFSET), heading);
            int elevationGap = Math.abs((int) Math.abs(player.getXRot()) - elevation);
            return headingGap < DIRECTION_TOLERANCE && elevationGap < ELEVATION_TOLERANCE;
        }

        boolean scannable(Player player) {
            return phase.scannableOffAxis || alignedWith(player);
        }
    }

    private record Observation(String name, CelestialBody body) {
    }

    private enum Filing {
        ALREADY_STUDIED(ALREADY_STUDIED_KEY), NO_MATERIALS(CANNOT_NOTE_KEY), NOTED(null);

        private final @Nullable String messageKey;

        Filing(@Nullable String messageKey) {
            this.messageKey = messageKey;
        }

        void announce(ServerPlayer player) {
            if (messageKey != null) {
                player.sendOverlayMessage(Component.translatable(messageKey).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    private static final class NoteLedger {
        private final ServerPlayer player;
        private final IPlayerKnowledge knowledge;
        private final int worldDay;

        NoteLedger(ServerPlayer player) {
            this.player = player;
            this.knowledge = KnowledgeAccess.of(player);
            this.worldDay = (int) (player.level().getGameTime() / TICKS_PER_DAY);
        }

        Filing settle(Observation observation) {
            Identifier key = ScanKeys.celestial(worldDay, observation.name());
            if (knowledge.isResearchKnown(key)) {
                Filing.ALREADY_STUDIED.announce(player);
                return Filing.ALREADY_STUDIED;
            }
            Filing filing = file(observation, key);
            filing.announce(player);
            purgeOtherDays();
            return filing;
        }

        private Filing file(Observation observation, Identifier key) {
            Inventory inventory = player.getInventory();
            int paperSlot = firstSlot(inventory, Items.PAPER);
            boolean hasTools = firstSlot(inventory, TTItems.SCRIBING_TOOLS.get()) != NO_SLOT;
            if (!hasTools || paperSlot == NO_SLOT) {
                return Filing.NO_MATERIALS;
            }
            inventory.getItem(paperSlot).shrink(1);
            ItemStack note = CelestialNotesItem.stackOf(observation.body());
            if (!inventory.add(note)) {
                player.drop(note, false);
            }
            ScanningManager.progressResearch(player, key);
            return Filing.NOTED;
        }

        private void purgeOtherDays() {
            String todayPrefix = ScanKeys.celestial(worldDay, "").getPath();
            List<Identifier> outdated = knowledge.researchList().stream().filter(id -> id.getNamespace().equals(TTIds.MODID))
                    .filter(id -> id.getPath().contains(CELESTIAL_SEGMENT) && !id.getPath().startsWith(todayPrefix)).toList();
            if (outdated.isEmpty()) {
                return;
            }
            outdated.forEach(knowledge::removeResearch);
            knowledge.sync(player);
        }
    }

    @Override
    public boolean matches(Player player, ScanTarget target) {
        return gateOpen(player, target) && skyAt(player).scannable(player);
    }

    @Override
    public void onScanned(Player player, ScanTarget target) {
        if (!gateOpen(player, target) || !(player instanceof ServerPlayer viewer)) {
            return;
        }
        SkyState sky = skyAt(viewer);
        Observation observation = sky.phase.resolver.resolve(viewer, sky.alignedWith(viewer));
        if (observation == null) {
            return;
        }
        new NoteLedger(viewer).settle(observation);
    }

    @Override
    public @Nullable Identifier research(Player player, ScanTarget target) {
        return null;
    }

    private static boolean gateOpen(Player player, ScanTarget target) {
        if (!(target instanceof ScannedSky) || player.getXRot() > 0) {
            return false;
        }
        Level level = player.level();
        if (level.dimension() != Level.OVERWORLD || !level.canSeeSky(player.blockPosition().above())) {
            return false;
        }
        return KnowledgeAccess.of(player).isResearchComplete(NODES);
    }

    private static SkyState skyAt(Player player) {
        int sunAngle = player.level().environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, player.position()).intValue();
        return SKY_TABLE[Math.floorMod(sunAngle + QUARTER_TURN, FULL_TURN)];
    }

    private static SkyState[] buildSkyTable() {
        SkyState[] table = new SkyState[FULL_TURN];
        for (SkyPhase phase : SkyPhase.values()) {
            for (int cycle = phase.firstCycle; cycle <= phase.lastCycle; cycle++) {
                int arc = cycle - phase.arcBase;
                ArcLeg leg = ArcLeg.at(arc);
                table[cycle] = new SkyState(phase, leg.elevationBase + leg.elevationSlope * arc, leg.heading);
            }
        }
        return table;
    }

    private static int turnGap(int first, int second) {
        int forward = Math.floorMod(first - second, FULL_TURN);
        return Math.min(forward, FULL_TURN - forward);
    }

    private static @Nullable Observation resolveDaylight(ServerPlayer player, boolean aligned) {
        return aligned ? new Observation(SUN_NAME, CelestialBody.SUN) : null;
    }

    private static Observation resolveNight(ServerPlayer player, boolean aligned) {
        if (aligned) {
            int phase = player.level().environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, player.position()).index();
            return new Observation(MOON_PREFIX + phase, CelestialBody.moon(phase));
        }
        int quadrant = STAR_QUADRANTS.getOrDefault(player.getDirection(), 0);
        return new Observation(STAR_PREFIX + quadrant, CelestialBody.star(quadrant));
    }

    private static int firstSlot(Inventory inventory, Item item) {
        return IntStream.range(0, inventory.getContainerSize()).filter(slot -> inventory.getItem(slot).is(item)).findFirst().orElse(NO_SLOT);
    }
}

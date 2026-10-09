package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ScanGlyphParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundBoreDigPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class Effects {
    static final double DEFAULT_RADIUS = 64.0;

    private static final double BORE_DIG_RADIUS = 32.0;
    private static final int NO_ENTITY = -1;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final EffectColor BAMF_COLOR = new EffectColor(0.5F, 0.1F, 0.6F);
    private static final int SPARKLE_SKIP_SIDES = 6;
    private static final int SPARKLE_SPAWN_SIDES = 4;
    private static final float SPARKLE_SCALE_BASE = 0.6F;
    private static final float SPARKLE_SCALE_SPREAD = 0.2F;
    private static final float SPARKLE_DECAY = 1.0F;
    private static final int SPARKLE_BASE_AGE = 2;
    private static final int WISPY_AGE = 30;
    private static final EffectColor WISPY_COLOR = new EffectColor(0.5F, 0.5F, 0.5F);
    private static final float WISPY_ENTITY_GRAVITY = 0.2F;
    private static final float WISPY_BLOCK_RED_BASE = 0.4F;
    private static final float WISPY_BLOCK_RED_SPREAD = 0.6F;
    private static final float WISPY_BLOCK_GREEN_BASE = 0.6F;
    private static final float WISPY_BLOCK_BLUE_BASE = 0.6F;
    private static final float WISPY_BLOCK_GB_SPREAD = 0.4F;
    private static final double CURLY_JITTER_BASE = 0.0025;
    private static final double CURLY_JITTER_SPREAD = 0.005;
    private static final double CURLY_SIDE_BIAS = 0.025;
    private static final double CURLY_SPAWN_LEAD = 5.0;
    private static final float VENT_DEFAULT_SCALE = 1.0F;
    private static final int VENT_FLAME_SIDES = 6;
    private static final int VENT_FLAME_CHANCE = 2;
    private static final EffectColor VENT_FLAME_COLOR = new EffectColor(1.0F, 0.7F, 0.2F);
    private static final float VENT_FLAME_ALPHA = 0.9F;
    private static final float VENT_FLAME_SCALE_BASE = 0.25F;
    private static final float VENT_FLAME_SCALE_SPREAD = 0.1F;
    private static final float VENT_FLAME_END_SCALE = 0.25F;
    private static final double VENT_FLAME_MOTION_FACTOR = 0.5;
    private static final int BLOCK_RUNES_DURATION = 30;
    private static final double BLOCK_RUNES_OFFSET = 0.5;
    private static final float SMOKE_RADIUS = 1.0F;
    private static final int BEAM_AGE = 20;
    private static final float BEAM_END_MODIFIER = 1.0F;
    private static final float ARC_GRAVITY = 0.1F;
    private static final float BOLT_WIDTH = 1.0F;
    private static final int BORE_STREAM_COLOR = 0x8040C0;
    private static final int BORE_STREAM_EXTEND = 10;
    private static final float STREAM_SCALE = 0.15F;
    private static final float WISP_FLAME_ALPHA = 0.5F;
    private static final float WISP_FLAME_SCALE_BASE = 1.0F;
    private static final float WISP_FLAME_SCALE_SPREAD = 0.25F;
    private static final float WISP_FLAME_END_SCALE = 0.05F;
    private static final float FIRE_MOTE_TRANSLUCENT_DIVISOR = 3.0F;
    private static final int STABILIZER_LIFE = 20;
    private static final double POLLUTION_INSET = 0.2;
    private static final double POLLUTION_SPAN = 0.6;
    private static final int PECH_AGE_BASE = 10;
    private static final int PECH_AGE_RANGE = 10;
    private static final float PECH_GRAVITY = -0.01F;
    private static final EffectColor FLUX_FUME_COLOR = new EffectColor(1.0F, 0.0F, 0.5F);
    private static final float FLUX_FUME_SCALE = 0.3F;
    private static final int FLUX_FUME_MAX_AGE = 3;
    private static final EffectColor BORE_SPARKLE_COLOR = new EffectColor(0.6F, 0.2F, 0.8F);
    private static final float SCAN_ADDITIVE_THRESHOLD = 0.25F;
    private static final double SLASH_MIN_HORIZONTAL = 1.0E-7;
    private static final double SLASH_YAW_OFFSET = 90.0;
    private static final double SLASH_ROLL_DEVIATION_DEGREES = 20.0;

    private Effects() {}

    public static Bamf bamf(ServerLevel level, Vec3 pos) {
        return new Bamf(level, pos);
    }

    public static Bamf bamf(ServerLevel level, BlockPos pos) {
        return new Bamf(level, Vec3.atCenterOf(pos));
    }

    public static Glint glint(ServerLevel level, Vec3 pos) {
        return new Glint(level, pos);
    }

    public static Glint glint(ServerLevel level, BlockPos pos) {
        return new Glint(level, Vec3.atCenterOf(pos));
    }

    public static SimpleSparkle simpleSparkle(ServerLevel level, Vec3 pos) {
        return new SimpleSparkle(level, pos);
    }

    public static WispyMotes wispyMotes(ServerLevel level, Vec3 pos) {
        return new WispyMotes(level, pos);
    }

    public static CurlyWisp curlyWisp(ServerLevel level, Vec3 pos) {
        return new CurlyWisp(level, pos);
    }

    public static Vent vent(ServerLevel level, Vec3 pos) {
        return new Vent(level, pos, false);
    }

    public static Vent vent2(ServerLevel level, Vec3 pos) {
        return new Vent(level, pos, true);
    }

    public static ZapArc zapArc(ServerLevel level, Vec3 from) {
        return new ZapArc(level, from);
    }

    public static BoltStrike boltStrike(ServerLevel level, Vec3 from) {
        return new BoltStrike(level, from);
    }

    public static GlyphField glyphField(ServerLevel level, Vec3 corner) {
        return new GlyphField(level, corner, false);
    }

    public static GlyphField glyphFieldAlt(ServerLevel level, Vec3 corner) {
        return new GlyphField(level, corner, true);
    }

    public static SpiralSmoke spiralSmoke(ServerLevel level, Vec3 pos) {
        return new SpiralSmoke(level, pos);
    }

    public static BeamWand beamWand(ServerLevel level, LivingEntity source) {
        return new BeamWand(level, source);
    }

    public static DrillBeam drillBeam(ServerLevel level, Vec3 from) {
        return new DrillBeam(level, from);
    }

    public static BoreDebris boreDebris(ServerLevel level, Vec3 from, BlockState state) {
        return new BoreDebris(level, from, state);
    }

    public static BoreSparkle boreSparkle(ServerLevel level, Vec3 from) {
        return new BoreSparkle(level, from);
    }

    public static void boreDig(ServerLevel level, BlockPos target, Entity bore, int delay) {
        sendBoreDig(level, target, bore.getId(), bore.blockPosition(), delay);
    }

    public static void boreDig(ServerLevel level, BlockPos target, BlockPos borePos, int delay) {
        sendBoreDig(level, target, NO_ENTITY, borePos, delay);
    }

    public static BoreStream boreStream(ServerLevel level, Vec3 from, Entity target) {
        return new BoreStream(level, from, target);
    }

    public static VoidStream voidStream(ServerLevel level, Vec3 from) {
        return new VoidStream(level, from);
    }

    public static FireMote fireMote(ServerLevel level, Vec3 pos) {
        return new FireMote(level, pos);
    }

    public static EmberGlow emberGlow(ServerLevel level, Vec3 pos) {
        return new EmberGlow(level, pos);
    }

    public static Taint taint(ServerLevel level, Vec3 pos) {
        return new Taint(level, pos);
    }

    public static LightningFlash lightningFlash(ServerLevel level, Vec3 pos) {
        return new LightningFlash(level, pos);
    }

    public static LiftMist liftMist(ServerLevel level, Vec3 pos) {
        return new LiftMist(level, pos);
    }

    public static SteadyRune steadyRune(ServerLevel level, Vec3 pos) {
        return new SteadyRune(level, pos);
    }

    public static GolemFly golemFly(ServerLevel level, Vec3 pos) {
        return new GolemFly(level, pos);
    }

    public static Pollution pollution(ServerLevel level, BlockPos corner) {
        return new Pollution(level, corner);
    }

    public static FocusCloud focusCloud(ServerLevel level, Vec3 pos) {
        return new FocusCloud(level, pos);
    }

    public static BlockMist blockMist(ServerLevel level, BlockPos pos) {
        return new BlockMist(level, pos);
    }

    public static BlockMistFlat blockMistFlat(ServerLevel level, BlockPos pos) {
        return new BlockMistFlat(level, pos);
    }

    public static WispParticles wispParticles(ServerLevel level, Vec3 pos) {
        return new WispParticles(level, pos);
    }

    public static CauldronBubble cauldronBubble(ServerLevel level, Vec3 pos) {
        return new CauldronBubble(level, pos);
    }

    public static CauldronBoil cauldronBoil(ServerLevel level, Vec3 pos) {
        return new CauldronBoil(level, pos);
    }

    public static CauldronFoamUp cauldronFoamUp(ServerLevel level, Vec3 pos) {
        return new CauldronFoamUp(level, pos);
    }

    public static CauldronFoamDown cauldronFoamDown(ServerLevel level, Vec3 pos) {
        return new CauldronFoamDown(level, pos);
    }

    public static Spark spark(ServerLevel level, Vec3 pos) {
        return new Spark(level, pos);
    }

    public static Burst burst(ServerLevel level, Vec3 pos) {
        return new Burst(level, pos);
    }

    public static EssentiaDrop essentiaDrop(ServerLevel level, Vec3 pos) {
        return new EssentiaDrop(level, pos);
    }

    public static JarSplash jarSplash(ServerLevel level, Vec3 pos) {
        return new JarSplash(level, pos);
    }

    public static LineSparkle lineSparkle(ServerLevel level, Vec3 pos) {
        return new LineSparkle(level, pos);
    }

    public static BlockSparkles blockSparkles(ServerLevel level, BlockPos pos) {
        return new BlockSparkles(level, pos);
    }

    public static PechsCurse pechsCurse(ServerLevel level, Vec3 pos) {
        return new PechsCurse(level, pos);
    }

    public static CrimsonPuff crimsonPuff(ServerLevel level, Vec3 pos) {
        return new CrimsonPuff(level, pos);
    }

    public static WispyMotesEntity wispyMotesEntity(ServerLevel level, Vec3 origin, int entityId) {
        return new WispyMotesEntity(level, origin, entityId);
    }

    public static WispyMotesOnBlock wispyMotesOnBlock(ServerLevel level, BlockPos corner) {
        return new WispyMotesOnBlock(level, corner);
    }

    public static FluxFume fluxFume(ServerLevel level, Vec3 pos) {
        return new FluxFume(level, pos);
    }

    public static FluxFume fluxFume(ServerLevel level, BlockPos pos) {
        return new FluxFume(level, Vec3.atCenterOf(pos));
    }

    public static FireMoteParticleOptions fireMoteData(RandomSource rng, double mx, double my, double mz, float red, float green, float blue, float opacity, float baseScale) {
        boolean translucent = rng.nextBoolean();
        float size = translucent ? baseScale / FIRE_MOTE_TRANSLUCENT_DIVISOR : baseScale;
        return new FireMoteParticleOptions(mx, my, mz, red, green, blue, opacity, size, translucent);
    }

    public static void spawn(ServerLevel level, ParticleOptions particle, double px, double py, double pz) {
        broadcastNear(level, px, py, pz, DEFAULT_RADIUS, new ClientboundSpawnParticlePayload(particle, px, py, pz, 0.0, 0.0, 0.0));
    }

    public static void spawn(ServerLevel level, ParticleOptions particle, double px, double py, double pz, double mx, double my, double mz) {
        broadcastNear(level, px, py, pz, DEFAULT_RADIUS, new ClientboundSpawnParticlePayload(particle, px, py, pz, mx, my, mz));
    }

    public static void scanGlyph(ServerPlayer viewer, double px, double py, double pz, int rgb, int delay) {
        boolean additive = EffectColor.mean(rgb) >= SCAN_ADDITIVE_THRESHOLD;
        PacketDistributor.sendToPlayer(viewer, new ClientboundSpawnParticlePayload(new ScanGlyphParticleOptions(rgb, delay, additive), px, py, pz));
    }

    public static void slash(ServerLevel level, double sx, double sy, double sz, double ex, double ey, double ez, int duration) {
        double dx = ex - sx;
        double dy = ey - sy;
        double dz = ez - sz;
        float roll = (float) Math.toRadians(level.getRandom().nextGaussian() * SLASH_ROLL_DEVIATION_DEGREES);
        double planar = Math.hypot(dx, dz);
        float yaw = 0.0F;
        float pitch = 0.0F;
        if (planar >= SLASH_MIN_HORIZONTAL) {
            yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - SLASH_YAW_OFFSET);
            pitch = (float) -Math.toDegrees(Math.atan2(dy, planar));
        }
        SlashParticleOptions options = new SlashParticleOptions(duration, yaw, pitch, roll);
        spawn(level, options, sx, sy, sz, dx / duration, dy / duration, dz / duration);
    }

    private static void sendBoreDig(ServerLevel level, BlockPos target, int boreEntityId, BlockPos borePos, int delay) {
        ClientboundBoreDigPayload payload = new ClientboundBoreDigPayload(target, boreEntityId, borePos, delay);
        broadcastNear(level, target.getX(), target.getY(), target.getZ(), BORE_DIG_RADIUS, payload);
    }

    private static void broadcastNear(ServerLevel level, double x, double y, double z, double radius, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }

    private static void broadcastStream(ServerLevel level, Vec3 origin, ClientboundStreamEffectPayload payload) {
        broadcastNear(level, origin.x, origin.y, origin.z, DEFAULT_RADIUS, payload);
    }

    private static void spawnMoving(ServerLevel level, ParticleOptions options, Vec3 at, Vec3 velocity) {
        spawn(level, options, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
    }

    public static final class Bamf {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = BAMF_COLOR;
        private boolean sound;
        private boolean fancy;
        private @Nullable Direction side;

        Bamf(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Bamf color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public Bamf withSound() {
            this.sound = true;
            return this;
        }

        public Bamf fancy() {
            this.fancy = true;
            return this;
        }

        public Bamf side(Direction side) {
            this.side = side;
            return this;
        }

        public void send() {
            PuffCloud.send(level, pos, color, sound, fancy, side);
        }
    }

    public static final class Glint {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = EffectColor.WHITE;

        Glint(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Glint color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public void send() {
            RandomSource random = level.getRandom();
            if (random.nextInt(SPARKLE_SKIP_SIDES) >= SPARKLE_SPAWN_SIDES) {
                return;
            }
            float scale = SPARKLE_SCALE_BASE + random.nextFloat() * SPARKLE_SCALE_SPREAD;
            spawn(level, new SparkleParticleOptions(color.argb(), scale, 0, SPARKLE_DECAY, 0.0F, SPARKLE_BASE_AGE, false), pos.x, pos.y, pos.z);
        }
    }

    public static final class SimpleSparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private final SparkleSettings settings = new SparkleSettings();

        SimpleSparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public SimpleSparkle motion(double x, double y, double z) {
            settings.motion = new Vec3(x, y, z);
            return this;
        }

        public SimpleSparkle color(float r, float g, float b) {
            settings.color = new EffectColor(r, g, b);
            return this;
        }

        public SimpleSparkle scale(float scale) {
            settings.scale = scale;
            return this;
        }

        public SimpleSparkle delay(int delay) {
            settings.delay = delay;
            return this;
        }

        public SimpleSparkle decay(float decay) {
            settings.decay = decay;
            return this;
        }

        public SimpleSparkle drift(float gravity) {
            settings.gravity = gravity;
            return this;
        }

        public SimpleSparkle baseAge(int baseAge) {
            settings.baseAge = baseAge;
            return this;
        }

        public void send() {
            settings.send(level, pos, true);
        }
    }

    public static final class LineSparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private final SparkleSettings settings = new SparkleSettings();

        LineSparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public LineSparkle motion(double x, double y, double z) {
            settings.motion = new Vec3(x, y, z);
            return this;
        }

        public LineSparkle color(float r, float g, float b) {
            settings.color = new EffectColor(r, g, b);
            return this;
        }

        public LineSparkle scale(float scale) {
            settings.scale = scale;
            return this;
        }

        public LineSparkle delay(int delay) {
            settings.delay = delay;
            return this;
        }

        public LineSparkle decay(float decay) {
            settings.decay = decay;
            return this;
        }

        public LineSparkle drift(float gravity) {
            settings.gravity = gravity;
            return this;
        }

        public LineSparkle baseAge(int baseAge) {
            settings.baseAge = baseAge;
            return this;
        }

        public void send() {
            settings.send(level, pos, false);
        }
    }

    public static final class WispyMotes {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private int age = WISPY_AGE;
        private EffectColor color = WISPY_COLOR;
        private boolean randomColor;
        private float gravity;

        WispyMotes(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public WispyMotes motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public WispyMotes age(int age) {
            this.age = age;
            return this;
        }

        public WispyMotes color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            this.randomColor = false;
            return this;
        }

        public WispyMotes randomColor() {
            this.randomColor = true;
            return this;
        }

        public WispyMotes drift(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            EffectColor shown = randomColor ? EffectColor.randomMote(level.getRandom()) : color;
            spawnMoving(level, new WispyMoteParticleOptions(shown.argb(), age, gravity, NO_ENTITY), pos, motion);
        }
    }

    public static final class WispyMotesEntity {
        private final ServerLevel level;
        private final Vec3 origin;
        private final int entityId;
        private EffectColor color = EffectColor.WHITE;

        WispyMotesEntity(ServerLevel level, Vec3 origin, int entityId) {
            this.level = level;
            this.origin = origin;
            this.entityId = entityId;
        }

        public WispyMotesEntity color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public void send() {
            spawn(level, new WispyMoteParticleOptions(color.argb(), WISPY_AGE, WISPY_ENTITY_GRAVITY, entityId), origin.x, origin.y, origin.z);
        }
    }

    public static final class WispyMotesOnBlock {
        private final ServerLevel level;
        private final BlockPos corner;
        private int age = WISPY_AGE;
        private float gravity;

        WispyMotesOnBlock(ServerLevel level, BlockPos corner) {
            this.level = level;
            this.corner = corner;
        }

        public WispyMotesOnBlock age(int age) {
            this.age = age;
            return this;
        }

        public WispyMotesOnBlock drift(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            RandomSource random = level.getRandom();
            EffectColor color = new EffectColor(WISPY_BLOCK_RED_BASE + random.nextFloat() * WISPY_BLOCK_RED_SPREAD, WISPY_BLOCK_GREEN_BASE + random.nextFloat() * WISPY_BLOCK_GB_SPREAD,
                    WISPY_BLOCK_BLUE_BASE + random.nextFloat() * WISPY_BLOCK_GB_SPREAD);
            spawn(level, new WispyMoteParticleOptions(color.argb(), age, gravity, NO_ENTITY), corner.getX() + random.nextDouble(), corner.getY(), corner.getZ() + random.nextDouble());
        }
    }

    public static final class CurlyWisp {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private float scale = 1.0F;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;
        private @Nullable Direction side;
        private int seed;
        private int delay;

        CurlyWisp(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CurlyWisp motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public CurlyWisp scale(float scale) {
            this.scale = scale;
            return this;
        }

        public CurlyWisp color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public CurlyWisp alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public CurlyWisp side(@Nullable Direction side) {
            this.side = side;
            return this;
        }

        public CurlyWisp seed(int seed) {
            this.seed = seed;
            return this;
        }

        public CurlyWisp delay(int delay) {
            this.delay = delay;
            return this;
        }

        public void send() {
            RandomSource random = level.getRandom();
            double mx = motion.x + EffectRandom.signedSpeed(random, CURLY_JITTER_BASE, CURLY_JITTER_SPREAD);
            double my = motion.y + EffectRandom.signedSpeed(random, CURLY_JITTER_BASE, CURLY_JITTER_SPREAD);
            double mz = motion.z + EffectRandom.signedSpeed(random, CURLY_JITTER_BASE, CURLY_JITTER_SPREAD);
            if (side != null) {
                mx += side.getStepX() * CURLY_SIDE_BIAS;
                my += side.getStepY() * CURLY_SIDE_BIAS;
                mz += side.getStepZ() * CURLY_SIDE_BIAS;
            }
            CurlyWispParticleOptions options = new CurlyWispParticleOptions(color.argb(), alpha, scale, delay, seed);
            spawn(level, options, pos.x + mx * CURLY_SPAWN_LEAD, pos.y + my * CURLY_SPAWN_LEAD, pos.z + mz * CURLY_SPAWN_LEAD, mx, my, mz);
        }
    }

    public static final class Vent {
        private final ServerLevel level;
        private final Vec3 pos;
        private final boolean variant;
        private Vec3 motion = Vec3.ZERO;
        private int color = WHITE_RGB;
        private float scale = VENT_DEFAULT_SCALE;
        private boolean flame;

        Vent(ServerLevel level, Vec3 pos, boolean variant) {
            this.level = level;
            this.pos = pos;
            this.variant = variant;
        }

        public Vent motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public Vent color(int color) {
            this.color = color;
            return this;
        }

        public Vent scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Vent withFlame() {
            this.flame = true;
            return this;
        }

        public void send() {
            spawn(level, new VentParticleOptions(motion.x, motion.y, motion.z, color, scale, variant), pos.x, pos.y, pos.z);
            RandomSource random = level.getRandom();
            if (flame && random.nextInt(VENT_FLAME_SIDES) < VENT_FLAME_CHANCE) {
                float flameScale = VENT_FLAME_SCALE_BASE + random.nextFloat() * VENT_FLAME_SCALE_SPREAD;
                WispFlameParticleOptions options = new WispFlameParticleOptions(VENT_FLAME_COLOR.argb(), VENT_FLAME_ALPHA, flameScale, VENT_FLAME_END_SCALE, 0);
                spawn(level, options, pos.x, pos.y, pos.z, motion.x * VENT_FLAME_MOTION_FACTOR, motion.y * VENT_FLAME_MOTION_FACTOR, motion.z * VENT_FLAME_MOTION_FACTOR);
            }
        }
    }

    public static final class GlyphField {
        private final ServerLevel level;
        private final Vec3 corner;
        private final boolean variant;
        private EffectColor color = EffectColor.WHITE;
        private int duration = BLOCK_RUNES_DURATION;
        private float gravity;

        GlyphField(ServerLevel level, Vec3 corner, boolean variant) {
            this.level = level;
            this.corner = corner;
            this.variant = variant;
        }

        public GlyphField color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public GlyphField lifetime(int duration) {
            this.duration = duration;
            return this;
        }

        public GlyphField drift(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            BlockRunesParticleOptions options = new BlockRunesParticleOptions(color.r(), color.g(), color.b(), duration, gravity, variant);
            spawn(level, options, corner.x + BLOCK_RUNES_OFFSET, corner.y + BLOCK_RUNES_OFFSET, corner.z + BLOCK_RUNES_OFFSET);
        }
    }

    public static final class SpiralSmoke {
        private final ServerLevel level;
        private final Vec3 pos;
        private float radius = SMOKE_RADIUS;
        private int start;
        private int minY;
        private int color = WHITE_RGB;

        SpiralSmoke(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public SpiralSmoke radius(float radius) {
            this.radius = radius;
            return this;
        }

        public SpiralSmoke start(int start) {
            this.start = start;
            return this;
        }

        public SpiralSmoke minY(int minY) {
            this.minY = minY;
            return this;
        }

        public SpiralSmoke color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            EffectColor channels = EffectColor.ofRgb(color);
            spawn(level, new SmokeSpiralParticleOptions(radius, start, minY, channels.r(), channels.g(), channels.b()), pos.x, pos.y, pos.z);
        }
    }

    private static final class BeamSettings {
        private int color = WHITE_RGB;
        private int age = BEAM_AGE;
        private int type;
        private float endMod = BEAM_END_MODIFIER;
        private boolean reverse;

        ClientboundStreamEffectPayload toPayload(Vec3 start, Vec3 end, int sourceId, boolean drill) {
            return ClientboundStreamEffectPayload.beam(start.x, start.y, start.z, end.x, end.y, end.z, color, age, type, endMod, reverse, sourceId, drill);
        }
    }

    public static final class BeamWand {
        private final ServerLevel level;
        private final Vec3 source;
        private final int sourceId;
        private final BeamSettings settings = new BeamSettings();
        private @Nullable Vec3 target;

        BeamWand(ServerLevel level, LivingEntity source) {
            this.level = level;
            this.source = source.position();
            this.sourceId = source.getId();
        }

        public BeamWand to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BeamWand color(int color) {
            settings.color = color;
            return this;
        }

        public BeamWand age(int age) {
            settings.age = age;
            return this;
        }

        public BeamWand type(int type) {
            settings.type = type;
            return this;
        }

        public BeamWand endMod(float endMod) {
            settings.endMod = endMod;
            return this;
        }

        public BeamWand reverse(boolean reverse) {
            settings.reverse = reverse;
            return this;
        }

        public void send() {
            Vec3 end = target;
            if (end != null) {
                broadcastStream(level, source, settings.toPayload(source, end, sourceId, false));
            }
        }
    }

    public static final class DrillBeam {
        private final ServerLevel level;
        private final Vec3 source;
        private final BeamSettings settings = new BeamSettings();
        private @Nullable Vec3 target;

        DrillBeam(ServerLevel level, Vec3 source) {
            this.level = level;
            this.source = source;
        }

        public DrillBeam to(Vec3 target) {
            this.target = target;
            return this;
        }

        public DrillBeam color(int color) {
            settings.color = color;
            return this;
        }

        public DrillBeam age(int age) {
            settings.age = age;
            return this;
        }

        public DrillBeam type(int type) {
            settings.type = type;
            return this;
        }

        public DrillBeam endMod(float endMod) {
            settings.endMod = endMod;
            return this;
        }

        public DrillBeam reverse(boolean reverse) {
            settings.reverse = reverse;
            return this;
        }

        public void send() {
            Vec3 end = target;
            if (end != null) {
                broadcastStream(level, source, settings.toPayload(source, end, NO_ENTITY, true));
            }
        }
    }

    public static final class ZapArc {
        private final ServerLevel level;
        private final Vec3 from;
        private @Nullable Vec3 target;
        private int color = WHITE_RGB;
        private float gravity = ARC_GRAVITY;

        ZapArc(ServerLevel level, Vec3 from) {
            this.level = level;
            this.from = from;
        }

        public ZapArc to(Vec3 target) {
            this.target = target;
            return this;
        }

        public ZapArc color(int color) {
            this.color = color;
            return this;
        }

        public ZapArc drift(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            Vec3 end = target;
            if (end == null) {
                return;
            }
            EffectDispatch.spawnArc(level, from, end, color, gravity);
        }
    }

    public static final class BoltStrike {
        private final ServerLevel level;
        private final Vec3 from;
        private @Nullable Vec3 target;
        private int color = WHITE_RGB;
        private float width = BOLT_WIDTH;
        private int sourceEntityId = NO_ENTITY;

        BoltStrike(ServerLevel level, Vec3 from) {
            this.level = level;
            this.from = from;
        }

        public BoltStrike to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BoltStrike color(int color) {
            this.color = color;
            return this;
        }

        public BoltStrike width(float width) {
            this.width = width;
            return this;
        }

        public BoltStrike sourceEntity(@Nullable Entity entity) {
            this.sourceEntityId = entity == null ? NO_ENTITY : entity.getId();
            return this;
        }

        public void send() {
            Vec3 end = target;
            if (end == null) {
                return;
            }
            EffectDispatch.spawnBolt(level, from, end, color, width, sourceEntityId);
        }
    }

    public static final class BoreStream {
        private final ServerLevel level;
        private final Vec3 source;
        private final int targetId;
        private int color = BORE_STREAM_COLOR;
        private int count;
        private float scale = STREAM_SCALE;
        private int extend = BORE_STREAM_EXTEND;
        private double upward;

        BoreStream(ServerLevel level, Vec3 source, @Nullable Entity target) {
            this.level = level;
            this.source = source;
            this.targetId = target == null ? NO_ENTITY : target.getId();
        }

        public BoreStream color(int color) {
            this.color = color;
            return this;
        }

        public BoreStream count(int count) {
            this.count = count;
            return this;
        }

        public BoreStream scale(float scale) {
            this.scale = scale;
            return this;
        }

        public BoreStream reach(int extend) {
            this.extend = extend;
            return this;
        }

        public BoreStream upward(double upward) {
            this.upward = upward;
            return this;
        }

        public void send() {
            if (targetId == NO_ENTITY) {
                return;
            }
            broadcastStream(level, source, ClientboundStreamEffectPayload.bore(source.x, source.y, source.z, targetId, color, count, scale, extend, upward));
        }
    }

    public static final class VoidStream {
        private final ServerLevel level;
        private final Vec3 source;
        private @Nullable Vec3 target;
        private int seed;
        private float scale = STREAM_SCALE;

        VoidStream(ServerLevel level, Vec3 source) {
            this.level = level;
            this.source = source;
        }

        public VoidStream to(Vec3 target) {
            this.target = target;
            return this;
        }

        public VoidStream seed(int seed) {
            this.seed = seed;
            return this;
        }

        public VoidStream scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            if (target == null) {
                return;
            }
            broadcastStream(level, source, ClientboundStreamEffectPayload.voidStream(source.x, source.y, source.z, target.x, target.y, target.z, seed, scale));
        }
    }

    public static final class BoreDebris {
        private final ServerLevel level;
        private final Vec3 source;
        private final BlockState state;
        private @Nullable Vec3 target;
        private Vec3 motion = Vec3.ZERO;

        BoreDebris(ServerLevel level, Vec3 source, BlockState state) {
            this.level = level;
            this.source = source;
            this.state = state;
        }

        public BoreDebris to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BoreDebris motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public void send() {
            if (target == null) {
                return;
            }
            BoreDebrisParticleOptions options = new BoreDebrisParticleOptions(state, target.x, target.y, target.z, motion.x, motion.y, motion.z);
            spawn(level, options, source.x, source.y, source.z);
        }
    }

    public static final class BoreSparkle {
        private final ServerLevel level;
        private final Vec3 source;
        private @Nullable Vec3 target;
        private EffectColor color = BORE_SPARKLE_COLOR;

        BoreSparkle(ServerLevel level, Vec3 source) {
            this.level = level;
            this.source = source;
        }

        public BoreSparkle to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BoreSparkle color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public BoreSparkle color(int rgb) {
            this.color = EffectColor.ofRgb(rgb);
            return this;
        }

        public void send() {
            if (target == null) {
                return;
            }
            BoreSparkleParticleOptions options = new BoreSparkleParticleOptions(target.x, target.y, target.z, color.r(), color.g(), color.b());
            spawn(level, options, source.x, source.y, source.z);
        }
    }

    public static final class FocusCloud {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private int color = WHITE_RGB;

        FocusCloud(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FocusCloud motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public FocusCloud color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            spawnMoving(level, TTParticles.colorOf(TTParticles.FOCUS_CLOUD, EffectColor.opaque(color)), pos, motion);
        }
    }

    public static final class WispParticles {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private int color = WHITE_RGB;
        private int delay;

        WispParticles(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public WispParticles motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public WispParticles color(int color) {
            this.color = color;
            return this;
        }

        public WispParticles delay(int delay) {
            this.delay = delay;
            return this;
        }

        public void send() {
            float scale = WISP_FLAME_SCALE_BASE + level.getRandom().nextFloat() * WISP_FLAME_SCALE_SPREAD;
            spawnMoving(level, new WispFlameParticleOptions(EffectColor.opaque(color), WISP_FLAME_ALPHA, scale, WISP_FLAME_END_SCALE, delay), pos, motion);
        }
    }

    public static final class LightningFlash {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        LightningFlash(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public LightningFlash color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public LightningFlash alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public LightningFlash scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, new LightningFlashParticleOptions(color.argb(), alpha, scale), pos.x, pos.y, pos.z);
        }
    }

    public static final class Spark {
        private final ServerLevel level;
        private final Vec3 pos;
        private float size = 1.0F;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;

        Spark(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Spark size(float size) {
            this.size = size;
            return this;
        }

        public Spark color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public Spark alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public void send() {
            spawn(level, new SparkParticleOptions(color.argb(), alpha, size), pos.x, pos.y, pos.z);
        }
    }

    public static final class Burst {
        private final ServerLevel level;
        private final Vec3 pos;
        private float size = 1.0F;

        Burst(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Burst size(float size) {
            this.size = size;
            return this;
        }

        public void send() {
            spawn(level, new BurstParticleOptions(size), pos.x, pos.y, pos.z);
        }
    }

    public static final class FireMote {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        FireMote(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FireMote motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public FireMote color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public FireMote alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public FireMote scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            FireMoteParticleOptions options = fireMoteData(level.getRandom(), motion.x, motion.y, motion.z, color.r(), color.g(), color.b(), alpha, scale);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }

    public static final class EmberGlow {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        EmberGlow(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public EmberGlow motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public EmberGlow color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public EmberGlow alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public EmberGlow scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, glowOptions(), pos.x, pos.y, pos.z);
        }

        private FireMoteParticleOptions glowOptions() {
            return new FireMoteParticleOptions(motion.x, motion.y, motion.z, color.r(), color.g(), color.b(), alpha, scale, true);
        }
    }

    public static final class LiftMist {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;

        LiftMist(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public LiftMist motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public void send() {
            spawnMoving(level, TTParticles.LEVITATOR_MIST.get(), pos, motion);
        }
    }

    public static final class GolemFly {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;

        GolemFly(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public GolemFly motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public void send() {
            spawnMoving(level, TTParticles.GOLEM_TRAIL.get(), pos, motion);
        }
    }

    public static final class CrimsonPuff {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;

        CrimsonPuff(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CrimsonPuff motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public void send() {
            spawnMoving(level, TTParticles.CRIMSON_SMOKE.get(), pos, motion);
        }
    }

    public static final class Taint {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private float scale = 1.0F;
        private int color = TaintFumeParticleOptions.RANDOM_COLOR;

        Taint(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Taint motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public Taint scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Taint color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            spawnMoving(level, new TaintFumeParticleOptions(color, scale), pos, motion);
        }
    }

    public static final class SteadyRune {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 motion = Vec3.ZERO;
        private int life = STABILIZER_LIFE;

        SteadyRune(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public SteadyRune motion(double x, double y, double z) {
            this.motion = new Vec3(x, y, z);
            return this;
        }

        public SteadyRune life(int life) {
            this.life = life;
            return this;
        }

        public void send() {
            spawnMoving(level, new StabilizerRuneParticleOptions(life), pos, motion);
        }
    }

    public static final class Pollution {
        private final ServerLevel level;
        private final BlockPos corner;

        Pollution(ServerLevel level, BlockPos corner) {
            this.level = level;
            this.corner = corner;
        }

        public void send() {
            RandomSource random = level.getRandom();
            double x = corner.getX() + POLLUTION_INSET + random.nextDouble() * POLLUTION_SPAN;
            double y = corner.getY() + POLLUTION_INSET + random.nextDouble() * POLLUTION_SPAN;
            double z = corner.getZ() + POLLUTION_INSET + random.nextDouble() * POLLUTION_SPAN;
            spawn(level, TTParticles.POLLUTION_FUME.get(), x, y, z);
        }
    }

    public static final class PechsCurse {
        private final ServerLevel level;
        private final Vec3 pos;

        PechsCurse(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource random = level.getRandom();
            spawn(level, TTParticles.PECH_CURSE.get(), pos.x, pos.y, pos.z);
            int age = PECH_AGE_BASE + random.nextInt(PECH_AGE_RANGE);
            spawn(level, new WispyMoteParticleOptions(EffectColor.randomMote(random).argb(), age, PECH_GRAVITY, NO_ENTITY), pos.x, pos.y, pos.z);
        }
    }

    public static final class BlockMist {
        private final ServerLevel level;
        private final BlockPos pos;
        private int color = WHITE_RGB;

        BlockMist(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public BlockMist color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            BlockEffects.mist(level, pos, color);
        }
    }

    public static final class BlockMistFlat {
        private final ServerLevel level;
        private final BlockPos pos;
        private int color = WHITE_RGB;

        BlockMistFlat(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public BlockMistFlat color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            BlockEffects.flatMist(level, pos, color);
        }
    }

    public static final class BlockSparkles {
        private final ServerLevel level;
        private final BlockPos pos;
        private Vec3 source;

        BlockSparkles(ServerLevel level, BlockPos pos) {
            this(level, pos, Vec3.atCenterOf(pos));
        }

        private BlockSparkles(ServerLevel level, BlockPos pos, Vec3 source) {
            this.level = level;
            this.pos = pos;
            this.source = source;
        }

        public BlockSparkles from(Vec3 source) {
            this.source = source;
            return this;
        }

        public void send() {
            BlockEffects.sparkles(level, pos, source);
        }
    }

    public static final class JarSplash {
        private final ServerLevel level;
        private final Vec3 pos;

        JarSplash(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            BubbleEffects.jarSplash(level, pos);
        }
    }

    public static final class EssentiaDrop {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = EffectColor.WHITE;
        private float alpha = 1.0F;

        EssentiaDrop(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public EssentiaDrop color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public EssentiaDrop alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public void send() {
            BubbleEffects.essentiaDrop(level, pos, color, alpha);
        }
    }

    public static final class FluxFume {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = FLUX_FUME_COLOR;
        private float scale = FLUX_FUME_SCALE;
        private int maxAge = FLUX_FUME_MAX_AGE;

        FluxFume(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FluxFume color(int rgb) {
            this.color = EffectColor.ofRgb(rgb);
            return this;
        }

        public FluxFume color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public FluxFume scale(float scale) {
            this.scale = scale;
            return this;
        }

        public FluxFume maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public void send() {
            BubbleEffects.fluxFume(level, pos, color, scale, maxAge);
        }
    }

    public static final class CauldronFoamDown {
        private final ServerLevel level;
        private final Vec3 pos;

        CauldronFoamDown(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            BubbleEffects.frothDown(level, pos);
        }
    }

    public static final class CauldronFoamUp {
        private final ServerLevel level;
        private final Vec3 pos;

        CauldronFoamUp(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            BubbleEffects.froth(level, pos);
        }
    }

    public static final class CauldronBoil {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = EffectColor.WHITE;
        private int heat = 1;

        CauldronBoil(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CauldronBoil color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public CauldronBoil heat(int heat) {
            this.heat = heat;
            return this;
        }

        public void send() {
            BubbleEffects.boil(level, pos, color, heat);
        }
    }

    public static final class CauldronBubble {
        private final ServerLevel level;
        private final Vec3 pos;
        private EffectColor color = EffectColor.WHITE;

        CauldronBubble(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CauldronBubble color(float r, float g, float b) {
            this.color = new EffectColor(r, g, b);
            return this;
        }

        public void send() {
            BubbleEffects.bubble(level, pos, color);
        }
    }
}

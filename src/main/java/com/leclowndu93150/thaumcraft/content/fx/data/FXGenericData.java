package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record FXGenericData(Motion motion, Visual visual, Behavior behavior) implements ParticleOptions {

    public record Motion(
            double vx, double vy, double vz,
            double driftX, double driftY, double driftZ,
            int maxAge,
            int delay,
            float gravity,
            double slowDown,
            double windScale,
            float randomX, float randomY, float randomZ
    ) {
        public static final MapCodec<Motion> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.DOUBLE.fieldOf("vx").forGetter(Motion::vx),
                Codec.DOUBLE.fieldOf("vy").forGetter(Motion::vy),
                Codec.DOUBLE.fieldOf("vz").forGetter(Motion::vz),
                Codec.DOUBLE.optionalFieldOf("drift_x", 0.0).forGetter(Motion::driftX),
                Codec.DOUBLE.optionalFieldOf("drift_y", 0.0).forGetter(Motion::driftY),
                Codec.DOUBLE.optionalFieldOf("drift_z", 0.0).forGetter(Motion::driftZ),
                Codec.INT.fieldOf("max_age").forGetter(Motion::maxAge),
                Codec.INT.fieldOf("delay").forGetter(Motion::delay),
                Codec.FLOAT.fieldOf("gravity").forGetter(Motion::gravity),
                Codec.DOUBLE.fieldOf("slow_down").forGetter(Motion::slowDown),
                Codec.DOUBLE.fieldOf("wind").forGetter(Motion::windScale),
                Codec.FLOAT.fieldOf("rand_x").forGetter(Motion::randomX),
                Codec.FLOAT.fieldOf("rand_y").forGetter(Motion::randomY),
                Codec.FLOAT.fieldOf("rand_z").forGetter(Motion::randomZ)
        ).apply(inst, Motion::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Motion> STREAM_CODEC = StreamCodec.of(
                (buf, m) -> {
                    buf.writeDouble(m.vx); buf.writeDouble(m.vy); buf.writeDouble(m.vz);
                    buf.writeDouble(m.driftX); buf.writeDouble(m.driftY); buf.writeDouble(m.driftZ);
                    buf.writeVarInt(m.maxAge); buf.writeVarInt(m.delay);
                    buf.writeFloat(m.gravity); buf.writeDouble(m.slowDown); buf.writeDouble(m.windScale);
                    buf.writeFloat(m.randomX); buf.writeFloat(m.randomY); buf.writeFloat(m.randomZ);
                },
                buf -> new Motion(
                        buf.readDouble(), buf.readDouble(), buf.readDouble(),
                        buf.readDouble(), buf.readDouble(), buf.readDouble(),
                        buf.readVarInt(), buf.readVarInt(),
                        buf.readFloat(), buf.readDouble(), buf.readDouble(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat()
                )
        );
    }

    public record Visual(
            float rStart, float gStart, float bStart,
            float rEnd, float gEnd, float bEnd,
            float[] alphaKeys,
            float[] scaleKeys,
            int gridSize,
            int startParticle, int numParticles, int particleInc,
            boolean loop,
            int layer,
            int[] finalFrames,
            boolean flipped
    ) {
        public static final MapCodec<Visual> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.fieldOf("r_start").forGetter(Visual::rStart),
                Codec.FLOAT.fieldOf("g_start").forGetter(Visual::gStart),
                Codec.FLOAT.fieldOf("b_start").forGetter(Visual::bStart),
                Codec.FLOAT.fieldOf("r_end").forGetter(Visual::rEnd),
                Codec.FLOAT.fieldOf("g_end").forGetter(Visual::gEnd),
                Codec.FLOAT.fieldOf("b_end").forGetter(Visual::bEnd),
                Codec.FLOAT.listOf().xmap(FXGenericData::toFloats, FXGenericData::fromFloats).fieldOf("alpha").forGetter(Visual::alphaKeys),
                Codec.FLOAT.listOf().xmap(FXGenericData::toFloats, FXGenericData::fromFloats).fieldOf("scale").forGetter(Visual::scaleKeys),
                Codec.INT.fieldOf("grid").forGetter(Visual::gridSize),
                Codec.INT.fieldOf("start").forGetter(Visual::startParticle),
                Codec.INT.fieldOf("num").forGetter(Visual::numParticles),
                Codec.INT.fieldOf("inc").forGetter(Visual::particleInc),
                Codec.BOOL.fieldOf("loop").forGetter(Visual::loop),
                Codec.INT.fieldOf("layer").forGetter(Visual::layer),
                Codec.INT.listOf().xmap(FXGenericData::toInts, FXGenericData::fromInts).fieldOf("final_frames").forGetter(Visual::finalFrames),
                Codec.BOOL.fieldOf("flipped").forGetter(Visual::flipped)
        ).apply(inst, Visual::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Visual> STREAM_CODEC = StreamCodec.of(
                (buf, v) -> {
                    buf.writeFloat(v.rStart); buf.writeFloat(v.gStart); buf.writeFloat(v.bStart);
                    buf.writeFloat(v.rEnd); buf.writeFloat(v.gEnd); buf.writeFloat(v.bEnd);
                    writeFloats(buf, v.alphaKeys);
                    writeFloats(buf, v.scaleKeys);
                    buf.writeVarInt(v.gridSize);
                    buf.writeVarInt(v.startParticle); buf.writeVarInt(v.numParticles); buf.writeVarInt(v.particleInc);
                    buf.writeBoolean(v.loop);
                    buf.writeVarInt(v.layer);
                    writeInts(buf, v.finalFrames);
                    buf.writeBoolean(v.flipped);
                },
                buf -> new Visual(
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        readFloats(buf), readFloats(buf),
                        buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readBoolean(),
                        buf.readVarInt(),
                        readInts(buf),
                        buf.readBoolean()
                )
        );
    }

    public record Behavior(
            float rotationStart, float rotationSpeed,
            boolean angled,
            float angleYaw, float anglePitch,
            int targetEntityId
    ) {
        public static final int NO_ENTITY = -1;

        public static final MapCodec<Behavior> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.fieldOf("rot_start").forGetter(Behavior::rotationStart),
                Codec.FLOAT.fieldOf("rot_speed").forGetter(Behavior::rotationSpeed),
                Codec.BOOL.fieldOf("angled").forGetter(Behavior::angled),
                Codec.FLOAT.fieldOf("yaw").forGetter(Behavior::angleYaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(Behavior::anglePitch),
                Codec.INT.fieldOf("target_entity").forGetter(Behavior::targetEntityId)
        ).apply(inst, Behavior::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Behavior> STREAM_CODEC = StreamCodec.of(
                (buf, b) -> {
                    buf.writeFloat(b.rotationStart); buf.writeFloat(b.rotationSpeed);
                    buf.writeBoolean(b.angled);
                    buf.writeFloat(b.angleYaw); buf.writeFloat(b.anglePitch);
                    buf.writeVarInt(b.targetEntityId);
                },
                buf -> new Behavior(
                        buf.readFloat(), buf.readFloat(),
                        buf.readBoolean(),
                        buf.readFloat(), buf.readFloat(),
                        buf.readVarInt()
                )
        );
    }

    public static final MapCodec<FXGenericData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Motion.CODEC.forGetter(FXGenericData::motion),
            Visual.CODEC.forGetter(FXGenericData::visual),
            Behavior.CODEC.forGetter(FXGenericData::behavior)
    ).apply(inst, FXGenericData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FXGenericData> STREAM_CODEC = StreamCodec.composite(
            Motion.STREAM_CODEC, FXGenericData::motion,
            Visual.STREAM_CODEC, FXGenericData::visual,
            Behavior.STREAM_CODEC, FXGenericData::behavior,
            FXGenericData::new
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FX_GENERIC.get();
    }

    public double vx() { return motion.vx; }
    public double vy() { return motion.vy; }
    public double vz() { return motion.vz; }
    public double driftX() { return motion.driftX; }
    public double driftY() { return motion.driftY; }
    public double driftZ() { return motion.driftZ; }
    public int maxAge() { return motion.maxAge; }
    public int delay() { return motion.delay; }
    public float gravity() { return motion.gravity; }
    public double slowDown() { return motion.slowDown; }
    public double windScale() { return motion.windScale; }
    public float randomX() { return motion.randomX; }
    public float randomY() { return motion.randomY; }
    public float randomZ() { return motion.randomZ; }
    public float rStart() { return visual.rStart; }
    public float gStart() { return visual.gStart; }
    public float bStart() { return visual.bStart; }
    public float rEnd() { return visual.rEnd; }
    public float gEnd() { return visual.gEnd; }
    public float bEnd() { return visual.bEnd; }
    public float[] alphaKeys() { return visual.alphaKeys; }
    public float[] scaleKeys() { return visual.scaleKeys; }
    public int gridSize() { return visual.gridSize; }
    public int startParticle() { return visual.startParticle; }
    public int numParticles() { return visual.numParticles; }
    public int particleInc() { return visual.particleInc; }
    public boolean loop() { return visual.loop; }
    public int layer() { return visual.layer; }
    public int[] finalFrames() { return visual.finalFrames; }
    public boolean flipped() { return visual.flipped; }
    public float rotationStart() { return behavior.rotationStart; }
    public float rotationSpeed() { return behavior.rotationSpeed; }
    public boolean angled() { return behavior.angled; }
    public float angleYaw() { return behavior.angleYaw; }
    public float anglePitch() { return behavior.anglePitch; }
    public int targetEntityId() { return behavior.targetEntityId; }

    static float[] toFloats(java.util.List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    static java.util.List<Float> fromFloats(float[] arr) {
        java.util.List<Float> out = new java.util.ArrayList<>(arr.length);
        for (float f : arr) out.add(f);
        return out;
    }

    static int[] toInts(java.util.List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    static java.util.List<Integer> fromInts(int[] arr) {
        java.util.List<Integer> out = new java.util.ArrayList<>(arr.length);
        for (int v : arr) out.add(v);
        return out;
    }

    static void writeFloats(RegistryFriendlyByteBuf buf, float[] arr) {
        buf.writeVarInt(arr.length);
        for (float v : arr) buf.writeFloat(v);
    }

    static float[] readFloats(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        float[] arr = new float[n];
        for (int i = 0; i < n; i++) arr[i] = buf.readFloat();
        return arr;
    }

    static void writeInts(RegistryFriendlyByteBuf buf, int[] arr) {
        buf.writeVarInt(arr.length);
        for (int v : arr) buf.writeVarInt(v);
    }

    static int[] readInts(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = buf.readVarInt();
        return arr;
    }

    public static final class Builder {
        private double vx, vy, vz;
        private double driftX, driftY, driftZ;
        private int maxAge = 20;
        private int delay = 0;
        private float rStart = 1.0F, gStart = 1.0F, bStart = 1.0F;
        private float rEnd = 1.0F, gEnd = 1.0F, bEnd = 1.0F;
        private float[] alpha = new float[]{1.0F};
        private float[] scale = new float[]{1.0F};
        private int gridSize = 64;
        private int startParticle = 0, numParticles = 1, particleInc = 1;
        private boolean loop = false;
        private int layer = 0;
        private float gravity = 0.0F;
        private double slowDown = 0.98;
        private double windScale = 0.0;
        private float rotationStart = 0.0F, rotationSpeed = 0.0F;
        private float randomX = 0.0F, randomY = 0.0F, randomZ = 0.0F;
        private int[] finalFrames = new int[0];
        private boolean flipped = false, angled = false;
        private float angleYaw = 0.0F, anglePitch = 0.0F;
        private int targetEntityId = Behavior.NO_ENTITY;

        public Builder motion(double vx, double vy, double vz) { this.vx = vx; this.vy = vy; this.vz = vz; return this; }
        public Builder drift(double dx, double dy, double dz) { this.driftX = dx; this.driftY = dy; this.driftZ = dz; return this; }
        public Builder maxAge(int maxAge) { this.maxAge = maxAge; return this; }
        public Builder delay(int delay) { this.delay = delay; return this; }
        public Builder color(float r, float g, float b) { this.rStart = r; this.gStart = g; this.bStart = b; this.rEnd = r; this.gEnd = g; this.bEnd = b; return this; }
        public Builder color(float rs, float gs, float bs, float re, float ge, float be) { this.rStart = rs; this.gStart = gs; this.bStart = bs; this.rEnd = re; this.gEnd = ge; this.bEnd = be; return this; }
        public Builder alpha(float... keys) { this.alpha = keys; return this; }
        public Builder scale(float... keys) { this.scale = keys; return this; }
        public Builder grid(int gridSize) { this.gridSize = gridSize; return this; }
        public Builder particles(int start, int num, int inc) { this.startParticle = start; this.numParticles = num; this.particleInc = inc; return this; }
        public Builder particle(int start) { this.startParticle = start; this.numParticles = 1; this.particleInc = 1; return this; }
        public Builder loop(boolean loop) { this.loop = loop; return this; }
        public Builder layer(int layer) { this.layer = layer; return this; }
        public Builder gravity(float gravity) { this.gravity = gravity; return this; }
        public Builder slowDown(double slowDown) { this.slowDown = slowDown; return this; }
        public Builder wind(double windScale) { this.windScale = windScale; return this; }
        public Builder rotation(float start, float speed) { this.rotationStart = start; this.rotationSpeed = speed; return this; }
        public Builder rotation(float speed) { this.rotationSpeed = speed; return this; }
        public Builder random(float rx, float ry, float rz) { this.randomX = rx; this.randomY = ry; this.randomZ = rz; return this; }
        public Builder finalFrames(int... frames) { this.finalFrames = frames; return this; }
        public Builder flipped(boolean flipped) { this.flipped = flipped; return this; }
        public Builder angles(float yaw, float pitch) { this.angleYaw = yaw; this.anglePitch = pitch; this.angled = true; return this; }
        public Builder targetEntity(int id) { this.targetEntityId = id; return this; }

        public FXGenericData build() {
            Motion m = new Motion(vx, vy, vz, driftX, driftY, driftZ, maxAge, delay, gravity, slowDown, windScale, randomX, randomY, randomZ);
            Visual v = new Visual(rStart, gStart, bStart, rEnd, gEnd, bEnd, alpha, scale, gridSize,
                    startParticle, numParticles, particleInc, loop, layer, finalFrames, flipped);
            Behavior b = new Behavior(rotationStart, rotationSpeed, angled, angleYaw, anglePitch, targetEntityId);
            return new FXGenericData(m, v, b);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

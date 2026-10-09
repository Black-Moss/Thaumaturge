package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaPort extends BlockEntity implements IEssentiaTransport {
    private static final int SOURCE_RANGE = 16;
    private static final int WORK_INTERVAL = 5;
    private static final int INTAKE_SUCTION = 128;
    private static final int VISUAL_EXTENSION = 5;
    private static final int SINGLE_POINT = 1;

    private final boolean input;
    private final EssentiaSources sources;
    private int ticks;

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state) {
        this(pos, state, state.getBlock() instanceof BlockEssentiaPort port && port.isInput());
    }

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state, boolean input) {
        super(TTBlockEntities.ESSENTIA_PORT.get(), pos, state);
        this.input = input;
        this.sources = new EssentiaSources(pos, SOURCE_RANGE).facing(state.getValue(BlockStateProperties.FACING)).drainEffectTarget(Vec3.atCenterOf(pos));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaPort port) {
        if (!(level instanceof ServerLevel server) || ++port.ticks % WORK_INTERVAL != 0) {
            return;
        }
        Direction tubeSide = port.tubeSide();
        IEssentiaTransport tube = EssentiaFlowHandler.transport(level, pos.relative(tubeSide), tubeSide.getOpposite());
        if (tube == null) {
            return;
        }
        if (port.input) {
            port.pushIntoSources(server, tube, tubeSide.getOpposite());
        } else {
            port.pullFromSources(server, tube, tubeSide.getOpposite());
        }
    }

    private void pushIntoSources(ServerLevel server, IEssentiaTransport tube, Direction touching) {
        if (!tube.canOutputTo(touching) || tube.getSuctionAmount(touching) >= INTAKE_SUCTION || INTAKE_SUCTION < tube.getMinimumSuction()) {
            return;
        }
        Holder<IAspect> held = tube.getEssentiaType(touching);
        if (held == null || tube.takeEssentia(held, SINGLE_POINT, touching, true) < SINGLE_POINT) {
            return;
        }
        if (sources.insert(server, held, VISUAL_EXTENSION)) {
            tube.takeEssentia(held, SINGLE_POINT, touching);
        }
    }

    private void pullFromSources(ServerLevel server, IEssentiaTransport tube, Direction touching) {
        Holder<IAspect> wanted = tube.getSuctionType(touching);
        if (!tube.canInputFrom(touching) || wanted == null || tube.getSuctionAmount(touching) <= 0 || !sources.drain(server, wanted, VISUAL_EXTENSION)) {
            return;
        }
        if (tube.addEssentia(wanted, SINGLE_POINT, touching) < SINGLE_POINT) {
            sources.insert(server, wanted, VISUAL_EXTENSION);
        }
    }

    private Direction tubeSide() {
        Direction mount = getBlockState().getValue(BlockStateProperties.FACING);
        return mount.getOpposite();
    }

    private boolean touchesTube(@Nullable Direction face) {
        return face != null && face == tubeSide();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return touchesTube(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return touchesTube(face) && input;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return touchesTube(face) && !input;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (input && touchesTube(face)) {
            return INTAKE_SUCTION;
        }
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (amount != SINGLE_POINT || !input || !touchesTube(face)) {
            return 0;
        }
        if (level instanceof ServerLevel server && sources.insert(server, aspect, VISUAL_EXTENSION)) {
            return SINGLE_POINT;
        }
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}

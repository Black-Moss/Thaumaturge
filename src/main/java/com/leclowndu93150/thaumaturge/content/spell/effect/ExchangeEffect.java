package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.spell.casting.CasterHands;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ExchangeEffect extends AbstractEffectBehavior implements IFocusBlockPicker {
    private static final float DEFAULT_VIS_COST = 0.25F;
    private static final float DEFAULT_SILK_VIS = 0.25F;
    private static final float DEFAULT_FORTUNE_VIS = 0.1F;
    private static final int SWAP_TINT = 0x7AA721;
    private static final int NO_SPREAD = 0;

    public static final MapCodec<ExchangeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("vis_cost", DEFAULT_VIS_COST).forGetter(ExchangeEffect::visCost),
            Codec.FLOAT.optionalFieldOf("silk_vis", DEFAULT_SILK_VIS).forGetter(ExchangeEffect::silkVis),
            Codec.FLOAT.optionalFieldOf("fortune_vis", DEFAULT_FORTUNE_VIS).forGetter(ExchangeEffect::fortuneVis)).apply(i, ExchangeEffect::new));

    private final float visCost;
    private final float silkVis;
    private final float fortuneVis;

    public ExchangeEffect(float visCost, float silkVis, float fortuneVis) {
        this.visCost = visCost;
        this.silkVis = silkVis;
        this.fortuneVis = fortuneVis;
    }

    public float visCost() {
        return visCost;
    }

    public float silkVis() {
        return silkVis;
    }

    public float fortuneVis() {
        return fortuneVis;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.EXCHANGE.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (target.block().isEmpty() || !(ctx.caster() instanceof Player player)) {
            return;
        }
        ItemStack held = CasterHands.held(player);
        BlockState picked = held.getItem() instanceof ICaster caster ? caster.getPickedBlock(held) : null;
        if (picked == null || picked.isAir()) {
            return;
        }
        ServerLevel level = ctx.level();
        BlockPos pos = target.block().get().getBlockPos();
        SpellState stats = ctx.state();
        boolean silk = stats.has(SpellStats.SILK_TOUCH);
        int fortune = Math.round(stats.get(SpellStats.FORTUNE));
        float cost = visCost + (silk ? silkVis : 0.0F) + fortune * fortuneVis;
        ctx.fx().impact(ctx.part().fx(), Vec3.atCenterOf(pos), ctx.color());
        BlockBreakerEngine.swapper(pos, null, picked, player).consumeTarget().lifespan(NO_SPREAD).pickupDrops().silkTouch(silk).fortune(fortune).showFx(SWAP_TINT, false)
                .visCost(cost, TTAspects.PERMUTATIO).queue(level);
    }
}

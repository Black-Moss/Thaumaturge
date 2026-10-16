package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class PechAttacks {
    private static final float POISON_CHANCE = 0.2F;
    private static final int POISON_TICKS = 40;
    private static final double ARROW_AIM_HEIGHT = 1.0 / 3.0;
    private static final double ARROW_LIFT_PER_BLOCK = 0.2;
    private static final float ARROW_SPEED = 1.6F;
    private static final int BASE_SPREAD = 14;
    private static final int SPREAD_PER_DIFFICULTY = 4;
    private static final float SHOT_VOLUME = 1.0F;
    private static final float SHOT_PITCH_MIN = 0.8F;
    private static final float SHOT_PITCH_RANGE = 0.4F;

    private static final double SPELL_LIFT_DIVISOR = 6.0;
    private static final String SPEED_SETTING = "speed";
    private static final int SPELL_SPEED = 2;
    private static final float SPELL_POWER = 1.0F;

    private static final List<SpellChoice> SPELL_TABLE = List.of(new SpellChoice(TTSpellParts.CURSE, 8), new SpellChoice(TTSpellParts.FLUX, 4), new SpellChoice(TTSpellParts.EARTH, 2),
            new SpellChoice(TTSpellParts.AIR, 1), new SpellChoice(TTSpellParts.FIRE, 1));
    private static final int SPELL_TABLE_TOTAL = SPELL_TABLE.stream().mapToInt(SpellChoice::weight).sum();

    private PechAttacks() {}

    static void shootArrow(EntityPech pech, LivingEntity target, float power) {
        if (!(pech.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = pech.getRandom();
        Arrow arrow = new Arrow(level, pech, random.nextFloat() < POISON_CHANCE ? poisonedArrowStack() : new ItemStack(Items.ARROW), heldBow(pech));
        arrow.setBaseDamageFromMob(power);
        double dx = target.getX() - pech.getX();
        double dz = target.getZ() - pech.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getY(ARROW_AIM_HEIGHT) - arrow.getY() + flat * ARROW_LIFT_PER_BLOCK;
        float spread = BASE_SPREAD - level.getDifficulty().getId() * SPREAD_PER_DIFFICULTY;
        Projectile.spawnProjectileUsingShoot(arrow, level, arrow.getPickupItemStackOrigin(), dx, dy, dz, ARROW_SPEED, spread);
        float pitch = 1.0F / (random.nextFloat() * SHOT_PITCH_RANGE + SHOT_PITCH_MIN);
        if (!pech.isSilent()) {
            level.playSound(null, pech.getX(), pech.getY(), pech.getZ(), SoundEvents.ARROW_SHOOT, pech.getSoundSource(), SHOT_VOLUME, pitch);
        }
    }

    private static ItemStack poisonedArrowStack() {
        ItemStack stack = new ItemStack(Items.TIPPED_ARROW);
        List<MobEffectInstance> effects = List.of(new MobEffectInstance(MobEffects.POISON, POISON_TICKS));
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.empty()));
        return stack;
    }

    static void castSpell(EntityPech pech, LivingEntity target) {
        if (!(pech.level() instanceof ServerLevel)) {
            return;
        }
        Vec3 centre = target.getBoundingBox().getCenter();
        Vec3 aim = centre.add(0.0, pech.distanceTo(target) / SPELL_LIFT_DIVISOR, 0.0);
        Spell spell = spellFor(pickEffect(pech.getRandom()));
        Spells.cast(pech, ItemStack.EMPTY, spell, List.of(SpellTarget.originTowards(pech, aim)), SPELL_POWER);
        pech.swing(InteractionHand.MAIN_HAND);
    }

    private static @Nullable ItemStack heldBow(EntityPech pech) {
        ItemStack held = pech.getMainHandItem();
        return held.is(Items.BOW) ? held : null;
    }

    private static Spell spellFor(ResourceKey<SpellPart> effect) {
        SpellNode carrier = SpellNode.of(TTSpellParts.PROJECTILE).withSetting(SPEED_SETTING, SPELL_SPEED).then(SpellNode.of(effect));
        return new Spell(CastStyle.INSTANT, SpellNode.of(Spell.ORIGIN).then(carrier));
    }

    private static ResourceKey<SpellPart> pickEffect(RandomSource random) {
        int roll = random.nextInt(SPELL_TABLE_TOTAL);
        for (SpellChoice choice : SPELL_TABLE) {
            roll -= choice.weight();
            if (roll < 0) {
                return choice.part();
            }
        }
        return SPELL_TABLE.getFirst().part();
    }

    private record SpellChoice(ResourceKey<SpellPart> part, int weight) {
    }
}

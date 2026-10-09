package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.List;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;

final class PechAttacks {
    private static final float POISON_ARROW_CHANCE = 0.2F;
    private static final int POISON_DURATION = 40;
    private static final int POISON_AMPLIFIER = 0;
    private static final float ARROW_SPEED = 1.6F;
    private static final int ARROW_INACCURACY_BASE = 14;
    private static final int ARROW_INACCURACY_STEP = 4;
    private static final double ARROW_HEIGHT_FRACTION = 1.0 / 3.0;
    private static final double ARROW_LOB = 0.2;
    private static final double ARROW_DAMAGE_FACTOR = 2.0;
    private static final double ARROW_DAMAGE_NOISE = 0.25;
    private static final double ARROW_DAMAGE_PER_DIFFICULTY = 0.11;
    private static final float SHOOT_VOLUME = 1.0F;
    private static final float SHOOT_PITCH_BASE = 0.8F;
    private static final float SHOOT_PITCH_SPREAD = 0.4F;
    private static final int PROJECTILE_SPEED = 2;
    private static final String SPEED_SETTING = "speed";
    private static final double SPELL_LIFT_DIVISOR = 6.0;
    private static final float SPELL_POWER = 1.0F;

    private static final Spell CURSE_SPELL = projectileSpell(TTSpellParts.CURSE);
    private static final Spell FLUX_SPELL = projectileSpell(TTSpellParts.FLUX);
    private static final Spell EARTH_SPELL = projectileSpell(TTSpellParts.EARTH);
    private static final Spell AIR_SPELL = projectileSpell(TTSpellParts.AIR);
    private static final Spell FIRE_SPELL = projectileSpell(TTSpellParts.FIRE);

    private PechAttacks() {}

    private static Spell projectileSpell(ResourceKey<SpellPart> effect) {
        SpellNode carrier = SpellNode.of(TTSpellParts.PROJECTILE).withSetting(SPEED_SETTING, PROJECTILE_SPEED).then(SpellNode.of(effect));
        return new Spell(CastStyle.INSTANT, SpellNode.of(Spell.ORIGIN).then(carrier));
    }

    static void shootArrow(EntityPech pech, LivingEntity target, float power) {
        if (!(pech.level() instanceof ServerLevel server)) {
            return;
        }
        RandomSource random = pech.getRandom();
        ItemStack ammo = new ItemStack(Items.ARROW);
        if (random.nextFloat() < POISON_ARROW_CHANCE) {
            ammo = new ItemStack(Items.TIPPED_ARROW);
            ammo.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, POISON_AMPLIFIER)));
        }
        int difficulty = server.getDifficulty().getId();
        AbstractArrow arrow = ProjectileUtil.getMobArrow(pech, ammo, power, null);
        arrow.setBaseDamage(power * ARROW_DAMAGE_FACTOR + random.nextGaussian() * ARROW_DAMAGE_NOISE + difficulty * ARROW_DAMAGE_PER_DIFFICULTY);
        double dx = target.getX() - pech.getX();
        double dz = target.getZ() - pech.getZ();
        double dy = target.getY(ARROW_HEIGHT_FRACTION) - arrow.getY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(arrow, server, ammo, dx, dy + horizontal * ARROW_LOB, dz, ARROW_SPEED, ARROW_INACCURACY_BASE - difficulty * ARROW_INACCURACY_STEP);
        pech.playSound(SoundEvents.ARROW_SHOOT, SHOOT_VOLUME, 1.0F / (random.nextFloat() * SHOOT_PITCH_SPREAD + SHOOT_PITCH_BASE));
    }

    static void castSpell(EntityPech pech, LivingEntity target) {
        RandomSource random = pech.getRandom();
        Spell spell;
        if (random.nextBoolean()) {
            spell = CURSE_SPELL;
        } else if (random.nextBoolean()) {
            spell = FLUX_SPELL;
        } else if (random.nextBoolean()) {
            spell = EARTH_SPELL;
        } else if (random.nextBoolean()) {
            spell = AIR_SPELL;
        } else {
            spell = FIRE_SPELL;
        }
        Vec3 aim = target.getBoundingBox().getCenter().add(0.0, pech.distanceTo(target) / SPELL_LIFT_DIVISOR, 0.0);
        Spells.cast(pech, ItemStack.EMPTY, spell, List.of(SpellTarget.originTowards(pech, aim)), SPELL_POWER);
        pech.swing(InteractionHand.MAIN_HAND);
    }
}

package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class GolemStatSheet {
    static final int HOME_RADIUS = 32;
    static final int SCOUT_HOME_RADIUS = 48;

    private static final double BASE_SPEED = 0.3D;
    private static final double BASE_HEALTH = 10.0D;
    private static final double BASE_ARMOR = 2.0D;
    private static final double BASE_FOLLOW_RANGE = 40.0D;
    private static final double SCOUT_FOLLOW_RANGE = 56.0D;
    private static final double DEFAULT_STEP_HEIGHT = 0.6D;
    private static final double WHEELED_STEP_HEIGHT = 0.5D;

    private GolemStatSheet() {}

    static AttributeSupplier.Builder baseAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, BASE_SPEED).add(Attributes.MAX_HEALTH, BASE_HEALTH).add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE).add(Attributes.ARMOR, BASE_ARMOR).add(Attributes.STEP_HEIGHT, DEFAULT_STEP_HEIGHT);
    }

    static void apply(EntityThaumaturgeGolem golem) {
        GolemProperties props = golem.properties();
        List<GolemAccessory> accessories = golem.getAccessories();
        double health = GolemStats.health(props) + props.rank();
        double armor = GolemStats.armor(props);
        for (GolemAccessory accessory : accessories) {
            health += accessory.healthBonus();
            armor += accessory.armorBonus();
        }
        setBase(golem, Attributes.MAX_HEALTH, health);
        if (golem.getHealth() > golem.getMaxHealth()) {
            golem.setHealth(golem.getMaxHealth());
        }
        setBase(golem, Attributes.ARMOR, armor);
        setBase(golem, Attributes.ATTACK_DAMAGE, GolemStats.meleeDamage(props));
        setBase(golem, Attributes.STEP_HEIGHT, golem.hasTrait(TTGolemTraits.WHEELED) ? WHEELED_STEP_HEIGHT : DEFAULT_STEP_HEIGHT);
        setBase(golem, Attributes.MOVEMENT_SPEED, BASE_SPEED * speedFactor(accessories));
        setBase(golem, Attributes.FOLLOW_RANGE, (golem.hasTrait(TTGolemTraits.SCOUT) ? SCOUT_FOLLOW_RANGE : BASE_FOLLOW_RANGE) * rangeFactor(accessories));
    }

    static int defaultHomeRadius(EntityThaumaturgeGolem golem) {
        return golem.hasTrait(TTGolemTraits.SCOUT) ? SCOUT_HOME_RADIUS : HOME_RADIUS;
    }

    static int homeRadius(EntityThaumaturgeGolem golem) {
        return (int) (defaultHomeRadius(golem) * rangeFactor(golem.getAccessories()));
    }

    static double speedFactor(List<GolemAccessory> accessories) {
        double factor = 1.0D;
        for (GolemAccessory accessory : accessories) {
            factor *= accessory.speedFactor();
        }
        return factor;
    }

    static double rangeFactor(List<GolemAccessory> accessories) {
        double factor = 1.0D;
        for (GolemAccessory accessory : accessories) {
            factor *= accessory.rangeFactor();
        }
        return factor;
    }

    static double regenFactor(List<GolemAccessory> accessories) {
        double factor = 1.0D;
        for (GolemAccessory accessory : accessories) {
            factor *= accessory.regenFactor();
        }
        return factor;
    }

    private static void setBase(EntityThaumaturgeGolem golem, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = golem.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}

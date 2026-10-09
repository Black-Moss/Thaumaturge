package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;

public class EntityCultistKnight extends EntityCultist {
    private static final double MAX_HEALTH = 30.0;
    private static final double MELEE_SPEED = 1.0;
    private static final int MELEE_PRIORITY = 2;

    public EntityCultistKnight(EntityType<? extends EntityCultistKnight> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes(MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, false));
        this.addIdleGoals();
        this.addTargetGoals();
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_KNIGHT;
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        this.enchantSpawnedWeapon(level, random, difficulty);
    }
}

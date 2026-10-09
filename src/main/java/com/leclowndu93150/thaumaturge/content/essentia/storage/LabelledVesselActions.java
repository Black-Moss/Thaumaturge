package com.leclowndu93150.thaumaturge.content.essentia.storage;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class LabelledVesselActions {
    private static final float POUR_JAR_VOLUME = 0.4F;
    private static final float POUR_FILL_VOLUME = 0.5F;
    private static final float POUR_FILL_SPREAD = 0.3F;
    private static final float LABEL_VOLUME = 1.0F;
    private static final float NEUTRAL_PITCH = 1.0F;

    private LabelledVesselActions() {}

    public static void pourOut(Level level, BlockPos pos, int amount) {
        RandomSource random = level.getRandom();
        level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, POUR_JAR_VOLUME, NEUTRAL_PITCH);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, POUR_FILL_VOLUME, NEUTRAL_PITCH + (random.nextFloat() - random.nextFloat()) * POUR_FILL_SPREAD);
        AuraHelper.polluteAura(level, pos, amount, true);
    }

    public static void removeLabel(Level level, BlockPos pos, Direction clickedFace) {
        playLabelSound(level, pos);
        Block.popResourceFromFace(level, pos, clickedFace, new ItemStack(TTItems.LABEL.get()));
    }

    public static void playLabelSound(Level level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.PAGE.get(), SoundSource.BLOCKS, LABEL_VOLUME, NEUTRAL_PITCH);
    }

    public static @Nullable ResourceKey<IAspect> aspectToLabel(ItemStack labelStack, Direction face, @Nullable ResourceKey<IAspect> existingFilter, @Nullable ResourceKey<IAspect> heldAspect, int amount) {
        if (!(labelStack.getItem() instanceof ILabel label) || face.getAxis().isVertical() || existingFilter != null) {
            return null;
        }
        ResourceKey<IAspect> current = amount > 0 ? heldAspect : null;
        ResourceKey<IAspect> written = label.getFilteredAspect(labelStack);
        if (written != null && current != null && !written.equals(current)) {
            return null;
        }
        return current != null ? current : written;
    }
}

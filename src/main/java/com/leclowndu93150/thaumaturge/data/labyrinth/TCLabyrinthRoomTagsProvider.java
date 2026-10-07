package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.TagKey;

public final class TCLabyrinthRoomTagsProvider extends KeyTagProvider<RoomType> {
    public TCLabyrinthRoomTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, RoomType.REGISTRY_KEY, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        for (RoomRecipe recipe : RoomCatalogData.recipes()) {
            for (TagKey<RoomType> key : recipe.tags()) {
                tag(key).add(recipe.key());
            }
        }
    }
}

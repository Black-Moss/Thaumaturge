package com.leclowndu93150.thaumcraft.client.model.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

@OnlyIn(Dist.CLIENT)
public final class TCObjUnbakedModel extends AbstractUnbakedModel {
    private final TCObjGeometry geometry;

    public TCObjUnbakedModel(StandardModelParameters parameters, Identifier model, boolean flipV) {
        super(parameters);
        this.geometry = new TCObjGeometry(model, flipV);
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    public static final class Loader implements UnbakedModelLoader<TCObjUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public TCObjUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
            StandardModelParameters parameters = StandardModelParameters.parse(jsonObject, context);
            Identifier model = Identifier.parse(GsonHelper.getAsString(jsonObject, "model"));
            boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
            return new TCObjUnbakedModel(parameters, model, flipV);
        }
    }
}

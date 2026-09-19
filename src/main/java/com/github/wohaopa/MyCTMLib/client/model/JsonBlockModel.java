package com.github.wohaopa.MyCTMLib.client.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonParseException;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement.Face;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuad;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing;

/** Reuses the item baker's geometry and UV handling while retaining block culling and orientations. */
public class JsonBlockModel extends JsonItemModel {

    public JsonBlockModel(JSONModel model) {
        super(model);
        // GTNHLib does not flatten aliases on models without parents.
        for (String key : textures.keySet()) {
            Set<String> visited = new HashSet<>();
            String texture = textures.get(key);
            while (texture.startsWith("#")) {
                if (!visited.add(texture) || !textures.containsKey(texture)) {
                    throw new JsonParseException("Unresolved or cyclic model texture: " + key);
                }
                texture = textures.get(texture);
            }
            textures.put(key, texture);
        }
    }

    @Override
    protected void bakeItemSprite(ModelQuad quad, String texture) {
        // Keep shared block-face UVs continuous instead of shrinking each face independently.
        super.bakeSprite(quad, texture);
    }

    public BakedBlock bakeBlock() {
        BakedItem item = bakeItem();
        ArrayList<ForgeDirection> cullFaces = new ArrayList<>();
        for (ModelElement element : elements) {
            for (Face face : element.faces()) cullFaces.add(face.cullFace());
        }
        Geometry[] orientations = new Geometry[36];
        for (int index = 0; index < orientations.length; index++) {
            ModelOrientation orientation = ModelOrientation.ORIENTATIONS[index];
            if (orientation == null) continue;
            ModelQuadView[] quads = new ModelQuadView[item.quads().length];
            float[] shades = new float[quads.length];
            ForgeDirection[] culls = new ForgeDirection[quads.length];
            for (int i = 0; i < quads.length; i++) {
                ModelQuadView source = item.quads()[i];
                ModelQuad quad = new ModelQuad(source);
                // GTNHLib's copy constructor omits these lighting properties.
                quad.setDirectionalShading(source.hasDirectionalShading());
                quad.setEmissiveness(source.getEmissiveness());
                for (int vertex = 0; vertex < 4; vertex++) {
                    float x = quad.getX(vertex) - 0.5F;
                    float y = quad.getY(vertex) - 0.5F;
                    float z = quad.getZ(vertex) - 0.5F;
                    quad.setX(vertex, orientation.x(x, y, z) + 0.5F);
                    quad.setY(vertex, orientation.y(x, y, z) + 0.5F);
                    quad.setZ(vertex, orientation.z(x, y, z) + 0.5F);
                }
                quad.setLightFace(
                    ModelQuadFacing.fromForgeDir(
                        orientation.rotate(
                            quad.getLightFace()
                                .toForgeDir())));
                quads[i] = quad;
                shades[i] = quad.hasDirectionalShading() ? ModelISBRH.diffuseLight(quad.getComputedFaceNormal()) : 1;
                culls[i] = orientation.rotate(cullFaces.get(i));
            }
            orientations[index] = new Geometry(quads, shades, culls);
        }
        return new BakedBlock(item, orientations);
    }

    public record BakedBlock(BakedItem item, Geometry[] orientations) {}

    public record Geometry(ModelQuadView[] quads, float[] shades, ForgeDirection[] cullFaces) {}
}

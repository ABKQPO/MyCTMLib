package com.github.wohaopa.MyCTMLib.client.model;

import static com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing.UNASSIGNED;

import java.util.ArrayList;
import java.util.EnumMap;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.baked.PileOfQuads;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement.Face;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement.Rotation;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuad;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Adapts resolved GTNHLib models to item quads, including vanilla face UV rotation.
 */
public class JsonItemModel extends JSONModel {

    private static final float UV_INSET_PIXELS = 1.0F / 32.0F;

    public JsonItemModel(JSONModel model) {
        super(model);
    }

    public BakedItem bakeItem() {
        ArrayList<ModelQuadView> quads = new ArrayList<>();
        for (ModelElement element : elements) {
            Matrix4f rotation = getElementTransform(element.rotation());
            for (Face face : element.faces()) {
                ModelQuad quad = new ModelQuad();
                Vector4f uv = face.uv() == null ? getDefaultUv(element, face) : face.uv();
                int uvRotation = Math.floorMod(face.rotation() / 90, 4);
                for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                    Vector3f vertex = mapSideToVertex(element.from(), element.to(), vertexIndex, face.name())
                        .mulPosition(rotation);
                    quad.setX(vertexIndex, vertex.x);
                    quad.setY(vertexIndex, vertex.y);
                    quad.setZ(vertexIndex, vertex.z);

                    int uvIndex = (vertexIndex + uvRotation) & 3;
                    setUV(quad, vertexIndex, uvIndex < 2 ? uv.x : uv.z, uvIndex == 0 || uvIndex == 3 ? uv.y : uv.w);
                }

                String texture = face.texture();
                if (texture.startsWith("#")) {
                    texture = textures.getOrDefault(texture, "minecraft:missing");
                }
                bakeItemSprite(quad, texture);
                quad.setColorIndex(face.tintIndex());
                quad.setDirectionalShading(element.shade());
                quad.setEmissiveness(element.lightEmission());
                quad.setHasAmbientOcclusion(useAO);
                quad.setLightFace(ModelQuadFacing.fromForgeDir(face.name()));
                quads.add(quad);
            }
        }

        // Items have no neighboring blocks, so every face is visible regardless of cullface.
        EnumMap<ModelQuadFacing, ArrayList<ModelQuadView>> faces = new EnumMap<>(ModelQuadFacing.class);
        faces.put(UNASSIGNED, quads);
        BakedModel model = new PileOfQuads(faces, display, getParticle());
        float[] shades = new float[quads.size()];
        IntArrayList tintIndices = new IntArrayList();
        for (int index = 0; index < quads.size(); index++) {
            ModelQuadView quad = quads.get(index);
            shades[index] = quad.hasDirectionalShading() ? ModelISBRH.diffuseLight(quad.getComputedFaceNormal()) : 1.0F;
            int tintIndex = quad.getColorIndex();
            if (tintIndex >= 0 && !tintIndices.contains(tintIndex)) {
                tintIndices.add(tintIndex);
            }
        }
        return new BakedItem(model, quads.toArray(new ModelQuadView[0]), shades, tintIndices.toIntArray());
    }

    public record BakedItem(BakedModel model, ModelQuadView[] quads, float[] shades, int[] tintIndices) {}

    protected void bakeItemSprite(ModelQuad quad, String texture) {
        IIcon sprite = Minecraft.getMinecraft()
            .getTextureMapBlocks()
            .getAtlasSprite(texture);
        quad.setSprite(sprite);
        float minU = Float.POSITIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY;
        float maxV = Float.NEGATIVE_INFINITY;
        for (int vertex = 0; vertex < 4; vertex++) {
            minU = Math.min(minU, quad.getTexU(vertex));
            minV = Math.min(minV, quad.getTexV(vertex));
            maxU = Math.max(maxU, quad.getTexU(vertex));
            maxV = Math.max(maxV, quad.getTexV(vertex));
        }
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = insetUv(quad.getTexU(vertex), minU, maxU, sprite.getIconWidth());
            float v = insetUv(quad.getTexV(vertex), minV, maxV, sprite.getIconHeight());
            quad.setTexU(vertex, sprite.getInterpolatedU(u));
            quad.setTexV(vertex, sprite.getInterpolatedV(v));
        }
    }

    private float insetUv(float coordinate, float min, float max, int textureSize) {
        float pixelsPerUnit = textureSize / 16.0F;
        float minPixel = min * pixelsPerUnit;
        float maxPixel = max * pixelsPerUnit;
        int firstPixel = (int) Math.floor(minPixel + 0.0001F);
        int lastPixel = (int) Math.ceil(maxPixel - 0.0001F) - 1;
        if (min == max || firstPixel == lastPixel) {
            // A one-pixel outline must never sample a transparent neighbor or disappear into mipmaps.
            return (firstPixel + 0.5F) / pixelsPerUnit;
        }
        float inset = Math.min(UV_INSET_PIXELS / pixelsPerUnit, (max - min) * 0.5F);
        return coordinate == min ? min + inset : max - inset;
    }

    private Matrix4f getElementTransform(Rotation rotation) {
        if (rotation == null) {
            return new Matrix4f();
        }
        Matrix4f transform = rotation.getAffineMatrix();
        if (rotation.rescale()) {
            float scale = 1.0F / (float) Math.cos(rotation.angle());
            Vector3f origin = rotation.origin();
            transform.translate(origin);
            switch (rotation.axis()) {
                case X -> transform.scale(1.0F, scale, scale);
                case Y -> transform.scale(scale, 1.0F, scale);
                case Z -> transform.scale(scale, scale, 1.0F);
            }
            transform.translate(-origin.x, -origin.y, -origin.z);
        }
        return transform;
    }

    private Vector4f getDefaultUv(ModelElement element, Face face) {
        Vector3f from = element.from();
        Vector3f to = element.to();
        Vector4f uv = switch (face.name()) {
            case DOWN -> new Vector4f(from.x, 1.0F - to.z, to.x, 1.0F - from.z);
            case UP -> new Vector4f(from.x, from.z, to.x, to.z);
            case NORTH -> new Vector4f(1.0F - to.x, 1.0F - to.y, 1.0F - from.x, 1.0F - from.y);
            case SOUTH -> new Vector4f(from.x, 1.0F - to.y, to.x, 1.0F - from.y);
            case WEST -> new Vector4f(from.z, 1.0F - to.y, to.z, 1.0F - from.y);
            case EAST -> new Vector4f(1.0F - to.z, 1.0F - to.y, 1.0F - from.z, 1.0F - from.y);
            default -> throw new IllegalArgumentException("An item model face must have a direction.");
        };
        return uv.mul(16.0F);
    }
}

package com.github.wohaopa.MyCTMLib.compat.ae2.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary.Snapshot;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;

public class Ae2EntityModelRenderer {

    public static boolean renderCompass(float pointerRadians) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        if (models == null) return false;
        BakedBlock base = models.devices()
            .get("meteorite_compass_base");
        BakedBlock pointer = models.devices()
            .get("meteorite_compass_pointer");
        if (base == null || pointer == null) return false;
        begin();
        try {
            // ModelCompass rests on y = 24 pixels and is centered on the origin.
            GL11.glTranslatef(-0.5F, 1.5F, 0.5F);
            GL11.glScalef(1, -1, -1);
            draw(base);
            // The two axis flips turn the model's y rotation around; both pointers start facing model +z.
            GL11.glTranslatef(0.5F, 0, 0.5F);
            GL11.glRotatef((float) -Math.toDegrees(pointerRadians), 0, 1, 0);
            GL11.glTranslatef(-0.5F, 0, -0.5F);
            draw(pointer);
        } finally {
            end();
        }
        return true;
    }

    public static boolean renderSkyChest(boolean smooth, float lidRadians) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        if (models == null) return false;
        String prefix = smooth ? "smooth_sky_chest_" : "sky_chest_";
        BakedBlock base = models.devices()
            .get(prefix + "base");
        BakedBlock lid = models.devices()
            .get(prefix + "lid");
        if (base == null || lid == null) return false;
        begin();
        try {
            // ModelChest spans y 16..0 pixels from bottom to top.
            GL11.glTranslatef(0, 1, 0);
            GL11.glScalef(1, -1, 1);
            draw(base);
            // Hinge on the back top edge; the y flip mirrors the lid's x rotation.
            GL11.glTranslatef(0, 10 / 16F, 15 / 16F);
            GL11.glRotatef((float) -Math.toDegrees(lidRadians), 1, 0, 0);
            GL11.glTranslatef(0, -10 / 16F, -15 / 16F);
            draw(lid);
        } finally {
            end();
        }
        return true;
    }

    private static void begin() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);
        // The callers mirror their matrices and pick a cull face to match ModelBase, so draw both sides.
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    private static void end() {
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void draw(BakedBlock model) {
        Tessellator tessellator = TessellatorManager.get();
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(1, 1, 1);
        for (ModelQuadView quad : model.item()
            .quads()) {
            ForgeDirection face = quad.getLightFace()
                .toForgeDir();
            tessellator.setNormal(face.offsetX, face.offsetY, face.offsetZ);
            for (int vertex = 0; vertex < 4; vertex++) {
                tessellator.addVertexWithUV(
                    quad.getX(vertex),
                    quad.getY(vertex),
                    quad.getZ(vertex),
                    quad.getTexU(vertex),
                    quad.getTexV(vertex));
            }
        }
        tessellator.draw();
    }
}

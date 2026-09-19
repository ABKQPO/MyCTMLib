package com.github.wohaopa.MyCTMLib.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel.BakedItem;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Draws an untinted item sprite over a JSON item without changing the model's tint or display pose.
 */
@SideOnly(Side.CLIENT)
public class ItemIconOverlayRenderer {

    private BakedItem cachedModel;
    private float frontZ;
    private float backZ;

    public void render(ItemRenderType type, BakedItem model, IIcon icon) {
        if (icon == null) {
            return;
        }
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(TextureMap.locationItemsTexture);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            if (type == ItemRenderType.INVENTORY) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glDisable(GL11.GL_CULL_FACE);
                drawQuad(icon, 0.0F, 16.0F, 16.0F, 0.0F, 0.0F, false);
            } else {
                updateBounds(model);
                JsonItemDisplayTransform.apply(type, model.model());
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glEnable(GL11.GL_CULL_FACE);
                // Keep the badge outside the bee on both sides while retaining normal world occlusion.
                drawQuad(icon, 0.0F, 1.0F, 0.0F, 1.0F, frontZ, false);
                drawQuad(icon, 1.0F, 0.0F, 0.0F, 1.0F, backZ, true);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void updateBounds(BakedItem model) {
        if (cachedModel == model) {
            return;
        }
        float minZ = 0.0F;
        float maxZ = 1.0F;
        for (ModelQuadView quad : model.quads()) {
            for (int vertex = 0; vertex < 4; vertex++) {
                minZ = Math.min(minZ, quad.getZ(vertex));
                maxZ = Math.max(maxZ, quad.getZ(vertex));
            }
        }
        backZ = minZ - 0.001F;
        frontZ = maxZ + 0.001F;
        cachedModel = model;
    }

    private void drawQuad(IIcon icon, float left, float right, float bottom, float top, float z, boolean back) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        tessellator.setNormal(0.0F, 0.0F, back ? -1.0F : 1.0F);
        tessellator.addVertexWithUV(left, bottom, z, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(right, bottom, z, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(right, top, z, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(left, top, z, icon.getMinU(), icon.getMinV());
        tessellator.draw();
    }
}

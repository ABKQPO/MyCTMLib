package com.github.wohaopa.MyCTMLib.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel.BakedItem;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JsonItemModelRenderer {

    private int[] tintColors = new int[0];

    public void render(ItemRenderType type, ItemStack stack, BakedItem model) {
        renderModel(stack, model, () -> JsonItemDisplayTransform.apply(type, model.model()));
    }

    public void renderGui(ItemStack stack, BakedItem model, int x, int y, float zLevel) {
        renderModel(stack, model, () -> {
            GL11.glTranslatef(x + 8.0F, y + 8.0F, zLevel);
            GL11.glScalef(16.0F, -16.0F, 16.0F);
            JsonItemDisplayTransform.applyGui(model.model());
        });
    }

    private void renderModel(ItemStack stack, BakedItem model, Runnable transform) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            transform.run();
            prepareTintColors(stack, model.tintIndices());
            tessellator.startDrawingQuads();

            ModelQuadView[] quads = model.quads();
            float[] shades = model.shades();
            for (int index = 0; index < quads.length; index++) {
                renderQuad(tessellator, quads[index], shades[index]);
            }

            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void prepareTintColors(ItemStack stack, int[] tintIndices) {
        int passes = stack.getItem()
            .getRenderPasses(stack.getItemDamage());
        if (tintColors.length != passes) {
            tintColors = new int[passes];
        }
        for (int tintIndex : tintIndices) {
            if (tintIndex < passes) {
                tintColors[tintIndex] = stack.getItem()
                    .getColorFromItemStack(stack, tintIndex);
            }
        }
    }

    private void renderQuad(Tessellator tessellator, ModelQuadView quad, float shade) {
        int tintIndex = quad.getColorIndex();
        int color = tintIndex >= 0 && tintIndex < tintColors.length ? tintColors[tintIndex] : 0xFFFFFF;
        tessellator.setColorOpaque_F(
            ((color >> 16) & 255) / 255.0F * shade,
            ((color >> 8) & 255) / 255.0F * shade,
            (color & 255) / 255.0F * shade);

        for (int index = 0; index < 4; index++) {
            tessellator.addVertexWithUV(
                quad.getX(index),
                quad.getY(index),
                quad.getZ(index),
                quad.getTexU(index),
                quad.getTexV(index));
        }
    }
}

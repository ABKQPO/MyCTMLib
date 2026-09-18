package com.github.wohaopa.MyCTMLib.client.render.item;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position.ModelDisplay;

public class JsonItemDisplayTransform {

    public static void apply(ItemRenderType type, BakedModel model) {
        Position position = switch (type) {
            case INVENTORY -> Position.GUI;
            case EQUIPPED -> Position.THIRDPERSON_RIGHTHAND;
            case EQUIPPED_FIRST_PERSON -> Position.FIRSTPERSON_RIGHTHAND;
            case ENTITY -> RenderItem.renderInFrame ? Position.FIXED : Position.GROUND;
            default -> Position.FIXED;
        };

        switch (type) {
            case INVENTORY -> {
                // Forge supplies a 16-pixel GUI origin when INVENTORY_BLOCK is disabled.
                GL11.glTranslatef(8.0F, 8.0F, 0.0F);
                GL11.glScalef(16.0F, -16.0F, 16.0F);
            }
            case ENTITY -> {
                // Forge scales non-block entity models by one half before calling the renderer.
                GL11.glScalef(2.0F, 2.0F, 2.0F);
            }
            case EQUIPPED_FIRST_PERSON -> {
                // EQUIPPED_BLOCK supplies a translation by negative one half on each axis.
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                // Cancel ItemRenderer's legacy scale and resting yaw before applying the JSON pose.
                GL11.glScalef(1.0F / 0.4F, 1.0F / 0.4F, 1.0F / 0.4F);
                GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
            }
            case EQUIPPED -> {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                // BLOCK_3D selects RenderPlayer's block pose with this scale and these rotations.
                GL11.glScalef(-1.0F / 0.375F, -1.0F / 0.375F, 1.0F / 0.375F);
                GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-20.0F, 1.0F, 0.0F, 0.0F);
                // Match the modern right-hand coordinate frame used by Blockbench.
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }
            default -> {}
        }

        ModelDisplay display = model.getDisplay(position, null);
        Vector3f translation = display.translation();
        Vector3f rotation = display.rotation();
        Vector3f scale = display.scale();
        GL11.glTranslatef(translation.x / 16.0F, translation.y / 16.0F, translation.z / 16.0F);
        GL11.glRotatef(rotation.x, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(rotation.y, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rotation.z, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(scale.x, scale.y, scale.z);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
    }
}

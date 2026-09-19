package com.github.wohaopa.MyCTMLib.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.Geometry;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;

/** Emits cached geometry into the current chunk batch without changing OpenGL state. */
public class JsonBlockModelRenderer {

    private final int[] brightness = new int[7];
    private final boolean[] visible = new boolean[7];
    private int interiorBrightness;

    public void prepare(Block block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer) {
        brightness[6] = block.getMixedBrightnessForBlock(world, x, y, z);
        visible[6] = true;
        interiorBrightness = brightness[6];
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            int nx = x + face.offsetX;
            int ny = y + face.offsetY;
            int nz = z + face.offsetZ;
            brightness[face.ordinal()] = block.getMixedBrightnessForBlock(world, nx, ny, nz);
            visible[face.ordinal()] = renderer.renderAllFaces || !world.getBlock(nx, ny, nz)
                .isOpaqueCube();
            interiorBrightness = Math.max(interiorBrightness & 0xF00000, brightness[face.ordinal()] & 0xF00000)
                | Math.max(interiorBrightness & 0xF0, brightness[face.ordinal()] & 0xF0);
        }
        if (block.getLightOpacity(world, x, y, z) < 255) interiorBrightness = brightness[6];
    }

    public void render(Geometry model, int x, int y, int z, float dx, float dy, float dz, int baseColor, int[] tints,
        boolean emissive, IIcon override) {
        Tessellator tessellator = TessellatorManager.get();
        for (int index = 0; index < model.quads().length; index++) {
            if (!visible[model.cullFaces()[index].ordinal()]) continue;
            ModelQuadView quad = model.quads()[index];
            int tint = quad.getColorIndex();
            int color = override != null ? 0xFFFFFF : tint >= 0 && tint < tints.length ? tints[tint] : baseColor;
            float shade = emissive && override == null ? 1 : model.shades()[index];
            tessellator.setColorOpaque_F(
                (color >> 16 & 255) / 255F * shade,
                (color >> 8 & 255) / 255F * shade,
                (color & 255) / 255F * shade);
            tessellator.setBrightness(light(quad, dx, dy, dz, emissive && override == null));
            for (int vertex = 0; vertex < 4; vertex++) {
                float u = quad.getTexU(vertex);
                float v = quad.getTexV(vertex);
                if (override != null) {
                    IIcon sprite = (IIcon) quad.celeritas$getSprite();
                    u = override.getInterpolatedU((u - sprite.getMinU()) / (sprite.getMaxU() - sprite.getMinU()) * 16);
                    v = override.getInterpolatedV((v - sprite.getMinV()) / (sprite.getMaxV() - sprite.getMinV()) * 16);
                }
                tessellator.addVertexWithUV(
                    x + (double) (quad.getX(vertex) + dx),
                    y + (double) (quad.getY(vertex) + dy),
                    z + (double) (quad.getZ(vertex) + dz),
                    u,
                    v);
            }
        }
    }

    private int light(ModelQuadView quad, float dx, float dy, float dz, boolean emissive) {
        if (emissive) return 0xF000F0;
        ForgeDirection face = quad.getLightFace()
            .toForgeDir();
        float average = 0;
        for (int i = 0; i < 4; i++) {
            average += face.offsetX * (quad.getX(i) + dx) + face.offsetY * (quad.getY(i) + dy)
                + face.offsetZ * (quad.getZ(i) + dz);
        }
        boolean positive = face.offsetX + face.offsetY + face.offsetZ > 0;
        int value = average >= (positive ? 4 : 0) - 0.0001F ? brightness[face.ordinal()] : interiorBrightness;
        int emission = quad.getEmissiveness();
        return Math.max(value & 0xF00000, emission << 20) | Math.max(value & 0xF0, emission << 4);
    }
}

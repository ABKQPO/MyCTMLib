package com.github.wohaopa.MyCTMLib.compat.ae2.render;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.client.model.ModelOrientation;
import com.github.wohaopa.MyCTMLib.client.render.block.JsonBlockModelRenderer;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary.Snapshot;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;

import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.tile.AEBaseTile;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.grindstone.TileCrank;
import appeng.tile.misc.TileCharger;
import appeng.tile.misc.TileInscriber;
import appeng.tile.networking.TileWireless;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;

public class Ae2WorldRenderer {

    private static final int UNROTATED = ModelOrientation.index(ForgeDirection.NORTH, ForgeDirection.UP);
    private static final ThreadLocal<Ae2WorldRenderer> RENDERERS = ThreadLocal.withInitial(Ae2WorldRenderer::new);
    private final JsonBlockModelRenderer renderer = new JsonBlockModelRenderer();
    private final int[] tints = new int[5];
    private int x;
    private int y;
    private int z;
    private int orientation;
    private IIcon override;

    public static boolean render(Block block, IBlockAccess world, int x, int y, int z, RenderBlocks renderBlocks) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        if (models == null || !(world.getTileEntity(x, y, z) instanceof AEBaseTile tile)) return false;
        ForgeDirection front = tile.getForward();
        ForgeDirection up = tile.getUp();
        if (front == ForgeDirection.UNKNOWN || up == ForgeDirection.UNKNOWN) return false;
        int orientation = ModelOrientation.index(front, up);
        if (ModelOrientation.ORIENTATIONS[orientation] == null) return false;
        Ae2WorldRenderer context = prepare(block, world, x, y, z, renderBlocks, orientation);
        try {
            return context.renderTile(models, tile);
        } finally {
            context.override = null;
        }
    }

    public static boolean renderStatic(String device, Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks) {
        return renderDevice(device, block, world, x, y, z, renderBlocks, UNROTATED, false);
    }

    public static boolean renderFacing(String device, Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks) {
        int orientation = UNROTATED;
        if (world.getTileEntity(x, y, z) instanceof AEBaseTile tile && tile.getForward() != ForgeDirection.UNKNOWN
            && tile.getUp() != ForgeDirection.UNKNOWN) {
            int facing = ModelOrientation.index(tile.getForward(), tile.getUp());
            if (ModelOrientation.ORIENTATIONS[facing] != null) orientation = facing;
        }
        return renderDevice(device, block, world, x, y, z, renderBlocks, orientation, false);
    }

    public static boolean renderMolecularAssembler(Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks, boolean lights) {
        if (!lights) return renderStatic("molecular_assembler", block, world, x, y, z, renderBlocks);
        return world.getTileEntity(x, y, z) instanceof TileMolecularAssembler assembler && assembler.isPowered()
            && renderDevice("molecular_assembler_lights", block, world, x, y, z, renderBlocks, UNROTATED, true);
    }

    public static boolean renderQuartzFixture(AEBaseBlock block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks) {
        IOrientable orientable = block.getOrientable(world, x, y, z);
        ForgeDirection up = orientable == null ? ForgeDirection.UP : orientable.getUp();
        if (up == ForgeDirection.UNKNOWN) up = ForgeDirection.UP;
        // AE2 alternates the crystal shape on neighboring blocks.
        String odd = ((x + y + z) & 1) == 0 ? "" : "_odd";
        if (up.offsetY != 0) {
            // A hanging fixture is the standing model turned upside down around the x axis.
            int orientation = ModelOrientation
                .index(up == ForgeDirection.UP ? ForgeDirection.NORTH : ForgeDirection.SOUTH, up);
            return renderDevice(
                "quartz_fixture_standing" + odd,
                block,
                world,
                x,
                y,
                z,
                renderBlocks,
                orientation,
                false);
        }
        int orientation = ModelOrientation.index(up, ForgeDirection.UP);
        return renderDevice("quartz_fixture_wall" + odd, block, world, x, y, z, renderBlocks, orientation, false);
    }

    public static boolean renderCrankBase(Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks) {
        if (!(world.getTileEntity(x, y, z) instanceof TileCrank crank)) return false;
        int orientation = crankOrientation(crank);
        return orientation >= 0 && renderDevice("crank_base", block, world, x, y, z, renderBlocks, orientation, false);
    }

    public static boolean renderCrankHandle(TileCrank crank, double x, double y, double z, RenderBlocks renderBlocks) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        BakedBlock model = models == null ? null
            : models.devices()
                .get("crank_handle");
        int orientation = crankOrientation(crank);
        if (model == null || orientation < 0 || !crank.hasWorldObj()) return false;
        ForgeDirection up = crank.getUp();
        Ae2WorldRenderer context = RENDERERS.get();
        context.renderer
            .prepare(crank.getBlockType(), crank.getWorldObj(), crank.xCoord, crank.yCoord, crank.zCoord, renderBlocks);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            GL11.glRotatef(crank.getVisibleRotation(), up.offsetX, up.offsetY, up.offsetZ);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            Tessellator tessellator = TessellatorManager.get();
            tessellator.startDrawingQuads();
            context.setColors(AEColor.Transparent);
            context.renderer
                .render(model.orientations()[orientation], 0, 0, 0, 0, 0, 0, 0xFFFFFF, context.tints, false, null);
            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            RenderHelper.enableStandardItemLighting();
        }
        return true;
    }

    public static IIcon sprite(String name, IIcon fallback) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        IIcon sprite = models == null ? null
            : models.sprites()
                .get(name);
        return sprite == null ? fallback : sprite;
    }

    public static boolean hasDevice(String device) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        return models != null && models.devices()
            .get(device) != null;
    }

    private static int crankOrientation(TileCrank crank) {
        ForgeDirection up = crank.getUp();
        if (up == null || up == ForgeDirection.UNKNOWN) return -1;
        ForgeDirection front = crank.getForward();
        if (front == null || front == ForgeDirection.UNKNOWN || front == up || front == up.getOpposite()) {
            // The handle spins around the up axis, so any perpendicular front is equivalent.
            front = up.offsetY == 0 ? ForgeDirection.UP : ForgeDirection.NORTH;
        }
        return ModelOrientation.index(front, up);
    }

    private static boolean renderDevice(String device, Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks, int orientation, boolean emissive) {
        Snapshot models = Ae2ModelLibrary.INSTANCE.getSnapshot();
        BakedBlock model = models == null ? null
            : models.devices()
                .get(device);
        if (model == null) return false;
        Ae2WorldRenderer context = prepare(block, world, x, y, z, renderBlocks, orientation);
        try {
            context.setColors(AEColor.Transparent);
            context.part(model, 0xFFFFFF, emissive);
            return true;
        } finally {
            context.override = null;
        }
    }

    private static Ae2WorldRenderer prepare(Block block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderBlocks, int orientation) {
        Ae2WorldRenderer context = RENDERERS.get();
        context.x = x;
        context.y = y;
        context.z = z;
        context.orientation = orientation;
        context.override = renderBlocks.overrideBlockTexture;
        context.renderer.prepare(block, world, x, y, z, renderBlocks);
        return context;
    }

    private boolean renderTile(Snapshot models, AEBaseTile tile) {
        Map<String, BakedBlock> devices = models.devices();
        if (tile instanceof TileDrive drive) {
            AEColor color = drive.getColor();
            boolean powered = drive.isPowered();
            setColors(color);
            part(devices.get("drive"), color.driveVariant, false);
            for (int slot = 0; slot < drive.getCellCount(); slot++) {
                int status = drive.getCellStatus(slot);
                if (status == 0) continue;
                float dx = (9 - (slot % 2) * 8) / 16F;
                float dy = (13 - (slot / 2) * 3) / 16F;
                ItemStack cell = drive.getStorageTypes()[slot];
                part(
                    models.cells()
                        .get(cell, drive.getCellType(slot)),
                    dx,
                    dy,
                    1 / 16F,
                    0xFFFFFF,
                    false);
                led(devices.get("cell_led"), status, powered, dx, dy, 1 / 16F);
            }
            return true;
        }
        if (tile instanceof TileChest storage) {
            AEColor color = storage.getColor();
            boolean powered = storage.isPowered();
            setColors(color);
            part(devices.get("chest"), color.driveVariant, false);
            part(devices.get(powered ? "chest_lights_on" : "chest_lights_off"), 0xFFFFFF, powered);
            int status = storage.getCellStatus(0);
            if (status != 0) {
                ItemStack cell = storage.getStorageType();
                part(
                    models.cells()
                        .get(cell, storage.getCellType(0)),
                    5 / 16F,
                    4 / 16F,
                    0,
                    0xFFFFFF,
                    false);
                led(devices.get("cell_led"), status, powered, 5 / 16F, 4 / 16F, 0);
            }
            return true;
        }
        if (tile instanceof TileWireless wireless) {
            setColors(AEColor.Transparent);
            int flags = wireless.getClientFlags();
            boolean powered = (flags & TileWireless.POWERED_FLAG) != 0;
            boolean channel = powered && (flags & TileWireless.CHANNEL_FLAG) != 0;
            part(devices.get("wireless_access_point"), 0xFFFFFF, false);
            part(devices.get(channel ? "wireless_on" : "wireless_off"), 0xFFFFFF, channel);
            String status = channel ? "wireless_status_on"
                : powered ? "wireless_status_channel" : "wireless_status_off";
            part(devices.get(status), 0xFFFFFF, powered);
            return true;
        }
        BakedBlock model = tile instanceof TileCharger ? devices.get("charger")
            : tile instanceof TileInscriber ? devices.get("inscriber") : null;
        if (model == null) return false;
        setColors(AEColor.Transparent);
        part(model, 0xFFFFFF, false);
        return true;
    }

    private void setColors(AEColor color) {
        tints[0] = color.driveVariant;
        tints[1] = color.blackVariant;
        tints[2] = color.mediumVariant;
        tints[3] = color.whiteVariant;
        tints[4] = 0;
    }

    private void led(BakedBlock model, int status, boolean powered, float dx, float dy, float dz) {
        tints[4] = !powered ? 0 : switch (status) {
            case 1 -> 0x00FF00;
            case 2 -> 0x00AAFF;
            case 3 -> 0xFFAA00;
            case 4 -> 0xFF0000;
            default -> 0;
        };
        part(model, dx, dy, dz, 0xFFFFFF, powered);
    }

    private void part(BakedBlock model, int color, boolean emissive) {
        part(model, 0, 0, 0, color, emissive);
    }

    private void part(BakedBlock model, float dx, float dy, float dz, int color, boolean emissive) {
        ModelOrientation rotation = ModelOrientation.ORIENTATIONS[orientation];
        renderer.render(
            model.orientations()[orientation],
            x,
            y,
            z,
            rotation.x(dx, dy, dz),
            rotation.y(dx, dy, dz),
            rotation.z(dx, dy, dz),
            color,
            tints,
            emissive,
            override);
    }
}

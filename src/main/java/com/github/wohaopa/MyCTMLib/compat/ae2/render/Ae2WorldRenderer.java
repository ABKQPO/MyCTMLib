package com.github.wohaopa.MyCTMLib.compat.ae2.render;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.client.model.ModelOrientation;
import com.github.wohaopa.MyCTMLib.client.render.block.JsonBlockModelRenderer;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary.Snapshot;

import appeng.api.util.AEColor;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileWireless;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;

public class Ae2WorldRenderer {

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
        Ae2WorldRenderer context = RENDERERS.get();
        context.x = x;
        context.y = y;
        context.z = z;
        context.orientation = orientation;
        context.override = renderBlocks.overrideBlockTexture;
        context.renderer.prepare(block, world, x, y, z, renderBlocks);
        try {
            return context.renderTile(models, tile);
        } finally {
            context.override = null;
        }
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
        return false;
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

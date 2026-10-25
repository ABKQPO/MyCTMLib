package com.github.wohaopa.MyCTMLib.compat.ae2.render;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel.BakedItem;
import com.github.wohaopa.MyCTMLib.client.render.item.JsonItemModelRenderer;
import com.github.wohaopa.MyCTMLib.client.render.item.SinglePassItemRenderer;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary.Snapshot;

import appeng.api.util.AEColor;
import appeng.block.misc.BlockSkyCompass;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;
import appeng.items.tools.powered.ToolColorApplicator;

public class Ae2ItemRenderer implements SinglePassItemRenderer {

    public static final Ae2ItemRenderer INSTANCE = new Ae2ItemRenderer();
    private static final String COLORED_APPLICATOR = "color_applicator_colored_item";
    private final Map<Item, Entry> entries = new IdentityHashMap<>();
    private final JsonItemModelRenderer renderer = new JsonItemModelRenderer();
    private final int[] tints = new int[4];

    public void register(ItemStack stack, String model, boolean block) {
        if (entries.containsKey(stack.getItem())) return;
        IItemRenderer fallback = MinecraftForgeClient.getItemRenderer(stack, ItemRenderType.INVENTORY);
        entries.put(stack.getItem(), new Entry(model, block, fallback));
        MinecraftForgeClient.registerItemRenderer(stack.getItem(), this);
    }

    @Override
    public boolean rendersAllPasses(ItemStack stack, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP && model(stack) != null;
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        IItemRenderer fallback = fallback(stack);
        return rendersAllPasses(stack, type) || fallback != null && fallback.handleRenderType(stack, type);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        if (rendersAllPasses(stack, type)) {
            boolean block = entries.get(stack.getItem())
                .block();
            return switch (helper) {
                case ENTITY_BOBBING, ENTITY_ROTATION, EQUIPPED_BLOCK -> true;
                case BLOCK_3D -> type == ItemRenderType.EQUIPPED || block && type == ItemRenderType.ENTITY;
                default -> false;
            };
        }
        IItemRenderer fallback = fallback(stack);
        return fallback != null && fallback.shouldUseRenderHelper(type, stack, helper);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        BakedItem model = model(stack);
        if (model == null) {
            IItemRenderer fallback = fallback(stack);
            if (fallback != null) fallback.renderItem(type, stack, data);
            return;
        }
        GL11.glPushMatrix();
        try {
            if (type == ItemRenderType.ENTITY && entries.get(stack.getItem())
                .block()) {
                // Forge scales block entities by one quarter; the shared item renderer cancels one half.
                GL11.glScalef(2, 2, 2);
                if (RenderItem.renderInFrame) {
                    GL11.glRotatef(90, 0, 1, 0);
                    GL11.glTranslatef(0, -0.1F, 0);
                    GL11.glScalef(1 / 1.25F, 1 / 1.25F, 1 / 1.25F);
                }
            }
            if (Block.getBlockFromItem(stack.getItem()) instanceof BlockSkyCompass) {
                renderer.render(type, stack, model);
                BakedItem pointer = device("meteorite_compass_pointer");
                if (pointer != null) {
                    double angle = compassPointerAngle(type, data);
                    renderer.render(type, stack, pointer, () -> {
                        // Modern AE2 turns the needle about the model center, 45 degrees behind its target.
                        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                        GL11.glRotatef((float) Math.toDegrees(angle - Math.PI / 4), 0, 1, 0);
                        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                    });
                }
            } else if (stack.getItem() instanceof ToolColorApplicator applicator) {
                AEColor color = applicator.getActiveColor(stack);
                tints[0] = 0xFFFFFF;
                tints[1] = color == null ? 0xFFFFFF : color.blackVariant;
                tints[2] = color == null ? 0xFFFFFF : color.mediumVariant;
                tints[3] = color == null ? 0xFFFFFF : color.whiteVariant;
                renderer.render(type, stack, model, tints);
            } else {
                renderer.render(type, stack, model);
            }
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static double compassPointerAngle(ItemRenderType type, Object... data) {
        Entity holder = switch (type) {
            case INVENTORY, EQUIPPED_FIRST_PERSON -> Minecraft.getMinecraft().thePlayer;
            case EQUIPPED -> data.length > 1 && data[1] instanceof Entity entity ? entity : null;
            default -> null;
        };
        long now = System.currentTimeMillis();
        double slowSpin = now % 3000 / 3000.0 * Math.PI * 2;
        if (holder == null) return slowSpin;
        int x = MathHelper.floor_double(holder.posX);
        int y = MathHelper.floor_double(holder.posY);
        int z = MathHelper.floor_double(holder.posZ);
        CompassResult result = CompassManager.INSTANCE.getCompassDirection(0, x, y, z);
        // Like AE2, ask for the neighboring blocks too so walking does not flash an unanswered request.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                CompassManager.INSTANCE.getCompassDirection(0, x + dx, y, z + dz);
            }
        }
        if (!result.isValidResult()) return slowSpin;
        if (result.isSpin()) return now % 500 / 500.0 * Math.PI * 2;
        float yaw = type == ItemRenderType.EQUIPPED && holder instanceof EntityLivingBase living
            ? living.renderYawOffset
            : holder.rotationYaw;
        return result.getRad() + Math.toRadians(yaw) + Math.PI;
    }

    private static BakedItem device(String name) {
        Snapshot snapshot = Ae2ModelLibrary.INSTANCE.getSnapshot();
        BakedBlock model = snapshot == null ? null
            : snapshot.devices()
                .get(name);
        return model == null ? null : model.item();
    }

    private BakedItem model(ItemStack stack) {
        Entry entry = stack == null ? null : entries.get(stack.getItem());
        if (entry == null) return null;
        if (stack.getItem() instanceof ToolColorApplicator applicator && applicator.getActiveColor(stack) != null) {
            // AE2 swaps in a model with tinted paint tips once a color is loaded.
            return device(COLORED_APPLICATOR);
        }
        return device(entry.model());
    }

    private IItemRenderer fallback(ItemStack stack) {
        Entry entry = stack == null ? null : entries.get(stack.getItem());
        return entry == null ? null : entry.fallback();
    }

    public record Entry(String model, boolean block, IItemRenderer fallback) {}
}

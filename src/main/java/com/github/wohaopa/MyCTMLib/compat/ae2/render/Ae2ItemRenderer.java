package com.github.wohaopa.MyCTMLib.compat.ae2.render;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel.BakedItem;
import com.github.wohaopa.MyCTMLib.client.render.item.JsonItemModelRenderer;
import com.github.wohaopa.MyCTMLib.client.render.item.SinglePassItemRenderer;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary.Snapshot;

public class Ae2ItemRenderer implements SinglePassItemRenderer {

    public static final Ae2ItemRenderer INSTANCE = new Ae2ItemRenderer();
    private final Map<Item, Entry> entries = new IdentityHashMap<>();
    private final JsonItemModelRenderer renderer = new JsonItemModelRenderer();

    public void register(ItemStack stack, String model) {
        if (entries.containsKey(stack.getItem())) return;
        IItemRenderer fallback = MinecraftForgeClient.getItemRenderer(stack, ItemRenderType.INVENTORY);
        entries.put(stack.getItem(), new Entry(model, fallback));
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
            return switch (helper) {
                case ENTITY_BOBBING, ENTITY_ROTATION, EQUIPPED_BLOCK -> true;
                case BLOCK_3D -> type == ItemRenderType.EQUIPPED || type == ItemRenderType.ENTITY;
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
            if (type == ItemRenderType.ENTITY) {
                // Forge scales block entities by one quarter; the shared item renderer cancels one half.
                GL11.glScalef(2, 2, 2);
                if (RenderItem.renderInFrame) {
                    GL11.glRotatef(90, 0, 1, 0);
                    GL11.glTranslatef(0, -0.1F, 0);
                    GL11.glScalef(1 / 1.25F, 1 / 1.25F, 1 / 1.25F);
                }
            }
            renderer.render(type, stack, model);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private BakedItem model(ItemStack stack) {
        Snapshot snapshot = Ae2ModelLibrary.INSTANCE.getSnapshot();
        Entry entry = stack == null ? null : entries.get(stack.getItem());
        return snapshot == null || entry == null ? null
            : snapshot.devices()
                .get(entry.model())
                .item();
    }

    private IItemRenderer fallback(ItemStack stack) {
        Entry entry = stack == null ? null : entries.get(stack.getItem());
        return entry == null ? null : entry.fallback();
    }

    public record Entry(String model, IItemRenderer fallback) {}
}

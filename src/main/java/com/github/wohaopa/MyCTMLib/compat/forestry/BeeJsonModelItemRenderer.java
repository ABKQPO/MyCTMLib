package com.github.wohaopa.MyCTMLib.compat.forestry;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel;
import com.github.wohaopa.MyCTMLib.client.model.JsonItemModel.BakedItem;
import com.github.wohaopa.MyCTMLib.client.render.item.JsonItemModelRenderer;
import com.github.wohaopa.MyCTMLib.client.render.item.SinglePassItemRenderer;
import com.github.wohaopa.MyCTMLib.client.resource.BeeJsonModelPackState;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc.ModelLoc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;

@SideOnly(Side.CLIENT)
public class BeeJsonModelItemRenderer implements SinglePassItemRenderer, IResourceManagerReloadListener {

    public static final BeeJsonModelItemRenderer INSTANCE = new BeeJsonModelItemRenderer();

    private static final String RESOURCE_DOMAIN = "myctmlib";
    private static final ModelLoc DRONE_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/drone");
    private static final ModelLoc PRINCESS_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/princess");
    private static final ModelLoc QUEEN_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/queen");
    private static final ModelLoc LARVAE_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/larvae");

    private BakedItem droneModel;
    private BakedItem princessModel;
    private BakedItem queenModel;
    private BakedItem larvaeModel;
    private final Map<Item, IItemRenderer> fallbackRenderers = new IdentityHashMap<>();
    private final JsonItemModelRenderer modelRenderer = new JsonItemModelRenderer();

    public void register(ItemStack stack) {
        IItemRenderer fallback = MinecraftForgeClient.getItemRenderer(stack, ItemRenderType.INVENTORY);
        if (fallback != this) {
            fallbackRenderers.put(stack.getItem(), fallback);
        }
        MinecraftForgeClient.registerItemRenderer(stack.getItem(), this);
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return rendersAllPasses(stack, type) || getFallbackRenderer(stack, type) != null;
    }

    @Override
    public boolean rendersAllPasses(ItemStack stack, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP && isJsonModelEnabled(stack);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        if (rendersAllPasses(stack, type)) {
            return switch (helper) {
                case ENTITY_BOBBING, ENTITY_ROTATION, EQUIPPED_BLOCK -> true;
                case BLOCK_3D -> type == ItemRenderType.EQUIPPED;
                default -> false;
            };
        }
        IItemRenderer fallback = getFallbackRenderer(stack, type);
        return fallback != null && fallback.shouldUseRenderHelper(type, stack, helper);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if (rendersAllPasses(stack, type)) {
            renderJsonModel(type, stack);
            return;
        }
        IItemRenderer fallback = getFallbackRenderer(stack, type);
        if (fallback != null) {
            fallback.renderItem(type, stack, data);
        }
    }

    private boolean isJsonModelEnabled(ItemStack stack) {
        return BeeJsonModelPackState.INSTANCE.isEnabled() && stack != null
            && fallbackRenderers.containsKey(stack.getItem());
    }

    private IItemRenderer getFallbackRenderer(ItemStack stack, ItemRenderType type) {
        if (stack == null) {
            return null;
        }
        IItemRenderer fallback = fallbackRenderers.get(stack.getItem());
        return fallback != null && fallback.handleRenderType(stack, type) ? fallback : null;
    }

    private void renderJsonModel(ItemRenderType type, ItemStack stack) {
        EnumBeeType beeType = getBeeType(stack);
        if (beeType == null) {
            return;
        }
        BakedItem model = getModel(beeType);
        if (model == null) {
            return;
        }

        modelRenderer.render(type, stack, model);
    }

    private EnumBeeType getBeeType(ItemStack stack) {
        return stack == null || BeeManager.beeRoot == null ? null : BeeManager.beeRoot.getType(stack);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        clearModels();
    }

    public void clearModels() {
        droneModel = null;
        princessModel = null;
        queenModel = null;
        larvaeModel = null;
    }

    private BakedItem getModel(EnumBeeType beeType) {
        return switch (beeType) {
            case DRONE -> getDroneModel();
            case PRINCESS -> getPrincessModel();
            case QUEEN -> getQueenModel();
            case LARVAE -> getLarvaeModel();
            default -> null;
        };
    }

    private BakedItem getDroneModel() {
        if (droneModel == null) {
            droneModel = bake(DRONE_MODEL_LOCATION);
        }
        return droneModel;
    }

    private BakedItem getPrincessModel() {
        if (princessModel == null) {
            princessModel = bake(PRINCESS_MODEL_LOCATION);
        }
        return princessModel;
    }

    private BakedItem getQueenModel() {
        if (queenModel == null) {
            queenModel = bake(QUEEN_MODEL_LOCATION);
        }
        return queenModel;
    }

    private BakedItem getLarvaeModel() {
        if (larvaeModel == null) {
            larvaeModel = bake(LARVAE_MODEL_LOCATION);
        }
        return larvaeModel;
    }

    private BakedItem bake(ModelLoc location) {
        return new JsonItemModel(ModelRegistry.getJSONModel(location)).bakeItem();
    }

}

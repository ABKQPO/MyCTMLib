package com.github.wohaopa.MyCTMLib;

import static com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing.VALUES;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.client.model.BakedModelQuadContext;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position.ModelDisplay;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc.ModelLoc;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadViewMutable;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;

@SideOnly(Side.CLIENT)
public class BeeJsonModelItemRenderer implements BakedModelQuadContext, IItemRenderer {

    public static final BeeJsonModelItemRenderer INSTANCE = new BeeJsonModelItemRenderer();

    private static final String RESOURCE_DOMAIN = "myctmlib";
    private static final ModelLoc DRONE_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/drone");
    private static final ModelLoc PRINCESS_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/princess");
    private static final ModelLoc QUEEN_MODEL_LOCATION = new ModelLoc(RESOURCE_DOMAIN, "item/bees/queen");

    private BakedModel droneModel;
    private BakedModel princessModel;
    private BakedModel queenModel;
    private ModelQuadFacing quadFacing;
    private final Random random = new Random(0L);
    private final Map<Object, IItemRenderer> fallbackRenderers = new IdentityHashMap<>();

    public void register(ItemStack stack) {
        IItemRenderer fallback = MinecraftForgeClient.getItemRenderer(stack, ItemRenderType.INVENTORY);
        if (fallback != null && fallback != this) {
            fallbackRenderers.put(stack.getItem(), fallback);
        }
        MinecraftForgeClient.registerItemRenderer(stack.getItem(), this);
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return isJsonModelEnabled(stack) || getFallbackRenderer(stack, type) != null;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        if (isJsonModelEnabled(stack)) {
            return helper != ItemRendererHelper.ENTITY_BOBBING && helper != ItemRendererHelper.ENTITY_ROTATION;
        }
        IItemRenderer fallback = getFallbackRenderer(stack, type);
        return fallback != null && fallback.shouldUseRenderHelper(type, stack, helper);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if (isJsonModelEnabled(stack)) {
            renderJsonModel(type, stack);
            return;
        }
        IItemRenderer fallback = getFallbackRenderer(stack, type);
        if (fallback != null) {
            fallback.renderItem(type, stack, data);
        }
    }

    private boolean isJsonModelEnabled(ItemStack stack) {
        return MyCTMLib.BEE_JSON_MODEL_PACK_STATE.isEnabled() && getBeeType(stack) != null;
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
        BakedModel model = getModel(beeType);
        if (model == null) {
            return;
        }

        renderModel(type, stack, model);
    }

    private EnumBeeType getBeeType(ItemStack stack) {
        return stack == null || BeeManager.beeRoot == null ? null : BeeManager.beeRoot.getType(stack);
    }

    public void clearModels() {
        droneModel = null;
        princessModel = null;
        queenModel = null;
    }

    private BakedModel getModel(EnumBeeType beeType) {
        return switch (beeType) {
            case DRONE -> getDroneModel();
            case PRINCESS -> getPrincessModel();
            case QUEEN -> getQueenModel();
            default -> null;
        };
    }

    private BakedModel getDroneModel() {
        if (droneModel == null) {
            droneModel = bake(DRONE_MODEL_LOCATION);
        }
        return droneModel;
    }

    private BakedModel getPrincessModel() {
        if (princessModel == null) {
            princessModel = bake(PRINCESS_MODEL_LOCATION);
        }
        return princessModel;
    }

    private BakedModel getQueenModel() {
        if (queenModel == null) {
            queenModel = bake(QUEEN_MODEL_LOCATION);
        }
        return queenModel;
    }

    private BakedModel bake(ModelLoc location) {
        return ModelRegistry.getJSONModel(location)
            .bake();
    }

    private void renderModel(ItemRenderType type, ItemStack stack, BakedModel model) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawingQuads();

            for (ModelQuadFacing direction : VALUES) {
                quadFacing = direction;
                random.setSeed(0L);
                for (ModelQuadView quad : model.getQuads(this)) {
                    renderQuad(tessellator, stack, quad);
                }
            }

            applyDisplayTransform(type, model);
            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void renderQuad(Tessellator tessellator, ItemStack stack, ModelQuadView quad) {
        int color = quad.getColorIndex() < 0 ? 0xFFFFFF
            : stack.getItem()
                .getColorFromItemStack(stack, quad.getColorIndex());
        float shade = ModelISBRH.diffuseLight(quad.getComputedFaceNormal());
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

    private void applyDisplayTransform(ItemRenderType type, BakedModel model) {
        Position position = getPosition(type);
        if (position == null) {
            return;
        }

        ModelDisplay display = model.getDisplay(position, null);
        Vector3f rotation = display.rotation();
        Vector3f translation = display.translation();
        Vector3f scale = display.scale();
        boolean hasTranslation = !translation.equals(0.0F, 0.0F, 0.0F);
        boolean hasRotation = !rotation.equals(0.0F, 0.0F, 0.0F);
        boolean hasScale = !scale.equals(1.0F, 1.0F, 1.0F);

        if (hasTranslation) {
            GL11.glTranslatef(-translation.z / 16.0F, translation.y / 16.0F, translation.x / 16.0F);
        }

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        if (hasRotation) {
            GL11.glRotatef(rotation.x, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(rotation.y, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-rotation.z, 1.0F, 0.0F, 0.0F);
        }

        if (hasScale) {
            GL11.glScalef(scale.z, scale.y, scale.x);
        } else {
            GL11.glScalef(getDefaultScale(type), getDefaultScale(type), getDefaultScale(type));
        }
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
    }

    private Position getPosition(ItemRenderType type) {
        return switch (type) {
            case ENTITY -> Position.GROUND;
            case EQUIPPED -> Position.THIRDPERSON_RIGHTHAND;
            case INVENTORY -> Position.GUI;
            default -> null;
        };
    }

    private float getDefaultScale(ItemRenderType type) {
        return switch (type) {
            case ENTITY -> 0.25F;
            case EQUIPPED -> 0.375F;
            case INVENTORY -> 0.625F;
            default -> 1.0F;
        };
    }

    @Override
    public BlockState getBlockState() {
        return null;
    }

    @Override
    public ModelQuadFacing getQuadFacing() {
        return quadFacing;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public Supplier<ModelQuadViewMutable> getQuadPool() {
        return null;
    }
}

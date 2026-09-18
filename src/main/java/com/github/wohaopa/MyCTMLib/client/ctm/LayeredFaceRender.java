package com.github.wohaopa.MyCTMLib.client.ctm;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Scopes depth ordering to an explicit sequence of texture layers on one block face.
 */
public class LayeredFaceRender implements AutoCloseable {

    // Two compact vertex units remain distinct after Angelica quantizes either face direction.
    private static final double LAYER_SPACING = 1.0D / 1024.0D;
    private static final ThreadLocal<LayeredFaceRender> scopes = ThreadLocal
        .withInitial(() -> new LayeredFaceRender(null));

    private final LayeredFaceRender parent;
    private LayeredFaceRender child;
    private RenderBlocks renderer;
    private ForgeDirection direction;
    private double x;
    private double y;
    private double z;
    private int layerCount;
    private boolean hasCtm;

    private LayeredFaceRender(LayeredFaceRender parent) {
        this.parent = parent;
    }

    public static LayeredFaceRender begin(RenderBlocks renderer, ForgeDirection direction, double x, double y,
        double z) {
        LayeredFaceRender scope = scopes.get();
        if (scope.renderer != null) {
            if (scope.child == null) {
                scope.child = new LayeredFaceRender(scope);
            }
            scope = scope.child;
            scopes.set(scope);
        }
        scope.renderer = renderer;
        scope.direction = direction;
        scope.x = x;
        scope.y = y;
        scope.z = z;
        return scope;
    }

    /**
     * Counts original face calls only; CTM's internal quadrant calls must bypass this method.
     */
    public static double nextOffset(RenderBlocks renderer, ForgeDirection direction, double x, double y, double z,
        boolean ctm) {
        LayeredFaceRender scope = scopes.get();
        if (scope.renderer != renderer || scope.direction != direction
            || scope.x != x
            || scope.y != y
            || scope.z != z) {
            return 0.0D;
        }
        int layer = scope.layerCount++;
        scope.hasCtm |= ctm;
        return scope.hasCtm ? layer * LAYER_SPACING : 0.0D;
    }

    @Override
    public void close() {
        renderer = null;
        direction = null;
        layerCount = 0;
        hasCtm = false;
        if (parent != null) {
            scopes.set(parent);
        }
    }
}

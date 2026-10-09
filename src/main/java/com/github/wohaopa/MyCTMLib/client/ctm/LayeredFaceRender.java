package com.github.wohaopa.MyCTMLib.client.ctm;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Scopes depth ordering to an explicit sequence of texture layers on one block face.
 */
public class LayeredFaceRender implements AutoCloseable {

    private static final ThreadLocal<LayeredFaceRender> scopes = ThreadLocal
        .withInitial(() -> new LayeredFaceRender(null));

    private final LayeredFaceRender parent;
    private LayeredFaceRender child;
    private RenderBlocks renderer;
    private ForgeDirection direction;
    private double x;
    private double y;
    private double z;
    private boolean tessellationKnown;
    private boolean splitIntoQuadrants;

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
    public static void recordTessellation(RenderBlocks renderer, ForgeDirection direction, double x, double y, double z,
        boolean split) {
        LayeredFaceRender scope = scopes.get();
        if (!scope.matches(renderer, direction, x, y, z) || scope.tessellationKnown) {
            return;
        }
        scope.tessellationKnown = true;
        scope.splitIntoQuadrants = split;
    }

    public static boolean needsQuadrantSplit(RenderBlocks renderer, ForgeDirection direction, double x, double y,
        double z) {
        LayeredFaceRender scope = scopes.get();
        return scope.matches(renderer, direction, x, y, z) && scope.tessellationKnown && scope.splitIntoQuadrants;
    }

    private boolean matches(RenderBlocks renderer, ForgeDirection direction, double x, double y, double z) {
        return this.renderer == renderer && this.direction == direction && this.x == x && this.y == y && this.z == z;
    }

    @Override
    public void close() {
        renderer = null;
        direction = null;
        tessellationKnown = false;
        splitIntoQuadrants = false;
        if (parent != null) {
            scopes.set(parent);
        }
    }
}

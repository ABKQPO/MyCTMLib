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
    private boolean splitIntoQuadrants;

    private LayeredFaceRender(LayeredFaceRender parent) {
        this.parent = parent;
    }

    public static LayeredFaceRender begin(RenderBlocks renderer, ForgeDirection direction, double x, double y, double z,
        boolean splitIntoQuadrants) {
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
        scope.splitIntoQuadrants = splitIntoQuadrants;
        return scope;
    }

    /**
     * @return whether a layer drawn on this face has to be split into quadrants to share its vertices with the rest of
     *         the stack. False outside a layered face, where a single quad matches what vanilla would have drawn.
     */
    public static boolean needsQuadrantSplit(RenderBlocks renderer, ForgeDirection direction, double x, double y,
        double z) {
        LayeredFaceRender scope = scopes.get();
        return scope.splitIntoQuadrants && scope.matches(renderer, direction, x, y, z);
    }

    private boolean matches(RenderBlocks renderer, ForgeDirection direction, double x, double y, double z) {
        return this.renderer == renderer && this.direction == direction && this.x == x && this.y == y && this.z == z;
    }

    @Override
    public void close() {
        renderer = null;
        direction = null;
        splitIntoQuadrants = false;
        if (parent != null) {
            scopes.set(parent);
        }
    }
}

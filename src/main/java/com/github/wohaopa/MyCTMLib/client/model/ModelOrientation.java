package com.github.wohaopa.MyCTMLib.client.model;

import net.minecraftforge.common.util.ForgeDirection;

/** A right-handed block orientation with the unrotated front facing north. */
public record ModelOrientation(ForgeDirection right, ForgeDirection up, ForgeDirection back) {

    public static final ModelOrientation[] ORIENTATIONS = createOrientations();

    public static int index(ForgeDirection front, ForgeDirection up) {
        return front.ordinal() * 6 + up.ordinal();
    }

    public float x(float x, float y, float z) {
        return right.offsetX * x + up.offsetX * y + back.offsetX * z;
    }

    public float y(float x, float y, float z) {
        return right.offsetY * x + up.offsetY * y + back.offsetY * z;
    }

    public float z(float x, float y, float z) {
        return right.offsetZ * x + up.offsetZ * y + back.offsetZ * z;
    }

    public ForgeDirection rotate(ForgeDirection face) {
        if (face == ForgeDirection.UNKNOWN) return face;
        return direction(
            (int) x(face.offsetX, face.offsetY, face.offsetZ),
            (int) y(face.offsetX, face.offsetY, face.offsetZ),
            (int) z(face.offsetX, face.offsetY, face.offsetZ));
    }

    private static ModelOrientation[] createOrientations() {
        ModelOrientation[] result = new ModelOrientation[36];
        for (ForgeDirection front : ForgeDirection.VALID_DIRECTIONS) {
            for (ForgeDirection up : ForgeDirection.VALID_DIRECTIONS) {
                if (front == up || front == up.getOpposite()) continue;
                ForgeDirection right = direction(
                    front.offsetY * up.offsetZ - front.offsetZ * up.offsetY,
                    front.offsetZ * up.offsetX - front.offsetX * up.offsetZ,
                    front.offsetX * up.offsetY - front.offsetY * up.offsetX);
                result[index(front, up)] = new ModelOrientation(right, up, front.getOpposite());
            }
        }
        return result;
    }

    private static ForgeDirection direction(int x, int y, int z) {
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            if (face.offsetX == x && face.offsetY == y && face.offsetZ == z) return face;
        }
        throw new IllegalArgumentException("Invalid orthogonal direction");
    }
}

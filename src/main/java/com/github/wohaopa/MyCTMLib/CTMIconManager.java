package com.github.wohaopa.MyCTMLib;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Provides CTM sub-icons derived from connection and fallback texture sheets.
 */
public class CTMIconManager {

    /**
     * Defines the neighborhood diameter used by a CTM layout.
     */
    public enum DetectionDiameter {

        DIAMETER_1(1),

        DIAMETER_3(3),

        DIAMETER_5(5);

        private final int value;

        DetectionDiameter(int value) {
            this.value = value;
        }

        /**
         * Returns the neighborhood diameter.
         *
         * @return the diameter value
         */
        public int getValue() {
            return value;
        }
    }

    // Stores generated sub-icons at their CTM indices.
    public IIcon[] icons = new CTMIcon[25];

    // Stores the regular 4 by 4 connection texture sheet.
    public IIcon iconCTM;

    // Stores the 2 by 2 fallback texture sheet.
    public IIcon iconSmall;

    // Stores the 2 by 2 alternate fallback texture sheet.
    public IIcon iconAlt;

    // Stores the ring texture sheet.
    public IIcon iconRing;

    // Stores the per-variant sprites of the connection texture.
    private IIcon[] iconVariants;

    // Maps every sub-icon index to the variant icon it belongs to, or null when the index is unused.
    private IIcon[] variantIcons = new IIcon[25];

    // Maps every sub-icon index to its quarter inside that variant, coded as left plus top times two.
    private int[] variantQuarters = new int[25];

    // Stores the single tile covering every face for a per face method, or null for the compact layout.
    private IIcon faceTile;

    // Stores the tiles of a layout driven method in tile order, or null when the method picks no tile per face.
    private IIcon[] tileIcons;

    // Stores the layout driven method that selects one tile per face.
    private CtmMethod faceMethod;

    // Stores the tile weights of the random layout, one value per tile.
    private int[] tileWeights;

    // Stores how many face indices of the random layout share one tile.
    private int faceGrouping = 1;

    // Prefix sums of the random layout weights, so picking a tile never has to walk the weights per face.
    private int[] randomCumulative;
    private int randomTotalWeight;

    // Quarters of the layout tiles, used where a face is drawn in pieces instead of as one quad.
    private IIcon[] tileQuadrants;

    // Stores the pattern size of the repeat layout.
    private int repeatWidth;
    private int repeatHeight;

    // Stores whether opposite faces of the repeat layout share a pattern position.
    private boolean repeatOpposite;

    // Stores whether the client installs the vanilla bottom face UV fix, or null while it is unknown.
    private static Boolean bottomFaceUvFixed;

    // World direction of the face-local right and down axes, indexed by the face direction ordinal.
    private static final ForgeDirection[] FACE_RIGHT = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.WEST,
        ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.NORTH };

    private static final ForgeDirection[] FACE_DOWN = { ForgeDirection.SOUTH, ForgeDirection.SOUTH, ForgeDirection.DOWN,
        ForgeDirection.DOWN, ForgeDirection.DOWN, ForgeDirection.DOWN };

    // Connection slots of the face being rendered, in the order up, right, down, left.
    private static final int CONNECT_UP = 0;
    private static final int CONNECT_RIGHT = 1;
    private static final int CONNECT_DOWN = 2;
    private static final int CONNECT_LEFT = 3;

    // Tile chosen by the horizontal and vertical layouts, indexed by their connection bits.
    private static final int[] AXIS_TILES = { 3, 2, 0, 1 };

    // Diagonal slots of the face being rendered.
    private static final int CONNECT_UR = 4;
    private static final int CONNECT_DR = 5;
    private static final int CONNECT_DL = 6;
    private static final int CONNECT_UL = 7;

    // Connection slot masks, used to skip the neighbour lookups a layout never reads.
    private static final int SLOT_UP = 1 << CONNECT_UP;
    private static final int SLOT_RIGHT = 1 << CONNECT_RIGHT;
    private static final int SLOT_DOWN = 1 << CONNECT_DOWN;
    private static final int SLOT_LEFT = 1 << CONNECT_LEFT;

    // Every connection slot, the mask of the layouts that read all eight neighbours.
    public static final int ALL_CONNECT_SLOTS = 0xff;

    // The two axes, and what the combined layouts still need once their first axis reports no variation.
    private static final int HORIZONTAL_SLOTS = SLOT_LEFT | SLOT_RIGHT;
    private static final int VERTICAL_SLOTS = SLOT_UP | SLOT_DOWN;
    private static final int HORIZONTAL_FALLBACK_SLOTS = ALL_CONNECT_SLOTS & ~HORIZONTAL_SLOTS;
    private static final int VERTICAL_FALLBACK_SLOTS = ALL_CONNECT_SLOTS & ~VERTICAL_SLOTS;

    // Returned when a face needs the remaining neighbour connections before a tile can be picked.
    public static final int NEED_MORE_CONNECTIONS = -2;

    // Tiles of the horizontal then vertical layout, indexed by the lower, lower diagonals and upper neighbours.
    private static final int[] HORIZONTAL_VERTICAL_TILES = { 3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3, 4, 4, 5, 4,
        4, 4, 4, 4, 3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3, 3,
        3, 6, 3, 3, 3, 3, 3 };

    // Maps the table's neighbour order, left, down-left, down, down-right, right, up-right, up, up-left, to the
    // connection slots of the face being rendered. Only the bottom face has its own orientation.
    private static final int[] FACE_CONNECT_SLOTS = { 3, 6, 2, 5, 1, 4, 0, 7 };

    // Bottom face orientation, where left is east, down is south, right is west and up is north.
    private static final int[] BOTTOM_CONNECT_SLOTS = { 1, 5, 2, 6, 3, 7, 0, 4 };

    // Tile chosen by the full layout, indexed by the eight neighbours around the face.
    private static final int[] FULL_TILES = { 0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15, 1, 2, 1, 2, 4, 7, 4,
        29, 1, 2, 1, 2, 13, 31, 13, 14, 0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15, 1, 2, 1, 2, 4, 7, 4, 29,
        1, 2, 1, 2, 13, 31, 13, 14, 36, 17, 36, 17, 24, 19, 24, 43, 36, 17, 36, 17, 24, 19, 24, 43, 16, 18, 16, 18, 6,
        46, 6, 21, 16, 18, 16, 18, 28, 9, 28, 22, 36, 17, 36, 17, 24, 19, 24, 43, 36, 17, 36, 17, 24, 19, 24, 43, 37,
        40, 37, 40, 30, 8, 30, 34, 37, 40, 37, 40, 25, 23, 25, 45, 0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15,
        1, 2, 1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14, 0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15, 1, 2,
        1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14, 36, 39, 36, 39, 24, 41, 24, 27, 36, 39, 36, 39, 24, 41, 24, 27,
        16, 42, 16, 42, 6, 20, 6, 10, 16, 42, 16, 42, 28, 35, 28, 44, 36, 39, 36, 39, 24, 41, 24, 27, 36, 39, 36, 39,
        24, 41, 24, 27, 37, 38, 37, 38, 30, 11, 30, 32, 37, 38, 37, 38, 25, 33, 25, 26 };

    // Tiles of the vertical then horizontal layout, indexed by the side, lower and upper neighbours.
    private static final int[] VERTICAL_HORIZONTAL_TILES = { 3, 6, 3, 3, 3, 6, 3, 3, 4, 5, 4, 4, 3, 6, 3, 3, 3, 6, 3, 3,
        3, 6, 3, 3, 3, 6, 3, 3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3 };

    // Stores the active CTM neighborhood diameter.
    public DetectionDiameter detectionDiameter = DetectionDiameter.DIAMETER_1;

    /**
     * Creates an empty manager for the builder.
     */
    private CTMIconManager() {}

    /**
     * Creates a fallback-only manager for compatibility.
     *
     * @param iconSmall the source icon for the fallback sheet
     */
    public CTMIconManager(IIcon iconSmall) {
        this.iconSmall = iconSmall;
    }

    // Tracks whether sub-icons have been generated.
    private boolean inited = false;

    /**
     * Generates sub-icons from the configured texture sheets.
     */
    public void init() {

        if (iconVariants != null && iconVariants.length == 4) {
            // Build the connection sub-icons from the per-variant sprites of the sheet.
            for (int i = 1; i <= 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int column = i - 1;
                    int row = j;
                    IIcon parent = iconVariants[row / 2 * 2 + column / 2];
                    if (parent != null) {
                        setIcon(i + j * 4, new CTMIcon(parent, 2, 2, column % 2, row % 2));
                        setVariant(i + j * 4, parent, column % 2 + row % 2 * 2);
                    }
                }
            }
        } else if (iconCTM != null) {
            // Build the regular 4 by 4 connection sub-icons, one variant per two by two block of cells.
            for (int i = 1; i <= 4; i++) {
                for (int j = 0; j < 4; j++) {
                    setIcon(i + j * 4, new CTMIcon(iconCTM, 4, 4, i - 1, j));
                    setVariant(i + j * 4, new CTMIcon(iconCTM, 2, 2, (i - 1) / 2, j / 2), (i - 1) % 2 + j % 2 * 2);
                }
            }
        }

        if (iconSmall != null) {
            // Build the regular 2 by 2 fallback sub-icons, whose variant is the whole base texture.
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 16, new CTMIcon(iconSmall, 2, 2, i - 1, j));
                    setVariant(i + j * 2 + 16, iconSmall, i - 1 + j * 2);
                }
            }
        }

        // Build the alternate 2 by 2 fallback sub-icons.
        if (iconAlt != null) {
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 20, new CTMIcon(iconAlt, 2, 2, i - 1, j));
                    setVariant(i + j * 2 + 20, iconAlt, i - 1 + j * 2);
                }
            }
        }

        IIcon[] tileSource = tileIcons;
        if (tileSource == null && faceTile != null) {
            // The per face method is a single tile, laid out as tile zero so faces drawn in pieces can use it too.
            tileSource = new IIcon[] { faceTile };
        }

        if (tileSource != null) {
            // Build the quarters of every layout tile, so a face drawn in pieces can still use one whole tile.
            IIcon[] quadrants = new IIcon[tileSource.length * 4];
            for (int tile = 0; tile < tileSource.length; tile++) {
                IIcon parent = tileSource[tile];
                if (parent == null) {
                    continue;
                }
                for (int quadrant = 0; quadrant < 4; quadrant++) {
                    quadrants[tile * 4 + quadrant] = new CTMIcon(parent, 2, 2, quadrant % 2, quadrant / 2);
                }
            }
            tileQuadrants = quadrants;
        }

        inited = true;
    }

    /**
     * Returns the icon covering the whole face when the four quadrant indices are the four quarters of one variant.
     * <p>
     * Rendering such a face as a single quad keeps its geometry identical to a vanilla face, which is what lets
     * coplanar overlay layers stay in front of it instead of fighting over depth.
     *
     * @param iconIndices the quadrant indices ordered top-left, top-right, bottom-left, bottom-right
     * @return the whole face icon, or null when the quadrants come from different variants
     */
    public IIcon getWholeFaceIcon(int[] iconIndices) {
        if (tileIcons != null) {
            int index = iconIndices != null && iconIndices.length > 0 ? iconIndices[0] : 0;
            return index >= 0 && index < tileIcons.length ? tileIcons[index] : null;
        }

        if (faceTile != null) {
            return faceTile;
        }

        if (iconIndices == null || iconIndices.length < 4) {
            return null;
        }

        IIcon variant = getVariantIcon(iconIndices[0]);
        if (variant == null) {
            return null;
        }

        for (int quarter = 0; quarter < 4; quarter++) {
            int index = iconIndices[quarter];
            if (getVariantIcon(index) != variant || variantQuarters[index] != quarter) {
                return null;
            }
        }

        return variant;
    }

    private IIcon getVariantIcon(int index) {
        if (index <= 0 || index >= variantIcons.length) {
            return null;
        }
        return variantIcons[index];
    }

    private void setVariant(int index, IIcon variant, int quarter) {
        variantIcons[index] = variant;
        variantQuarters[index] = quarter;
    }

    /**
     * Returns a generated sub-icon.
     *
     * @param index the CTM sub-icon index
     * @return the requested sub-icon
     */
    public IIcon getIcon(int index) {
        if (index > 0 && index < 25) return icons[index];
        throw new RuntimeException("Invalid index: " + index);
    }

    /**
     * Returns one quarter of a layout tile.
     *
     * @param tile     the tile number
     * @param quadrant the quarter, ordered left to right then top to bottom
     * @return the quarter icon, or null when it is unavailable
     */
    public IIcon getTileQuadrantIcon(int tile, int quadrant) {
        if (tileQuadrants == null || tile < 0 || quadrant < 0) {
            return null;
        }
        int index = tile * 4 + quadrant;
        return index < tileQuadrants.length ? tileQuadrants[index] : null;
    }

    private void setIcon(int index, IIcon icon) {
        icons[index] = icon;
    }

    /**
     * Returns whether sub-icons have been generated.
     *
     * @return whether this manager is initialized
     */
    public boolean hasInited() {
        return inited;
    }

    /**
     * Returns whether this manager owns a connection texture.
     *
     * @return whether a regular connection texture is available
     */
    public boolean hasConnectionTexture() {
        return iconCTM != null || iconVariants != null || faceTile != null || tileIcons != null;
    }

    /**
     * Returns whether every face is drawn by selecting one tile from a layout.
     *
     * @return whether the method selects a tile per face
     */
    public boolean hasFaceTiles() {
        return tileIcons != null && faceMethod != null;
    }

    /**
     * Returns whether the repeat layout picks tiles from block coordinates.
     *
     * @return whether the method is the repeat layout
     */
    public boolean usesRepeatTiles() {
        return faceMethod == CtmMethod.REPEAT;
    }

    /**
     * Selects the tile covering one face of the repeat layout by projecting the block position onto the face axes.
     *
     * @param x         the block x coordinate
     * @param y         the block y coordinate
     * @param z         the block z coordinate
     * @param direction the face being rendered
     * @return the tile number, or -1 when no tile can be chosen
     */
    public int selectRepeatTile(int x, int y, int z, ForgeDirection direction) {
        if (!usesRepeatTiles() || tileIcons == null || repeatWidth <= 0 || repeatHeight <= 0) {
            return -1;
        }

        int face = direction.ordinal();
        if (repeatOpposite) {
            // Group opposite faces, which are adjacent indices in the direction enum.
            face &= ~1;
        }

        if (bottomFaceIsFlipped(face)) {
            // The vanilla bottom face has its horizontal axis reversed unless a fix is installed, so the opposite
            // face supplies its axes instead.
            face = ForgeDirection.OPPOSITES[face];
        }

        ForgeDirection right = FACE_RIGHT[face];
        ForgeDirection down = FACE_DOWN[face];
        int offsetX = x * right.offsetX + y * right.offsetY + z * right.offsetZ;
        int offsetY = x * down.offsetX + y * down.offsetY + z * down.offsetZ;
        if (direction == ForgeDirection.NORTH || direction == ForgeDirection.EAST) {
            // These faces mirror their horizontal axis, so shift the pattern to keep it aligned.
            offsetX--;
        }

        offsetX = positiveModulo(offsetX, repeatWidth);
        offsetY = positiveModulo(offsetY, repeatHeight);
        int tile = offsetY * repeatWidth + offsetX;
        return tile >= 0 && tile < tileIcons.length ? tile : -1;
    }

    private static int positiveModulo(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    /**
     * Reports whether the bottom face needs the opposite orientation table.
     * <p>
     * The answer is read from the launch blackboard on first use, and is left uncached when the launcher is not ready
     * yet so a later lookup can still answer correctly.
     *
     * @param face the face being rendered
     * @return whether the face uses the opposite orientation
     */
    private static boolean bottomFaceIsFlipped(int face) {
        if (face != ForgeDirection.DOWN.ordinal()) {
            return false;
        }

        Boolean cached = bottomFaceUvFixed;
        if (cached != null) {
            return !cached;
        }

        boolean fixed;
        try {
            Object flag = Launch.blackboard.get("hodgepodge.FixesConfig.fixBottomFaceUV");
            fixed = flag instanceof Boolean && (Boolean) flag;
        } catch (Throwable t) {
            // The launcher is not ready yet, so leave the answer uncached and retry on the next lookup.
            return true;
        }

        bottomFaceUvFixed = fixed;
        return !fixed;
    }

    /**
     * Returns whether the top layout picks its tile from the block above alone.
     *
     * @return whether the method is the top layout
     */
    public boolean usesTopTiles() {
        return faceMethod == CtmMethod.TOP;
    }

    /**
     * Selects the tile covering one side face of the top layout.
     *
     * @param connectedAbove whether the block above connects
     * @return the tile number, or -1 when the face keeps its original texture
     */
    public int selectTopTile(boolean connectedAbove) {
        if (!usesTopTiles() || tileIcons == null || tileIcons.length == 0) {
            return -1;
        }
        return connectedAbove ? 0 : -1;
    }

    /**
     * Returns whether the random layout picks tiles from block coordinates.
     *
     * @return whether the method is the random layout
     */
    public boolean usesRandomTiles() {
        return faceMethod == CtmMethod.RANDOM;
    }

    /**
     * Selects the tile covering one face of the random layout.
     *
     * @param x         the block x coordinate
     * @param y         the block y coordinate
     * @param z         the block z coordinate
     * @param direction the face being rendered
     * @return the tile number, or -1 when no tile can be chosen
     */
    public int selectRandomTile(int x, int y, int z, ForgeDirection direction) {
        if (tileIcons == null || tileIcons.length == 0) {
            return -1;
        }

        if (randomCumulative == null || randomTotalWeight <= 0) {
            return 0;
        }

        long hash = hash128To64(x, y, z, direction.ordinal() / faceGrouping);
        int pick = mod(hash, randomTotalWeight);

        int tile = 0;
        while (tile < randomCumulative.length - 1 && pick >= randomCumulative[tile]) {
            tile++;
        }
        return tile;
    }

    // Builds the prefix sums of the random layout weights, called once when the manager is built.
    private void buildRandomWeights() {
        int length = tileIcons == null ? 0 : tileIcons.length;
        if (length == 0) {
            randomCumulative = null;
            randomTotalWeight = 0;
            return;
        }

        int[] cumulative = new int[length];
        int total = 0;
        for (int tile = 0; tile < length; tile++) {
            total += weightOf(tile);
            cumulative[tile] = total;
        }
        randomCumulative = cumulative;
        randomTotalWeight = total;
    }

    private int weightOf(int tile) {
        if (tileWeights == null || tile >= tileWeights.length) {
            return 1;
        }
        return Math.max(0, tileWeights[tile]);
    }

    // Adapted from CityHash, matching the reference implementation used by the connected texture overrides.
    private static long hash128To64(int i, int j, int k, int l) {
        return hash128To64(((long) i << 32) | ((long) j & 0xffffffffL), ((long) k << 32) | ((long) l & 0xffffffffL));
    }

    private static long hash128To64(long a, long b) {
        a = shiftMix(a * 0xb492b66fbe98f273L) * 0xb492b66fbe98f273L;
        long c = b * 0xb492b66fbe98f273L + mix128To64(a, b);
        long d = shiftMix(a + b);
        a = mix128To64(a, c);
        b = mix128To64(d, b);
        return a ^ b ^ mix128To64(b, a);
    }

    private static long shiftMix(long value) {
        return value ^ (value >>> 47);
    }

    private static long mix128To64(long u, long v) {
        long a = (u ^ v) * 0x9ddfea08eb382d69L;
        a ^= a >>> 47;
        long b = (v ^ a) * 0x9ddfea08eb382d69L;
        b ^= b >>> 47;
        b *= 0x9ddfea08eb382d69L;
        return b;
    }

    private static int mod(long value, int modulus) {
        return (int) (((value >> 32) ^ value) & 0x7fffffff) % modulus;
    }

    /**
     * Returns the neighbour connections one face of this layout reads, as a mask over the connection slots.
     *
     * @param direction the face being rendered
     * @return the connection slot mask, zero when no tile can cover the face
     */
    public int getRequiredConnections(ForgeDirection direction) {
        if (faceMethod == null || tileIcons == null) {
            return 0;
        }

        if ((direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) && faceMethod != CtmMethod.FULL
            && faceMethod != CtmMethod.FIXED) {
            return 0;
        }

        return switch (faceMethod) {
            case FULL -> ALL_CONNECT_SLOTS;
            case HORIZONTAL, HORIZONTAL_VERTICAL -> HORIZONTAL_SLOTS;
            case VERTICAL, VERTICAL_HORIZONTAL -> VERTICAL_SLOTS;
            case TOP -> SLOT_UP;
            default -> 0;
        };
    }

    /**
     * Selects the tile covering one face for a layout driven method.
     *
     * @param connections the neighbour connections of the face, indexed up, right, down, left, then the diagonals
     * @param direction   the face being rendered
     * @param slotMask    the connections present in the array, as returned by getRequiredConnections
     * @return the tile number, -1 when the face keeps its original texture, or NEED_MORE_CONNECTIONS when the
     *         remaining neighbour connections have to be gathered before a tile can be picked
     */
    public int selectFaceTile(boolean[] connections, ForgeDirection direction, int slotMask) {
        if (faceMethod == null || connections == null || tileIcons == null) {
            return -1;
        }

        if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) {
            // Only the compact, full and fixed layouts describe the top and bottom faces.
            if (faceMethod != CtmMethod.FULL && faceMethod != CtmMethod.FIXED) {
                return -1;
            }
        }

        boolean up = connections[CONNECT_UP];
        boolean right = connections[CONNECT_RIGHT];
        boolean down = connections[CONNECT_DOWN];
        boolean left = connections[CONNECT_LEFT];

        int[] slots = direction == ForgeDirection.DOWN ? BOTTOM_CONNECT_SLOTS : FACE_CONNECT_SLOTS;
        int neighborBits = 0;
        for (int bit = 0; bit < slots.length; bit++) {
            if (connections[slots[bit]]) {
                neighborBits |= 1 << bit;
            }
        }

        int tile = switch (faceMethod) {
            case FULL -> FULL_TILES[neighborBits];
            case HORIZONTAL -> AXIS_TILES[(left ? 1 : 0) | (right ? 2 : 0)];
            case VERTICAL -> AXIS_TILES[(down ? 1 : 0) | (up ? 2 : 0)];
            case TOP -> up ? 0 : -1;
            case HORIZONTAL_VERTICAL -> {
                int horizontal = AXIS_TILES[(left ? 1 : 0) | (right ? 2 : 0)];
                if (horizontal != AXIS_TILES[0]) {
                    yield horizontal;
                }
                if ((slotMask & HORIZONTAL_FALLBACK_SLOTS) != HORIZONTAL_FALLBACK_SLOTS) {
                    yield NEED_MORE_CONNECTIONS;
                }
                int bits = (connections[CONNECT_DL] ? 1 : 0) | (down ? 2 : 0)
                    | (connections[CONNECT_DR] ? 4 : 0)
                    | (connections[CONNECT_UR] ? 8 : 0)
                    | (up ? 16 : 0)
                    | (connections[CONNECT_UL] ? 32 : 0);
                yield HORIZONTAL_VERTICAL_TILES[bits];
            }
            case VERTICAL_HORIZONTAL -> {
                int vertical = AXIS_TILES[(down ? 1 : 0) | (up ? 2 : 0)];
                if (vertical != AXIS_TILES[0]) {
                    yield vertical;
                }
                if ((slotMask & VERTICAL_FALLBACK_SLOTS) != VERTICAL_FALLBACK_SLOTS) {
                    yield NEED_MORE_CONNECTIONS;
                }
                int bits = (left ? 1 : 0) | (connections[CONNECT_DL] ? 2 : 0)
                    | (connections[CONNECT_DR] ? 4 : 0)
                    | (right ? 8 : 0)
                    | (connections[CONNECT_UR] ? 16 : 0)
                    | (connections[CONNECT_UL] ? 32 : 0);
                yield VERTICAL_HORIZONTAL_TILES[bits];
            }
            default -> -1;
        };

        if (tile == NEED_MORE_CONNECTIONS) {
            return NEED_MORE_CONNECTIONS;
        }

        return tile >= 0 && tile < tileIcons.length ? tile : -1;
    }

    /**
     * Returns whether every face is drawn from one tile.
     *
     * @return whether a single tile covers the whole face
     */
    public boolean hasFaceTile() {
        return faceTile != null;
    }

    /**
     * Builds CTM icon managers.
     */
    public static class Builder {

        private final CTMIconManager manager;

        public Builder() {
            this.manager = new CTMIconManager();
        }

        /**
         * Sets the regular connection texture sheet.
         */
        public Builder setIconCTM(IIcon iconCTM) {
            manager.iconCTM = iconCTM;
            return this;
        }

        /**
         * Sets the repeat layout options.
         */
        public Builder setRepeatOptions(int width, int height, String symmetry) {
            manager.repeatWidth = width;
            manager.repeatHeight = height;
            manager.repeatOpposite = "opposite".equalsIgnoreCase(symmetry);
            return this;
        }

        /**
         * Sets the random layout options.
         */
        public Builder setRandomOptions(String symmetry, int[] weights) {
            manager.faceGrouping = switch (symmetry == null ? "none" : symmetry.toLowerCase()) {
                case "all" -> 6;
                case "opposite" -> 2;
                default -> 1;
            };
            manager.tileWeights = weights;
            return this;
        }

        /**
         * Sets the tiles of a layout driven method, in tile order.
         */
        public Builder setFaceTiles(CtmMethod method, IIcon[] tiles) {
            manager.faceMethod = method;
            manager.tileIcons = tiles;
            return this;
        }

        /**
         * Sets the single tile that covers every face.
         */
        public Builder setFaceTile(IIcon faceTile) {
            manager.faceTile = faceTile;
            return this;
        }

        /**
         * Sets the per-variant sprites of the connection texture.
         */
        public Builder setIconVariants(IIcon[] iconVariants) {
            manager.iconVariants = iconVariants;
            return this;
        }

        /**
         * Sets the fallback texture sheet.
         */
        public Builder setIconSmall(IIcon iconSmall) {
            manager.iconSmall = iconSmall;
            return this;
        }

        /**
         * Sets the alternate fallback texture sheet.
         */
        public Builder setIconAlt(IIcon iconAlt) {
            manager.iconAlt = iconAlt;
            return this;
        }

        /**
         * Sets the ring texture sheet.
         */
        public Builder setIconRing(IIcon iconRing) {
            manager.iconRing = iconRing;
            return this;
        }

        /**
         * Sets the requested neighborhood diameter.
         */
        public Builder setDetectionDiameter(DetectionDiameter diameter) {
            manager.detectionDiameter = diameter;
            return this;
        }

        /**
         * Builds a manager.
         */
        public CTMIconManager build() {
            if (manager.iconSmall == null) {
                throw new IllegalStateException("iconSmall is required");
            }

            if (manager.iconCTM != null || manager.iconVariants != null
                || manager.faceTile != null
                || manager.tileIcons != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_3;
            } else if (manager.iconRing != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_5;
            } else {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_1;
            }

            manager.buildRandomWeights();

            return manager;
        }

        /**
         * Builds and initializes a manager.
         */
        public CTMIconManager buildAndInit() {
            CTMIconManager result = build();
            result.init();
            return result;
        }
    }

    /**
     * Creates a manager builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Derives UV coordinates dynamically from a parent icon.
     */
    private static class CTMIcon implements IIcon {

        // Stores this sub-icon position in the source grid.
        private final int subTextureX;
        private final int subTextureY;

        // Stores the source grid dimensions.
        private final int gridWidth;
        private final int gridHeight;

        // Stores the parent icon.
        private final IIcon parentIcon;

        // Caches the resolved bounds once the parent icon has been stitched into the atlas. The flag is volatile
        // because render threads resolve the bounds concurrently, and it publishes the coordinates written before it.
        private volatile boolean boundsResolved;
        private float minU;
        private float maxU;
        private float minV;
        private float maxV;

        /**
         * Creates a sub-icon view of a parent icon.
         *
         * @param parent the parent icon
         * @param w      the source grid width
         * @param h      the source grid height
         * @param x      the horizontal grid position
         * @param y      the vertical grid position
         */
        private CTMIcon(IIcon parent, int w, int h, int x, int y) {
            this.parentIcon = parent;
            this.gridWidth = w;
            this.gridHeight = h;
            this.subTextureX = x;
            this.subTextureY = y;

        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinU() {
            resolveBounds();
            return minU;
        }

        /**
         * Resolves and caches this sub-icon's atlas bounds.
         */
        private void resolveBounds() {
            if (boundsResolved) {
                return;
            }

            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            float parentMinV = parentIcon.getMinV();
            float parentMaxV = parentIcon.getMaxV();
            float spanU = parentMaxU - parentMinU;
            float spanV = parentMaxV - parentMinV;
            if (spanU <= 0.0F || spanV <= 0.0F) {
                // The parent is not stitched into the atlas yet, so its coordinates cannot be cached.
                minU = parentMinU;
                maxU = parentMaxU;
                minV = parentMinV;
                maxV = parentMaxV;
                return;
            }

            float insetU = getPixelInset(spanU, parentIcon.getIconWidth());
            float insetV = getPixelInset(spanV, parentIcon.getIconHeight());
            minU = parentMinU + spanU * subTextureX / gridWidth + insetU;
            maxU = parentMinU + spanU * (subTextureX + 1) / gridWidth - insetU;
            minV = parentMinV + spanV * subTextureY / gridHeight + insetV;
            maxV = parentMinV + spanV * (subTextureY + 1) / gridHeight - insetV;
            boundsResolved = true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            resolveBounds();
            return maxU;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedU(double d0) {
            resolveBounds();
            return (float) (minU + (maxU - minU) * d0 / 16.0);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinV() {
            resolveBounds();
            return minV;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            resolveBounds();
            return maxV;
        }

        private float getPixelInset(float span, int pixels) {
            return pixels > 0 ? span / pixels * 0.01F : 0.0F;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedV(double d0) {
            float subVmin = getMinV();
            float subVmax = getMaxV();
            return (float) (subVmin + (subVmax - subVmin) * d0 / 16.0);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getIconName() {
            return parentIcon.getIconName();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconWidth() {
            return parentIcon.getIconWidth() / gridWidth;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconHeight() {
            return parentIcon.getIconHeight() / gridHeight;
        }
    }

}

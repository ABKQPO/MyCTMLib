package com.github.wohaopa.MyCTMLib.client.ctm;

/**
 * Layout of the sheet that holds every connection state.
 * <p>
 * Cells are always addressed top to bottom, left to right. Every method except the compact layout picks a single tile
 * for the whole face, which is why those methods render through the same single quad path vanilla uses.
 */
public enum CtmMethod {

    COMPACT(2, 2, true),

    FULL(12, 4, true),

    HORIZONTAL(4, 1, true),

    VERTICAL(1, 4, true),

    HORIZONTAL_VERTICAL(4, 4, true),

    VERTICAL_HORIZONTAL(4, 4, true),

    TOP(1, 1, true),

    REPEAT(0, 0, true),

    RANDOM(0, 0, true),

    FIXED(1, 1, true);

    private final int columns;
    private final int rows;
    private final boolean implemented;

    CtmMethod(int columns, int rows, boolean implemented) {
        this.columns = columns;
        this.rows = rows;
        this.implemented = implemented;
    }

    /**
     * Resolves a method name. Only the documented names are accepted, anything else is unknown.
     *
     * @param name the configured method name, may be null
     * @return the method, or null when the name is absent or unknown
     */
    public static CtmMethod fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        return switch (name.trim()
            .toLowerCase()) {
            case "compact" -> COMPACT;
            case "full" -> FULL;
            case "horizontal" -> HORIZONTAL;
            case "vertical" -> VERTICAL;
            case "horizontal+vertical" -> HORIZONTAL_VERTICAL;
            case "vertical+horizontal" -> VERTICAL_HORIZONTAL;
            case "top" -> TOP;
            case "repeat" -> REPEAT;
            case "random" -> RANDOM;
            case "fixed" -> FIXED;
            default -> null;
        };
    }

    /**
     * Returns whether this method is ported. Metadata naming a method that is not ported yet is ignored so the
     * original texture stays in place.
     *
     * @return whether the method can be used
     */
    public boolean isImplemented() {
        return implemented;
    }

    /**
     * Returns the number of unit columns of the sheet, or zero when the layout is driven by metadata.
     *
     * @return the unit column count
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Returns the number of unit rows of the sheet, or zero when the layout is driven by metadata.
     *
     * @return the unit row count
     */
    public int getRows() {
        return rows;
    }

    /**
     * Maps a tile number to the cell holding it, following the documented layout of each method.
     *
     * @param tile the tile number
     * @return the cell index in the sheet
     */
    public int getCellIndex(int tile) {
        return switch (this) {
            case VERTICAL -> rows - 1 - tile;
            // row three, zero, one, two, then four, five and six down the first column
            case HORIZONTAL_VERTICAL -> switch (tile) {
                    case 0 -> 1;
                    case 1 -> 2;
                    case 2 -> 3;
                    case 3 -> 0;
                    case 4 -> 4;
                    case 5 -> 8;
                    case 6 -> 12;
                    default -> tile;
                };
            // row three, four, five, six, then two, one and zero down the first column
            case VERTICAL_HORIZONTAL -> switch (tile) {
                    case 0 -> 12;
                    case 1 -> 8;
                    case 2 -> 4;
                    case 3 -> 0;
                    case 4 -> 1;
                    case 5 -> 2;
                    case 6 -> 3;
                    default -> tile;
                };
            default -> tile;
        };
    }
}

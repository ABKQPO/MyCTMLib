# MyCTMLib CTM Methods (English)

MyCTMLib reads connection textures from a single sheet image that contains every state of the texture. The layout of
that sheet is decided by the `method` field of the `myctmlib` metadata section. When `method` is omitted the sheet is
read as **compact**, which is the original MyCTMLib format.

All methods follow the same addressing rule: cells are laid out **top to bottom, left to right**. The cell size in
pixels is derived from the image, so a higher resolution sheet scales automatically (192x64 and 1920x640 are both
valid for `full`).

## Metadata

```json
{
  "myctmlib": {
    "connection": "gregtech:iconsets/MACHINE_CASING_THAUMIUM_ctm",
    "method": "full",
    "columns": 12,
    "width": 5,
    "height": 2,
    "symmetry": "none",
    "weights": [10, 2],
    "linked": false
  }
}
```

| Field | Scope | Meaning |
| --- | --- | --- |
| `connection` | all | the sheet holding every state |
| `method` | all | layout of that sheet, defaults to `compact` |
| `columns` | `random` | overrides the automatic column count, default is one row (`1 x N`) |
| `width`, `height` | `repeat` | tile grid of the pattern, also the sheet layout |
| `symmetry` | `repeat`, `random` | `repeat`: `none` or `opposite`; `random`: `none`, `opposite` or `all` |
| `weights` | `random` | per tile weights, one value per tile in sheet order |
| `linked` | `random` | accepted for compatibility. The reference only applies coordinate offsets for special render types such as stacked crossed squares, so a regular world block is unaffected |
| `alt`, `equivalents`, `random` | all | unchanged, see the original format |

## Method list and sheet layouts

`full`

```text
 0  1  2  3  4  5  6  7  8  9 10 11
12 13 14 15 16 17 18 19 20 21 22 23
24 25 26 27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44 45 46 --
```

A 12 x 4 grid, row major, the last cell is unused. Every face picks exactly one tile from the 256 entry neighbour
table, then draws the whole face with it.

`compact`

The original MyCTMLib format: a sheet of 2 x 2 variants, each variant holding the four quarter tiles of one
connection state. Faces are composed from four quadrants.

`horizontal`

```text
0 1 2 3
```

A 4 x 1 grid. Only the horizontal axis is considered: neither side connected uses tile 3, only left uses 2, only right
uses 0, both use 1. Vertical faces (top and bottom) are left alone.

`vertical`

```text
3
2
1
0
```

A 1 x 4 grid, top to bottom. Only the vertical axis is considered: neither uses 3, only down uses 2, only up uses 0,
both use 1.

`horizontal+vertical`

```text
3 0 1 2
4 . . .
5 . . .
6 . . .
```

A 4 x 4 grid, the bottom right 3 x 3 block is unused. Horizontal is evaluated first; when it reports "no horizontal
variation" (tile 3) the six remaining neighbours select one of tiles 3 to 6.

`vertical+horizontal`

```text
3 4 5 6
2 . . .
1 . . .
0 . . .
```

A 4 x 4 grid, the bottom right 3 x 3 block is unused. Vertical is evaluated first, then horizontal for the fallback.

`top`

A 1 x 1 grid. Side faces use the tile only when the block above matches, which is how smooth sandstone connects.

`repeat`

A `width x height` grid, row major, with `width` and `height` taken from the metadata. The tile is chosen from the
block position projected onto the face axes, which makes the pattern continue across neighbouring blocks:

```text
index = row * width + column
```

`symmetry=opposite` groups opposite faces together, `none` keeps every face independent.

`random`

Any number of tiles. The sheet is read as a single row (`1 x N`) unless `columns` says otherwise. The tile is picked
by hashing the block position and the face:

```text
symmetry=none      every face is independent
symmetry=opposite  opposite faces share a tile
symmetry=all       all six faces share a tile
```

`weights` gives one value per tile in sheet order; equal weights are used when it is omitted. `linked=true` keeps the
choice consistent between render types.

`fixed`

A 1 x 1 grid, every face always uses tile 0. Useful together with `heights` or biome limits, or as a plain replacement.

## Sheet size examples

The cell size follows the base texture, so the sheet scales with it. With a 16 x 16 base texture:

| Method | Cells | Sheet at 16 px cells | Sheet at 32 px cells |
| --- | --- | --- | --- |
| `full` | 12 x 4, last cell unused | 192 x 64 | 384 x 128 |
| `horizontal` | 4 x 1 | 64 x 16 | 128 x 32 |
| `vertical` | 1 x 4 | 16 x 64 | 32 x 128 |
| `horizontal+vertical` | 4 x 4, bottom right 3 x 3 unused | 64 x 64 | 128 x 128 |
| `vertical+horizontal` | 4 x 4, bottom right 3 x 3 unused | 64 x 64 | 128 x 128 |
| `top`, `fixed` | 1 x 1 | 16 x 16 | 32 x 32 |
| `repeat` | `width` x `height` | `16w x 16h` | `32w x 32h` |
| `random` | `1 x N` by default | `16N x 16` | `32N x 32` |
| `compact` | 2 x 2 variants of 2 x 2 cells | 32 x 32 | 64 x 64 |

Every cell is padded with copies of its own edge pixels at load time, so the sheet itself never needs to contain that
padding. Animation frames are stacked vertically the usual way and are padded per frame.

## Examples

Full CTM on a machine casing sheet, 12 x 4 grid:

```json
{
  "myctmlib": {
    "connection": "gregtech:iconsets/MACHINE_CASING_THAUMIUM_ctm",
    "method": "full"
  }
}
```

Horizontal bookshelf style connection, 4 tiles in one row:

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/bookshelf_ctm",
    "method": "horizontal"
  }
}
```

Sandstone style top connection, single tile:

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/sandstone_top_ctm",
    "method": "top"
  }
}
```

Repeating 5 x 2 pattern, sheet is 5 columns by 2 rows:

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/pattern_ctm",
    "method": "repeat",
    "width": 5,
    "height": 2,
    "symmetry": "opposite"
  }
}
```

Random variants with weights, six tiles in a single row:

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/variant_ctm",
    "method": "random",
    "weights": [10, 10, 4, 4, 1, 1],
    "symmetry": "all"
  }
}
```

Four variants in a 2 x 2 sheet using an explicit column count:

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/variant_ctm",
    "method": "random",
    "columns": 2
  }
}
```

## Implementation status

All methods below are implemented and selected through the `myctmlib` metadata section.

| Method | Status |
| --- | --- |
| `compact` | implemented, default, unchanged behaviour |
| `full` | implemented, 47 tiles over a 12 x 4 sheet |
| `horizontal`, `vertical` | implemented, 4 tiles, side faces only |
| `horizontal+vertical`, `vertical+horizontal` | implemented, 7 tiles over a 4 x 4 sheet |
| `top` | implemented, one tile, decided by the block above |
| `repeat` | implemented, `width` x `height` tiles |
| `random` | implemented, hashing and weighted choice match the reference implementation |
| `fixed` | implemented, one tile for every face |

Metadata naming a method that is not ported is ignored and the original texture stays in place, so an unfinished method
can never change how the world looks.

## Glass panes

A pane is drawn in pieces instead of one quad, so a layout driven method cannot cover a panel with a single quad. Every
visible panel face instead takes the tile the layout selects for that face, and each drawn quarter samples the matching
quarter of that tile, which is the same tile a full cube face uses. A face without a tile, such as the edge faces of a
layout driven by one axis, keeps the original texture. `fixed` uses its single tile for every quarter.

Known limitations:

- None of the layouts listed above, so every method works on full cube blocks, on the panes drawn per face and on the
  sprites that request interpolation.
- IC2 block textures are loaded by their own loader, which only reads `connection`, `alt` and `equivalents` and always
  uses the classic 4 x 4 connection sheet, so `method` and its options do not apply to them.
- `linked` behaves the same as the reference for world blocks because that is where the reference also leaves the
  coordinates untouched.

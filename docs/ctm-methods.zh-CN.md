# MyCTMLib CTM 方法说明（中文）

MyCTMLib 从**一张包含全部状态的贴图**里取连接纹理。这张图的排布由 `myctmlib` 元数据段里的 `method` 决定。不写
`method` 时按 **compact** 读取，也就是 MyCTMLib 原有的格式，行为完全不变。

所有方法都遵守同一条定位规则：单元格**自上而下、自左而右**。单元格像素尺寸由图片本身推导，因此高清图会自动适配
（`full` 用 192x64 或 1920x640 都可以）。

## 元数据

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

| 字段 | 适用方法 | 含义 |
| --- | --- | --- |
| `connection` | 全部 | 含全部状态的那张图 |
| `method` | 全部 | 该图的排布，缺省为 `compact` |
| `columns` | `random` | 覆盖自动列数，缺省是单行（`1 x N`） |
| `width`、`height` | `repeat` | 图案的格子数，同时也是整图的切片比例 |
| `symmetry` | `repeat`、`random` | `repeat`：`none` 或 `opposite`；`random`：`none`、`opposite` 或 `all` |
| `weights` | `random` | 每个格子的权重，按图上的顺序一一对应 |
| `linked` | `random` | 为兼容而接受。参考实现只对特殊渲染类型（例如堆叠的交叉方块渲染）叠加坐标偏移，因此世界常规方块不受影响 |
| `alt`、`equivalents`、`random` | 全部 | 保持原样，语义见原有格式 |

## 方法列表与切片布局

`full`

```text
 0  1  2  3  4  5  6  7  8  9 10 11
12 13 14 15 16 17 18 19 20 21 22 23
24 25 26 27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44 45 46 --
```

12 x 4 网格、行优先，最后一格空置不用。每个面依据 256 项邻接表选中唯一一格，然后用这一格画满整面。

`compact`

MyCTMLib 原有格式：2 x 2 个变体的图，每个变体含某个连接状态的四个四分之一块，面由四个象限拼成。

`horizontal`

```text
0 1 2 3
```

4 x 1 网格。只看水平方向：两侧都不连用 3，只连左用 2，只连右用 0，两侧都连用 1。上下面不参与。

`vertical`

```text
3
2
1
0
```

1 x 4 网格，自上而下。只看竖直方向：都不连用 3，只连下用 2，只连上用 0，都连用 1。

`horizontal+vertical`

```text
3 0 1 2
4 . . .
5 . . .
6 . . .
```

4 x 4 网格，右下 3 x 3 不用。先判水平，当水平结果为“无水平变化”（3）时，再用其余六个邻接方向在 3 到 6 之间选一格。

`vertical+horizontal`

```text
3 4 5 6
2 . . .
1 . . .
0 . . .
```

4 x 4 网格，右下 3 x 3 不用。先判竖直，再以水平作为回退。

`top`

1 x 1 网格。仅当上方方块匹配时，侧面才改用这一格——平滑砂岩的连接方式。

`repeat`

`width x height` 网格、行优先，`width` 与 `height` 由元数据给出。选格方式是把方块坐标投影到该面的两条轴上，因此
图案会跨方块延续：

```text
索引 = 行 * width + 列
```

`symmetry=opposite` 把相对的两个面归为一组，`none` 表示各面独立。

`random`

格子数量任意。除非用 `columns` 指定，整图按单行（`1 x N`）切。选格是对坐标与面做哈希：

```text
symmetry=none      各面独立
symmetry=opposite  相对的面共用同一格
symmetry=all       六个面共用同一格
```

`weights` 按图上顺序给每格一个权重，省略时等权。`linked=true` 让不同渲染层之间保持一致。

`fixed`

1 x 1 网格，所有面恒定使用第 0 格。适合与高度、生物群系限制配合，或单纯替换贴图。

## 图片尺寸模板

单元格尺寸跟随基础贴图，因此整图随基础分辨率等比缩放。基础贴图为 16 x 16 时：

| 方法 | 格子数 | 单元格 16 px 时的图 | 单元格 32 px 时的图 |
| --- | --- | --- | --- |
| `full` | 12 x 4，末格空置 | 192 x 64 | 384 x 128 |
| `horizontal` | 4 x 1 | 64 x 16 | 128 x 32 |
| `vertical` | 1 x 4 | 16 x 64 | 32 x 128 |
| `horizontal+vertical` | 4 x 4，右下 3 x 3 空置 | 64 x 64 | 128 x 128 |
| `vertical+horizontal` | 4 x 4，右下 3 x 3 空置 | 64 x 64 | 128 x 128 |
| `top`、`fixed` | 1 x 1 | 16 x 16 | 32 x 32 |
| `repeat` | `width` x `height` | `16w x 16h` | `32w x 32h` |
| `random` | 缺省为 `1 x N` | `16N x 16` | `32N x 32` |
| `compact` | 2 x 2 个变体，每变体 2 x 2 格 | 32 x 32 | 64 x 64 |

每一格在加载时都会用自身的边缘像素向外复制 padding，因此**图中不需要预留这部分留白**。动画贴图仍按惯例纵向堆叠帧，逐帧同样处理。

## 示例

机器外壳全套 CTM，12 x 4 网格：

```json
{
  "myctmlib": {
    "connection": "gregtech:iconsets/MACHINE_CASING_THAUMIUM_ctm",
    "method": "full"
  }
}
```

书架式水平连接，一行四格：

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/bookshelf_ctm",
    "method": "horizontal"
  }
}
```

砂岩式顶面连接，单格：

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/sandstone_top_ctm",
    "method": "top"
  }
}
```

5 x 2 循环图案，图按 5 列 2 行切：

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

带权重的随机变体，单行六格：

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

四个变体放在 2 x 2 的图里，显式指定列数：

```json
{
  "myctmlib": {
    "connection": "minecraft:blocks/variant_ctm",
    "method": "random",
    "columns": 2
  }
}
```

## 实现状态

下列方法均已实现，全部通过 `myctmlib` 元数据段选择。

| 方法 | 状态 |
| --- | --- |
| `compact` | 已实现，缺省，行为不变 |
| `full` | 已实现，12 x 4 图上共 47 格 |
| `horizontal`、`vertical` | 已实现，4 格，仅作用于侧面 |
| `horizontal+vertical`、`vertical+horizontal` | 已实现，4 x 4 图上共 7 格 |
| `top` | 已实现，单格，由上方方块决定 |
| `repeat` | 已实现，`width` x `height` 格 |
| `random` | 已实现，哈希与加权选择与参考实现逐值一致 |
| `fixed` | 已实现，所有面使用同一格 |

写入未移植方法名的元数据会被忽略并保留原贴图，因此未完成的方法不会改变世界的观感。

## 玻璃板

玻璃板是按小块绘制而不是用一个四边形覆盖整面，因此布局类方法无法用单个四边形覆盖一块面板。每个可见面板面改用该
布局为这一面选出的那一格，绘制的每个四分之一格取样同一格的对应四分之一格，与完整方块面使用的是同一格。某一面没有
对应格子时（例如单轴布局的边缘面）保留原贴图。`fixed` 的每一格都用它唯一的那一格。

已知限制：

- 上述布局本身没有限制：所有方法都可用于完整方块面、按面绘制的玻璃板，以及声明了插值的动画贴图。
- IC2 方块贴图由它自己的加载路径处理，只读取 `connection`、`alt`、`equivalents`，并固定使用经典 4 x 4 连接图，
  因此 `method` 及其选项对 IC2 贴图不生效。
- `linked` 在世界方块上的行为与参考实现一致，因为参考实现在该场景同样不改动坐标。

---
tags:
  - 文档/开发
---
### 基本设置

```Json
{
   "fusion": {
      "type": "<texture type>",
      <configuration>
   }
}
```

> [!Note] 
> 注：json 模型文件被单独存放到 `modid/blockmodel`

### Base

| key         | values                                            | 说明   |
| ----------- | ------------------------------------------------- | ---- |
| type        | `base`                                            |      |
| emissive    | `true`<br>`false`                                 |      |
| render_type | `opaque`<br>`cutout`<br>`translucent`             |      |
| tinting     | `biome_grass`<br>`biome_foliage`<br>`biome_water` |      |
| model       | `json`                                            | 模型路径 |

### Connecting

| key    | values          | 说明             |
| ------ | --------------- | -------------- |
| type   | `connecting`    | 优先支持           |
| layout | `simple`        |                |
|        | `full`          | 6 x 8 图片       |
|        | `pieced`        |                |
|        | `compact`       |                |
|        | `horizontal`    |                |
|        | `vertical`      |                |
|        | `overlay`       |                |
| random | `true`, `false` | CTM 的 Random支持 |
| model  | `Json`          | 模型数据           |

#### 模型数据配置选项


| key       | values          | 说明                 |
| --------- | --------------- | ------------------ |
| `type`    | `connecting`    | 优先支持               |
| `layout`  | `simple`        | 4 x 4 图片           |
|           | `full`          | 6 x 8 图片           |
|           | `pieced`        |                    |
|           | `compact`       |                    |
|           | `horizontal`    |                    |
|           | `vertical`      |                    |
|           | `overlay`       |                    |
| `random`  | `true`, `false` | CTM 的 Random 支持    |
| `variant` |                 | 对标 BlockState，暂不支持 |
| `model`   | `path:String`   | 模型数据               |

### Continuous

| key      | values    | 说明 |
|----------|-----------|------|
| type     | `continuous` |      |
| rows     | `integer` |      |
| columns  | `integer` |      |

### Random

| key      | values    | 说明 |
|----------|-----------|------|
| type     | `random`  |      |
| rows     | `integer` |      |
| columns  | `integer` |      |
| seed     | `integer` |      |

> [!Note] Random 支持
> 为了支持 CTM 的 Random 效果，现在 Random 作为子字段提供支持。这个字段目前仅作为保留。

### Scrolling

| key          | values                                                       | 说明  |
| ------------ | ------------------------------------------------------------ | --- |
| type         | `scrolling`                                                  |     |
| from         | `top_left`<br>`top_right`<br>`bottom_left`<br>`bottom_right` |     |
| to           | `top_left`<br>`top_right`<br>`bottom_left`<br>`bottom_right` |     |
| frame_width  | `integer`                                                    |     |
| frame_height | `integer`                                                    |     |
| frame_time   | `integer`                                                    |     |
| loop_type    | `reset`<br>`reverse`                                         |     |
| loop_pause   | `integer`                                                    |     |

### 模型数据配置选项

模型路径：命名空间/models/block/XXX.json

| MainFiled     | Value                    |     |
| ------------- | ------------------------ | --- |
| `loader`      | `“ctmlib:model”`         |     |
| `type`        | `"connection"`, `"base"` |     |
| `textures`    |                          |     |
| `connections` |                          |     |
| `elements`    |                          |     |

#### `elements`

| SubFiled | Subfiled |               |
| -------- | -------- | ------------- |
| `from`   |          |               |
| `to`     |          |               |
| `faces`  | `up`     | 暂时只支持 `faces` |
|          |          |               |
#### `textures`

```Json
"textures": {
      "ice": "block/oak_tiles",
      "stone": "block/oak_tiles",
      "particle": "#ice"
      "ice_random": [
          {"block/ice_1",1},
          {"block/ice_2",4}
        ]
   }
```

纹理后面的数字为权重

#### `connections`
```Json
"connections": {
      "blue": [
         {
            "condition": "match_block",
            "block": "ice"
         },
         {
            "condition": "match_block",
            "block": "lapis_block"
         }
      ],
      "lapis": "#blue",
      "dirt": {
         "condition": "is_same_block"
      },
      "default": {
         "condition": "is_same_block"
      }
   }
```

##### 连接谓词
- `is_same_block`
- `is_same_texture`: 用来匹配仓室
- ``





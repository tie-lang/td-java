# td-java

td (tie data) 配置解析/写出库 —— 纯 Java（JDK 17+，零依赖，包名
`org.tielang.td`）。td 是 tie 生态的配置/数据文本格式：与 tiec `config.parse_data`
（`--compress-data` 输入的 td 子集，见 tie-main `compiler/config.tie`、`tdzd.tie`）
同语法。

td-java: the td (tie data) configuration parser/writer — pure Java (JDK 17+,
no dependencies, package `org.tielang.td`). td is the tie-ecosystem
configuration/data text format, syntax-compatible with tiec `config.parse_data`
(the td subset feeding `--compress-data`; see tie-main `compiler/config.tie`,
`tdzd.tie`).

## 格式 / Format

```text
type tie<data>
worldgen = [
  terrain = "山地",
  count = 42,
  ratio = 1.5,
  active = true,
  tags = ["a", "b"],
  nested = [
    x = 1,
  ],
]
```

* 可选头 `type tie<data>` 与可选表名 `name = [...]` 会被剥离（对齐 tiec `parse_data`）
* 表（键值）与数组（值列表）都用 `[...]`，靠首个条目是 `key = value` 还是裸值区分
* 支持：字符串（转义 `\"` `\\` `\n` `\t` `\r`）、整数、浮点、`true`/`false`、
  嵌套表、数组元素；`//` 行注释；条目间逗号可选（容忍尾逗号）
* 确定性纯函数：非法输入抛 `IllegalArgumentException`

* Optional `type tie<data>` header and optional table name (`name = [...]`) are
  stripped (matching tiec `parse_data`).
* Tables (key/value) and arrays (value lists) both use `[...]`, distinguished by
  whether the first entry is `key = value` or a bare value.
* Supports: strings (escapes `\"` `\\` `\n` `\t` `\r`), integers, floats,
  `true`/`false`, nested tables, array elements; `//` line comments; commas are
  optional between entries (trailing commas tolerated).
* Deterministic and pure: malformed input raises `IllegalArgumentException`.

## API (`org.tielang.td`)

| class / method | description |
| --- | --- |
| `Td.parse(String) -> TdTable` | parse a td document into an immutable root table |
| `Td.write(TdTable) -> String` | serialize a table back to td text (2-space indent) |
| `TdTable.get(key) / keys() / elements()` | named lookup, ordered keys, array elements |
| `TdValue.str / of(long) / of(double) / of(boolean)` | scalar factories |
| `TdValue.asString / asInt / asFloat / asBool` | typed accessors (wrong kind -> default) |
| `TdTable.builder().put(...).element(...).build()` | programmatic model construction |

## 使用 / Usage

```java
import org.tielang.td.Td;
import org.tielang.td.TdTable;

TdTable table = Td.parse("type tie<data>\nworldgen = [\n  terrain = \"山地\",\n]\n");
String terrain = table.get("terrain").asString(); // "山地"

TdTable built = TdTable.builder()
        .put("note", "line1\nline2\t\"q\"\\path")
        .put("count", TdValue.of(42L))
        .element(TdValue.of(1L))
        .build();
String text = Td.write(built);
```

## 构建与测试 / Build & test

零依赖，无构建步骤（两个纯 JDK 文件集）：

```bash
javac -d out src/main/java/org/tielang/td/*.java \
          src/test/java/org/tielang/td/*.java
java -cp out org.tielang.td.TestTd
```

或用 Maven：

```bash
mvn compile
```

## License

本仓库使用 **Tie Public License v2.0 (TPL 2.0)**，完整文本见 [LICENSE](LICENSE)。
This repository is distributed under the **Tie Public License v2.0 (TPL 2.0)** — see [LICENSE](LICENSE) for the full text.
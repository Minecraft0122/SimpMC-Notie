# SimpMC-Notie

[![Build and Release](https://github.com/Minecraft0122/SimpMC-Notie/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/Minecraft0122/SimpMC-Notie/actions/workflows/build-and-release.yml)

> A Plugin For SimpMC Network.

SimpMC-Notie 是一个支持随机或指定前缀的 Folia 全服广播插件，适用于服务器公告、提示和特殊通知。

## 快速开始

1. 从 [Releases](https://github.com/Minecraft0122/SimpMC-Notie/releases/latest) 下载最新版 `SimpMC-Notie-*.jar`。
2. 确保服务端使用 **Folia 26.1.2** 和 **Java 25**。
3. 把 JAR 放入服务端的 `plugins` 文件夹，然后重启服务端。

## 使用方法

| 指令 | 用途 |
| --- | --- |
| `/notie <内容>` | 从所有 `random: true` 的前缀中随机选择一个并广播 |
| `/notie1 <内容>` | 固定使用配置中的第一个前缀 |
| `/notie2 <内容>` | 固定使用配置中的第二个前缀 |
| `/notie3` 至 `/notie9` | 按数字固定使用对应位置的前缀 |

使用权限为 `simpmc.notie.use`，默认只有 OP 拥有。

### 使用示例

配置中默认有四个前缀：公告、提示、通知和特殊通知。

```text
/notie 服务器将在五分钟后重启
```

从配置中所有 `random: true` 的前缀里随机选择一个。特殊通知设置为 `random: false`，因此永远不会在这里出现。

```text
/notie1 服务器将在五分钟后重启
```

固定使用配置中的第一个前缀。`/notie2` 使用第二个，依此类推，最多可通过 `/notie9` 直接指定第九个。

```text
/notie4 这条消息只会在明确调用时发出
```

固定调用第四个“特殊通知”前缀；即使它不参与随机，数字指令仍然可以调用。

## 配置

前缀位于 `plugins/SimpMC-Notie/config.yml` 的 `prefixes` 列表中：

```yaml
prefixes:
  - text: "&6[公告]"
    random: true
  - text: "&b[提示]"
    random: true
  - text: "&d[通知]"
    random: true
  - text: "&c[特殊通知]"
    random: false
```

可以增删或修改前缀；`random: false` 表示排除出随机池，但对应的 `/notie数字` 仍然有效。`&6` 等传统 Minecraft 颜色代码可用。1.1.0 的纯字符串配置仍然兼容，并默认视为 `random: true`。修改后重启服务端，或使用服务端现有的插件管理方式重新加载本插件。

### 文字格式与颜色

前缀、提示语和指令正文支持混用以下格式：

| 格式 | 示例 |
| --- | --- |
| 传统颜色代码 | `&a绿色`、`&6金色`、`&l粗体`、`&r重置` |
| 十六进制颜色 | `&#12ABEF文字` |
| Bungee 风格十六进制 | `&x&1&2&A&B&E&F文字` |
| MiniMessage | `<red>红色</red>`、`<#12ABEF>文字</#12ABEF>` |
| MiniMessage 渐变 | `<gradient:red:blue>渐变文字</gradient>` |

例如：

```yaml
prefixes:
  - text: "<gradient:#00aaff:#aa00ff><bold>[SimpMC]</bold></gradient>"
    random: true
  - text: "&#FF8800[活动通知]"
    random: false
```

前缀、提示语和指令正文始终解析上述颜色及格式，无需额外开关。前缀的颜色会延续到后面的空格和正文；为了兼容旧配置，即使前缀以 `&r`、`<reset>` 或 `</颜色>` 结尾，插件也会移除末尾边界。正文中的新颜色代码仍可覆盖前缀颜色。MiniMessage 使用纯文字格式预设，支持颜色、渐变和彩虹等显示效果，不接受点击命令、悬浮事件或 NBT 等交互标签。

如需自行构建，请使用 Java 25 或更高版本运行 `mvn clean package`。插件面向 Folia 26.1.2，使用官方 `folia-api 26.1.2.build.8-stable` 构建，并声明了 Folia 区域化多线程支持。运行时不依赖 EssentialsX。

作者：Minecraft0122, SimpMC, GPT-5.6

## 自动构建和发布

- 推送到 `main` 或提交 Pull Request 时，会自动使用 Java 25 构建、运行测试并上传 JAR 产物。
- 推送形如 `v1.3.0` 的版本标签时，会自动创建 GitHub Release，并把构建出的 JAR 作为发布附件。
- Actions 构建产物保留 30 天；GitHub Release 附件长期保留。

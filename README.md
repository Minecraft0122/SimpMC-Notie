# SimpMC-Notice

[![Build and Release](https://github.com/Minecraft0122/SimpMC-Notice/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/Minecraft0122/SimpMC-Notice/actions/workflows/build-and-release.yml)

> A Plugin For SimpMC Network.

SimpMC-Notice 是一个面向 Folia 26.1.2 的全服公告插件，支持定时随机公告、随机前缀广播、固定前缀广播和运行时配置重载。

## 快速开始

1. 从 [Releases](https://github.com/Minecraft0122/SimpMC-Notice/releases/latest) 下载最新版 `SimpMC-Notice-*.jar`。
2. 确保服务端使用 **Folia 26.1.2** 和 **Java 25**。
3. 把 JAR 放入服务端的 `plugins` 文件夹，然后重启服务端。

## 指令

| 指令 | 功能 | 权限 |
| --- | --- | --- |
| `/noti <内容>` | 从随机前缀库随机选择前缀并广播内容 | `simpmc.notice.noti` |
| `/notice <内容>` | 从固定前缀库随机选择前缀并广播内容 | `simpmc.notice.notice` |
| `/noticrreload` | 重载配置、三个内容库和自动公告时间范围 | `simpmc.notice.reload` |

所有权限默认仅 OP 拥有。

指令内容可以包含任意数量的空格。例如：

```text
/noti 服务器将在十分钟后重新启动，请大家提前保存物品
/notice 今晚八点将开启限时活动，欢迎大家参加
```

## 自动公告

启用后，插件会在配置的最小和最大秒数之间随机等待。到达发送时间时：

1. 检查服务器是否有玩家在线；无人在线时不发送。
2. 从 `random-prefixes` 随机选择一个前缀。
3. 从 `random-messages` 随机选择一条消息。
4. 广播后重新生成下一次随机等待时间。

## 配置和三个内容库

三个内容库与自动公告设置都位于 `plugins/SimpMC-Notice/config.yml`：

```yaml
# 1. 自动公告和 /noti 使用
random-prefixes:
  - "&6[公告]"
  - "&b[提示]"
  - "&d[通知]"

# 2. 自动公告使用
random-messages:
  - "欢迎来到 SimpMC Network！"
  - "请文明游戏，并遵守服务器规则。"

# 3. /notice 使用
fixed-prefixes:
  - "&c[重要通知]"
  - "&#FF8800[活动通知]"

announcement:
  enabled: true
  interval-seconds:
    min: 300
    max: 600

separator: " "
```

修改后执行 `/noticrreload` 即可立即刷新三个内容库、提示消息和公告时间范围。

## 颜色和文字格式

所有前缀和消息均支持混用：

- 传统颜色：`&a`、`&6`、`&l`、`&r`
- 十六进制颜色：`&#12ABEF`
- Bungee 风格十六进制：`&x&1&2&A&B&E&F`
- MiniMessage：`<red>`、`<#12ABEF>`、`<gradient:red:blue>渐变</gradient>`

前缀颜色会延续到后续正文，正文中的新颜色可以覆盖它。MiniMessage 仅开放文字格式，不允许点击命令、悬浮事件或 NBT 等交互标签。

## 自行构建

使用 Java 25 或更高版本运行：

```shell
mvn clean package
```

产物位于 `target/SimpMC-Notice-2.0.0.jar`。插件使用官方 `folia-api 26.1.2.build.8-stable` 构建，运行时不依赖 EssentialsX。

作者：Minecraft0122, SimpMC, GPT-5.6

## 自动构建和发布

- 推送到 `main` 或提交 Pull Request 时，GitHub Actions 自动构建、测试并上传 JAR。
- 推送形如 `v2.0.0` 的版本标签时，自动创建 GitHub Release 并上传 JAR。
- Actions 临时构建产物保留 30 天；Release 附件长期保留。

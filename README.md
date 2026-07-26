# SimpMC-Notie

> A Plugin For SimpMC Network.

一个支持随机或指定前缀的 Folia 全服广播插件。

## 指令

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

权限为 `simpmc.notie.use`，默认只有 OP 拥有。

## 配置

前缀位于 `plugins/SimpMC-Notie/config.yml` 的 `prefixes` 列表中：

```yaml
prefixes:
  - text: "&6[公告]&r"
    random: true
  - text: "&b[提示]&r"
    random: true
  - text: "&d[通知]&r"
    random: true
  - text: "&c[特殊通知]&r"
    random: false
```

可以增删或修改前缀；`random: false` 表示排除出随机池，但对应的 `/notie数字` 仍然有效。`&6` 等传统 Minecraft 颜色代码可用。1.1.0 的纯字符串配置仍然兼容，并默认视为 `random: true`。修改后重启服务端，或使用服务端现有的插件管理方式重新加载本插件。

## 安装

1. 使用 Java 25 或更高版本运行 `mvn clean package`。
2. 把 `target/SimpMC-Notie-1.3.0.jar` 放进服务端的 `plugins` 文件夹。
3. 重启服务端。

插件面向 Folia 26.1.2，使用官方 `folia-api 26.1.2.build.8-stable` 构建，并声明了 Folia 区域化多线程支持。运行时不依赖 EssentialsX。

作者：Minecraft0122, SimpMC, GPT-5.6

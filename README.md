# 点检 (Dianjian)

一个 Android 授权码生成器：输入设备号、开始时间、到期时间，使用内置 RSA 公钥加密后显示授权码，并生成对应的二维码。

应用名称：点检
版本：1.0

## 功能

- 设备号：手动输入，提示一般为设备号后 6 位
- 开始时间：
  - 实时模式：每秒读秒，输入框只读并显示为淡色
  - 自定义模式：输入框可编辑，手动填写时间
  - 通过右侧小按钮在「实时 / 自定义」之间切换，按钮文字表示可切换到的模式
- 到期时间：默认 `4102502399000`，以可读时间显示，可编辑
- 时间字段均以 `yyyy-MM-dd HH:mm:ss` 显示，加密时自动转换为毫秒时间戳
- 点击「生成」：对 `设备号,开始时间戳,到期时间戳` 进行 RSA 加密，显示加密后的「授权码」并生成二维码

## 目录结构

```
app/
  AndroidManifest.xml                     应用清单
  res/layout/activity_main.xml            界面布局
  src/com/example/qrencrypt/MainActivity.java   主逻辑（RSA 加密 + 二维码）
  libs/zxing-core.jar                     二维码库
signing/my.keystore                       签名密钥库
build.sh                                  无 Gradle 构建脚本
qrencrypt.apk                             构建产物
```

## 构建

无需 Gradle，构建脚本直接调用 Android SDK 命令行工具。

依赖：
- JDK 17
- Android build-tools（aapt2、d8、zipalign、apksigner）
- Android platform 的 `android.jar`

默认工具路径可通过环境变量覆盖：

- `BT`：build-tools 目录，默认 `/opt/android-sdk/android-14`
- `PLATFORM`：包含 `android.jar` 的 platform 目录，默认 `/opt/android-sdk/android-34-ext12`

```bash
bash build.sh
```

产物输出到 `qrencrypt.apk`。

## 安装

```bash
adb install -r qrencrypt.apk
```

## 说明

- 加密使用 `RSA/ECB/NoPadding`，明文左侧补零至密钥长度（64 字节），与原始实现一致。
- 内置公钥为 512 位，单次可加密 64 字节数据。

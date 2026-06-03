# imageeditor

[English](./README.md) | **简体中文**

一个 Compose 优先、微信风格的 Android 图片编辑器：**裁剪/旋转、涂鸦、马赛克、文字、贴纸**——
单一入口 Composable，输入输出契约干净、与业务零耦合。

它填补了生态里的一个真实空白：**内置马赛克笔刷**的 Jetpack Compose 编辑器
（多数还在维护的库要么是 View 实现、要么没有马赛克、要么已停更）。

## 功能

- ✂️ **裁剪 / 旋转 / 翻转** —— 基于 [CanHub/Android-Image-Cropper](https://github.com/CanHub/Android-Image-Cropper)（前置阶段，可跳过）
- ✏️ **涂鸦** —— 色板、笔刷粗细、橡皮
- ▦ **马赛克** —— 自研像素块笔刷（沿笔迹用降采样 `BitmapShader` 绘制），支持点按/涂抹
- 🅣 **文字** —— 可拖拽/缩放/旋转图层，双击编辑，自动对比阴影提升可读性
- 😀 **贴纸** —— 素材由宿主经 `StickerProvider` 注入
- ↩ **撤销 / 重做** —— 单键，跨标注与马赛克统一时间序
- 解码时按 EXIF 自动转正；导出一次性拍平

涂鸦/文字/贴纸基于 [burhanrashid52/PhotoEditor](https://github.com/burhanrashid52/PhotoEditor)，经 `AndroidView` 桥接进 Compose。

## 接入

### 通过 Maven Central

```kotlin
implementation("io.github.robeshell:compose-image-editor:0.2.0")
```

> 需维护者先完成发布 —— 见 [PUBLISHING.md](./PUBLISHING.md)。

### 通过 JitPack

在仓库配置里加 JitPack（`settings.gradle.kts`）：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

再依赖库：

```kotlin
implementation("com.github.robeshell:compose-image-editor:0.2.0")
```

### 作为 Gradle 模块 / git submodule

也可以把源码作为模块引入（如 git submodule）。库位于本仓库的 `imageeditor/` 子目录，
在宿主 `settings.gradle.kts` 中指向它：

```kotlin
include(":imageeditor")
project(":imageeditor").projectDir = file("path/to/compose-image-editor/imageeditor")
```

```kotlin
implementation(project(":imageeditor"))
```

### ⚠️ 必做配置 —— 给裁剪 Activity 配 AppCompat 主题

裁剪页（`com.canhub.cropper.CropImageActivity`）继承自 `AppCompatActivity`，
并把**「完成 / 旋转」放在系统 ActionBar 菜单**里。你**必须**在 App 的 manifest 中
为它声明一个**带 ActionBar 的 AppCompat 主题**，否则它会崩溃
（`You need to use a Theme.AppCompat theme`）或只显示裁剪框、没有按钮：

```xml
<activity
    android:name="com.canhub.cropper.CropImageActivity"
    android:theme="@style/Theme.AppCompat.Light.DarkActionBar" />
```

### 传递依赖提示

本模块（经 cropper）会引入 `androidx.appcompat`，可能把 `androidx.core` /
`androidx.activity` **顶到比你工程当前更高的版本**。注意类似
`Activity.onRequestPermissionsResult(..., permissions: Array<String>, ...)` 这类签名变化。

## 运行示例

`:sample` 是一个可直接运行的 demo（选图 → 编辑 → 查看结果）：

```bash
./gradlew :sample:installDebug
```

## 从源码构建

这是标准的 Android Gradle 工程。单独 clone 构建需要在仓库根放一个
`local.properties` 指向你的 Android SDK（Android Studio 首次打开会自动生成）：

```properties
sdk.dir=/path/to/Android/sdk
```

## 使用

`ImageEditorScreen` 是一个 Composable —— 放进你的导航或全屏浮层即可。
它接收 `ImageEditorRequest`，通过 `onResult` 回吐结果。它对你的 App、发送、网络
一无所知 —— 只产出一个本地图片 `Uri`。

```kotlin
var editingUri by remember { mutableStateOf<Uri?>(null) }

editingUri?.let { uri ->
    ImageEditorScreen(
        request = ImageEditorRequest(
            sourceUri = uri,
            config = ImageEditorConfig(
                theme = EditorTheme(primary = Color(0xFF1E6FFF)),
                // 可选:stickerProvider、brushColors、mosaicBlockSize、output、tools…
            ),
        ),
        onResult = { result ->
            editingUri = null
            when (result) {
                is ImageEditorResult.Success -> handleEditedImage(result.uri)
                is ImageEditorResult.Cancelled -> { /* 用户返回 */ }
                is ImageEditorResult.Failed -> showError(result.cause)
            }
        },
    )
}
```

### 配置项（`ImageEditorConfig`）

| 字段 | 默认 | 说明 |
|---|---|---|
| `tools` | 全部 | 显示哪些工具及顺序;含 `CROP` 则启用裁剪前置阶段 |
| `cropFirst` | `true` | 标注前先裁剪(在 cropper 内取消即跳过) |
| `brushColors` | 8 色 | 涂鸦/文字色板 |
| `brushWidths` | `[8, 16, 28]` | 笔刷粗细档位(px) |
| `mosaicBlockSize` | `24` | 马赛克块边长(px),越大越糊 |
| `textShadow` | `true` | 文字是否加对比阴影 |
| `stickerProvider` | `null` | 提供贴纸 `Uri`;为 `null` 时隐藏贴纸工具 |
| `output` | JPEG q95 | 输出格式/质量/目录(默认 cacheDir) |
| `theme` | 蓝 | 工具栏配色 |

### 结果

`ImageEditorResult` 为 `Success(uri)`、`Cancelled` 或 `Failed(cause)`。
输出 `Uri` 是配置目录(或 `cacheDir`)下的本地文件,导出图片**像素方向已转正**(无 EXIF 旋转标记)。

## 已知限制

- **贴纸由宿主提供** —— 模块不内置素材(经 `StickerProvider` 注入)。
- **文字样式有限** —— 仅颜色 + 自动对比阴影(暂无自定义字体/完整描边)。

## 路线图

- [x] 示例 app
- [x] JitPack 发布
- [x] 单元测试(EXIF、马赛克坐标映射、撤销路由)
- [x] 文字阴影(可读性)
- [x] 马赛克点按 + 重做
- [~] Maven Central 发布 —— 配置就绪;需 Sonatype 命名空间 + GPG 密钥才能发布(见 [PUBLISHING.md](./PUBLISHING.md))

## 许可证

[MIT](./LICENSE)。

基于 [CanHub/Android-Image-Cropper](https://github.com/CanHub/Android-Image-Cropper)
(Apache-2.0) 与 [burhanrashid52/PhotoEditor](https://github.com/burhanrashid52/PhotoEditor)
(MIT) 构建。马赛克笔刷为原创实现。

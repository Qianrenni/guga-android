# Reading - Android 客户端

[![License](https://img.shields.io/badge/license-ISC-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0+-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/android-24+-brightgreen.svg)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/jetpack_compose-latest-teal.svg)](https://developer.android.com/jetpack/compose)

GUGA Reading 的 **Android 原生客户端**，基于 Kotlin 和 Jetpack Compose 构建，提供流畅的移动阅读体验。

## 📱 应用概述

Reading Android 是 GUGA Reading 在线阅读平台的移动端应用，为用户提供便捷的书籍阅读、收藏和管理功能。应用采用现代化的 Android 开发技术栈，支持离线阅读、多设备同步等特性。

### ✨ 核心特性

- **📖 优质阅读体验**: 流畅的翻页动画、自定义字体和主题
- **🌙 夜间模式**: 护眼的深色主题支持
- **💾 离线阅读**: 缓存章节内容，无网络也能阅读
- **🔄 进度同步**: 与 Web 端实时同步阅读进度
- **🔍 智能搜索**: 快速查找书籍和章节
- **📚 书架管理**: 个性化书架，追更提醒
- **💬 互动功能**: 评论、点赞、收藏
- **⚡ 高性能**: Jetpack Compose 声明式 UI，流畅动画

## 🏗️ 技术架构

```
┌──────────────────────────────────────┐
│        Android App (Kotlin)          │
├──────────────────────────────────────┤
│  UI Layer (Jetpack Compose)          │
│  ┌──────────┐ ┌──────────┐          │
│  │  Views   │ │Components│          │
│  └──────────┘ └──────────┘          │
├──────────────────────────────────────┤
│  ViewModel Layer                     │
│  ┌──────────────────────────┐       │
│  │   State Management       │       │
│  └──────────────────────────┘       │
├──────────────────────────────────────┤
│  Data Layer                          │
│  ┌──────────┐ ┌──────────┐          │
│  │  Ktor    │ │DataStore │          │
│  │ Client   │ │ (Local)  │          │
│  └──────────┘ └──────────┘          │
├──────────────────────────────────────┤
│         Network (HTTPS)              │
└──────────────┬───────────────────────┘
               │
               ▼
    ┌─────────────────────┐
    │   Backend API       │
    │   (FastAPI)         │
    └─────────────────────┘
```

### 架构说明

- **UI Layer**: 使用 Jetpack Compose 构建声明式 UI
- **ViewModel Layer**: 管理 UI 状态和业务逻辑
- **Data Layer**: 网络请求（Ktor）和本地存储（DataStore）
- **Navigation**: Navigation Compose 实现页面路由

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **语言** | Kotlin | 现代 Android 开发语言 |
| **最低 SDK** | API 24 (Android 7.0) | 兼容大多数设备 |
| **目标 SDK** | API 36 (Android 15) | 最新 Android 特性 |
| **UI 框架** | Jetpack Compose | 声明式 UI  toolkit |
| **Material Design** | Material3 | 现代化设计系统 |
| **网络** | Ktor Client | 轻量级 HTTP 客户端 |
| **序列化** | kotlinx.serialization | JSON 序列化 |
| **图片加载** | Coil 2.6.0 | 高效的图片加载库 |
| **导航** | Navigation Compose 2.9.8 | 页面导航 |
| **本地存储** | DataStore Preferences | 类型安全的键值存储 |
| **安全存储** | Security Crypto | 加密敏感数据 |
| **异步** | Kotlin Coroutines | 协程支持 |
| **依赖注入** | Hilt (可选) | 依赖注入框架 |
| **构建工具** | Gradle Kotlin DSL | 类型安全的构建配置 |

## 📁 项目结构

```
reading/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/qianrenni/reading/
│   │   │   │   ├── MainActivity.kt          # 主 Activity
│   │   │   │   ├── Router.kt                # 路由配置
│   │   │   │   ├── common/                  # 通用组件
│   │   │   │   ├── components/              # UI 组件
│   │   │   │   ├── data/                    # 数据层
│   │   │   │   │   ├── api/                 # API 接口
│   │   │   │   │   ├── model/               # 数据模型
│   │   │   │   │   └── repository/          # 数据仓库
│   │   │   │   ├── ui/                      # UI 主题
│   │   │   │   ├── util/                    # 工具类
│   │   │   │   ├── viewmodels/              # ViewModel
│   │   │   │   └── views/                   # 页面视图
│   │   │   ├── res/                         # 资源文件
│   │   │   └── AndroidManifest.xml          # 应用清单
│   │   ├── test/                            # 单元测试
│   │   └── androidTest/                     # 仪器化测试
│   └── build.gradle.kts                     # 模块构建配置
├── gradle/
│   └── libs.versions.toml                   # 依赖版本管理
├── build.gradle.kts                         # 项目构建配置
├── settings.gradle.kts                      # 项目设置
├── local.properties                         # 本地配置（签名等）
└── README.md                                # 项目说明
```

## 🚀 快速开始

### 环境要求

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **JDK**: 11 或更高版本
- **Android SDK**: 
  - Compile SDK: 36
  - Min SDK: 24
  - Target SDK: 36
- **Gradle**: 8.0+
- **Kotlin**: 2.0+

### 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd reading
```

#### 2. 配置后端 API 地址

在项目中找到网络配置文件（通常在 `data/api` 目录），修改后端 API 基础 URL：

```kotlin
// 示例：ApiConfig.kt
object ApiConfig {
    const val BASE_URL = "http://your-backend-server:8000"
}
```

#### 3. 配置签名（可选，用于发布版本）

在 `local.properties` 文件中添加签名配置：

```properties
storeFile=/path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

> ⚠️ **注意**: `local.properties` 已加入 `.gitignore`，不会提交到版本控制系统。

#### 4. 使用 Android Studio 打开项目

1. 打开 Android Studio
2. 选择 **File** → **Open**
3. 选择 `reading` 目录
4. 等待 Gradle 同步完成（首次可能需要几分钟下载依赖）

#### 5. 运行应用

**方法一：通过 Android Studio**
- 连接 Android 设备或启动模拟器
- 点击工具栏的 **Run** 按钮（绿色三角形）
- 或按快捷键 `Shift + F10`

**方法二：命令行构建**

```bash
# macOS/Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK 文件位置：`app/build/outputs/apk/debug/app-debug.apk`

#### 6. 安装到设备

```bash
# 安装调试版本
./gradlew installDebug

# 或直接使用 adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📖 主要功能

### 1. 用户认证

- 手机号/邮箱注册和登录
- JWT Token 自动管理
- 个人资料编辑
- 密码重置

### 2. 书籍浏览

- 分类浏览
- 标签筛选
- 全文搜索
- 热门推荐
- 新书上架

### 3. 阅读体验

- 章节列表展示
- 流畅的翻页/滑动阅读
- 字体大小调节
- 背景主题切换（白天/夜间）
- 阅读进度自动保存
- 断点续读

### 4. 书架管理

- 添加/移除收藏
- 追更提醒
- 阅读统计
- 最近阅读

### 5. 互动功能

- 章节评论
- 点赞支持
- 分享书籍

### 6. 离线支持

- 章节内容缓存
- 离线阅读模式
- 缓存管理

## 🔧 开发指南

### 构建变体

项目支持以下构建变体：

- **debug**: 调试版本，启用日志和调试工具
- **release**: 发布版本，优化性能，启用代码混淆

### 常用 Gradle 命令

```bash
# 清理构建
./gradlew clean

# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行仪器化测试
./gradlew connectedAndroidTest

# 生成测试覆盖率报告
./gradlew jacocoTestReport

# 检查代码问题
./gradlew lint

# 格式化代码
./gradlew ktlintFormat
```

### 代码规范

- 遵循 [Kotlin 官方代码风格](https://kotlinlang.org/docs/coding-conventions.html)
- 使用有意义的变量和函数命名
- 添加必要的注释和文档
- 保持函数职责单一
- 使用 Compose 最佳实践

### 调试技巧

1. **启用日志**: 在 debug 版本中，应用会输出详细日志
2. **Layout Inspector**: 使用 Android Studio 的 Layout Inspector 检查 Compose UI
3. **Network Profiler**: 监控网络请求和响应
4. **Database Inspector**: 查看 DataStore 数据

## 🧪 测试

### 单元测试

```bash
./gradlew test
```

测试报告位置：`app/build/reports/tests/testDebugUnitTest/index.html`

### 仪器化测试

```bash
# 需要连接设备或模拟器
./gradlew connectedAndroidTest
```

### UI 测试

使用 Compose Testing API 进行 UI 测试：

```kotlin
@Test
fun testBookListDisplay() {
    composeTestRule.setContent {
        BookListScreen()
    }
    
    composeTestRule.onNodeWithText("Book Title").assertIsDisplayed()
}
```

## 📊 性能优化

### 已实施的优化

- **🖼️ 图片优化**: Coil 自动缓存和压缩图片
- **📡 网络优化**: Ktor 连接池、请求合并、超时控制
- **💾 本地缓存**: DataStore 持久化用户偏好和阅读进度
- **🔋 电量优化**: 协程结构化并发，避免内存泄漏
- **🎨 UI 优化**: Compose 重组优化、懒加载列表
- **📦 APK 大小**: ProGuard/R8 代码 shrinker

### 性能指标

- 冷启动时间：< 2 秒
- 首屏渲染：< 500ms
- 列表滚动：60 FPS
- 内存占用：< 100MB（典型场景）

## 🔒 安全特性

- **🔐 HTTPS**: 所有网络通信使用 HTTPS
- **🔑 Token 管理**: JWT Token 安全存储和自动刷新
- **🛡️ 数据加密**: 敏感数据使用 Security Crypto 加密
- **🚫 权限最小化**: 仅请求必要的系统权限
- **📝 输入验证**: 防止注入攻击

## 📱 兼容性

### 支持的 Android 版本

- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 15 (API 36)
- **推荐版本**: Android 10+ (API 29+)

### 屏幕适配

- 支持手机和平板
- 自适应布局
- 横竖屏切换支持

### 架构支持

- armeabi-v7a
- arm64-v8a
- x86
- x86_64

## 🐛 常见问题

### 1. Gradle 同步失败

**解决方案**:
- 检查网络连接
- 清除 Gradle 缓存：`./gradlew clean`
- 更新 Android Studio 到最新版本
- 检查 JDK 版本是否正确

### 2. 无法连接后端 API

**解决方案**:
- 检查 `ApiConfig.BASE_URL` 配置
- 确认后端服务正在运行
- 检查网络连接和防火墙设置
- 查看 Logcat 中的网络错误日志

### 3. 应用崩溃

**解决方案**:
- 查看 Logcat 日志
- 检查设备 Android 版本是否满足最低要求
- 确保有足够的存储空间
- 尝试清除应用数据后重新安装

### 4. 图片加载失败

**解决方案**:
- 检查网络连接
- 确认图片 URL 可访问
- 清除 Coil 缓存：设置 → 清除缓存

## 📦 发布流程

### 1. 生成签名密钥

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

### 2. 配置签名

在 `local.properties` 中添加签名信息（见上文配置步骤）。

### 3. 构建发布版本

```bash
./gradlew assembleRelease
```

APK 位置：`app/build/outputs/apk/release/app-release.apk`

### 4. 测试发布版本

在真实设备上全面测试后再发布。

### 5. 上传到应用商店

- Google Play Store
- 华为应用市场
- 小米应用商店
- 其他国内应用市场

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 贡献流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循 Kotlin 官方代码风格
- 使用 Compose 最佳实践
- 添加必要的注释和文档
- 确保测试通过
- 保持提交信息清晰

## 📄 许可证

ISC License

## 👥 联系方式

- **作者**: qianrenni
- **邮箱**: 2112183503@qq.com
- **项目主页**: [GUGA Reading](https://github.com/Qianrenni/guga_reading)

## 🔗 相关链接

- [GUGA Reading Web 版](http://49.235.107.221)
- [作者端](http://49.235.107.221/author/#)
- [管理端](http://49.235.107.221/admin/#)
- [后端 API 文档](http://localhost:8000/docs)
- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)

---

**注意**: 本项目仅供学习交流使用，请勿用于商业用途。

# MSDK AiHelp Android SDK - 用户手册

## 目录

- [快速开始](#快速开始)
- [初始化配置](#初始化配置)
- [用户信息](#用户信息)
- [打开客服聊天](#打开客服聊天)
- [打开帮助中心](#打开帮助中心)
- [主题定制](#主题定制)
- [事件监听](#事件监听)
- [未读消息](#未读消息)
- [ProGuard 配置](#proguard-配置)
- [常见问题](#常见问题)

---

## 快速开始

### 1. 依赖集成

在宿主 App 的 `build.gradle` 中添加：

```groovy
dependencies {
    implementation files('libs/msdk_aihelp.aar')
    // SDK 内部依赖（如宿主已有，无需重复添加）
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
}
```

### 2. 初始化

在 `Application.onCreate()` 中调用：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        AiHelpConfig config = new AiHelpConfig.Builder()
            .setDomain("https://cs.yourgame.com")
            .setAppId("game_001")
            .setAppSecret("your_secret_key")
            .setThemeColor(0xFF1A73E8)   // 可选，默认蓝色
            .setLogoResId(R.drawable.logo) // 可选
            .build();
        
        MSDKAiHelp.init(this, config);
    }
}
```

### 3. 使用

```java
// 打开客服聊天
MSDKAiHelp.openChat();

// 打开帮助中心
MSDKAiHelp.openFAQ();
```

---

## 初始化配置

### AiHelpConfig

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| domain | String | 是 | 后端服务地址，如 `https://cs.yourgame.com` |
| appId | String | 是 | 应用唯一标识 |
| appSecret | String | 是 | 应用鉴权密钥 |
| themeColor | int | 否 | 主题色（ARGB），默认 `0xFF1A73E8` |
| logoResId | int | 否 | 顶部 Logo 资源 ID |

```java
AiHelpConfig config = new AiHelpConfig.Builder()
    .setDomain("https://cs.yourgame.com")
    .setAppId("game_001")
    .setAppSecret("secret")
    .setThemeColor(0xFF1A73E8)
    .build();
```

---

## 用户信息

### 设置用户

用户登录后调用，客服端可看到用户信息：

```java
UserInfo user = new UserInfo.Builder()
    .setUserId("player_12345")      // 游戏用户ID
    .setUserName("PlayerName")       // 用户昵称
    .setServerId("server_01")        // 服务器ID
    .addCustomData("level", "50")    // 自定义字段
    .addCustomData("vip", "3")
    .build();

MSDKAiHelp.setUser(user);
```

### 清除用户

用户登出时调用：

```java
MSDKAiHelp.clearUser();
```

---

## 打开客服聊天

### 基础用法

```java
MSDKAiHelp.openChat();
```

### 自定义欢迎语

```java
ChatConfig chatConfig = new ChatConfig.Builder()
    .setWelcomeMessage("欢迎来到客服中心，请问有什么可以帮您？")
    .build();

MSDKAiHelp.openChat(chatConfig);
```

### 聊天功能

- 发送文本消息
- 发送图片（支持拍照和相册选择）
- 查看历史记录（本地持久化）
- AI 自动回复 + 人工客服转接（后端控制）
- 图片点击全屏查看

---

## 打开帮助中心

### 基础用法

```java
MSDKAiHelp.openFAQ();
```

### 直接打开指定分类

```java
FAQConfig faqConfig = new FAQConfig.Builder()
    .setSectionId("recharge")        // 直接打开"充值相关"分类
    .setShowContactUs(true)          // 显示"联系客服"按钮
    .build();

MSDKAiHelp.openFAQ(faqConfig);
```

### 帮助中心功能

- FAQ 分类列表
- 分类下问题列表
- 问题详情（HTML 富文本）
- 关键词搜索（防抖 300ms）
- 有帮助/没帮助反馈
- 底部联系客服入口

---

## 主题定制

### 运行时切换主题色

```java
// 切换为红色主题
MSDKAiHelp.setThemeColor(0xFFE53935);

// 切换为绿色主题
MSDKAiHelp.setThemeColor(0xFF43A047);
```

主题色影响范围：
- 聊天界面 Toolbar
- 发送按钮
- 帮助中心 Toolbar
- "联系客服"按钮

---

## 事件监听

```java
MSDKAiHelp.setEventListener(new AiHelpEventListener() {
    @Override
    public void onInitialized(boolean success, String message) {
        // SDK 初始化完成
    }

    @Override
    public void onSessionOpened() {
        // 聊天会话已建立
    }

    @Override
    public void onSessionClosed() {
        // 聊天会话已关闭
    }

    @Override
    public void onUnreadCountChanged(int count) {
        // 未读消息数变化（可更新游戏内红点）
    }
});
```

---

## 未读消息

### 获取未读数

```java
MSDKAiHelp.getUnreadCount(count -> {
    // count: 未读消息数量
    // 可在此更新游戏内客服入口的红点提示
});
```

---

## ProGuard 配置

SDK 已内置 `proguard-rules.pro`，宿主 App 无需额外配置。如使用自定义 ProGuard 规则，请保留：

```proguard
-keep class com.msdk.aihelp.** { *; }
```

---

## 常见问题

### Q: 最低支持 Android 版本？
**A:** API 23 (Android 6.0)

### Q: SDK 体积多大？
**A:** AAR 文件小于 500KB（不含 OkHttp/Glide 等宿主通常已有的依赖）

### Q: 聊天记录会丢失吗？
**A:** 不会。消息使用 Room 本地持久化，App 重启后自动加载历史记录。

### Q: 支持多语言吗？
**A:** 支持。通过 `MSDKAiHelp.setLanguage("zh-CN")` 设置语言，后端根据语言返回对应内容。

### Q: WebSocket 断线怎么办？
**A:** SDK 内置自动重连机制，指数退避：1s → 2s → 4s → 8s → 最大 30s。

# MSDK AiHelp Android SDK - Design Spec

## Overview

公司级 AiHelp 平替方案的 Android 客户端 SDK，服务于公司内部游戏产品，提供 AI 聊天机器人、人工客服对话和 FAQ 帮助中心三大核心功能。

## Goals

- 为公司游戏产品提供统一的应用内客服能力
- 以 AAR/SDK 形式交付，集成简单（一行依赖 + 几行初始化）
- 第一期聚焦核心客服链路：聊天（AI + 人工）+ FAQ 自助

## Non-Goals (Phase 1)

- 工单系统
- 智能表单
- 应用内消息推送 / 运营公告
- 多渠道接入（Web、iOS 等）
- UI 高度自定义（仅支持主题色 + Logo）

## Technical Decisions

| 维度 | 决策 | 理由 |
|------|------|------|
| 架构 | 单模块 SDK | 第一期功能聚焦，后续可平滑拆分 |
| 语言 | Java | 兼容老项目 |
| 最低版本 | API 23 (Android 6.0) | 平衡兼容性与开发成本 |
| 实时通信 | WebSocket (OkHttp) | 聊天实时性要求高 |
| AI 路由 | 后端统一接口 | 客户端无需感知 AI/人工 |
| FAQ 数据 | 后端下发 | 管理后台配置，客户端拉取展示 |
| 消息类型 | 文本 + 图片 | 支持截图反馈 |

---

## Architecture

```
┌─────────────────────────────────────────────┐
│              宿主游戏 App                      │
├─────────────────────────────────────────────┤
│            SDK 公开 API 层                     │
│   MSDKAiHelp.init() / .openChat() / .openFAQ() │
├─────────────────────────────────────────────┤
│              UI 层 (Activity/Fragment)         │
│   ChatActivity │ FAQActivity │ ImageViewer    │
├─────────────────────────────────────────────┤
│              业务逻辑层                        │
│   ChatManager │ FAQManager │ ConfigManager    │
├─────────────────────────────────────────────┤
│              数据层                            │
│   WebSocketClient │ HttpClient │ LocalCache   │
├─────────────────────────────────────────────┤
│              基础设施层                        │
│   Logger │ ImageLoader │ ThreadPool           │
└─────────────────────────────────────────────┘
```

### Layer Responsibilities

- **公开 API 层**：对外暴露的唯一入口，宿主 App 只与此层交互
- **UI 层**：固定样式的 Activity/Fragment，通过配置支持主题色
- **业务逻辑层**：聊天会话管理、FAQ 数据处理、配置管理
- **数据层**：WebSocket 通信、HTTP 请求、本地消息缓存
- **基础设施层**：日志、图片加载、线程管理等通用能力

SDK 以独立 Activity 栈运行，与宿主 App 隔离。

---

## Public API

```java
public class MSDKAiHelp {

    // 初始化（Application.onCreate 中调用）
    static void init(Context context, AiHelpConfig config);

    // 打开聊天界面
    static void openChat();
    static void openChat(ChatConfig chatConfig);

    // 打开 FAQ 帮助中心
    static void openFAQ();
    static void openFAQ(FAQConfig faqConfig);

    // 设置用户信息（登录后调用）
    static void setUser(UserInfo userInfo);

    // 清除用户信息（登出时调用）
    static void clearUser();

    // 获取未读消息数
    static void getUnreadCount(UnreadCountCallback callback);

    // 注册事件监听
    static void setEventListener(AiHelpEventListener listener);

    // 更新语言
    static void setLanguage(String language);
}
```

### Configuration Objects

```java
public class AiHelpConfig {
    String domain;       // 后端服务地址
    String appId;        // 应用标识
    String appSecret;    // 鉴权密钥
    int themeColor;      // 主题色
    int logoResId;       // Logo 资源 ID
}

public class ChatConfig {
    String welcomeMessage;  // 自定义欢迎语（为空则使用后端配置）
}

public class FAQConfig {
    String sectionId;       // 直接打开某个分类（为空则打开分类列表）
    boolean showContactUs;  // 是否显示"联系客服"按钮，默认 true
}

public class UserInfo {
    String userId;       // 游戏用户 ID
    String userName;     // 用户昵称
    String serverId;     // 游戏服务器 ID
    Map<String, String> customData; // 自定义字段（等级、VIP等）
}
```

---

## Chat Module

### Session Flow

1. 用户点击"联系客服"
2. 打开 ChatActivity
3. WebSocket 连接后端
4. 后端自动分配会话（AI/人工由后端决定）
5. 用户与客服实时对话

### Message Types

| 类型 | 方向 | 说明 |
|------|------|------|
| TEXT | 双向 | 纯文本消息 |
| IMAGE | 双向 | 图片消息 |
| SYSTEM | 下行 | 系统提示（排队中、会话结束等） |
| LOADING | 本地 | AI 正在输入的等待动画 |

### WebSocket Protocol (Client Perspective)

```json
// 发送消息
{"type": "send", "msgType": "text", "content": "我充值没到账"}
{"type": "send", "msgType": "image", "content": "<url>"}

// 接收消息
{"type": "receive", "msgType": "text", "content": "...", "sender": "agent"}
{"type": "receive", "msgType": "system", "content": "正在为您转接人工客服..."}

// 连接控制
{"type": "connect", "sessionId": "xxx", "token": "xxx"}
{"type": "heartbeat"}
{"type": "close", "reason": "session_end"}
```

### Reconnection Strategy

- 断开后自动重连，指数退避：1s → 2s → 4s → 8s → 最大 30s
- 重连成功后通过 sessionId 恢复会话，后端下发未读消息
- 发送失败的消息本地标记为"发送中"，重连后自动重发
- 消息本地持久化到 SQLite，重新打开可查看历史

### Image Sending Flow

1. 用户选择/拍摄图片
2. 本地压缩（最大 1280px，质量 80%）
3. HTTP 上传到文件服务获取 URL
4. 通过 WebSocket 发送图片消息（含 URL）

---

## FAQ Module

### Page Structure

- **分类列表页**：展示所有 FAQ 分类，顶部有搜索框
- **问题列表页**：展示某分类下的问题列表
- **问题详情页**：展示答案（HTML 富文本），底部有反馈按钮

### Data Model

```java
public class FAQSection {
    String sectionId;
    String title;
    int sortOrder;
    List<FAQItem> items;
}

public class FAQItem {
    String faqId;
    String question;
    String answer;       // HTML 富文本
    int sortOrder;
}
```

### Data Loading Strategy

- 打开帮助中心时通过 HTTP 拉取分类列表
- 点击分类时加载该分类下的问题列表
- 点击具体问题时加载答案详情
- 本地缓存：分类列表 30 分钟，答案详情 1 小时
- 缓存失效后静默刷新

### Search

- 用户输入关键词，调用后端搜索接口
- 防抖处理：停止输入 300ms 后发起请求
- 展示匹配的问题列表

### Chat Integration

- 每个页面底部都有"联系客服"入口
- 从 FAQ 进入聊天时，自动携带上下文（用户正在查看的问题）

---

## Project Structure

```
MSDK_AiHelp/
├── sdk/                          # SDK 模块 (AAR 输出)
│   └── src/main/java/com/msdk/aihelp/
│       ├── MSDKAiHelp.java
│       ├── config/
│       │   ├── AiHelpConfig.java
│       │   ├── ChatConfig.java
│       │   └── FAQConfig.java
│       ├── model/
│       │   ├── Message.java
│       │   ├── UserInfo.java
│       │   ├── FAQSection.java
│       │   └── FAQItem.java
│       ├── chat/
│       │   ├── ChatActivity.java
│       │   ├── ChatFragment.java
│       │   ├── ChatManager.java
│       │   └── adapter/
│       ├── faq/
│       │   ├── FAQActivity.java
│       │   ├── FAQListFragment.java
│       │   ├── FAQDetailFragment.java
│       │   └── FAQManager.java
│       ├── network/
│       │   ├── WebSocketClient.java
│       │   ├── HttpClient.java
│       │   └── ApiService.java
│       ├── storage/
│       │   ├── MessageDatabase.java
│       │   └── CacheManager.java
│       ├── ui/
│       │   ├── ImagePickerUtil.java
│       │   ├── ImageViewerActivity.java
│       │   └── theme/
│       └── util/
│           ├── Logger.java
│           ├── ImageCompressor.java
│           └── ThreadUtil.java
├── demo/                          # Demo App 模块
│   └── src/main/java/com/msdk/aihelp/demo/
│       ├── DemoApplication.java
│       └── MainActivity.java
├── build.gradle
└── settings.gradle
```

## Dependencies

| 依赖 | 用途 | 说明 |
|------|------|------|
| OkHttp | HTTP + WebSocket | 游戏 App 普遍已引入 |
| Gson | JSON 序列化 | 轻量 |
| Glide | 图片加载 | 聊天图片渲染 |
| SQLite (原生) | 消息持久化 | 无额外依赖 |

## Theme Mechanism

```java
AiHelpConfig config = new AiHelpConfig.Builder()
    .setDomain("https://cs.yourgame.com")
    .setAppId("game_001")
    .setAppSecret("xxx")
    .setThemeColor(0xFF1A73E8)
    .setLogoResId(R.drawable.logo)
    .build();
```

SDK 内部通过 `ThemeManager` 动态设置颜色，不使用 Android Theme/Style 系统（避免与宿主冲突）。

## Thread Model

- WebSocket 收发：独立子线程
- HTTP 请求：OkHttp 线程池
- 数据库操作：单一后台线程
- UI 更新：主线程（Handler 切换）

## Size & ProGuard

- 预估 AAR 体积 < 500KB（不含 OkHttp/Glide）
- SDK 提供 proguard 规则文件，宿主集成时自动应用

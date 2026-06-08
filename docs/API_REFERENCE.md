# MSDK AiHelp Android SDK - API 参考文档

## MSDKAiHelp

SDK 主入口类，所有方法均为静态调用。

> **线程安全：** 所有回调均在**主线程**执行。
>
> **调用顺序：** 除 `init()` 和 `setEventListener()` 外，所有方法必须在 `init()` 调用后才能使用，否则抛出 `IllegalStateException`。

### init

```java
public static void init(Context context, AiHelpConfig config)
```

初始化 SDK，必须在 `Application.onCreate()` 中调用一次。

| 参数 | 说明 |
|------|------|
| context | Application 上下文 |
| config | 初始化配置 |

**异常：**
- `IllegalArgumentException` — context 或 config 为 null

**备注：** 必须在 `Application.onCreate()` 中调用。重复调用会以新配置重新初始化 SDK。

---

### openChat

```java
public static void openChat()
public static void openChat(ChatConfig chatConfig)
```

打开客服聊天界面。

| 参数 | 必填 | 说明 |
|------|------|------|
| chatConfig | 否 | 聊天配置，可为 null |

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

### openFAQ

```java
public static void openFAQ()
public static void openFAQ(FAQConfig faqConfig)
```

打开帮助中心。

| 参数 | 必填 | 说明 |
|------|------|------|
| faqConfig | 否 | FAQ 配置，可为 null |

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

### setUser

```java
public static void setUser(UserInfo userInfo)
```

设置用户信息，客服端可查看。建议在用户登录后调用。

| 参数 | 说明 |
|------|------|
| userInfo | 用户身份信息 |

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

### clearUser

```java
public static void clearUser()
```

清除用户信息，用户登出时调用。

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

### getUnreadCount

```java
public static void getUnreadCount(UnreadCountCallback callback)
```

获取未读消息数。

| 参数 | 说明 |
|------|------|
| callback | 结果回调 |

**异常：**
- `IllegalStateException` — 未调用 `init()`

**备注：** 本地内存未读数 > 0 时直接通过回调返回，不发起网络请求。

**示例：**
```java
MSDKAiHelp.getUnreadCount(count -> {
    Log.d("Unread", "count=" + count);
});
```

---

### setEventListener

```java
public static void setEventListener(AiHelpEventListener listener)
```

注册 SDK 事件监听器。

| 参数 | 说明 |
|------|------|
| listener | 事件监听器，可为 null（取消监听） |

**备注：** 传入 `null` 可取消监听。

---

### setLanguage

```java
public static void setLanguage(String language)
```

设置语言，影响后端返回的内容语言。

| 参数 | 示例 |
|------|------|
| language | `"zh-CN"`, `"en"`, `"ja"` 等 |

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

### setThemeColor

```java
public static void setThemeColor(int color)
```

运行时切换主题色。

| 参数 | 示例 |
|------|------|
| color | `0xFF1A73E8` (蓝色), `0xFFE53935` (红色) |

**异常：**
- `IllegalStateException` — 未调用 `init()`

---

## AiHelpConfig

初始化配置类，使用 Builder 模式构建。

### Builder 方法

| 方法 | 类型 | 必填 | 说明 |
|------|------|------|------|
| setDomain(String) | Builder | 是 | 后端服务域名，须包含协议，如 `https://cs.yourgame.com` |
| setAppId(String) | Builder | 是 | 应用标识 |
| setAppSecret(String) | Builder | 是 | 应用密钥 |
| setThemeColor(int) | Builder | 否 | 主题色（ARGB），默认 `0xFF1A73E8`（蓝色） |
| setLogoResId(int) | Builder | 否 | Logo 资源 ID，默认 `0`（不显示） |
| build() | AiHelpConfig | - | 构建配置对象，校验必填字段 |

**`build()` 异常：**
- `IllegalArgumentException` — `domain` 为 null 或空字符串
- `IllegalArgumentException` — `appId` 为 null 或空字符串
- `IllegalArgumentException` — `appSecret` 为 null 或空字符串

---

## ChatConfig

聊天配置类。

### Builder 方法

| 方法 | 类型 | 必填 | 说明 |
|------|------|------|------|
| setWelcomeMessage(String) | Builder | 否 | 自定义欢迎语 |
| build() | ChatConfig | - | 构建配置对象 |

---

## FAQConfig

帮助中心配置类。

### Builder 方法

| 方法 | 类型 | 必填 | 说明 |
|------|------|------|------|
| setSectionId(String) | Builder | 否 | 直接打开指定分类 |
| setShowContactUs(boolean) | Builder | 否 | 是否显示联系客服按钮，默认 true |
| build() | FAQConfig | - | 构建配置对象 |

---

## UserInfo

用户信息类。

### Builder 方法

| 方法 | 类型 | 必填 | 说明 |
|------|------|------|------|
| setUserId(String) | Builder | 是 | 游戏用户 ID |
| setUserName(String) | Builder | 否 | 用户昵称 |
| setServerId(String) | Builder | 否 | 服务器 ID |
| addCustomData(String, String) | Builder | 否 | 自定义字段（键值对），默认空 Map |
| build() | UserInfo | - | 构建用户对象，校验 userId |

**`build()` 异常：**
- `IllegalArgumentException` — `userId` 为 null 或空字符串

---

## AiHelpEventListener

事件监听接口。

```java
public interface AiHelpEventListener {
    void onInitialized(boolean success, String message);
    void onSessionOpened();
    void onSessionClosed();
    void onUnreadCountChanged(int count);
}
```

| 方法 | 触发时机 |
|------|----------|
| onInitialized | SDK 初始化完成 |
| onSessionOpened | WebSocket 会话建立 |
| onSessionClosed | WebSocket 会话关闭 |
| onUnreadCountChanged | 未读消息数变化 |

---

## UnreadCountCallback

未读数回调接口。

```java
public interface UnreadCountCallback {
    void onResult(int count);
}
```

---

## 后端接口规范

### HTTP 请求头

所有 HTTP 请求自动携带以下头：

| 头 | 来源 |
|----|------|
| X-App-Id | AiHelpConfig.appId |
| X-App-Secret | AiHelpConfig.appSecret |
| X-User-Id | UserInfo.userId（如已设置） |
| X-Language | setLanguage() 的值（如已设置） |

### WebSocket 连接

```
URL: wss://{domain}/ws/chat
Headers: X-App-Id, X-App-Secret, X-Token
```

### REST API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/faq/sections` | FAQ 分类列表 |
| GET | `/api/v1/faq/sections/{id}/items` | 分类下问题 |
| GET | `/api/v1/faq/items/{id}` | 问题详情 |
| GET | `/api/v1/faq/search?q={keyword}` | 搜索 |
| POST | `/api/v1/faq/items/{id}/feedback` | 反馈 |
| POST | `/api/v1/upload` | 图片上传 |
| GET | `/api/v1/chat/unread` | 未读数 |

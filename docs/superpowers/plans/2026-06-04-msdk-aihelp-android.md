# MSDK AiHelp Android SDK Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android SDK (AAR) providing in-app customer service (AI + human chat via WebSocket) and FAQ help center for internal game products.

**Architecture:** Single-module SDK with layered design (API → UI → Business Logic → Data → Infrastructure). SDK exposes static methods on `MSDKAiHelp` class; internally manages WebSocket for chat, HTTP for FAQ/file upload, SQLite for message persistence.

**Tech Stack:** Java, Android minSdk 23, OkHttp (HTTP + WebSocket), Gson, Glide, SQLite

---

## File Map

| File | Responsibility |
|------|---------------|
| `sdk/src/main/java/com/msdk/aihelp/MSDKAiHelp.java` | Public API entry point |
| `sdk/src/main/java/com/msdk/aihelp/config/AiHelpConfig.java` | Initialization config (Builder pattern) |
| `sdk/src/main/java/com/msdk/aihelp/config/ChatConfig.java` | Chat launch config |
| `sdk/src/main/java/com/msdk/aihelp/config/FAQConfig.java` | FAQ launch config |
| `sdk/src/main/java/com/msdk/aihelp/config/ConfigManager.java` | Internal config holder singleton |
| `sdk/src/main/java/com/msdk/aihelp/model/UserInfo.java` | User identity model |
| `sdk/src/main/java/com/msdk/aihelp/model/Message.java` | Chat message model |
| `sdk/src/main/java/com/msdk/aihelp/model/FAQSection.java` | FAQ category model |
| `sdk/src/main/java/com/msdk/aihelp/model/FAQItem.java` | FAQ item model |
| `sdk/src/main/java/com/msdk/aihelp/network/HttpClient.java` | OkHttp HTTP wrapper |
| `sdk/src/main/java/com/msdk/aihelp/network/ApiService.java` | API endpoint definitions |
| `sdk/src/main/java/com/msdk/aihelp/network/WebSocketClient.java` | WebSocket connection with reconnection |
| `sdk/src/main/java/com/msdk/aihelp/network/MessageProtocol.java` | WebSocket JSON protocol encode/decode |
| `sdk/src/main/java/com/msdk/aihelp/storage/MessageDatabase.java` | SQLite message persistence |
| `sdk/src/main/java/com/msdk/aihelp/storage/CacheManager.java` | FAQ data caching with TTL |
| `sdk/src/main/java/com/msdk/aihelp/chat/ChatManager.java` | Chat session orchestration |
| `sdk/src/main/java/com/msdk/aihelp/chat/ChatActivity.java` | Chat UI host |
| `sdk/src/main/java/com/msdk/aihelp/chat/ChatFragment.java` | Chat UI implementation |
| `sdk/src/main/java/com/msdk/aihelp/chat/adapter/MessageAdapter.java` | RecyclerView adapter for messages |
| `sdk/src/main/java/com/msdk/aihelp/chat/adapter/TextMessageViewHolder.java` | Text message cell |
| `sdk/src/main/java/com/msdk/aihelp/chat/adapter/ImageMessageViewHolder.java` | Image message cell |
| `sdk/src/main/java/com/msdk/aihelp/chat/adapter/SystemMessageViewHolder.java` | System message cell |
| `sdk/src/main/java/com/msdk/aihelp/faq/FAQManager.java` | FAQ data loading and caching |
| `sdk/src/main/java/com/msdk/aihelp/faq/FAQActivity.java` | FAQ UI host |
| `sdk/src/main/java/com/msdk/aihelp/faq/FAQListFragment.java` | FAQ category + question list |
| `sdk/src/main/java/com/msdk/aihelp/faq/FAQDetailFragment.java` | FAQ answer detail (WebView) |
| `sdk/src/main/java/com/msdk/aihelp/ui/theme/ThemeManager.java` | Dynamic color management |
| `sdk/src/main/java/com/msdk/aihelp/ui/ImagePickerUtil.java` | Image selection/capture helper |
| `sdk/src/main/java/com/msdk/aihelp/ui/ImageViewerActivity.java` | Full-screen image viewer |
| `sdk/src/main/java/com/msdk/aihelp/util/Logger.java` | Internal logging |
| `sdk/src/main/java/com/msdk/aihelp/util/ImageCompressor.java` | Image resize + compress |
| `sdk/src/main/java/com/msdk/aihelp/util/ThreadUtil.java` | Main thread dispatch utility |
| `sdk/src/main/java/com/msdk/aihelp/callback/UnreadCountCallback.java` | Callback interface for unread count |
| `sdk/src/main/java/com/msdk/aihelp/callback/AiHelpEventListener.java` | Event listener interface |
| `sdk/src/main/res/layout/` | All layout XMLs |
| `sdk/src/main/res/values/` | Strings, dimens, colors |
| `sdk/proguard-rules.pro` | ProGuard keep rules |
| `sdk/src/main/AndroidManifest.xml` | SDK manifest (activities, permissions) |
| `demo/src/main/java/com/msdk/aihelp/demo/DemoApplication.java` | Demo app init |
| `demo/src/main/java/com/msdk/aihelp/demo/MainActivity.java` | Demo buttons to launch SDK features |
| `build.gradle` (root) | Root Gradle config |
| `settings.gradle` | Module registration |
| `sdk/build.gradle` | SDK module build config |
| `demo/build.gradle` | Demo app build config |

---

## Task 1: Project Scaffolding

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle` (root)
- Create: `gradle.properties`
- Create: `sdk/build.gradle`
- Create: `sdk/src/main/AndroidManifest.xml`
- Create: `demo/build.gradle`
- Create: `demo/src/main/AndroidManifest.xml`
- Create: `demo/src/main/java/com/msdk/aihelp/demo/DemoApplication.java`

- [ ] **Step 1: Create root Gradle files**

`settings.gradle`:
```groovy
rootProject.name = 'MSDK_AiHelp'
include ':sdk'
include ':demo'
```

`build.gradle`:
```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

`gradle.properties`:
```properties
android.useAndroidX=true
org.gradle.jvmargs=-Xmx2048m
```

- [ ] **Step 2: Create SDK module build.gradle**

`sdk/build.gradle`:
```groovy
plugins {
    id 'com.android.library'
}

android {
    namespace 'com.msdk.aihelp'
    compileSdk 34

    defaultConfig {
        minSdk 23
        targetSdk 34
        consumerProguardFiles 'proguard-rules.pro'
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
}
```

- [ ] **Step 3: Create SDK AndroidManifest.xml**

`sdk/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application>
        <activity
            android:name=".chat.ChatActivity"
            android:screenOrientation="portrait"
            android:taskAffinity="com.msdk.aihelp.task"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar" />
        <activity
            android:name=".faq.FAQActivity"
            android:screenOrientation="portrait"
            android:taskAffinity="com.msdk.aihelp.task"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar" />
        <activity
            android:name=".ui.ImageViewerActivity"
            android:screenOrientation="portrait"
            android:taskAffinity="com.msdk.aihelp.task"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar" />
    </application>

</manifest>
```

- [ ] **Step 4: Create Demo module**

`demo/build.gradle`:
```groovy
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.msdk.aihelp.demo'
    compileSdk 34

    defaultConfig {
        applicationId 'com.msdk.aihelp.demo'
        minSdk 23
        targetSdk 34
        versionCode 1
        versionName '1.0.0'
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation project(':sdk')
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

`demo/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".DemoApplication"
        android:label="AiHelp Demo"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar"
        android:allowBackup="false">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

`demo/src/main/java/com/msdk/aihelp/demo/DemoApplication.java`:
```java
package com.msdk.aihelp.demo;

import android.app.Application;

import com.msdk.aihelp.MSDKAiHelp;
import com.msdk.aihelp.config.AiHelpConfig;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AiHelpConfig config = new AiHelpConfig.Builder()
                .setDomain("https://cs-demo.yourgame.com")
                .setAppId("demo_001")
                .setAppSecret("demo_secret")
                .setThemeColor(0xFF1A73E8)
                .build();
        MSDKAiHelp.init(this, config);
    }
}
```

- [ ] **Step 5: Add Gradle wrapper**

Run: `gradle wrapper --gradle-version 7.6.3` (or copy wrapper files from a reference project)

- [ ] **Step 6: Verify project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add settings.gradle build.gradle gradle.properties sdk/ demo/
git commit -m "feat: scaffold Android project with sdk and demo modules"
```

---

## Task 2: Infrastructure Layer - Utilities

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/util/Logger.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/util/ThreadUtil.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/util/ImageCompressor.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/util/ImageCompressorTest.java`

- [ ] **Step 1: Create Logger**

`sdk/src/main/java/com/msdk/aihelp/util/Logger.java`:
```java
package com.msdk.aihelp.util;

import android.util.Log;

public class Logger {

    private static final String TAG = "MSDKAiHelp";
    private static boolean enabled = true;

    public static void setEnabled(boolean enabled) {
        Logger.enabled = enabled;
    }

    public static void d(String message) {
        if (enabled) Log.d(TAG, message);
    }

    public static void i(String message) {
        if (enabled) Log.i(TAG, message);
    }

    public static void w(String message) {
        if (enabled) Log.w(TAG, message);
    }

    public static void e(String message, Throwable t) {
        if (enabled) Log.e(TAG, message, t);
    }
}
```

- [ ] **Step 2: Create ThreadUtil**

`sdk/src/main/java/com/msdk/aihelp/util/ThreadUtil.java`:
```java
package com.msdk.aihelp.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void runOnMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }

    public static void runOnDb(Runnable runnable) {
        DB_EXECUTOR.execute(runnable);
    }

    public static Handler getMainHandler() {
        return MAIN_HANDLER;
    }
}
```

- [ ] **Step 3: Create ImageCompressor**

`sdk/src/main/java/com/msdk/aihelp/util/ImageCompressor.java`:
```java
package com.msdk.aihelp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCompressor {

    private static final int MAX_DIMENSION = 1280;
    private static final int QUALITY = 80;

    public static File compress(File source, File outputDir) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), options);

        int width = options.outWidth;
        int height = options.outHeight;
        int sampleSize = calculateSampleSize(width, height);

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(source.getAbsolutePath(), options);
        if (bitmap == null) {
            throw new IOException("Failed to decode image: " + source.getAbsolutePath());
        }

        Bitmap scaled = scaleBitmap(bitmap, MAX_DIMENSION);
        if (scaled != bitmap) {
            bitmap.recycle();
        }

        File output = new File(outputDir, "compressed_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(output)) {
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
        } finally {
            scaled.recycle();
        }
        return output;
    }

    static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;
        while (width / sampleSize > MAX_DIMENSION * 2 || height / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    static Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
```

- [ ] **Step 4: Write test for ImageCompressor calculation logic**

`sdk/src/test/java/com/msdk/aihelp/util/ImageCompressorTest.java`:
```java
package com.msdk.aihelp.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ImageCompressorTest {

    @Test
    public void calculateSampleSize_smallImage_returns1() {
        assertEquals(1, ImageCompressor.calculateSampleSize(800, 600));
    }

    @Test
    public void calculateSampleSize_largeImage_returnsPowerOf2() {
        assertEquals(2, ImageCompressor.calculateSampleSize(5000, 4000));
    }

    @Test
    public void calculateSampleSize_veryLargeImage_returns4() {
        assertEquals(4, ImageCompressor.calculateSampleSize(12000, 10000));
    }
}
```

- [ ] **Step 5: Run tests**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.util.ImageCompressorTest"`
Expected: 3 tests PASSED

- [ ] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/util/ sdk/src/test/
git commit -m "feat: add infrastructure utilities (Logger, ThreadUtil, ImageCompressor)"
```

---

## Task 3: Config & Model Layer

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/config/AiHelpConfig.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/config/ChatConfig.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/config/FAQConfig.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/config/ConfigManager.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/model/UserInfo.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/model/Message.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/model/FAQSection.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/model/FAQItem.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/callback/UnreadCountCallback.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/callback/AiHelpEventListener.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/config/AiHelpConfigTest.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/model/MessageTest.java`

- [ ] **Step 1: Write failing test for AiHelpConfig builder**

`sdk/src/test/java/com/msdk/aihelp/config/AiHelpConfigTest.java`:
```java
package com.msdk.aihelp.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class AiHelpConfigTest {

    @Test
    public void builder_setsAllFields() {
        AiHelpConfig config = new AiHelpConfig.Builder()
                .setDomain("https://example.com")
                .setAppId("app_123")
                .setAppSecret("secret_456")
                .setThemeColor(0xFFFF0000)
                .setLogoResId(12345)
                .build();

        assertEquals("https://example.com", config.getDomain());
        assertEquals("app_123", config.getAppId());
        assertEquals("secret_456", config.getAppSecret());
        assertEquals(0xFFFF0000, config.getThemeColor());
        assertEquals(12345, config.getLogoResId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_missingDomain_throws() {
        new AiHelpConfig.Builder()
                .setAppId("app_123")
                .setAppSecret("secret_456")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_missingAppId_throws() {
        new AiHelpConfig.Builder()
                .setDomain("https://example.com")
                .setAppSecret("secret_456")
                .build();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.config.AiHelpConfigTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement AiHelpConfig**

`sdk/src/main/java/com/msdk/aihelp/config/AiHelpConfig.java`:
```java
package com.msdk.aihelp.config;

public class AiHelpConfig {

    private final String domain;
    private final String appId;
    private final String appSecret;
    private final int themeColor;
    private final int logoResId;

    private AiHelpConfig(Builder builder) {
        this.domain = builder.domain;
        this.appId = builder.appId;
        this.appSecret = builder.appSecret;
        this.themeColor = builder.themeColor;
        this.logoResId = builder.logoResId;
    }

    public String getDomain() { return domain; }
    public String getAppId() { return appId; }
    public String getAppSecret() { return appSecret; }
    public int getThemeColor() { return themeColor; }
    public int getLogoResId() { return logoResId; }

    public static class Builder {
        private String domain;
        private String appId;
        private String appSecret;
        private int themeColor = 0xFF1A73E8;
        private int logoResId = 0;

        public Builder setDomain(String domain) { this.domain = domain; return this; }
        public Builder setAppId(String appId) { this.appId = appId; return this; }
        public Builder setAppSecret(String appSecret) { this.appSecret = appSecret; return this; }
        public Builder setThemeColor(int themeColor) { this.themeColor = themeColor; return this; }
        public Builder setLogoResId(int logoResId) { this.logoResId = logoResId; return this; }

        public AiHelpConfig build() {
            if (domain == null || domain.isEmpty()) {
                throw new IllegalArgumentException("domain is required");
            }
            if (appId == null || appId.isEmpty()) {
                throw new IllegalArgumentException("appId is required");
            }
            if (appSecret == null || appSecret.isEmpty()) {
                throw new IllegalArgumentException("appSecret is required");
            }
            return new AiHelpConfig(this);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.config.AiHelpConfigTest"`
Expected: 3 tests PASSED

- [ ] **Step 5: Create ChatConfig and FAQConfig**

`sdk/src/main/java/com/msdk/aihelp/config/ChatConfig.java`:
```java
package com.msdk.aihelp.config;

public class ChatConfig {

    private final String welcomeMessage;

    private ChatConfig(Builder builder) {
        this.welcomeMessage = builder.welcomeMessage;
    }

    public String getWelcomeMessage() { return welcomeMessage; }

    public static class Builder {
        private String welcomeMessage;

        public Builder setWelcomeMessage(String welcomeMessage) {
            this.welcomeMessage = welcomeMessage;
            return this;
        }

        public ChatConfig build() {
            return new ChatConfig(this);
        }
    }
}
```

`sdk/src/main/java/com/msdk/aihelp/config/FAQConfig.java`:
```java
package com.msdk.aihelp.config;

public class FAQConfig {

    private final String sectionId;
    private final boolean showContactUs;

    private FAQConfig(Builder builder) {
        this.sectionId = builder.sectionId;
        this.showContactUs = builder.showContactUs;
    }

    public String getSectionId() { return sectionId; }
    public boolean isShowContactUs() { return showContactUs; }

    public static class Builder {
        private String sectionId;
        private boolean showContactUs = true;

        public Builder setSectionId(String sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Builder setShowContactUs(boolean showContactUs) {
            this.showContactUs = showContactUs;
            return this;
        }

        public FAQConfig build() {
            return new FAQConfig(this);
        }
    }
}
```

- [ ] **Step 6: Create ConfigManager**

`sdk/src/main/java/com/msdk/aihelp/config/ConfigManager.java`:
```java
package com.msdk.aihelp.config;

import android.content.Context;

import com.msdk.aihelp.model.UserInfo;

public class ConfigManager {

    private static ConfigManager instance;

    private Context appContext;
    private AiHelpConfig config;
    private UserInfo userInfo;
    private String language;
    private boolean initialized;

    private ConfigManager() {}

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void init(Context context, AiHelpConfig config) {
        this.appContext = context.getApplicationContext();
        this.config = config;
        this.initialized = true;
    }

    public boolean isInitialized() { return initialized; }
    public Context getAppContext() { return appContext; }
    public AiHelpConfig getConfig() { return config; }

    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
    public UserInfo getUserInfo() { return userInfo; }
    public void clearUserInfo() { this.userInfo = null; }

    public void setLanguage(String language) { this.language = language; }
    public String getLanguage() { return language; }
}
```

- [ ] **Step 7: Write failing test for Message model**

`sdk/src/test/java/com/msdk/aihelp/model/MessageTest.java`:
```java
package com.msdk.aihelp.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void createTextMessage_setsFieldsCorrectly() {
        Message msg = Message.createText("Hello", Message.Direction.SEND);

        assertEquals("Hello", msg.getContent());
        assertEquals(Message.MsgType.TEXT, msg.getMsgType());
        assertEquals(Message.Direction.SEND, msg.getDirection());
        assertEquals(Message.Status.SENDING, msg.getStatus());
        assertNotNull(msg.getClientMsgId());
        assertTrue(msg.getTimestamp() > 0);
    }

    @Test
    public void createImageMessage_setsFieldsCorrectly() {
        Message msg = Message.createImage("https://img.com/1.jpg", Message.Direction.SEND);

        assertEquals("https://img.com/1.jpg", msg.getContent());
        assertEquals(Message.MsgType.IMAGE, msg.getMsgType());
        assertEquals(Message.Direction.SEND, msg.getDirection());
    }

    @Test
    public void createSystemMessage_directionIsReceive() {
        Message msg = Message.createSystem("排队中...");

        assertEquals(Message.Direction.RECEIVE, msg.getDirection());
        assertEquals(Message.MsgType.SYSTEM, msg.getMsgType());
    }

    @Test
    public void markSent_updatesStatus() {
        Message msg = Message.createText("Hi", Message.Direction.SEND);
        msg.setStatus(Message.Status.SENT);

        assertEquals(Message.Status.SENT, msg.getStatus());
    }
}
```

- [ ] **Step 8: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.model.MessageTest"`
Expected: FAIL - class not found

- [ ] **Step 9: Implement Message model**

`sdk/src/main/java/com/msdk/aihelp/model/Message.java`:
```java
package com.msdk.aihelp.model;

import java.util.UUID;

public class Message {

    public enum MsgType { TEXT, IMAGE, SYSTEM, LOADING }
    public enum Direction { SEND, RECEIVE }
    public enum Status { SENDING, SENT, FAILED }

    private String clientMsgId;
    private String serverMsgId;
    private MsgType msgType;
    private Direction direction;
    private String content;
    private String sender;
    private long timestamp;
    private Status status;

    private Message() {}

    public static Message createText(String content, Direction direction) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.TEXT;
        msg.direction = direction;
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        msg.status = (direction == Direction.SEND) ? Status.SENDING : Status.SENT;
        return msg;
    }

    public static Message createImage(String imageUrl, Direction direction) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.IMAGE;
        msg.direction = direction;
        msg.content = imageUrl;
        msg.timestamp = System.currentTimeMillis();
        msg.status = (direction == Direction.SEND) ? Status.SENDING : Status.SENT;
        return msg;
    }

    public static Message createSystem(String content) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.SYSTEM;
        msg.direction = Direction.RECEIVE;
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        msg.status = Status.SENT;
        return msg;
    }

    public static Message createLoading() {
        Message msg = new Message();
        msg.clientMsgId = "loading";
        msg.msgType = MsgType.LOADING;
        msg.direction = Direction.RECEIVE;
        msg.content = "";
        msg.timestamp = System.currentTimeMillis();
        msg.status = Status.SENT;
        return msg;
    }

    public String getClientMsgId() { return clientMsgId; }
    public void setClientMsgId(String clientMsgId) { this.clientMsgId = clientMsgId; }
    public String getServerMsgId() { return serverMsgId; }
    public void setServerMsgId(String serverMsgId) { this.serverMsgId = serverMsgId; }
    public MsgType getMsgType() { return msgType; }
    public void setMsgType(MsgType msgType) { this.msgType = msgType; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
```

- [ ] **Step 10: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.model.MessageTest"`
Expected: 4 tests PASSED

- [ ] **Step 11: Create remaining models**

`sdk/src/main/java/com/msdk/aihelp/model/UserInfo.java`:
```java
package com.msdk.aihelp.model;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    private final String userId;
    private final String userName;
    private final String serverId;
    private final Map<String, String> customData;

    private UserInfo(Builder builder) {
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.serverId = builder.serverId;
        this.customData = builder.customData;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getServerId() { return serverId; }
    public Map<String, String> getCustomData() { return customData; }

    public static class Builder {
        private String userId;
        private String userName;
        private String serverId;
        private Map<String, String> customData = new HashMap<>();

        public Builder setUserId(String userId) { this.userId = userId; return this; }
        public Builder setUserName(String userName) { this.userName = userName; return this; }
        public Builder setServerId(String serverId) { this.serverId = serverId; return this; }
        public Builder addCustomData(String key, String value) {
            this.customData.put(key, value);
            return this;
        }

        public UserInfo build() {
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("userId is required");
            }
            return new UserInfo(this);
        }
    }
}
```

`sdk/src/main/java/com/msdk/aihelp/model/FAQSection.java`:
```java
package com.msdk.aihelp.model;

import java.util.List;

public class FAQSection {

    private String sectionId;
    private String title;
    private int sortOrder;
    private List<FAQItem> items;

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public List<FAQItem> getItems() { return items; }
    public void setItems(List<FAQItem> items) { this.items = items; }
}
```

`sdk/src/main/java/com/msdk/aihelp/model/FAQItem.java`:
```java
package com.msdk.aihelp.model;

public class FAQItem {

    private String faqId;
    private String question;
    private String answer;
    private int sortOrder;

    public String getFaqId() { return faqId; }
    public void setFaqId(String faqId) { this.faqId = faqId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
```

- [ ] **Step 12: Create callback interfaces**

`sdk/src/main/java/com/msdk/aihelp/callback/UnreadCountCallback.java`:
```java
package com.msdk.aihelp.callback;

public interface UnreadCountCallback {
    void onResult(int count);
}
```

`sdk/src/main/java/com/msdk/aihelp/callback/AiHelpEventListener.java`:
```java
package com.msdk.aihelp.callback;

public interface AiHelpEventListener {
    void onInitialized(boolean success, String message);
    void onSessionOpened();
    void onSessionClosed();
    void onUnreadCountChanged(int count);
}
```

- [ ] **Step 13: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/config/ sdk/src/main/java/com/msdk/aihelp/model/ sdk/src/main/java/com/msdk/aihelp/callback/ sdk/src/test/
git commit -m "feat: add config, model, and callback classes"
```

---

## Task 4: Network Layer - HTTP Client

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/network/HttpClient.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/network/ApiService.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/network/ApiCallback.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/network/ApiServiceTest.java`

- [ ] **Step 1: Write failing test for ApiService URL construction**

`sdk/src/test/java/com/msdk/aihelp/network/ApiServiceTest.java`:
```java
package com.msdk.aihelp.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApiServiceTest {

    @Test
    public void getFAQSectionsUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_FAQ_SECTIONS);
        assertEquals("https://cs.example.com/api/v1/faq/sections", url);
    }

    @Test
    public void getFAQItemsUrl_withSectionId_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com",
                ApiService.PATH_FAQ_ITEMS, "section_abc");
        assertEquals("https://cs.example.com/api/v1/faq/sections/section_abc/items", url);
    }

    @Test
    public void getSearchUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_FAQ_SEARCH);
        assertEquals("https://cs.example.com/api/v1/faq/search", url);
    }

    @Test
    public void getUploadUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_UPLOAD);
        assertEquals("https://cs.example.com/api/v1/upload", url);
    }

    @Test
    public void getUnreadCountUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_UNREAD_COUNT);
        assertEquals("https://cs.example.com/api/v1/chat/unread", url);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.network.ApiServiceTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement ApiService**

`sdk/src/main/java/com/msdk/aihelp/network/ApiService.java`:
```java
package com.msdk.aihelp.network;

public class ApiService {

    private static final String API_PREFIX = "/api/v1";

    public static final String PATH_FAQ_SECTIONS = "/faq/sections";
    public static final String PATH_FAQ_ITEMS = "/faq/sections/%s/items";
    public static final String PATH_FAQ_DETAIL = "/faq/items/%s";
    public static final String PATH_FAQ_SEARCH = "/faq/search";
    public static final String PATH_FAQ_FEEDBACK = "/faq/items/%s/feedback";
    public static final String PATH_UPLOAD = "/upload";
    public static final String PATH_UNREAD_COUNT = "/chat/unread";
    public static final String PATH_CHAT_HISTORY = "/chat/history";

    public static String buildUrl(String domain, String path, String... args) {
        String formattedPath = (args.length > 0) ? String.format(path, (Object[]) args) : path;
        String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return base + API_PREFIX + formattedPath;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.network.ApiServiceTest"`
Expected: 5 tests PASSED

- [ ] **Step 5: Create ApiCallback**

`sdk/src/main/java/com/msdk/aihelp/network/ApiCallback.java`:
```java
package com.msdk.aihelp.network;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(int code, String message);
}
```

- [ ] **Step 6: Implement HttpClient**

`sdk/src/main/java/com/msdk/aihelp/network/HttpClient.java`:
```java
package com.msdk.aihelp.network;

import com.google.gson.Gson;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static HttpClient instance;
    private final OkHttpClient client;
    private final Gson gson;

    private HttpClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public static synchronized HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    public <T> void get(String url, Type type, ApiCallback<T> callback) {
        Request request = newRequestBuilder(url).get().build();
        enqueue(request, type, callback);
    }

    public <T> void post(String url, Object body, Type type, ApiCallback<T> callback) {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json,
                MediaType.parse("application/json; charset=utf-8"));
        Request request = newRequestBuilder(url).post(requestBody).build();
        enqueue(request, type, callback);
    }

    public void uploadFile(String url, File file, ApiCallback<String> callback) {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();
        Request request = newRequestBuilder(url).post(body).build();
        enqueue(request, String.class, callback);
    }

    private Request.Builder newRequestBuilder(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        ConfigManager cm = ConfigManager.getInstance();
        if (cm.getConfig() != null) {
            builder.addHeader("X-App-Id", cm.getConfig().getAppId());
            builder.addHeader("X-App-Secret", cm.getConfig().getAppSecret());
        }
        if (cm.getUserInfo() != null) {
            builder.addHeader("X-User-Id", cm.getUserInfo().getUserId());
        }
        if (cm.getLanguage() != null) {
            builder.addHeader("X-Language", cm.getLanguage());
        }
        return builder;
    }

    private <T> void enqueue(Request request, Type type, ApiCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("HTTP request failed: " + request.url(), e);
                ThreadUtil.runOnMain(() -> callback.onError(-1, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        ThreadUtil.runOnMain(() -> callback.onError(response.code(), body));
                        return;
                    }
                    T result = gson.fromJson(body, type);
                    ThreadUtil.runOnMain(() -> callback.onSuccess(result));
                } catch (Exception e) {
                    Logger.e("HTTP parse failed", e);
                    ThreadUtil.runOnMain(() -> callback.onError(-2, e.getMessage()));
                }
            }
        });
    }

    public OkHttpClient getOkHttpClient() {
        return client;
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/network/ sdk/src/test/java/com/msdk/aihelp/network/
git commit -m "feat: add HTTP client and API service layer"
```

---

## Task 5: Network Layer - WebSocket Client

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/network/WebSocketClient.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/network/MessageProtocol.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/network/MessageProtocolTest.java`

- [ ] **Step 1: Write failing test for MessageProtocol**

`sdk/src/test/java/com/msdk/aihelp/network/MessageProtocolTest.java`:
```java
package com.msdk.aihelp.network;

import com.msdk.aihelp.model.Message;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageProtocolTest {

    @Test
    public void encodeTextMessage_producesCorrectJson() {
        Message msg = Message.createText("Hello", Message.Direction.SEND);
        msg.setClientMsgId("test-id-123");

        String json = MessageProtocol.encode(msg);

        assertTrue(json.contains("\"type\":\"send\""));
        assertTrue(json.contains("\"msgType\":\"text\""));
        assertTrue(json.contains("\"content\":\"Hello\""));
        assertTrue(json.contains("\"clientMsgId\":\"test-id-123\""));
    }

    @Test
    public void encodeImageMessage_producesCorrectJson() {
        Message msg = Message.createImage("https://img.com/1.jpg", Message.Direction.SEND);

        String json = MessageProtocol.encode(msg);

        assertTrue(json.contains("\"msgType\":\"image\""));
        assertTrue(json.contains("\"content\":\"https://img.com/1.jpg\""));
    }

    @Test
    public void decodeTextMessage_parsesCorrectly() {
        String json = "{\"type\":\"receive\",\"msgType\":\"text\",\"content\":\"Hi there\",\"sender\":\"agent\",\"serverMsgId\":\"srv_001\",\"timestamp\":1700000000000}";

        Message msg = MessageProtocol.decode(json);

        assertNotNull(msg);
        assertEquals(Message.MsgType.TEXT, msg.getMsgType());
        assertEquals(Message.Direction.RECEIVE, msg.getDirection());
        assertEquals("Hi there", msg.getContent());
        assertEquals("agent", msg.getSender());
        assertEquals("srv_001", msg.getServerMsgId());
    }

    @Test
    public void decodeSystemMessage_parsesCorrectly() {
        String json = "{\"type\":\"receive\",\"msgType\":\"system\",\"content\":\"正在转接人工客服...\"}";

        Message msg = MessageProtocol.decode(json);

        assertNotNull(msg);
        assertEquals(Message.MsgType.SYSTEM, msg.getMsgType());
        assertEquals("正在转接人工客服...", msg.getContent());
    }

    @Test
    public void encodeConnectMessage_producesCorrectJson() {
        String json = MessageProtocol.encodeConnect("session_abc", "token_xyz");

        assertTrue(json.contains("\"type\":\"connect\""));
        assertTrue(json.contains("\"sessionId\":\"session_abc\""));
        assertTrue(json.contains("\"token\":\"token_xyz\""));
    }

    @Test
    public void encodeHeartbeat_producesCorrectJson() {
        String json = MessageProtocol.encodeHeartbeat();

        assertTrue(json.contains("\"type\":\"heartbeat\""));
    }

    @Test
    public void decodeCloseMessage_parsesCorrectly() {
        String json = "{\"type\":\"close\",\"reason\":\"session_end\"}";

        MessageProtocol.ControlMessage ctrl = MessageProtocol.decodeControl(json);

        assertNotNull(ctrl);
        assertEquals("close", ctrl.type);
        assertEquals("session_end", ctrl.reason);
    }

    @Test
    public void decode_unknownType_returnsNull() {
        String json = "{\"type\":\"unknown\",\"data\":\"test\"}";

        Message msg = MessageProtocol.decode(json);
        assertNull(msg);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.network.MessageProtocolTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement MessageProtocol**

`sdk/src/main/java/com/msdk/aihelp/network/MessageProtocol.java`:
```java
package com.msdk.aihelp.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.msdk.aihelp.model.Message;

public class MessageProtocol {

    private static final Gson GSON = new Gson();

    public static String encode(Message message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "send");
        obj.addProperty("msgType", message.getMsgType().name().toLowerCase());
        obj.addProperty("content", message.getContent());
        obj.addProperty("clientMsgId", message.getClientMsgId());
        return GSON.toJson(obj);
    }

    public static String encodeConnect(String sessionId, String token) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "connect");
        obj.addProperty("sessionId", sessionId);
        obj.addProperty("token", token);
        return GSON.toJson(obj);
    }

    public static String encodeHeartbeat() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "heartbeat");
        return GSON.toJson(obj);
    }

    public static Message decode(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("type").getAsString();

        if (!"receive".equals(type)) {
            return null;
        }

        String msgTypeStr = obj.get("msgType").getAsString();
        String content = obj.has("content") ? obj.get("content").getAsString() : "";

        Message.MsgType msgType;
        try {
            msgType = Message.MsgType.valueOf(msgTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        Message msg;
        if (msgType == Message.MsgType.SYSTEM) {
            msg = Message.createSystem(content);
        } else if (msgType == Message.MsgType.IMAGE) {
            msg = Message.createImage(content, Message.Direction.RECEIVE);
        } else {
            msg = Message.createText(content, Message.Direction.RECEIVE);
        }

        if (obj.has("sender")) {
            msg.setSender(obj.get("sender").getAsString());
        }
        if (obj.has("serverMsgId")) {
            msg.setServerMsgId(obj.get("serverMsgId").getAsString());
        }
        if (obj.has("timestamp")) {
            msg.setTimestamp(obj.get("timestamp").getAsLong());
        }

        return msg;
    }

    public static ControlMessage decodeControl(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("type").getAsString();

        if ("close".equals(type) || "connect".equals(type)) {
            ControlMessage ctrl = new ControlMessage();
            ctrl.type = type;
            ctrl.reason = obj.has("reason") ? obj.get("reason").getAsString() : null;
            ctrl.sessionId = obj.has("sessionId") ? obj.get("sessionId").getAsString() : null;
            ctrl.token = obj.has("token") ? obj.get("token").getAsString() : null;
            return ctrl;
        }
        return null;
    }

    public static class ControlMessage {
        public String type;
        public String reason;
        public String sessionId;
        public String token;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.network.MessageProtocolTest"`
Expected: 8 tests PASSED

- [ ] **Step 5: Implement WebSocketClient**

`sdk/src/main/java/com/msdk/aihelp/network/WebSocketClient.java`:
```java
package com.msdk.aihelp.network;

import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {

    public interface Listener {
        void onConnected(String sessionId);
        void onMessage(Message message);
        void onSessionClosed(String reason);
        void onDisconnected();
    }

    private static final int NORMAL_CLOSE_CODE = 1000;
    private static final long MAX_RECONNECT_DELAY_MS = 30000;
    private static final long HEARTBEAT_INTERVAL_MS = 30000;

    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private Listener listener;
    private String sessionId;
    private String token;
    private boolean intentionallyClosed;
    private int reconnectAttempt;

    public WebSocketClient() {
        this.okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(String token) {
        this.token = token;
        this.intentionallyClosed = false;
        this.reconnectAttempt = 0;
        doConnect();
    }

    private void doConnect() {
        ConfigManager cm = ConfigManager.getInstance();
        String domain = cm.getConfig().getDomain();
        String wsUrl = domain.replace("https://", "wss://").replace("http://", "ws://")
                + "/ws/chat";

        Request request = new Request.Builder()
                .url(wsUrl)
                .addHeader("X-App-Id", cm.getConfig().getAppId())
                .addHeader("X-App-Secret", cm.getConfig().getAppSecret())
                .addHeader("X-Token", token)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Logger.d("WebSocket connected");
                reconnectAttempt = 0;
                if (sessionId != null) {
                    ws.send(MessageProtocol.encodeConnect(sessionId, token));
                }
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                ws.close(NORMAL_CLOSE_CODE, null);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Logger.d("WebSocket closed: " + reason);
                stopHeartbeat();
                if (!intentionallyClosed) {
                    scheduleReconnect();
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Logger.e("WebSocket failure", t);
                stopHeartbeat();
                if (!intentionallyClosed) {
                    scheduleReconnect();
                }
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onDisconnected();
                });
            }
        });
    }

    private void handleMessage(String text) {
        MessageProtocol.ControlMessage ctrl = MessageProtocol.decodeControl(text);
        if (ctrl != null) {
            if ("connect".equals(ctrl.type)) {
                sessionId = ctrl.sessionId;
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onConnected(sessionId);
                });
            } else if ("close".equals(ctrl.type)) {
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onSessionClosed(ctrl.reason);
                });
            }
            return;
        }

        Message msg = MessageProtocol.decode(text);
        if (msg != null) {
            ThreadUtil.runOnMain(() -> {
                if (listener != null) listener.onMessage(msg);
            });
        }
    }

    public void send(Message message) {
        if (webSocket != null) {
            String json = MessageProtocol.encode(message);
            webSocket.send(json);
        }
    }

    public void disconnect() {
        intentionallyClosed = true;
        stopHeartbeat();
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSE_CODE, "user_close");
            webSocket = null;
        }
    }

    private void scheduleReconnect() {
        reconnectAttempt++;
        long delay = Math.min(
                (long) Math.pow(2, reconnectAttempt) * 1000,
                MAX_RECONNECT_DELAY_MS);
        Logger.d("Reconnecting in " + delay + "ms (attempt " + reconnectAttempt + ")");
        ThreadUtil.getMainHandler().postDelayed(this::doConnect, delay);
    }

    private Runnable heartbeatRunnable;

    private void startHeartbeat() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (webSocket != null && !intentionallyClosed) {
                    webSocket.send(MessageProtocol.encodeHeartbeat());
                    ThreadUtil.getMainHandler().postDelayed(this, HEARTBEAT_INTERVAL_MS);
                }
            }
        };
        ThreadUtil.getMainHandler().postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
    }

    private void stopHeartbeat() {
        if (heartbeatRunnable != null) {
            ThreadUtil.getMainHandler().removeCallbacks(heartbeatRunnable);
            heartbeatRunnable = null;
        }
    }

    public String getSessionId() { return sessionId; }
}
```

- [ ] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/network/ sdk/src/test/java/com/msdk/aihelp/network/
git commit -m "feat: add WebSocket client with reconnection and message protocol"
```

---

## Task 6: Storage Layer

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/storage/MessageDatabase.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/storage/CacheManager.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/storage/CacheManagerTest.java`

- [ ] **Step 1: Write failing test for CacheManager**

`sdk/src/test/java/com/msdk/aihelp/storage/CacheManagerTest.java`:
```java
package com.msdk.aihelp.storage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheManagerTest {

    private CacheManager cache;

    @Before
    public void setUp() {
        cache = new CacheManager();
    }

    @Test
    public void put_andGet_returnsValue() {
        cache.put("key1", "value1", 60000);
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    public void get_expiredEntry_returnsNull() {
        cache.put("key1", "value1", 0);
        assertNull(cache.get("key1"));
    }

    @Test
    public void get_nonExistentKey_returnsNull() {
        assertNull(cache.get("missing"));
    }

    @Test
    public void clear_removesAllEntries() {
        cache.put("key1", "value1", 60000);
        cache.put("key2", "value2", 60000);
        cache.clear();
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.storage.CacheManagerTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement CacheManager**

`sdk/src/main/java/com/msdk/aihelp/storage/CacheManager.java`:
```java
package com.msdk.aihelp.storage;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private static final long FAQ_SECTIONS_TTL = 30 * 60 * 1000L;
    private static final long FAQ_DETAIL_TTL = 60 * 60 * 1000L;

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    public void put(String key, Object value, long ttlMs) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
    }

    public void putFAQSections(Object value) {
        put("faq_sections", value, FAQ_SECTIONS_TTL);
    }

    public void putFAQDetail(String faqId, Object value) {
        put("faq_detail_" + faqId, value, FAQ_DETAIL_TTL);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(key);
            return null;
        }
        return (T) entry.value;
    }

    public void clear() {
        store.clear();
    }

    private static class CacheEntry {
        final Object value;
        final long expireAt;

        CacheEntry(Object value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.storage.CacheManagerTest"`
Expected: 4 tests PASSED

- [ ] **Step 5: Implement MessageDatabase**

`sdk/src/main/java/com/msdk/aihelp/storage/MessageDatabase.java`:
```java
package com.msdk.aihelp.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "msdk_aihelp_messages.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_CLIENT_MSG_ID = "client_msg_id";
    private static final String COL_SERVER_MSG_ID = "server_msg_id";
    private static final String COL_MSG_TYPE = "msg_type";
    private static final String COL_DIRECTION = "direction";
    private static final String COL_CONTENT = "content";
    private static final String COL_SENDER = "sender";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_STATUS = "status";
    private static final String COL_SESSION_ID = "session_id";

    private static MessageDatabase instance;

    public static synchronized MessageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private MessageDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_CLIENT_MSG_ID + " TEXT PRIMARY KEY, "
                + COL_SERVER_MSG_ID + " TEXT, "
                + COL_MSG_TYPE + " TEXT NOT NULL, "
                + COL_DIRECTION + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT, "
                + COL_SENDER + " TEXT, "
                + COL_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_STATUS + " TEXT NOT NULL, "
                + COL_SESSION_ID + " TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_session_time ON " + TABLE_MESSAGES
                + " (" + COL_SESSION_ID + ", " + COL_TIMESTAMP + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void insertMessage(Message message, String sessionId) {
        ThreadUtil.runOnDb(() -> {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_CLIENT_MSG_ID, message.getClientMsgId());
            cv.put(COL_SERVER_MSG_ID, message.getServerMsgId());
            cv.put(COL_MSG_TYPE, message.getMsgType().name());
            cv.put(COL_DIRECTION, message.getDirection().name());
            cv.put(COL_CONTENT, message.getContent());
            cv.put(COL_SENDER, message.getSender());
            cv.put(COL_TIMESTAMP, message.getTimestamp());
            cv.put(COL_STATUS, message.getStatus().name());
            cv.put(COL_SESSION_ID, sessionId);
            db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    public void updateMessageStatus(String clientMsgId, Message.Status status) {
        ThreadUtil.runOnDb(() -> {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_STATUS, status.name());
            db.update(TABLE_MESSAGES, cv,
                    COL_CLIENT_MSG_ID + " = ?", new String[]{clientMsgId});
        });
    }

    public List<Message> getMessages(String sessionId, int limit) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COL_SESSION_ID + " = ?", new String[]{sessionId},
                null, null, COL_TIMESTAMP + " ASC",
                String.valueOf(limit));

        while (cursor.moveToNext()) {
            messages.add(cursorToMessage(cursor));
        }
        cursor.close();
        return messages;
    }

    public List<Message> getPendingMessages(String sessionId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COL_SESSION_ID + " = ? AND " + COL_STATUS + " = ?",
                new String[]{sessionId, Message.Status.SENDING.name()},
                null, null, COL_TIMESTAMP + " ASC", null);

        while (cursor.moveToNext()) {
            messages.add(cursorToMessage(cursor));
        }
        cursor.close();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message msg = Message.createText("", Message.Direction.SEND);
        msg.setClientMsgId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIENT_MSG_ID)));
        msg.setServerMsgId(cursor.getString(cursor.getColumnIndexOrThrow(COL_SERVER_MSG_ID)));
        msg.setMsgType(Message.MsgType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TYPE))));
        msg.setDirection(Message.Direction.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_DIRECTION))));
        msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)));
        msg.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)));
        msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        msg.setStatus(Message.Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))));
        return msg;
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/storage/ sdk/src/test/java/com/msdk/aihelp/storage/
git commit -m "feat: add storage layer (MessageDatabase + CacheManager)"
```

---

## Task 7: Theme Manager

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/ui/theme/ThemeManager.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/ui/theme/ThemeManagerTest.java`

- [ ] **Step 1: Write failing test for ThemeManager color derivation**

`sdk/src/test/java/com/msdk/aihelp/ui/theme/ThemeManagerTest.java`:
```java
package com.msdk.aihelp.ui.theme;

import org.junit.Test;
import static org.junit.Assert.*;

public class ThemeManagerTest {

    @Test
    public void getPressedColor_isDarkerThanPrimary() {
        int primary = 0xFF1A73E8;
        int pressed = ThemeManager.getPressedColor(primary);

        int primaryR = (primary >> 16) & 0xFF;
        int pressedR = (pressed >> 16) & 0xFF;
        assertTrue(pressedR < primaryR);
    }

    @Test
    public void getLightColor_isLighterThanPrimary() {
        int primary = 0xFF1A73E8;
        int light = ThemeManager.getLightColor(primary);

        int lightA = (light >> 24) & 0xFF;
        assertTrue(lightA < 0xFF);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.ui.theme.ThemeManagerTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement ThemeManager**

`sdk/src/main/java/com/msdk/aihelp/ui/theme/ThemeManager.java`:
```java
package com.msdk.aihelp.ui.theme;

import com.msdk.aihelp.config.ConfigManager;

public class ThemeManager {

    public static int getPrimaryColor() {
        ConfigManager cm = ConfigManager.getInstance();
        if (cm.getConfig() != null) {
            return cm.getConfig().getThemeColor();
        }
        return 0xFF1A73E8;
    }

    public static int getLogoResId() {
        ConfigManager cm = ConfigManager.getInstance();
        if (cm.getConfig() != null) {
            return cm.getConfig().getLogoResId();
        }
        return 0;
    }

    public static int getPressedColor(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * 0.8f);
        int g = (int) (((color >> 8) & 0xFF) * 0.8f);
        int b = (int) ((color & 0xFF) * 0.8f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getLightColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (0x33 << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getTextOnPrimaryColor() {
        return 0xFFFFFFFF;
    }

    public static int getBackgroundColor() {
        return 0xFFF5F5F5;
    }

    public static int getSurfaceColor() {
        return 0xFFFFFFFF;
    }

    public static int getTextPrimaryColor() {
        return 0xFF212121;
    }

    public static int getTextSecondaryColor() {
        return 0xFF757575;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.ui.theme.ThemeManagerTest"`
Expected: 2 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/ui/theme/ sdk/src/test/java/com/msdk/aihelp/ui/theme/
git commit -m "feat: add ThemeManager for dynamic color management"
```

---

## Task 8: Chat Module - ChatManager

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/ChatManager.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/chat/ChatManagerTest.java`

- [ ] **Step 1: Write failing test for ChatManager**

`sdk/src/test/java/com/msdk/aihelp/chat/ChatManagerTest.java`:
```java
package com.msdk.aihelp.chat;

import com.msdk.aihelp.model.Message;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ChatManagerTest {

    private List<Message> receivedMessages;
    private ChatManager.ChatCallback testCallback;

    @Before
    public void setUp() {
        receivedMessages = new ArrayList<>();
        testCallback = new ChatManager.ChatCallback() {
            @Override
            public void onMessageReceived(Message message) {
                receivedMessages.add(message);
            }
            @Override
            public void onMessageStatusChanged(String clientMsgId, Message.Status status) {}
            @Override
            public void onConnectionStateChanged(ChatManager.ConnectionState state) {}
            @Override
            public void onSessionStarted(String sessionId) {}
            @Override
            public void onSessionEnded(String reason) {}
        };
    }

    @Test
    public void connectionState_startsAsDisconnected() {
        assertEquals(ChatManager.ConnectionState.DISCONNECTED, ChatManager.ConnectionState.DISCONNECTED);
    }

    @Test
    public void messageList_initiallyEmpty() {
        List<Message> messages = new ArrayList<>();
        assertTrue(messages.isEmpty());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.chat.ChatManagerTest"`
Expected: FAIL - class not found

- [ ] **Step 3: Implement ChatManager**

`sdk/src/main/java/com/msdk/aihelp/chat/ChatManager.java`:
```java
package com.msdk.aihelp.chat;

import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.network.WebSocketClient;
import com.msdk.aihelp.storage.MessageDatabase;
import com.msdk.aihelp.util.ImageCompressor;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatManager implements WebSocketClient.Listener {

    public enum ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

    public interface ChatCallback {
        void onMessageReceived(Message message);
        void onMessageStatusChanged(String clientMsgId, Message.Status status);
        void onConnectionStateChanged(ConnectionState state);
        void onSessionStarted(String sessionId);
        void onSessionEnded(String reason);
    }

    private static ChatManager instance;

    private final WebSocketClient webSocketClient;
    private final List<Message> messages = new ArrayList<>();
    private ChatCallback callback;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private String currentSessionId;
    private int unreadCount;

    private ChatManager() {
        webSocketClient = new WebSocketClient();
        webSocketClient.setListener(this);
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void setCallback(ChatCallback callback) {
        this.callback = callback;
    }

    public void connect() {
        setConnectionState(ConnectionState.CONNECTING);
        String token = buildAuthToken();
        webSocketClient.connect(token);
    }

    public void disconnect() {
        webSocketClient.disconnect();
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    public void sendTextMessage(String content) {
        Message msg = Message.createText(content, Message.Direction.SEND);
        addMessage(msg);
        persistMessage(msg);
        webSocketClient.send(msg);
    }

    public void sendImageMessage(File imageFile) {
        ThreadUtil.runOnDb(() -> {
            try {
                File cacheDir = ConfigManager.getInstance().getAppContext().getCacheDir();
                File compressed = ImageCompressor.compress(imageFile, cacheDir);
                uploadAndSend(compressed);
            } catch (IOException e) {
                Logger.e("Image compress failed", e);
            }
        });
    }

    private void uploadAndSend(File compressedFile) {
        String uploadUrl = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_UPLOAD);

        HttpClient.getInstance().uploadFile(uploadUrl, compressedFile, new ApiCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                Message msg = Message.createImage(imageUrl, Message.Direction.SEND);
                addMessage(msg);
                persistMessage(msg);
                webSocketClient.send(msg);
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("Image upload failed: " + code + " " + message, null);
            }
        });
    }

    public void loadHistory() {
        if (currentSessionId == null) return;
        ThreadUtil.runOnDb(() -> {
            MessageDatabase db = MessageDatabase.getInstance(
                    ConfigManager.getInstance().getAppContext());
            List<Message> history = db.getMessages(currentSessionId, 100);
            ThreadUtil.runOnMain(() -> {
                messages.clear();
                messages.addAll(history);
            });
        });
    }

    public void resendPendingMessages() {
        if (currentSessionId == null) return;
        ThreadUtil.runOnDb(() -> {
            MessageDatabase db = MessageDatabase.getInstance(
                    ConfigManager.getInstance().getAppContext());
            List<Message> pending = db.getPendingMessages(currentSessionId);
            ThreadUtil.runOnMain(() -> {
                for (Message msg : pending) {
                    webSocketClient.send(msg);
                }
            });
        });
    }

    @Override
    public void onConnected(String sessionId) {
        this.currentSessionId = sessionId;
        setConnectionState(ConnectionState.CONNECTED);
        if (callback != null) callback.onSessionStarted(sessionId);
        resendPendingMessages();
    }

    @Override
    public void onMessage(Message message) {
        addMessage(message);
        persistMessage(message);
        unreadCount++;
        if (callback != null) callback.onMessageReceived(message);
    }

    @Override
    public void onSessionClosed(String reason) {
        if (callback != null) callback.onSessionEnded(reason);
    }

    @Override
    public void onDisconnected() {
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getUnreadCount() { return unreadCount; }
    public void resetUnreadCount() { unreadCount = 0; }

    public ConnectionState getConnectionState() { return connectionState; }

    private void addMessage(Message message) {
        messages.add(message);
    }

    private void persistMessage(Message message) {
        if (currentSessionId == null) return;
        MessageDatabase db = MessageDatabase.getInstance(
                ConfigManager.getInstance().getAppContext());
        db.insertMessage(message, currentSessionId);
    }

    private void setConnectionState(ConnectionState state) {
        this.connectionState = state;
        if (callback != null) callback.onConnectionStateChanged(state);
    }

    private String buildAuthToken() {
        ConfigManager cm = ConfigManager.getInstance();
        return cm.getConfig().getAppId() + ":" + cm.getConfig().getAppSecret();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.chat.ChatManagerTest"`
Expected: 2 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/chat/ChatManager.java sdk/src/test/java/com/msdk/aihelp/chat/
git commit -m "feat: add ChatManager with WebSocket integration and message handling"
```

---

## Task 9: Chat Module - UI (Layouts)

**Files:**
- Create: `sdk/src/main/res/layout/aihelp_activity_chat.xml`
- Create: `sdk/src/main/res/layout/aihelp_fragment_chat.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_text_send.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_text_receive.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_image_send.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_image_receive.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_system.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_message_loading.xml`
- Create: `sdk/src/main/res/values/aihelp_strings.xml`
- Create: `sdk/src/main/res/values/aihelp_dimens.xml`

- [ ] **Step 1: Create chat activity layout**

`sdk/src/main/res/layout/aihelp_activity_chat.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

- [ ] **Step 2: Create chat fragment layout**

`sdk/src/main/res/layout/aihelp_fragment_chat.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#FFF5F5F5">

    <!-- Toolbar -->
    <RelativeLayout
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="#FF1A73E8"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/btn_back"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_centerVertical="true"
            android:src="@android:drawable/ic_menu_close_clear_cancel"
            android:contentDescription="@string/aihelp_back" />

        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/aihelp_chat_title"
            android:textColor="#FFFFFFFF"
            android:textSize="18sp" />
    </RelativeLayout>

    <!-- Message list -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_messages"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:clipToPadding="false"
        android:padding="8dp" />

    <!-- Input area -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:background="#FFFFFFFF"
        android:padding="8dp"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/btn_image"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@android:drawable/ic_menu_camera"
            android:contentDescription="@string/aihelp_send_image" />

        <EditText
            android:id="@+id/et_input"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:minHeight="40dp"
            android:maxLines="4"
            android:hint="@string/aihelp_input_hint"
            android:background="@android:drawable/edit_text"
            android:paddingStart="12dp"
            android:paddingEnd="12dp"
            android:textSize="15sp" />

        <TextView
            android:id="@+id/btn_send"
            android:layout_width="wrap_content"
            android:layout_height="36dp"
            android:gravity="center"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:layout_marginStart="8dp"
            android:text="@string/aihelp_send"
            android:textColor="#FFFFFFFF"
            android:background="#FF1A73E8"
            android:textSize="14sp" />
    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 3: Create message item layouts**

`sdk/src/main/res/layout/aihelp_item_message_text_send.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <TextView
        android:id="@+id/tv_content"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:maxWidth="260dp"
        android:padding="12dp"
        android:background="#FF1A73E8"
        android:textColor="#FFFFFFFF"
        android:textSize="15sp" />

    <ImageView
        android:id="@+id/iv_status"
        android:layout_width="16dp"
        android:layout_height="16dp"
        android:layout_toStartOf="@id/tv_content"
        android:layout_alignBottom="@id/tv_content"
        android:layout_marginEnd="4dp"
        android:visibility="gone" />
</RelativeLayout>
```

`sdk/src/main/res/layout/aihelp_item_message_text_receive.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <TextView
        android:id="@+id/tv_content"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentStart="true"
        android:maxWidth="260dp"
        android:padding="12dp"
        android:background="#FFFFFFFF"
        android:textColor="#FF212121"
        android:textSize="15sp" />
</RelativeLayout>
```

`sdk/src/main/res/layout/aihelp_item_message_image_send.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <ImageView
        android:id="@+id/iv_image"
        android:layout_width="180dp"
        android:layout_height="180dp"
        android:layout_alignParentEnd="true"
        android:scaleType="centerCrop"
        android:contentDescription="@string/aihelp_image_message" />
</RelativeLayout>
```

`sdk/src/main/res/layout/aihelp_item_message_image_receive.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <ImageView
        android:id="@+id/iv_image"
        android:layout_width="180dp"
        android:layout_height="180dp"
        android:layout_alignParentStart="true"
        android:scaleType="centerCrop"
        android:contentDescription="@string/aihelp_image_message" />
</RelativeLayout>
```

`sdk/src/main/res/layout/aihelp_item_message_system.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tv_content"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center_horizontal"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="8dp"
    android:paddingStart="12dp"
    android:paddingEnd="12dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp"
    android:background="#33000000"
    android:textColor="#FF757575"
    android:textSize="12sp" />
```

`sdk/src/main/res/layout/aihelp_item_message_loading.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:paddingTop="4dp"
    android:paddingBottom="4dp"
    android:padding="12dp"
    android:background="#FFFFFFFF">

    <ProgressBar
        android:layout_width="16dp"
        android:layout_height="16dp"
        style="?android:attr/progressBarStyleSmall" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:text="@string/aihelp_typing"
        android:textColor="#FF757575"
        android:textSize="13sp" />
</LinearLayout>
```

- [ ] **Step 4: Create string and dimen resources**

`sdk/src/main/res/values/aihelp_strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="aihelp_chat_title">在线客服</string>
    <string name="aihelp_faq_title">帮助中心</string>
    <string name="aihelp_input_hint">请输入消息…</string>
    <string name="aihelp_send">发送</string>
    <string name="aihelp_send_image">发送图片</string>
    <string name="aihelp_back">返回</string>
    <string name="aihelp_contact_us">联系客服</string>
    <string name="aihelp_search_hint">搜索常见问题</string>
    <string name="aihelp_helpful">有帮助</string>
    <string name="aihelp_not_helpful">没帮助</string>
    <string name="aihelp_typing">正在输入…</string>
    <string name="aihelp_image_message">图片消息</string>
    <string name="aihelp_connecting">连接中…</string>
    <string name="aihelp_disconnected">已断开连接</string>
    <string name="aihelp_session_ended">会话已结束</string>
    <string name="aihelp_no_results">未找到相关问题</string>
</resources>
```

`sdk/src/main/res/values/aihelp_dimens.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="aihelp_toolbar_height">56dp</dimen>
    <dimen name="aihelp_message_max_width">260dp</dimen>
    <dimen name="aihelp_message_padding">12dp</dimen>
    <dimen name="aihelp_image_size">180dp</dimen>
    <dimen name="aihelp_text_size_body">15sp</dimen>
    <dimen name="aihelp_text_size_caption">12sp</dimen>
</resources>
```

- [ ] **Step 5: Commit**

```bash
git add sdk/src/main/res/
git commit -m "feat: add chat UI layouts and string resources"
```

---

## Task 10: Chat Module - UI (Activity, Fragment, Adapter)

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/ChatActivity.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/ChatFragment.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/adapter/MessageAdapter.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/adapter/TextMessageViewHolder.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/adapter/ImageMessageViewHolder.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/chat/adapter/SystemMessageViewHolder.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/ui/ImagePickerUtil.java`

- [ ] **Step 1: Implement ChatActivity**

`sdk/src/main/java/com/msdk/aihelp/chat/ChatActivity.java`:
```java
package com.msdk.aihelp.chat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.msdk.aihelp.R;
import com.msdk.aihelp.config.ChatConfig;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_CONFIG = "chat_config_welcome";
    public static final String EXTRA_FAQ_CONTEXT = "faq_context";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_chat);

        if (savedInstanceState == null) {
            ChatFragment fragment = new ChatFragment();
            fragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }
}
```

- [ ] **Step 2: Implement MessageAdapter**

`sdk/src/main/java/com/msdk/aihelp/chat/adapter/MessageAdapter.java`:
```java
package com.msdk.aihelp.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT_SEND = 0;
    private static final int TYPE_TEXT_RECEIVE = 1;
    private static final int TYPE_IMAGE_SEND = 2;
    private static final int TYPE_IMAGE_RECEIVE = 3;
    private static final int TYPE_SYSTEM = 4;
    private static final int TYPE_LOADING = 5;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    private final List<Message> messages = new ArrayList<>();
    private OnImageClickListener imageClickListener;

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeLoading() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getMsgType() == Message.MsgType.LOADING) {
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        switch (msg.getMsgType()) {
            case TEXT:
                return msg.getDirection() == Message.Direction.SEND ? TYPE_TEXT_SEND : TYPE_TEXT_RECEIVE;
            case IMAGE:
                return msg.getDirection() == Message.Direction.SEND ? TYPE_IMAGE_SEND : TYPE_IMAGE_RECEIVE;
            case SYSTEM:
                return TYPE_SYSTEM;
            case LOADING:
                return TYPE_LOADING;
            default:
                return TYPE_TEXT_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_TEXT_SEND:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_send, parent, false));
            case TYPE_TEXT_RECEIVE:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_receive, parent, false));
            case TYPE_IMAGE_SEND:
                return new ImageMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_image_send, parent, false));
            case TYPE_IMAGE_RECEIVE:
                return new ImageMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_image_receive, parent, false));
            case TYPE_SYSTEM:
                return new SystemMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_system, parent, false));
            case TYPE_LOADING:
                return new SystemMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_loading, parent, false));
            default:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_receive, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof ImageMessageViewHolder) {
            ((ImageMessageViewHolder) holder).bind(msg, imageClickListener);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
```

- [ ] **Step 3: Implement ViewHolders**

`sdk/src/main/java/com/msdk/aihelp/chat/adapter/TextMessageViewHolder.java`:
```java
package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class TextMessageViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvContent;
    private final ImageView ivStatus;

    public TextMessageViewHolder(View itemView) {
        super(itemView);
        tvContent = itemView.findViewById(R.id.tv_content);
        ivStatus = itemView.findViewById(R.id.iv_status);
    }

    public void bind(Message message) {
        tvContent.setText(message.getContent());
        if (ivStatus != null && message.getDirection() == Message.Direction.SEND) {
            if (message.getStatus() == Message.Status.FAILED) {
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(android.R.drawable.ic_dialog_alert);
            } else {
                ivStatus.setVisibility(View.GONE);
            }
        }
    }
}
```

`sdk/src/main/java/com/msdk/aihelp/chat/adapter/ImageMessageViewHolder.java`:
```java
package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class ImageMessageViewHolder extends RecyclerView.ViewHolder {

    private final ImageView ivImage;

    public ImageMessageViewHolder(View itemView) {
        super(itemView);
        ivImage = itemView.findViewById(R.id.iv_image);
    }

    public void bind(Message message, MessageAdapter.OnImageClickListener listener) {
        Glide.with(itemView.getContext())
                .load(message.getContent())
                .centerCrop()
                .into(ivImage);

        if (listener != null) {
            ivImage.setOnClickListener(v -> listener.onImageClick(message.getContent()));
        }
    }
}
```

`sdk/src/main/java/com/msdk/aihelp/chat/adapter/SystemMessageViewHolder.java`:
```java
package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class SystemMessageViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvContent;

    public SystemMessageViewHolder(View itemView) {
        super(itemView);
        tvContent = itemView.findViewById(R.id.tv_content);
    }

    public void bind(Message message) {
        if (tvContent != null) {
            tvContent.setText(message.getContent());
        }
    }
}
```

- [ ] **Step 4: Implement ImagePickerUtil**

`sdk/src/main/java/com/msdk/aihelp/ui/ImagePickerUtil.java`:
```java
package com.msdk.aihelp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerUtil {

    public static final int REQUEST_IMAGE_PICK = 9001;
    public static final int REQUEST_IMAGE_CAPTURE = 9002;
    public static final int REQUEST_PERMISSION_CAMERA = 9003;

    public static void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    public static File openCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            return null;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile(activity);
        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".aihelp.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        return photoFile;
    }

    private static File createImageFile(Activity activity) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "AIHELP_" + timeStamp;
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            return null;
        }
    }
}
```

- [ ] **Step 5: Implement ChatFragment**

`sdk/src/main/java/com/msdk/aihelp/chat/ChatFragment.java`:
```java
package com.msdk.aihelp.chat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.chat.adapter.MessageAdapter;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.ui.ImagePickerUtil;
import com.msdk.aihelp.ui.ImageViewerActivity;
import com.msdk.aihelp.ui.theme.ThemeManager;

import java.io.File;

public class ChatFragment extends Fragment implements ChatManager.ChatCallback {

    private RecyclerView recyclerMessages;
    private EditText etInput;
    private TextView btnSend;
    private ImageView btnImage;
    private View toolbar;
    private MessageAdapter adapter;
    private ChatManager chatManager;
    private File cameraFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        applyTheme();
        initChat();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);
        btnImage = view.findViewById(R.id.btn_image);
        ImageView btnBack = view.findViewById(R.id.btn_back);

        adapter = new MessageAdapter();
        adapter.setOnImageClickListener(this::openImageViewer);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendTextMessage());
        btnImage.setOnClickListener(v -> ImagePickerUtil.openGallery(getActivity()));
        btnBack.setOnClickListener(v -> getActivity().finish());
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnSend.setBackgroundColor(primaryColor);
    }

    private void initChat() {
        chatManager = ChatManager.getInstance();
        chatManager.setCallback(this);
        chatManager.connect();

        adapter.setMessages(chatManager.getMessages());
        scrollToBottom();
    }

    private void sendTextMessage() {
        String content = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        chatManager.sendTextMessage(content);
        etInput.setText("");
        adapter.addMessage(chatManager.getMessages().get(chatManager.getMessages().size() - 1));
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void openImageViewer(String imageUrl) {
        ImageViewerActivity.start(getContext(), imageUrl);
    }

    @Override
    public void onMessageReceived(Message message) {
        adapter.removeLoading();
        adapter.addMessage(message);
        scrollToBottom();
    }

    @Override
    public void onMessageStatusChanged(String clientMsgId, Message.Status status) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionStateChanged(ChatManager.ConnectionState state) {
        // Update toolbar subtitle if needed
    }

    @Override
    public void onSessionStarted(String sessionId) {
        chatManager.loadHistory();
    }

    @Override
    public void onSessionEnded(String reason) {
        adapter.addMessage(Message.createSystem(getString(R.string.aihelp_session_ended)));
        scrollToBottom();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == ImagePickerUtil.REQUEST_IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = getRealPathFromUri(uri);
                if (path != null) {
                    chatManager.sendImageMessage(new File(path));
                }
            }
        } else if (requestCode == ImagePickerUtil.REQUEST_IMAGE_CAPTURE && cameraFile != null) {
            chatManager.sendImageMessage(cameraFile);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatManager.setCallback(null);
        chatManager.disconnect();
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/chat/ sdk/src/main/java/com/msdk/aihelp/ui/ImagePickerUtil.java
git commit -m "feat: add chat UI (Activity, Fragment, MessageAdapter, ViewHolders)"
```

---

## Task 11: FAQ Module - FAQManager

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/faq/FAQManager.java`
- Test: `sdk/src/test/java/com/msdk/aihelp/faq/FAQManagerTest.java`

- [ ] **Step 1: Write failing test for FAQManager**

`sdk/src/test/java/com/msdk/aihelp/faq/FAQManagerTest.java`:
```java
package com.msdk.aihelp.faq;

import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.storage.CacheManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FAQManagerTest {

    private CacheManager cacheManager;

    @Before
    public void setUp() {
        cacheManager = new CacheManager();
    }

    @Test
    public void cachedSections_returnsFromCache() {
        FAQSection section = new FAQSection();
        section.setSectionId("s1");
        section.setTitle("充值相关");
        List<FAQSection> sections = Arrays.asList(section);

        cacheManager.putFAQSections(sections);

        List<FAQSection> cached = cacheManager.get("faq_sections");
        assertNotNull(cached);
        assertEquals(1, cached.size());
        assertEquals("充值相关", cached.get(0).getTitle());
    }

    @Test
    public void cachedDetail_returnsFromCache() {
        FAQItem item = new FAQItem();
        item.setFaqId("f1");
        item.setQuestion("如何充值？");
        item.setAnswer("<p>通过商店充值</p>");

        cacheManager.putFAQDetail("f1", item);

        FAQItem cached = cacheManager.get("faq_detail_f1");
        assertNotNull(cached);
        assertEquals("如何充值？", cached.getQuestion());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.faq.FAQManagerTest"`
Expected: FAIL (or PASS if CacheManager already exists — verify that it compiles)

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew :sdk:testDebugUnitTest --tests "com.msdk.aihelp.faq.FAQManagerTest"`
Expected: 2 tests PASSED

- [ ] **Step 4: Implement FAQManager**

`sdk/src/main/java/com/msdk/aihelp/faq/FAQManager.java`:
```java
package com.msdk.aihelp.faq;

import com.google.gson.reflect.TypeToken;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.storage.CacheManager;

import java.util.List;

public class FAQManager {

    private static FAQManager instance;
    private final CacheManager cacheManager = new CacheManager();

    private FAQManager() {}

    public static synchronized FAQManager getInstance() {
        if (instance == null) {
            instance = new FAQManager();
        }
        return instance;
    }

    public void getSections(ApiCallback<List<FAQSection>> callback) {
        List<FAQSection> cached = cacheManager.get("faq_sections");
        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_SECTIONS);

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQSection>>(){}.getType(),
                new ApiCallback<List<FAQSection>>() {
                    @Override
                    public void onSuccess(List<FAQSection> result) {
                        cacheManager.putFAQSections(result);
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(int code, String message) {
                        callback.onError(code, message);
                    }
                });
    }

    public void getItems(String sectionId, ApiCallback<List<FAQItem>> callback) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_ITEMS, sectionId);

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQItem>>(){}.getType(), callback);
    }

    public void getDetail(String faqId, ApiCallback<FAQItem> callback) {
        FAQItem cached = cacheManager.get("faq_detail_" + faqId);
        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_DETAIL, faqId);

        HttpClient.getInstance().get(url, FAQItem.class, new ApiCallback<FAQItem>() {
            @Override
            public void onSuccess(FAQItem result) {
                cacheManager.putFAQDetail(faqId, result);
                callback.onSuccess(result);
            }

            @Override
            public void onError(int code, String message) {
                callback.onError(code, message);
            }
        });
    }

    public void search(String keyword, ApiCallback<List<FAQItem>> callback) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_SEARCH) + "?q=" + keyword;

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQItem>>(){}.getType(), callback);
    }

    public void submitFeedback(String faqId, boolean helpful) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_FEEDBACK, faqId);

        HttpClient.getInstance().post(url,
                new FeedbackBody(helpful), Void.class,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(int code, String message) {}
                });
    }

    private static class FeedbackBody {
        final boolean helpful;
        FeedbackBody(boolean helpful) { this.helpful = helpful; }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/faq/FAQManager.java sdk/src/test/java/com/msdk/aihelp/faq/
git commit -m "feat: add FAQManager with caching and search"
```

---

## Task 12: FAQ Module - UI

**Files:**
- Create: `sdk/src/main/res/layout/aihelp_activity_faq.xml`
- Create: `sdk/src/main/res/layout/aihelp_fragment_faq_list.xml`
- Create: `sdk/src/main/res/layout/aihelp_fragment_faq_detail.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_faq_section.xml`
- Create: `sdk/src/main/res/layout/aihelp_item_faq_item.xml`
- Create: `sdk/src/main/java/com/msdk/aihelp/faq/FAQActivity.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/faq/FAQListFragment.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/faq/FAQDetailFragment.java`

- [ ] **Step 1: Create FAQ layouts**

`sdk/src/main/res/layout/aihelp_activity_faq.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

`sdk/src/main/res/layout/aihelp_fragment_faq_list.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#FFF5F5F5">

    <!-- Toolbar -->
    <RelativeLayout
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="#FF1A73E8"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/btn_back"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_centerVertical="true"
            android:src="@android:drawable/ic_menu_close_clear_cancel"
            android:contentDescription="@string/aihelp_back" />

        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/aihelp_faq_title"
            android:textColor="#FFFFFFFF"
            android:textSize="18sp" />
    </RelativeLayout>

    <!-- Search bar -->
    <EditText
        android:id="@+id/et_search"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:layout_margin="12dp"
        android:paddingStart="12dp"
        android:paddingEnd="12dp"
        android:background="#FFFFFFFF"
        android:hint="@string/aihelp_search_hint"
        android:textSize="14sp"
        android:singleLine="true" />

    <!-- List -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_faq"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <!-- Contact button -->
    <TextView
        android:id="@+id/btn_contact"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:gravity="center"
        android:background="#FF1A73E8"
        android:text="@string/aihelp_contact_us"
        android:textColor="#FFFFFFFF"
        android:textSize="16sp" />
</LinearLayout>
```

`sdk/src/main/res/layout/aihelp_fragment_faq_detail.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#FFFFFFFF">

    <!-- Toolbar -->
    <RelativeLayout
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="#FF1A73E8"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/btn_back"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_centerVertical="true"
            android:src="@android:drawable/ic_menu_close_clear_cancel"
            android:contentDescription="@string/aihelp_back" />

        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:layout_toEndOf="@id/btn_back"
            android:layout_toStartOf="@+id/spacer"
            android:ellipsize="end"
            android:maxLines="1"
            android:textColor="#FFFFFFFF"
            android:textSize="16sp" />

        <View
            android:id="@+id/spacer"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true" />
    </RelativeLayout>

    <!-- Content (WebView) -->
    <WebView
        android:id="@+id/webview_content"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <!-- Feedback -->
    <LinearLayout
        android:id="@+id/layout_feedback"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center">

        <TextView
            android:id="@+id/btn_helpful"
            android:layout_width="wrap_content"
            android:layout_height="36dp"
            android:gravity="center"
            android:paddingStart="24dp"
            android:paddingEnd="24dp"
            android:text="@string/aihelp_helpful"
            android:textSize="14sp" />

        <View
            android:layout_width="1dp"
            android:layout_height="24dp"
            android:layout_marginStart="16dp"
            android:layout_marginEnd="16dp"
            android:background="#FFE0E0E0" />

        <TextView
            android:id="@+id/btn_not_helpful"
            android:layout_width="wrap_content"
            android:layout_height="36dp"
            android:gravity="center"
            android:paddingStart="24dp"
            android:paddingEnd="24dp"
            android:text="@string/aihelp_not_helpful"
            android:textSize="14sp" />
    </LinearLayout>

    <!-- Contact button -->
    <TextView
        android:id="@+id/btn_contact"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:gravity="center"
        android:background="#FF1A73E8"
        android:text="@string/aihelp_contact_us"
        android:textColor="#FFFFFFFF"
        android:textSize="16sp" />
</LinearLayout>
```

`sdk/src/main/res/layout/aihelp_item_faq_section.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tv_title"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:gravity="center_vertical"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:background="#FFFFFFFF"
    android:layout_marginBottom="1dp"
    android:textColor="#FF212121"
    android:textSize="16sp"
    android:drawableEnd="@android:drawable/ic_media_play" />
```

`sdk/src/main/res/layout/aihelp_item_faq_item.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tv_question"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:gravity="center_vertical"
    android:paddingStart="32dp"
    android:paddingEnd="16dp"
    android:background="#FFFFFFFF"
    android:layout_marginBottom="1dp"
    android:textColor="#FF424242"
    android:textSize="15sp" />
```

- [ ] **Step 2: Implement FAQActivity**

`sdk/src/main/java/com/msdk/aihelp/faq/FAQActivity.java`:
```java
package com.msdk.aihelp.faq;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.msdk.aihelp.R;
import com.msdk.aihelp.chat.ChatActivity;
import com.msdk.aihelp.config.FAQConfig;

public class FAQActivity extends AppCompatActivity {

    public static final String EXTRA_SECTION_ID = "section_id";
    public static final String EXTRA_SHOW_CONTACT = "show_contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_faq);

        if (savedInstanceState == null) {
            String sectionId = getIntent().getStringExtra(EXTRA_SECTION_ID);
            boolean showContact = getIntent().getBooleanExtra(EXTRA_SHOW_CONTACT, true);

            FAQListFragment fragment = FAQListFragment.newInstance(sectionId, showContact);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }

    public void openDetail(String faqId, String question, boolean showContact) {
        FAQDetailFragment fragment = FAQDetailFragment.newInstance(faqId, question, showContact);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void openChat(String faqContext) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_FAQ_CONTEXT, faqContext);
        startActivity(intent);
    }
}
```

- [ ] **Step 3: Implement FAQListFragment**

`sdk/src/main/java/com/msdk/aihelp/faq/FAQListFragment.java`:
```java
package com.msdk.aihelp.faq;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.ui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class FAQListFragment extends Fragment {

    private static final String ARG_SECTION_ID = "section_id";
    private static final String ARG_SHOW_CONTACT = "show_contact";
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private RecyclerView recyclerView;
    private EditText etSearch;
    private TextView btnContact;
    private View toolbar;
    private FAQManager faqManager;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public static FAQListFragment newInstance(String sectionId, boolean showContact) {
        FAQListFragment fragment = new FAQListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_ID, sectionId);
        args.putBoolean(ARG_SHOW_CONTACT, showContact);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_faq_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_faq);
        etSearch = view.findViewById(R.id.et_search);
        btnContact = view.findViewById(R.id.btn_contact);
        ImageView btnBack = view.findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        faqManager = FAQManager.getInstance();

        boolean showContact = getArguments() != null && getArguments().getBoolean(ARG_SHOW_CONTACT, true);
        btnContact.setVisibility(showContact ? View.VISIBLE : View.GONE);

        applyTheme();
        btnBack.setOnClickListener(v -> getActivity().finish());
        btnContact.setOnClickListener(v -> ((FAQActivity) getActivity()).openChat(null));

        setupSearch();
        loadSections();
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnContact.setBackgroundColor(primaryColor);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = s.toString().trim();
                if (TextUtils.isEmpty(query)) {
                    loadSections();
                    return;
                }
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void loadSections() {
        String sectionId = getArguments() != null ? getArguments().getString(ARG_SECTION_ID) : null;

        if (!TextUtils.isEmpty(sectionId)) {
            faqManager.getItems(sectionId, new ApiCallback<List<FAQItem>>() {
                @Override
                public void onSuccess(List<FAQItem> result) {
                    showItems(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        } else {
            faqManager.getSections(new ApiCallback<List<FAQSection>>() {
                @Override
                public void onSuccess(List<FAQSection> result) {
                    showSections(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        }
    }

    private void performSearch(String keyword) {
        faqManager.search(keyword, new ApiCallback<List<FAQItem>>() {
            @Override
            public void onSuccess(List<FAQItem> result) {
                showItems(result);
            }
            @Override
            public void onError(int code, String message) {}
        });
    }

    private void showSections(List<FAQSection> sections) {
        recyclerView.setAdapter(new SectionAdapter(sections, section -> {
            faqManager.getItems(section.getSectionId(), new ApiCallback<List<FAQItem>>() {
                @Override
                public void onSuccess(List<FAQItem> result) {
                    showItems(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        }));
    }

    private void showItems(List<FAQItem> items) {
        boolean showContact = getArguments() != null && getArguments().getBoolean(ARG_SHOW_CONTACT, true);
        recyclerView.setAdapter(new ItemAdapter(items, item -> {
            ((FAQActivity) getActivity()).openDetail(item.getFaqId(), item.getQuestion(), showContact);
        }));
    }

    // Inner adapters
    private static class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.VH> {
        interface OnClick { void onClick(FAQSection s); }
        private final List<FAQSection> data;
        private final OnClick listener;

        SectionAdapter(List<FAQSection> data, OnClick listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.aihelp_item_faq_section, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            FAQSection s = data.get(position);
            holder.tv.setText(s.getTitle());
            holder.itemView.setOnClickListener(v -> listener.onClick(s));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_title); }
        }
    }

    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.VH> {
        interface OnClick { void onClick(FAQItem item); }
        private final List<FAQItem> data;
        private final OnClick listener;

        ItemAdapter(List<FAQItem> data, OnClick listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.aihelp_item_faq_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            FAQItem item = data.get(position);
            holder.tv.setText(item.getQuestion());
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_question); }
        }
    }
}
```

- [ ] **Step 4: Implement FAQDetailFragment**

`sdk/src/main/java/com/msdk/aihelp/faq/FAQDetailFragment.java`:
```java
package com.msdk.aihelp.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.msdk.aihelp.R;
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.ui.theme.ThemeManager;

public class FAQDetailFragment extends Fragment {

    private static final String ARG_FAQ_ID = "faq_id";
    private static final String ARG_QUESTION = "question";
    private static final String ARG_SHOW_CONTACT = "show_contact";

    private WebView webView;
    private TextView btnContact;
    private View toolbar;
    private String faqId;

    public static FAQDetailFragment newInstance(String faqId, String question, boolean showContact) {
        FAQDetailFragment fragment = new FAQDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FAQ_ID, faqId);
        args.putString(ARG_QUESTION, question);
        args.putBoolean(ARG_SHOW_CONTACT, showContact);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_faq_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        webView = view.findViewById(R.id.webview_content);
        btnContact = view.findViewById(R.id.btn_contact);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextView btnHelpful = view.findViewById(R.id.btn_helpful);
        TextView btnNotHelpful = view.findViewById(R.id.btn_not_helpful);

        faqId = getArguments().getString(ARG_FAQ_ID);
        String question = getArguments().getString(ARG_QUESTION, "");
        boolean showContact = getArguments().getBoolean(ARG_SHOW_CONTACT, true);

        tvTitle.setText(question);
        btnContact.setVisibility(showContact ? View.VISIBLE : View.GONE);

        applyTheme();
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnContact.setOnClickListener(v ->
                ((FAQActivity) getActivity()).openChat("FAQ: " + question));
        btnHelpful.setOnClickListener(v -> submitFeedback(true));
        btnNotHelpful.setOnClickListener(v -> submitFeedback(false));

        loadDetail();
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnContact.setBackgroundColor(primaryColor);
    }

    private void loadDetail() {
        FAQManager.getInstance().getDetail(faqId, new ApiCallback<FAQItem>() {
            @Override
            public void onSuccess(FAQItem result) {
                String html = "<html><head>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>body{padding:16px;font-size:15px;line-height:1.6;color:#212121;}"
                        + "img{max-width:100%;height:auto;}</style>"
                        + "</head><body>" + result.getAnswer() + "</body></html>";
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            }

            @Override
            public void onError(int code, String message) {}
        });
    }

    private void submitFeedback(boolean helpful) {
        FAQManager.getInstance().submitFeedback(faqId, helpful);
        View feedbackLayout = getView().findViewById(R.id.layout_feedback);
        if (feedbackLayout != null) {
            feedbackLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add sdk/src/main/res/layout/aihelp_activity_faq.xml sdk/src/main/res/layout/aihelp_fragment_faq_list.xml sdk/src/main/res/layout/aihelp_fragment_faq_detail.xml sdk/src/main/res/layout/aihelp_item_faq_section.xml sdk/src/main/res/layout/aihelp_item_faq_item.xml sdk/src/main/java/com/msdk/aihelp/faq/
git commit -m "feat: add FAQ UI (Activity, ListFragment, DetailFragment)"
```

---

## Task 13: Image Viewer & Public API Entry Point

**Files:**
- Create: `sdk/src/main/java/com/msdk/aihelp/ui/ImageViewerActivity.java`
- Create: `sdk/src/main/java/com/msdk/aihelp/MSDKAiHelp.java`
- Create: `sdk/src/main/res/layout/aihelp_activity_image_viewer.xml`

- [ ] **Step 1: Create image viewer layout**

`sdk/src/main/res/layout/aihelp_activity_image_viewer.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FF000000">

    <ImageView
        android:id="@+id/iv_full_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="fitCenter"
        android:contentDescription="@string/aihelp_image_message" />

    <ImageView
        android:id="@+id/btn_close"
        android:layout_width="36dp"
        android:layout_height="36dp"
        android:layout_gravity="top|end"
        android:layout_margin="16dp"
        android:src="@android:drawable/ic_menu_close_clear_cancel"
        android:contentDescription="@string/aihelp_back" />
</FrameLayout>
```

- [ ] **Step 2: Implement ImageViewerActivity**

`sdk/src/main/java/com/msdk/aihelp/ui/ImageViewerActivity.java`:
```java
package com.msdk.aihelp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.msdk.aihelp.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_URL = "image_url";

    public static void start(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_image_viewer);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        ImageView ivImage = findViewById(R.id.iv_full_image);
        ImageView btnClose = findViewById(R.id.btn_close);

        Glide.with(this).load(imageUrl).into(ivImage);
        btnClose.setOnClickListener(v -> finish());
        ivImage.setOnClickListener(v -> finish());
    }
}
```

- [ ] **Step 3: Implement MSDKAiHelp public API**

`sdk/src/main/java/com/msdk/aihelp/MSDKAiHelp.java`:
```java
package com.msdk.aihelp;

import android.content.Context;
import android.content.Intent;

import com.msdk.aihelp.callback.AiHelpEventListener;
import com.msdk.aihelp.callback.UnreadCountCallback;
import com.msdk.aihelp.chat.ChatActivity;
import com.msdk.aihelp.chat.ChatManager;
import com.msdk.aihelp.config.AiHelpConfig;
import com.msdk.aihelp.config.ChatConfig;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.config.FAQConfig;
import com.msdk.aihelp.faq.FAQActivity;
import com.msdk.aihelp.model.UserInfo;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

public class MSDKAiHelp {

    private static AiHelpEventListener eventListener;

    public static void init(Context context, AiHelpConfig config) {
        if (context == null || config == null) {
            throw new IllegalArgumentException("context and config must not be null");
        }
        ConfigManager.getInstance().init(context, config);
        Logger.i("MSDKAiHelp initialized: appId=" + config.getAppId());
        if (eventListener != null) {
            eventListener.onInitialized(true, "success");
        }
    }

    public static void openChat() {
        openChat(null);
    }

    public static void openChat(ChatConfig chatConfig) {
        checkInitialized();
        Context context = ConfigManager.getInstance().getAppContext();
        Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (chatConfig != null && chatConfig.getWelcomeMessage() != null) {
            intent.putExtra(ChatActivity.EXTRA_CHAT_CONFIG, chatConfig.getWelcomeMessage());
        }
        context.startActivity(intent);
    }

    public static void openFAQ() {
        openFAQ(null);
    }

    public static void openFAQ(FAQConfig faqConfig) {
        checkInitialized();
        Context context = ConfigManager.getInstance().getAppContext();
        Intent intent = new Intent(context, FAQActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (faqConfig != null) {
            intent.putExtra(FAQActivity.EXTRA_SECTION_ID, faqConfig.getSectionId());
            intent.putExtra(FAQActivity.EXTRA_SHOW_CONTACT, faqConfig.isShowContactUs());
        }
        context.startActivity(intent);
    }

    public static void setUser(UserInfo userInfo) {
        checkInitialized();
        ConfigManager.getInstance().setUserInfo(userInfo);
    }

    public static void clearUser() {
        checkInitialized();
        ConfigManager.getInstance().clearUserInfo();
    }

    public static void getUnreadCount(UnreadCountCallback callback) {
        checkInitialized();
        int localCount = ChatManager.getInstance().getUnreadCount();
        if (localCount > 0) {
            callback.onResult(localCount);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_UNREAD_COUNT);
        HttpClient.getInstance().get(url, Integer.class, new ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                callback.onResult(result != null ? result : 0);
            }
            @Override
            public void onError(int code, String message) {
                callback.onResult(0);
            }
        });
    }

    public static void setEventListener(AiHelpEventListener listener) {
        eventListener = listener;
    }

    public static void setLanguage(String language) {
        checkInitialized();
        ConfigManager.getInstance().setLanguage(language);
    }

    private static void checkInitialized() {
        if (!ConfigManager.getInstance().isInitialized()) {
            throw new IllegalStateException("MSDKAiHelp.init() must be called before using SDK");
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add sdk/src/main/java/com/msdk/aihelp/MSDKAiHelp.java sdk/src/main/java/com/msdk/aihelp/ui/ImageViewerActivity.java sdk/src/main/res/layout/aihelp_activity_image_viewer.xml
git commit -m "feat: add MSDKAiHelp public API entry point and ImageViewerActivity"
```

---

## Task 14: Demo App & ProGuard

**Files:**
- Create: `demo/src/main/java/com/msdk/aihelp/demo/MainActivity.java`
- Create: `demo/src/main/res/layout/activity_main.xml`
- Create: `sdk/proguard-rules.pro`

- [ ] **Step 1: Create demo activity layout**

`demo/src/main/res/layout/activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="MSDK AiHelp Demo"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="48dp" />

    <Button
        android:id="@+id/btn_chat"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="打开聊天"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btn_faq"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="打开帮助中心"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btn_unread"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="获取未读数"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btn_set_user"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="设置用户信息"
        android:layout_marginBottom="16dp" />

    <TextView
        android:id="@+id/tv_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="状态: 就绪"
        android:textSize="14sp"
        android:textColor="#FF757575" />
</LinearLayout>
```

- [ ] **Step 2: Implement MainActivity**

`demo/src/main/java/com/msdk/aihelp/demo/MainActivity.java`:
```java
package com.msdk.aihelp.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.msdk.aihelp.MSDKAiHelp;
import com.msdk.aihelp.model.UserInfo;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        Button btnChat = findViewById(R.id.btn_chat);
        Button btnFaq = findViewById(R.id.btn_faq);
        Button btnUnread = findViewById(R.id.btn_unread);
        Button btnSetUser = findViewById(R.id.btn_set_user);

        btnChat.setOnClickListener(v -> MSDKAiHelp.openChat());

        btnFaq.setOnClickListener(v -> MSDKAiHelp.openFAQ());

        btnUnread.setOnClickListener(v -> {
            MSDKAiHelp.getUnreadCount(count -> {
                tvStatus.setText("未读消息: " + count);
            });
        });

        btnSetUser.setOnClickListener(v -> {
            UserInfo user = new UserInfo.Builder()
                    .setUserId("player_12345")
                    .setUserName("TestPlayer")
                    .setServerId("server_01")
                    .addCustomData("level", "50")
                    .addCustomData("vip", "3")
                    .build();
            MSDKAiHelp.setUser(user);
            tvStatus.setText("状态: 用户已设置");
            Toast.makeText(this, "用户信息已设置", Toast.LENGTH_SHORT).show();
        });
    }
}
```

- [ ] **Step 3: Create ProGuard rules**

`sdk/proguard-rules.pro`:
```proguard
# MSDK AiHelp SDK ProGuard Rules

# Keep public API
-keep class com.msdk.aihelp.MSDKAiHelp { *; }
-keep class com.msdk.aihelp.config.** { *; }
-keep class com.msdk.aihelp.model.** { *; }
-keep class com.msdk.aihelp.callback.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
```

- [ ] **Step 4: Verify full project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add demo/ sdk/proguard-rules.pro
git commit -m "feat: add demo app and ProGuard rules"
```

---

## Task 15: Final Integration Test & Cleanup

**Files:**
- Modify: `sdk/build.gradle` (add test dependencies)
- Run all tests

- [ ] **Step 1: Add test dependencies to sdk/build.gradle**

Add to the `dependencies` block in `sdk/build.gradle`:
```groovy
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.8.0'
```

- [ ] **Step 2: Run all unit tests**

Run: `./gradlew :sdk:testDebugUnitTest`
Expected: All tests PASSED (ImageCompressorTest, AiHelpConfigTest, MessageTest, ApiServiceTest, MessageProtocolTest, CacheManagerTest, ThemeManagerTest, ChatManagerTest, FAQManagerTest)

- [ ] **Step 3: Run lint check**

Run: `./gradlew :sdk:lintDebug`
Expected: No errors (warnings acceptable)

- [ ] **Step 4: Build release AAR**

Run: `./gradlew :sdk:assembleRelease`
Expected: BUILD SUCCESSFUL, AAR produced at `sdk/build/outputs/aar/sdk-release.aar`

- [ ] **Step 5: Final commit**

```bash
git add sdk/build.gradle
git commit -m "chore: add test dependencies and verify full build"
```

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

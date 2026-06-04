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

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

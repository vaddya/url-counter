package com.vaddya;

import com.vaddya.urlcounter.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilsTest {
    @Test
    void testExtractDomain() {
        assertNull(Utils.extractDomain("example"));
        assertEquals("example.com", Utils.extractDomain("www.example.com"));
        assertEquals("example.com", Utils.extractDomain("test.example.com"));
        assertEquals("example.com", Utils.extractDomain("example.com/post234.html?a=b"));
        assertEquals("example.com", Utils.extractDomain("test.example.com/post"));
        assertEquals("example.com", Utils.extractDomain("www.example.com?query=param"));
    }
}
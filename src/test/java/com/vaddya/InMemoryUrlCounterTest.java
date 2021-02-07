package com.vaddya;

import java.util.List;
import java.util.Map;

import com.vaddya.urlcounter.local.InMemoryUrlCounter;
import com.vaddya.urlcounter.local.UrlCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryUrlCounterTest {
    @Test
    void test() {
        UrlCounter service = new InMemoryUrlCounter();
        service.add("yandex.ru");
        service.add("yandex.ru");
        service.add("yandex.ru");
        service.add("mail.ru");
        service.add("google.com");
        service.add("mail.ru");
        assertEquals(List.of("yandex.ru", "mail.ru"), service.top(2)); // y3, m2
        assertEquals(Map.of("yandex.ru", 3, "mail.ru", 2), service.topCount(2)); // y3, m2
        service.add("yandex.ru");
        service.add("google.com");
        service.add("google.com");
        assertEquals(List.of("yandex.ru", "google.com"), service.top(2)); // y4, g3
        service.add("google.com");
        service.add("google.com");
        assertEquals(List.of("google.com", "yandex.ru"), service.top(2)); // g5, y4
        assertEquals(List.of("google.com", "yandex.ru", "mail.ru"), service.top(3)); // g5, y4, m2
        assertEquals(List.of("google.com", "yandex.ru", "mail.ru"), service.top(100)); // g5, y4, m2
    }
}
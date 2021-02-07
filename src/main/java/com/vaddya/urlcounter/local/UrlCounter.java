package com.vaddya.urlcounter.local;

import java.util.List;
import java.util.Map;

public interface UrlCounter {
    void add(String domain);

    List<String> top(int n);

    Map<String, Integer> topCount(int n);
}

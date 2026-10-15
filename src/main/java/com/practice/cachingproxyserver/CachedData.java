package com.practice.cachingproxyserver;

import java.util.List;
import java.util.Map;

public record CachedData(
        int statusCode,
        Map<String, List<String>> headers,
        byte[] body
){
}

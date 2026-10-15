package com.practice.cachingproxyserver;

public record CachedDataDto(
        String cacheStatus,
        CachedData cachedData
) {
}

package com.practice.cachingproxyserver;

import org.springframework.stereotype.Component;


import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheStore {
    private static final ConcurrentHashMap<String,CachedData> cacheStore=new ConcurrentHashMap<>();

    public boolean containsKey(String pathAndQuery){
        return cacheStore.containsKey(pathAndQuery);
    }

    public static CachedData get(String pathAndQuery){
        return cacheStore.get(pathAndQuery);
    }

    public static void put(String pathAndQuery, CachedData cachedData){
        cacheStore.put(pathAndQuery,cachedData);
    }

    public String clear(){
        int size= cacheStore.size();
        cacheStore.clear();
        return "Deleted "+size+" entries";
    }
}

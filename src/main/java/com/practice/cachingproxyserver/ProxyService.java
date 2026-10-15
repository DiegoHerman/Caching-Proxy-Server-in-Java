package com.practice.cachingproxyserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpHeaders;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProxyService {
    private final CacheStore cacheStore;
    private String origin;
    public ProxyService(@Value("${origin}")String origin, CacheStore cacheStore){
        this.cacheStore=cacheStore;
        this.origin=origin;
    }

    public CachedDataDto get(String pathAndQuery){
        if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "origin must start with http:// or https:// but was: '" + origin + "'");
        }
        if(cacheStore.containsKey(pathAndQuery)){
            /*if the path of the proxied site is already in the cache, we mark it as "HIT" and set it at the
            headers in the ProxyController*/
            return new CachedDataDto(
                    "HIT",
                    CacheStore.get(pathAndQuery)
            );


        }
        RestClient client=RestClient.builder()
                .baseUrl(origin)
                .build();

        CachedData cachedData=client.get()
                .uri(pathAndQuery)
                .exchange((request,response)->{
                    /*We store the headers, statusCode and body from the response. If we used retrieve(), we would
                    only get the response body*/
                    int statusCode=response.getStatusCode().value();
                    Map<String, List<String>> headers=new LinkedHashMap<>();
                    response.getHeaders().forEach(headers::put);
                    byte[] body=response.getBody().readAllBytes();

                    return new CachedData(
                            statusCode,
                            headers,
                            body
                    );
                });
        //only cache data that have a response status code of 2**
        if(cachedData.statusCode()/100==2){
            CacheStore.put(pathAndQuery,cachedData);
        }
        /*Since the data was not received from the cacheStore, we mark it as "MISS" and set the header at the ProxyController
        The response is now cached and in any succeeding request, the response would be found in the cache*/
        return new CachedDataDto(
                "MISS",
                cachedData
        );
    }

}

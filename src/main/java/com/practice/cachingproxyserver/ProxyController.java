package com.practice.cachingproxyserver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProxyController {
    @Autowired
    private ProxyService proxyService;
    @Autowired
    private CacheStore cacheStore;

    //all requests to the proxy server go through this endpoint
    @GetMapping("/**")
    public ResponseEntity<?> getProxyResponse(HttpServletRequest request) {
        //The request that comes here is the path of the site being visited since the site itself is proxied. i.e;http://localhost:3000/funnygifs/dogs

        //extracts the query part of the url i.e; funnygifs/dogs?breed=goldenRetriever. 'breed=goldenRetriever' is extracted.
        String query=request.getQueryString();
        String fetchRequest=query==null?request.getRequestURI():request.getRequestURI()+"?"+query;
        CachedDataDto cachedDataDto=proxyService.get(fetchRequest);

        /*Headers from the response are set at the http Headers including the header which indicates whether the cache
        was a HIT or MISS*/
        HttpHeaders headers=new HttpHeaders();
        cachedDataDto.cachedData().headers().forEach(headers::addAll);
        headers.set("X-CACHE",cachedDataDto.cacheStatus());

        /*Here, cachedData statusCode is the status code received from the url we fetched and cached, the body is sent as
        bytes to retain as much information as possible from the original response*/
        return ResponseEntity.status(cachedDataDto.cachedData().statusCode()).headers(headers).body(cachedDataDto.cachedData().body());
    }

    @DeleteMapping("/clearCache")
    public ResponseEntity<String> clearCache() {
        String deletedEntries=cacheStore.clear();
        return ResponseEntity.ok(deletedEntries);
    }
}

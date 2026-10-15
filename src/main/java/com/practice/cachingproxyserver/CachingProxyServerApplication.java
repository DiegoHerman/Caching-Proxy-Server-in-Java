package com.practice.cachingproxyserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class CachingProxyServerApplication {

    public static void main(String[] args) throws Exception {
        /*First package your java code using the command 'mvn clean package -DskipTests', the tests are skipped because origin is not
        supplied until startup*/

        /*when you run the command java -jar {name of packaged jar in your target folder} --port {insert port here} --origin {insert url of the page being visited}
        The string arguments after the compile command are passed into args[] above and are executed before the app starts*/
        List<String> translated=new ArrayList<>();
        String port="8082";
        String origin;
        boolean clearCache=false;
        for(int i=0;i<args.length;i++){
            switch(args[i]){
                case "--port"->{port=args[++i];
                translated.add("--server.port="+port);}
                case "--origin"->{
                    origin=args[++i];
                    translated.add("--origin="+origin);
                }
                case "--clear-cache" ->clearCache=true;
                default->{
                    translated.add(args[i]);
                }
            }
        }
        /*If clearing cache, clear the cache in the running port and exit immediately without starting the app. Otherwise,
        the port would collide with the already running process*/
        if(clearCache){
            clearRunningInstance(port);
            return;
        }
        //Here is where the strings from the CLI are finally passed and the app boots up
        SpringApplication.run(CachingProxyServerApplication.class, translated.toArray(new String[0]));
    }

    /*Since there's no way to clear cache from a running process through CLI, we can create a delete request to the controller
    using an http request*/
    private static void clearRunningInstance(String port) throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + "/clearCache"))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (java.net.ConnectException e) {
            System.err.println("No caching proxy is running on port " + port);
        }
    }

}

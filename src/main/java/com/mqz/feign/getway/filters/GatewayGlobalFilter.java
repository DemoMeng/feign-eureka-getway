package com.mqz.feign.getway.filters;

import com.mqz.feign.getway.exception.WithoutLoginException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author mqz
 * @description
 * @abount https://github.com/DemoMeng
 * @since 2021/2/5
 */
@Component
public class GatewayGlobalFilter implements GlobalFilter, Ordered {

    private static List<String> needPermission;
    static {
        List<String> list = new CopyOnWriteArrayList<>();
        list.add("/feign/client/consumer/userInfo");
        needPermission = Collections.unmodifiableList(list);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        URI uri = serverHttpRequest.getURI();
        System.out.println(uri.getPath());
        //根据请求路径拦截、放行指定的请求
        String path = uri.getPath();
        if(needPermission.contains(path)){
            List<String> tokenList = serverHttpRequest.getQueryParams().get("token");
            if(CollectionUtils.isEmpty(tokenList)){
                throw new WithoutLoginException();
            }
            String tokenValue = tokenList.get(0);
            if(StringUtils.isEmpty(tokenValue)){
                throw new WithoutLoginException();
            }
        }

        //URL请求参数
        MultiValueMap<String,String> map = serverHttpRequest.getQueryParams();
        for(String key:map.keySet()){
            System.out.println("请求参数：【key:"+key+"】【value:"+map.get(key)+"】");
        }
        //Header参数
        HttpHeaders headers = serverHttpRequest.getHeaders();
        for(String key:headers.keySet()){
            System.out.println("请求头参数：【key："+key+"】【value:"+headers.get(key).toString()+"】");
        }
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        System.out.println(body);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

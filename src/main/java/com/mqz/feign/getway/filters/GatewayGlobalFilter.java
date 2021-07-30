package com.mqz.feign.getway.filters;

import cn.hutool.core.util.URLUtil;
import com.mqz.feign.getway.exception.WithoutLoginException;
import com.mqz.mars.base.constants.FeignCloudConstant;
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
import org.springframework.web.util.UriComponentsBuilder;
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

        //拼接请求参数
        List<String> list = serverHttpRequest.getQueryParams().get(FeignCloudConstant.Gateway.KEY_TOKEN);
        if(!CollectionUtils.isEmpty(list)){
            //TODO 校验参数合法性
            String tokenValue = list.get(0);
            StringBuilder query = new StringBuilder();
            //获取请求uri的请求参数（GET请求参数通过拼接key=value形式进行传参）
            String originalQuery = uri.getRawQuery();
            //判断最后一个字符是否是&，如果不是则拼接一个&，以备后续的参数进行连接
            if (StringUtils.hasText(originalQuery)) {
                query.append(originalQuery);
                if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
                    query.append('&');
                }
            }
            query.append(FeignCloudConstant.Gateway.KEY_GATEWAY_WITH);
            query.append('=');
            query.append("requestFromGateway");
            query.append("&s"+FeignCloudConstant.Gateway.KEY_TOKEN);
            query.append('=');
            query.append(URLUtil.encode(tokenValue));//hutool 转码
            String lastQuery = query.toString();
            //把请求参数重新拼接回去，并放入request中传递到过滤链的下一个请求中去
            try {
                URI newUri = UriComponentsBuilder.fromUri(uri).replaceQuery(query.toString()).build(true).toUri();
                ServerHttpRequest request = exchange.getRequest().mutate().uri(newUri).build();
                return chain.filter(exchange.mutate().request(request).build());
            }catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid URI query: " + query.toString());
            }
        }


        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

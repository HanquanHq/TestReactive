package com.mashibing.admin.controller;

import com.mashibing.admin.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.stream.IntStream;

// 针对IO密集度比较高的系统，性能会有提升。
@RestController
@RequestMapping("/person")
public class PersonController {
    @Autowired
    PersonService personService;

    @GetMapping("")
    Mono<Object> get() {
        System.out.println("=========here get=====");
        Mono<Object> mono = Mono.create(sink -> {//组装数据序列
            sink.success(personService.getPerson());
        }).doOnSubscribe(sub -> {//订阅数据
            System.out.println("1. doOnSubscribe..." + sub);
        }).doOnNext(data -> {//得到数据
            System.out.println("2. data:" + data);
        }).doOnSuccess(onSuccess -> {//整体完成
            System.out.println("3. onSuccess:" + onSuccess);
        });

        System.out.println("before return, mono: " + mono);

        // 得到一个包装的数据序列，return给了容器
        // 容器拿到这个序列，再去执行序列里的方法
        // 这和 ajax 很像
        // 1. 写回调接口，让b调用
        // 2. 将方法传过去，看起来像是异步，实质上，阻塞过程在容器内部
        // 并不是提高效率，只是将阻塞延后
        System.out.println("return方法线程名称：" + Thread.currentThread().getName());
        return mono; // 组织数据的过程，是netty容器做的，获取数据的过程不依赖controller了
    }

    @GetMapping("xxoo")
        //serverHttpRequest是webFlux特有的，用法也不一样
        //拓展思维，SpringCloud Gateway函数式，比zuul的性能高，底层是基于netty的
    Mono<Object> get01(String name, ServerHttpRequest serverHttpRequest, WebSession webSession) {
        System.out.println("In get01, name = " + name);

        System.out.println("In get01, serverHttpRequest = " + serverHttpRequest);
        //org.springframework.http.server.reactive.ReactorServerHttpRequest

        System.out.println("In get01, headers = " + serverHttpRequest.getHeaders());
        // [Host:"127.0.0.1:8080", User-Agent:"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0", ...

        System.out.println("In get01, parameters = " + serverHttpRequest.getQueryParams().get("name"));
        //[gongluyang]

        // session 的使用方法
        if (StringUtils.isEmpty(webSession.getAttribute("code"))) {
            System.out.println("第一次请求，我要set session 了");
            webSession.getAttributes().put("code", 111222333);
        }
        return Mono.just("me me da!");
    }

    // 不引入 spring-webmvc，仅使用 netty 实现
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sse() {
        Flux<String> flux = Flux.fromStream(IntStream.range(1, 10).mapToObj(i -> {// map 是映射，理解为遍历，但是不是遍历
            try {
                Thread.sleep(new Random().nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "haha, " + i;
        })).doOnSubscribe(sub -> {
            System.out.println("1. sub is: " + sub);// reactor.core.publisher.FluxIterable$IterableSubscription
        }).doOnComplete(() -> {
            System.out.println("doOnComplete,全部完成了！这里传一个新线程");
        }).doOnNext(data -> {
            System.out.println("3. data is: " + data);
        });

        return flux;
    }
}

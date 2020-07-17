package com.mashibing.admin.service;

import com.mashibing.admin.pojo.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersonService {
    static ConcurrentHashMap<Integer, Person> map = new ConcurrentHashMap();

    static {
        for (int i = 0; i < 100; i++) {
            Person person = new Person();
            person.setId(i);
            person.setName("person" + i);
            map.put(i, person);
        }
    }

    public Person getPerson() {
        try {
            System.out.println("getPerson线程名称：" + Thread.currentThread().getName());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("getPerson(): " + map.get(1));
        return map.get(1);
    }

    public Flux<Person> getPersons() {
        // 需要让数据自己变成响应式的，我们关注的是前后端的响应式
        Flux<Person> personFlux = Flux.fromIterable(map.values());
        return personFlux;
    }

}

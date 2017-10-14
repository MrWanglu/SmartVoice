package cn.fintecher.pangolin.business;

import lombok.Data;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by james on 2017-10-14.
 */
public class ListToSumTest {

    @Data
    public class Apple {
        private Integer id;
        private String name;
        private BigDecimal money;
        private Integer num;

        public Apple(Integer id, String name, BigDecimal money, Integer num) {
            this.id = id;
            this.name = name;
            this.money = money;
            this.num = num;
        }
    }

    @Test
    public void getTest() {
        List<Apple> appleList = new ArrayList<>();//存放apple对象集合

        Apple apple1 = new Apple(1, "苹果1", new BigDecimal("3.25"), 10);
        Apple apple12 = new Apple(1, "苹果2", new BigDecimal("1.35"), 20);
        Apple apple2 = new Apple(2, "香蕉", new BigDecimal("2.89"), 30);
        Apple apple3 = new Apple(3, "荔枝", new BigDecimal("9.99"), 40);

        appleList.add(apple1);
        appleList.add(apple12);
        appleList.add(apple2);
        appleList.add(apple3);

        //把第二条数据过滤
        Map<Integer, Apple> appleMap = appleList.stream().collect(Collectors.toMap(Apple::getId, a -> a, (k1, k2) -> k1));
        System.out.println(appleMap);
        System.out.println("____________________________________________________________________________________________");

        Map<Integer, List<Apple>> groupBy = appleList.stream().collect(Collectors.groupingBy(Apple::getId));
        System.out.println(groupBy);
        System.out.println("____________________________________________________________________________________________");


    }


}



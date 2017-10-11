package cn.fintecher.pangolin.business.web;
import cn.fintecher.pangolin.entity.OutsourcePool
global java.util.List checkedList
dialect  "mvel"

rule "${id}"
no-loop true            //只检查一次
dialect "mvel"
when
$c : OutsourcePool(${strategyText})
then
System.out.println($c);
System.out.println(1111);
checkedList.add($c);
end
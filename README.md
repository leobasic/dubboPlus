# 对dubbo进行扩展，支持thrift原生协议直接调用


  - 不入侵,不改变dubbo原来的任何特性
  - 新增对thrift原生协议支持，原生协议的调用，同样可以进行原来的监控统计
  - 唯一丢失的特性，就是没有了软路由。因为原生协议不是使用dubbo客户端调用

##### 重构方法
1、对dubbo schema的service标签增加一个属性：thrift_native="true"    
2、对内置的thrift解码、编码模块进行改造，根据thrift_native值，去兼容原生调用，关键在于原生调用的时候，正确解码出request和编码response。

##### dubbo三种协议性能对比

![alt text](/performance.png "Title")


# Bug Fix

修复dubbo thrift协议下，不能使用telnet命令		
修复dubbo thrift协议下，cacherequest有内存溢出风险




# 关于dubboPlus

  - 基于dubbo2.5.3扩展
  - 高性能的、支持容错的、协议无关且异步的RPC框架
  - 增加了对thrift原生协议支持，从而实现了更多语言的调用

##### dubbo三种协议性能对比

![alt text](/performance.png "Title")

##### thrift原生协议使用方法

待续......


# Bug Fix

修复dubbo thrift协议下，不能使用telnet命令		
修复dubbo thrift协议下，cacherequest有内存溢出风险


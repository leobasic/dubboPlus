# 关于dubboPlus

  - 基于dubbo2.5.3扩展
  - 高性能的、支持容错的、协议无关且异步的RPC框架
  - 增加了对thrift原生协议支持，从而实现了更多语言的调用

##### dubbo三种协议性能对比

![alt text](/performance.png "Title")

##### thrift原生协议使用方法


	<dubbo:protocol name="dubbo" port="20002" />
	<dubbo:protocol name="thrift" port="20001" /> （dubbo thrift协议）
	<dubbo:protocol name="thriftx" port="20003" />

	<!-- 声明需要暴露的服务接口 -->
	<dubbo:service interface="com.dubbo.apps.test.SampleFunction"
		class="com.dubbo.apps.test.SampleFunctionImpl" />

	<dubbo:service interface="com.dubbo.apps.test.SampleFunction2"
		class="com.dubbo.apps.test.SampleFunctionImpl2" />

	<dubbo:service interface="com.dubbo.apps.thrift.Hello$Iface"
		class="com.dubbo.apps.thrift.HelloServiceImpl" />
	<dubbo:service interface="com.dubbo.apps.thrift2.SharedService$Iface"
		class="com.dubbo.apps.thrift2.SharedServiceImpl" />

以上配置说明：非thrift服务分别通过dubbo和dubbo thrift协议进行暴露。thrift原生协议只暴露带$接口服务。		
当前版本，配置多个$服务，只暴露其中一个。



# Bug Fix

修复dubbo thrift协议下，不能使用telnet命令		
修复dubbo thrift协议下，cacherequest有内存溢出风险


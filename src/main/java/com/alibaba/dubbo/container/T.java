package com.alibaba.dubbo.container;

import java.io.UnsupportedEncodingException;

public class T {

	public static void main(String args[]) throws UnsupportedEncodingException
	{
		String s = "registry://10.17.174.61:2181/com.alibaba.dubbo.registry.RegistryService?application=echo-service-provider-1&dubbo=2.0.0&export=dubbo%3A%2F%2F10.17.173.69%3A20001%2Fcom.dubbo.apps.test.SampleFunction%3Fanyhost%3Dtrue%26application%3Decho-service-provider-1%26dubbo%3D2.0.0%26interface%3Dcom.dubbo.apps.test.SampleFunction%26methods%3DprocessString%26pid%3D3704%26side%3Dprovider%26timestamp%3D1464318672775&pid=3704&registry=zookeeper&timestamp=1464318672758";
		
		System.out.println(
				java.net.URLDecoder.decode(s,"utf-8")
				);
		
	}
	
}

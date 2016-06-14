package com.dubbo.apps.test;

public class SampleFunctionImpl2 implements SampleFunction2 {

	@Override
	public String processString2(String str) {
		System.err.println("执行 " + this.getClass().getName());

		return "接收到字符串：" + str;
	}

}

package com.dubbo.apps.test;

public class SampleFunctionImpl implements SampleFunction {

	@Override
	public String processString(String str) {
		System.err.println("执行 " + this.getClass().getName());

		return "接收到字符串：" + str;
	}

	@Override
	public DataBean processObj(DataBean dataBean) {
		return dataBean;
	}

}

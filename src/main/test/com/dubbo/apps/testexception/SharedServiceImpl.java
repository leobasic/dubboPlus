package com.dubbo.apps.testexception;

import org.apache.thrift.TException;

public class SharedServiceImpl implements SharedService.Iface {

	@Override
	public void getStruct(int key, String people) throws TException {
		
		System.out.println("getStruct() : key="+key+", people="+people);
	}

	@Override
	public String getStruct2(int key, String people) throws TException {
		
		System.out.println("getStruct2() : key="+key+", people="+people);
		
		return people;
	}

}

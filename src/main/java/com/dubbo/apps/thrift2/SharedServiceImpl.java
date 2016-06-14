package com.dubbo.apps.thrift2;

import org.apache.thrift.TException;

public class SharedServiceImpl implements SharedService.Iface {

	@Override
	public Man getStruct(int key, People people) throws InvalidOperationException, TException {

		Man man = new Man();
		man.setSex(people.getSex());
		man.setAge(people.getAge());
		man.setName(people.getName());
		man.setPrice(people.getPrice());

		man.toString();

		return man;
	}

}

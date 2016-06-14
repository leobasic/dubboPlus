package com.dubbo.apps.test;

import java.io.UnsupportedEncodingException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.dubbo.apps.thrift2.InvalidOperationException;
import com.dubbo.apps.thrift2.Man;
import com.dubbo.apps.thrift2.People;
import com.dubbo.apps.thrift2.SharedService;

public class ShareClient {

	public void startClient() throws UnsupportedEncodingException {
		
		TTransport transport;
		try {
			transport = new TSocket("10.17.174.14", 20001);
			TProtocol protocol = new TBinaryProtocol(transport);
			
			SharedService.Client client = new SharedService.Client(protocol);
			transport.open();
			
			Man man = client.getStruct(1, new People(2,"名字",99,86475.387567));
			System.out.println(man.toString());
			
			transport.close();
			
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} catch (InvalidOperationException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		ShareClient client = new ShareClient();
		client.startClient();
	}

}

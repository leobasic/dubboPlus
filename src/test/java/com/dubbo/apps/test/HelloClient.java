package com.dubbo.apps.test;

import java.io.UnsupportedEncodingException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.dubbo.apps.thrift.Hello;

public class HelloClient {

	public void startClient() throws UnsupportedEncodingException {
		
		TTransport transport;
		try {
			transport = new TSocket("localhost", 20001);
			TProtocol protocol = new TBinaryProtocol(transport);
			Hello.Client client = new Hello.Client(protocol);
			transport.open();
			
			for (int i=0; i<10000; i++)
			{
				System.out.println(i+") "+client.helloString("哈哈ooo改造成功！"));
			}
			
			
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		HelloClient client = new HelloClient();
		client.startClient();
	}

}

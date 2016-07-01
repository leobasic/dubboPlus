package com.dubbo.test;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

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

public class ShareThriftClient {

	private static final AtomicInteger THRIFT_SEQ_ID = new AtomicInteger( 0 );
	
	public void startClient() throws UnsupportedEncodingException {
		
		for (int j=0; j<1; j++)
		{
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					TTransport transport;
					try {
						transport = new TSocket("localhost", 20003);
						TProtocol protocol = new TBinaryProtocol(transport);
						
						SharedService.Client client = new SharedService.Client(protocol);
						transport.open();
						
						
						for (int i=0; i<10; i++)
						{
							int uid = THRIFT_SEQ_ID.getAndIncrement();
							String tn = Thread.currentThread().getName();
							
							String key = String.valueOf(uid).concat("@").concat(tn);
							
							Man man = client.getStruct(1, new People(2,key,99,86475.387567));
							
							System.out.println(man.toString());
							
						}
						
						transport.close();
						
					} catch (TTransportException e) {
						e.printStackTrace();
					} catch (TException e) {
						e.printStackTrace();
					} catch (InvalidOperationException e) {
						e.printStackTrace();
					}
					
				}
				
			}).start();
		}
		
		
		
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		ShareThriftClient client = new ShareThriftClient();
		client.startClient();
	}

}
